package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.WebSocket
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.ws.AdminStatus
import vip.mystery0.xhu.timetable.model.ws.TextMessage
import vip.mystery0.xhu.timetable.module.HINT_NETWORK
import vip.mystery0.xhu.timetable.repository.FeedbackRepo

class FeedbackViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "FeedbackViewModel"
    }

    private var webSocket: WebSocket? = null

    private val _loading = MutableStateFlow(LoadingState())
    val loading: StateFlow<LoadingState> = _loading

    private val _wsStatus = MutableStateFlow(WebSocketState(WebSocketStatus.DISCONNECTED))
    val wsStatus: StateFlow<WebSocketState> = _wsStatus

    private val _adminStatus = MutableStateFlow(AdminOnlineState(false))
    val adminStatus: StateFlow<AdminOnlineState> = _adminStatus

    val messageState = MessageState(emptyList())

    init {
        viewModelScope.launch {
            initWebSocket()
        }
        loadLastMessage(20)
    }

    fun loadLastMessage(size: Int) {
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load message list failed", throwable)
            _loading.value = LoadingState(
                loading = false,
                errorMessage = throwable.message ?: throwable.javaClass.simpleName,
            )
        }) {
            _loading.value = LoadingState(loading = true)
            val lastId = messageState.messages.lastOrNull()?.id ?: Long.MAX_VALUE
            val result = FeedbackRepo.loadLastMessage(lastId, size)
            messageState.loadMessage(result)
            if (result.isNotEmpty()) {
                setCacheStore {
                    firstFeedbackMessageId = result.maxOf { it.id }
                }
            }
            _loading.value = LoadingState()
            if (!isConnected()) {
                connectWebSocket()
            }
            EventBus.post(EventType.UPDATE_FEEDBACK_CHECK)
        }
    }

    fun sendMessage(content: String) {
        viewModelScope.launch {
            if (!isOnline()) {
                _wsStatus.value = WebSocketState(WebSocketStatus.DISCONNECTED, HINT_NETWORK)
                return@launch
            }
            if (!isConnected()) {
                _wsStatus.value =
                    WebSocketState(
                        WebSocketStatus.DISCONNECTED,
                        "网络连接异常，请点击右上角的图标进行重连"
                    )
                return@launch
            }
            webSocket?.send(content)
        }
    }

    fun connectWebSocket() {
        viewModelScope.launch {
            if (isConnected()) {
                //连接中，或者已经建立连接
                return@launch
            }
            initWebSocket()
        }
    }

    private fun isConnected(): Boolean =
        _wsStatus.value.status != WebSocketStatus.DISCONNECTED && _wsStatus.value.status != WebSocketStatus.FAILED

    private suspend fun initWebSocket() {
        if (!isOnline()) {
            _wsStatus.value = WebSocketState(WebSocketStatus.DISCONNECTED, HINT_NETWORK)
            return
        }
        runCatching { webSocket?.close(1000, "关闭旧连接") }
        webSocket = FeedbackRepo.initWebSocket(
            messageConsumer = {
                messageState.addMessage(it)
            },
            systemMessageConsumer = {
                if (it is AdminStatus) {
                    val message = if (it.online) "" else "管理员下线"
                    _adminStatus.value = AdminOnlineState(it.online, message)
                }
            },
            statusConsumer = {
                _wsStatus.value = it
            }
        )
    }

    override fun onCleared() {
        webSocket?.close(1000, "客户端下线")
        super.onCleared()
    }

    fun clearLoadingErrorMessage() {
        _loading.value = _loading.value.copy(errorMessage = "")
    }

    fun clearWebSocketErrorMessage() {
        _wsStatus.value = _wsStatus.value.copy(errorMessage = "")
    }

    fun clearAdminOnlineMessage() {
        _adminStatus.value = _adminStatus.value.copy(message = "")
    }
}

data class LoadingState(
    val loading: Boolean = false,
    val errorMessage: String = "",
)

class MessageState(initialMessages: List<TextMessage>) {
    private val textMessages: MutableList<TextMessage> =
        mutableStateListOf(*initialMessages.toTypedArray())
    val messages: List<TextMessage> = textMessages

    fun loadMessage(msgList: List<TextMessage>) {
        textMessages.addAll(msgList)
    }

    fun addMessage(msg: TextMessage) {
        textMessages.add(0, msg)
    }
}

data class WebSocketState(
    val status: WebSocketStatus,
    val errorMessage: String = "",
)

data class AdminOnlineState(
    val online: Boolean,
    val message: String = "",
)

enum class WebSocketStatus {
    CONNECTED,
    DISCONNECTED,
    CONNECTING,
    FAILED,
}