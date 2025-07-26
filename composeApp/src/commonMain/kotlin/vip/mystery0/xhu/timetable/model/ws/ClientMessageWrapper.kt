package vip.mystery0.xhu.timetable.model.ws

import kotlinx.serialization.Serializable

/**
 * @author Mystery0
 * Create at 2025/7/27
 */
@Serializable
data class ClientMessageWrapper(
    val contentType: ClientMessageContentType,
    val content: String,
)

enum class ClientMessageContentType {
    DATA, SYSTEM,
}
