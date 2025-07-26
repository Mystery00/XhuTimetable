package vip.mystery0.xhu.timetable.config.ktor

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.api.SendingRequest
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.content.TextContent
import io.ktor.http.HttpHeaders
import io.ktor.http.content.OutgoingContent
import io.ktor.http.encodedPath
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.base.publicDeviceId
import vip.mystery0.xhu.timetable.config.ErrorMessage
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha256

private val errorMessageJson = Json {
    isLenient = true
    ignoreUnknownKeys = true
}

val ServerApiPlugin = createClientPlugin("ServerApiPlugin") {
    on(SendingRequest) { request, content ->
        Logger.d("request: ${request.url.encodedPath}")
        val contentType =
            content.contentType?.toString() ?: request.headers[HttpHeaders.ContentType]
        val body = getBodyString(content)
        Logger.d("body: $body")
        val signTime = Clock.System.now()
        val map = LinkedHashMap<String, String>()
        map["method"] = request.method.value.uppercase()
        map["url"] = request.url.encodedPath.substringBefore("?")
        map["body"] = body
        map["content-type"] = contentType ?: "empty"
        map["content-length"] = (content.contentLength ?: 0L).toString()
        map["signTime"] = signTime.toEpochMilliseconds().toString()
        map["clientVersionName"] = appVersionName()
        map["clientVersionCode"] = appVersionCode()
        val sortMap = map.keys.sorted().associateWith { map[it] }
        val signKey = request.headers["sessionToken"] ?: signTime.toEpochMilliseconds().toString()
        Logger.d("signKey: $signKey")
        val salt = "$signKey:XhuTimeTable".md5().uppercase()
        Logger.d("sign: $sortMap:$salt")
        val sign = "$sortMap:$salt".sha256().uppercase()

        request.header("sign", sign)
        request.header("signTime", signTime.toEpochMilliseconds().toString())
        request.header("deviceId", publicDeviceId())
        request.header("clientVersionName", appVersionName())
        request.header("clientVersionCode", appVersionCode())
    }
    onResponse { resp ->
        if (resp.status.value in 200..299) {
            return@onResponse
        }
        if (resp.status.value == 401) {
            throw ServerNeedLoginException()
        }
        val body = resp.bodyAsText()
        Logger.d("body: $body")
        if (body.isBlank()) {
            throw ServerError("response body is empty, http code: ${resp.status.value}")
        }
        try {
            val errorMessage = errorMessageJson.decodeFromString<ErrorMessage>(body)
            throw ServerError(errorMessage.message)
        } catch (e: Exception) {
            if (e is ServerError) {
                throw e
            }
            Logger.w(e) { "decode error message failed, body: $body" }
            throw ServerError(body)
        }
    }
}

private fun getBodyString(body: OutgoingContent): String {
    if (body is TextContent) {
        return body.text
    }
    return ""
}

class ServerNeedLoginException : RuntimeException()