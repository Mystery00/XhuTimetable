package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FeedbackApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.response.Message
import vip.mystery0.xhu.timetable.module.HINT_NETWORK
import java.util.concurrent.TimeUnit

class FeedbackViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "FeedbackViewModel"
    }

    private val jsonAdapter =
        Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(Message::class.java)
    private var webSocket: WebSocket? = null
    private val feedbackApi: FeedbackApi by inject()

    private val _loading = MutableStateFlow(LoadingState())
    val loading: StateFlow<LoadingState> = _loading

    private val _wsStatus = MutableStateFlow(WebSocketState(WebSocketStatus.DISCONNECTED))
    val wsStatus: StateFlow<WebSocketState> = _wsStatus

    private var adminMode = false
    private var token = ""
    private var targetUserId = ""

    val messageState = MessageState(emptyList())

    init {
        viewModelScope.launch {
            initWebSocket()
        }
        loadLastMessage(20)
    }

    fun changeToken(token: String, targetUserId: String) {
        Log.i(TAG, "changeToken: admin mode enabled")
        this.adminMode = true
        this.token = token
        this.targetUserId = targetUserId
        viewModelScope.launch {
            initWebSocket()
        }
        messageState.clearMessage()
        loadLastMessage(20)
    }

    fun loadLastMessage(size: Int) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load message list failed", throwable)
            _loading.value = LoadingState(
                loading = false,
                errorMessage = throwable.message ?: throwable.javaClass.simpleName,
            )
        }) {
            _loading.value = LoadingState(loading = true)
            val mainUser = SessionManager.mainUserOrNull()
            if (mainUser == null) {
                _loading.value = LoadingState(loading = false, errorMessage = "用户未登录")
                return@launch
            }
            if (token.isEmpty()) {
                token = mainUser.token
            }
            val lastId = messageState.messages.lastOrNull()?.id ?: Long.MAX_VALUE
            val pullMessage =
                if (adminMode)
                    feedbackApi.pullAdminMessage(token, lastId, size, targetUserId)
                else
                    SessionManager.mainUser().withAutoLogin {
                        feedbackApi.pullMessage(it, lastId, size).checkLogin()
                    }.first
            pullMessage.forEach {
                it.generate(if (adminMode) "System" else mainUser.studentId)
            }
            val result = pullMessage.reversed()
            messageState.loadMessage(result)
            if (result.isNotEmpty()) {
                setConfig {
                    firstFeedbackMessageId = result.maxOf { it.id }
                }
            }
            _loading.value = LoadingState()
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            if (!isOnline()) {
                _wsStatus.value = WebSocketState(WebSocketStatus.DISCONNECTED, HINT_NETWORK)
                return@launch
            }
            if (_wsStatus.value.status == WebSocketStatus.DISCONNECTED || _wsStatus.value.status == WebSocketStatus.FAILED) {
                _wsStatus.value =
                    WebSocketState(WebSocketStatus.DISCONNECTED, "网络连接异常，请点击右上角的图标进行重连")
                return@launch
            }
            webSocket?.send(content)
        }
    }

    fun connectWebSocket() {
        viewModelScope.launch {
            if (_wsStatus.value.status != WebSocketStatus.FAILED && _wsStatus.value.status != WebSocketStatus.DISCONNECTED) {
                //连接中，或者已经建立连接
                return@launch
            }
            initWebSocket()
        }
    }

    private suspend fun initWebSocket() {
        val mainUser = SessionManager.mainUserOrNull()
        if (mainUser == null) {
            _wsStatus.value = WebSocketState(WebSocketStatus.DISCONNECTED, "用户未登录")
            return
        }
        if (!isOnline()) {
            _wsStatus.value = WebSocketState(WebSocketStatus.DISCONNECTED, "网络连接失败，请稍候重试")
            return
        }
        webSocket?.close(1000, "管理员登录")
        _wsStatus.value = WebSocketState(WebSocketStatus.CONNECTING)
        val url = if (adminMode) {
            "wss://ws.api.mystery0.vip/admin/ws?token=${token}&receiveUserId=${targetUserId}"
        } else {
            if (token.isEmpty()) {
                token = mainUser.token
            }
            "wss://ws.api.mystery0.vip/ws?token=${token}"
        }
        val request = Request.Builder()
            .url(url)
            .build()
        val webSocketClient = OkHttpClient.Builder()
            .pingInterval(5, TimeUnit.SECONDS)
            .build()
        webSocket = webSocketClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                _wsStatus.value = WebSocketState(WebSocketStatus.CONNECTED)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                jsonAdapter.fromJson(text)?.let {
                    it.generate(if (adminMode) "System" else mainUser.studentId)
                    messageState.addMessage(it)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                _wsStatus.value = WebSocketState(WebSocketStatus.DISCONNECTED, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                Log.e(TAG, "onFailure: ", t)
                _wsStatus.value =
                    WebSocketState(WebSocketStatus.FAILED, t.message ?: "异常断开，请重新连接")
            }
        })
    }

    override fun onCleared() {
        webSocket?.close(1000, "客户端下线")
        super.onCleared()
    }
}

data class LoadingState(
    val loading: Boolean = false,
    val errorMessage: String = "",
)

class MessageState(
    initialMessages: List<Message>
) {
    private val _messages: MutableList<Message> =
        mutableStateListOf(*initialMessages.toTypedArray())
    val messages: List<Message> = _messages

    fun clearMessage() {
        _messages.clear()
    }

    fun loadMessage(msgList: List<Message>) {
        _messages.addAll(msgList)
    }

    fun addMessage(msg: Message) {
        _messages.add(0, msg)
    }
}

data class WebSocketState(
    val status: WebSocketStatus,
    val errorMessage: String = "",
)

enum class WebSocketStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    FAILED,
}