package vip.mystery0.xhu.timetable.model.ws

import kotlin.time.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant

@Serializable
data class TextMessage(
    val id: Long,
    val time: Long,
    val fromUserId: String,
    val content: String,
    val messageType: MessageType,
    val status: Status,
) : BaseMessage() {
    @Transient
    var isMe: Boolean = false

    @Transient
    var sendTime: XhuInstant = Clock.System.now()

    fun generate(studentId: String) {
        isMe = studentId == fromUserId
        sendTime = XhuInstant.fromEpochMilliseconds(time)
    }
}

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