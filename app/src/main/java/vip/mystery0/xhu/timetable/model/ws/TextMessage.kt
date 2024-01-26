package vip.mystery0.xhu.timetable.model.ws

import java.time.Instant

data class TextMessage(
    val id: Long,
    val time: Long,
    val fromUserId: String,
    val content: String,
    val messageType: MessageType,
    val status: Status,
) : BaseMessage {
    @Transient
    var isMe: Boolean = false

    @Transient
    var sendTime: Instant = Instant.now()

    fun generate(studentId: String) {
        isMe = studentId == fromUserId
        sendTime = Instant.ofEpochMilli(time)
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