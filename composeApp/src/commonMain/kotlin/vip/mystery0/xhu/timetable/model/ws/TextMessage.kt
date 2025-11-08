package vip.mystery0.xhu.timetable.model.ws

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant

@Immutable
@Serializable
data class TextMessage(
    val id: Long,
    val time: Long,
    val fromUserId: String,
    val content: String,
    val messageType: MessageType,
    val status: Status,
    @Transient
    val isMe: Boolean = false,
    @Transient
    val sendTime: XhuInstant = XhuInstant.fromEpochMilliseconds(time),
) : BaseMessage()

enum class MessageType {
    TEXT,
    IMAGE,
    FILE,
}

enum class Status {
    NORMAL,
    DELETE,
    RECALL,
}