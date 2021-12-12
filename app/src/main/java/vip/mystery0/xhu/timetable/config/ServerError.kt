package vip.mystery0.xhu.timetable.config

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException

data class ErrorMessage(
    val code: Int,
    val message: String,
)

class ServerError(override val message: String) : RuntimeException(message)

private val errorMessageMoshi: JsonAdapter<ErrorMessage> =
    Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build().adapter(ErrorMessage::class.java)

fun serverExceptionHandler(
    messageHandler: (ErrorMessage) -> Boolean = { false },
    handler: (Throwable) -> Unit
): CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        var exception: Throwable = throwable
        if (exception is HttpException) {
            val response = exception.response()?.errorBody()?.string()
            if (response != null) {
                parseServerError(exception.code(), response)?.let {
                    val result = messageHandler(it)
                    if (result) {
                        return@CoroutineExceptionHandler
                    }
                    exception = ServerError(it.message)
                }
            }
        }
        handler(exception)
    }

class CoroutineStopException(override val message: String) : RuntimeException(message)

fun parseServerError(httpCode: Int, response: String): ErrorMessage? =
    runCatching {
        errorMessageMoshi.fromJson(response)
    }.getOrElse {
        ErrorMessage(httpCode, response.trim())
    }