package vip.mystery0.xhu.timetable.model.ws

import kotlinx.serialization.Serializable

@Serializable
data class AdminStatus(
    val online: Boolean,
    val onlineMessage: String,
)

@Serializable
data class UnknownSystemMessage(
    val type: Int,
    val content: String,
) : BaseMessage()
