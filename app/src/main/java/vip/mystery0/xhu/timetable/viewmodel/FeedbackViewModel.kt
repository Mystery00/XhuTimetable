package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.*
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FeedbackApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.model.response.Message
import java.util.concurrent.TimeUnit

class FeedbackViewModel : ComposeViewModel() {
    private val jsonAdapter = Moshi.Builder().build().adapter(Message::class.java)
    private var webSocket: WebSocket? = null
    private val feedbackApi: FeedbackApi by inject()

    private val _loading = MutableStateFlow(LoadingState())
    val loading: StateFlow<LoadingState> = _loading

    private val _wsStatus = MutableStateFlow(WebSocketState(WebSocketStatus.DISCONNECTED))
    val wsStatus: StateFlow<WebSocketState> = _wsStatus

    val messageState = MessageState(emptyList())

    init {
        viewModelScope.launch {
            initWebSocket()
        }
        loadLast20Message()
    }

    fun loadLast20Message() {
        viewModelScope.launch {
            _loading.value = LoadingState(loading = true)
            val mainUser = SessionManager.mainUserOrNull()
            if (mainUser == null) {
                _loading.value = LoadingState(loading = false, errorMessage = "用户未登录")
                return@launch
            }
            val lastId = messageState.messages.lastOrNull()?.id ?: Long.MAX_VALUE
            val pullMessage = feedbackApi.pullMessage(mainUser.token, lastId, 10)
            pullMessage.forEach {
                it.generate(mainUser)
            }
            messageState.loadMessage(pullMessage.reversed())
            _loading.value = LoadingState()
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
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
        _wsStatus.value = WebSocketState(WebSocketStatus.CONNECTING)
        val request = Request.Builder()
            .url("wss://ws.api.mystery0.vip/ws?token=${mainUser.token}")
            .build()
        val webSocketClient = OkHttpClient.Builder()
            .pingInterval(10, TimeUnit.SECONDS)
            .build()
        webSocket = webSocketClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                super.onOpen(webSocket, response)
                _wsStatus.value = WebSocketState(WebSocketStatus.CONNECTED)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                super.onMessage(webSocket, text)
                jsonAdapter.fromJson(text)?.let {
                    it.generate(mainUser)
                    messageState.addMessage(it)
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                super.onClosed(webSocket, code, reason)
                _wsStatus.value = WebSocketState(WebSocketStatus.DISCONNECTED, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                super.onFailure(webSocket, t, response)
                _wsStatus.value = WebSocketState(WebSocketStatus.FAILED, t.message ?: "异常断开，请重新连接")
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