package vip.mystery0.xhu.timetable.model.response

import vip.mystery0.xhu.timetable.config.User
import java.time.Instant

data class Message(
    val id: Long,
    val time: Long,
    val fromUserId: String,
    val content: String,
    val messageType: MessageType,
    val status: Status,
) {
    @Transient
    var isMe: Boolean = false

    @Transient
    var sendTime: Instant = Instant.now()

    fun generate(mainUser: User) {
        isMe = mainUser.studentId == fromUserId
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