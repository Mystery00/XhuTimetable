package vip.mystery0.xhu.timetable.model.ws

import co.touchlab.kermit.Logger
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.utils.parseJsonToObj

@Serializable
sealed class BaseMessage

enum class WsMessageType(
    val type: Int,
) {
    ADMIN_STATUS(1),
}

fun parseMessage(json: String): Any {
    val wrap = json.parseJsonToObj<ClientMessageWrapper>()
    when (wrap.contentType) {
        ClientMessageContentType.DATA -> {
            return wrap.content.parseJsonToObj<TextMessage>()
        }

        ClientMessageContentType.SYSTEM -> {
            val systemMessage = wrap.content.parseJsonToObj<UnknownSystemMessage>()
            val messageType = WsMessageType.entries.find { it.type == systemMessage.type }
            if (messageType == null) {
                Logger.i("unknown system message: $systemMessage")
                return systemMessage
            }
            return when (systemMessage.type) {
                WsMessageType.ADMIN_STATUS.type -> systemMessage.content.parseJsonToObj<AdminStatus>()
                else -> systemMessage
            }
        }
    }
}