package vip.mystery0.xhu.timetable.config

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.HttpRequestTimeoutException
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.utils.isOnline

@Serializable
data class ErrorMessage(
    val code: Int,
    val message: String,
)

class ServerError(override val message: String) : RuntimeException(message)

fun networkErrorHandler(handler: (Throwable) -> Unit): ((throwable: Throwable) -> Boolean) = {
    when {
        !isOnline() -> {
            handler(RuntimeException(HINT_NETWORK))
        }

        it is HttpRequestTimeoutException -> {
            handler(RuntimeException(HINT_REQUEST_TIMEOUT))
        }

        else -> {
            Logger.w("network error", it)
            handler(it)
        }
    }
    true
}

const val HINT_NETWORK = "网络无法使用，请检查网络连接！"
const val HINT_REQUEST_TIMEOUT = "请求超时，请稍后再试！"