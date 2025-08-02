package vip.mystery0.xhu.timetable.config

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.utils.isOnline

@Serializable
data class ErrorMessage(
    val code: Int,
    val message: String,
)

class ServerError(override val message: String) : RuntimeException(message)

fun networkErrorHandler(handler: (Throwable) -> Unit): CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        if (!isOnline()) {
            //没有网络，统一报错 网络连接失败
            handler(NetworkNotConnectException())
            return@CoroutineExceptionHandler
        }
        Logger.w("network error", throwable)
        handler(throwable)
    }

class CoroutineStopException(override val message: String) : RuntimeException(message)

const val HINT_NETWORK = "网络无法使用，请检查网络连接！"

class NetworkNotConnectException : RuntimeException(HINT_NETWORK)