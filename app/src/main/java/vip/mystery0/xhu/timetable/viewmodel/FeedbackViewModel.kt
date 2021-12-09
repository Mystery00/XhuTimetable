package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import java.time.Instant
import java.time.temporal.ChronoUnit

class FeedbackViewModel : ComposeViewModel() {
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    val messageState = MessageState(emptyList())

    init {
        loadLast20Message()
    }

    fun loadLast20Message() {
        viewModelScope.launch {
            _loading.value = true
            delay(2000)
            messageState.loadMessage(
                arrayListOf(
                    Message(
                        "message from ${Instant.now().minus(10, ChronoUnit.MINUTES)}",
                        false,
                        Instant.now().minus(10, ChronoUnit.MINUTES)
                    ),
                    Message(
                        "message from ${Instant.now().minus(11, ChronoUnit.MINUTES)}",
                        false,
                        Instant.now().minus(11, ChronoUnit.MINUTES)
                    ),
                    Message(
                        "message from ${Instant.now().minus(30, ChronoUnit.MINUTES)}",
                        true,
                        Instant.now().minus(30, ChronoUnit.MINUTES)
                    ),
                    Message(
                        "message from ${Instant.now().minus(32, ChronoUnit.MINUTES)}",
                        true,
                        Instant.now().minus(32, ChronoUnit.MINUTES)
                    )
                )
            )
            _loading.value = false
        }
    }

    fun sendMessage(content: String) {
        messageState.addMessage(Message(content, true, Instant.now()))
    }
}

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

data class Message(
    val content: String,
    val isMe: Boolean,
    val time: Instant,
)