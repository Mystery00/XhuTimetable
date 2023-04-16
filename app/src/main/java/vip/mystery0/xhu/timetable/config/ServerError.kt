package vip.mystery0.xhu.timetable.config

import android.util.Log
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineExceptionHandler
import retrofit2.HttpException
import retrofit2.Response
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.module.NetworkNotConnectException
import vip.mystery0.xhu.timetable.module.registerAdapter

data class ErrorMessage(
    val code: Int,
    val message: String,
)

class ServerError(override val message: String) : RuntimeException(message)

private val errorMessageMoshi: JsonAdapter<ErrorMessage> =
    Moshi.Builder().registerAdapter().build().adapter(ErrorMessage::class.java)

fun serverExceptionHandler(
    messageHandler: (ErrorMessage) -> Boolean = { false },
    handler: (Throwable) -> Unit
): CoroutineExceptionHandler =
    CoroutineExceptionHandler { _, throwable ->
        Log.i("TAG", "serverExceptionHandler: ${throwable.message}}")
        if (!isOnline()) {
            handler(NetworkNotConnectException())
            return@CoroutineExceptionHandler
        }
        var exception: Throwable = throwable
        if (exception is HttpException) {
            val response = exception.response()?.errorBody()?.string()
            if (response != null) {
                parseServerError(exception.code(), response)?.let {
                    Log.d("serverExceptionHandler", it.toString())
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

fun <T> Response<T>.checkNeedLogin(): T {
    if (isSuccessful) {
        return body()!!
    }
    if (code() == 401) {
        throw ServerNeedLoginException()
    }
    val response = errorBody()?.string()?.trim()
    if (response.isNullOrBlank()) {
        throw ServerError("no response body, http code: ${code()}")
    }
    val errorMessage = kotlin.runCatching {
        errorMessageMoshi.fromJson(response)
    }.getOrElse {
        throw ServerError(response)
    }
    throw ServerError(errorMessage?.message ?: response)
}