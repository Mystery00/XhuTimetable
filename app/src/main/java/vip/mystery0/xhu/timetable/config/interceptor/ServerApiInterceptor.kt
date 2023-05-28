package vip.mystery0.xhu.timetable.config.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okio.Buffer
import vip.mystery0.xhu.timetable.appVersionCode
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.publicDeviceId
import vip.mystery0.xhu.timetable.utils.md5
import vip.mystery0.xhu.timetable.utils.sha256
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.TreeMap

class ServerApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val requestBody = request.body
        val buffer = Buffer()
        requestBody?.writeTo(buffer)

        val contentType = requestBody?.contentType()
        val charset: Charset =
            contentType?.charset(StandardCharsets.UTF_8) ?: StandardCharsets.UTF_8
        val body = buffer.readString(charset)

        val signTime = Instant.now()

        val map = TreeMap<String, String>()
        map["method"] = request.method.uppercase()
        map["url"] = request.url.encodedPath.substringBefore("?")
        map["body"] = body
        map["content-type"] = requestBody?.contentType()?.toString() ?: "empty"
        map["content-length"] = (requestBody?.contentLength() ?: 0L).toString()
        map["signTime"] = signTime.toEpochMilli().toString()
        map["clientVersionName"] = appVersionName
        map["clientVersionCode"] = appVersionCode

        val signKey = request.headers["sessionToken"] ?: signTime.toEpochMilli().toString()
        val salt = "$signKey:XhuTimeTable".md5().uppercase()
        val sign = "$map:$salt".sha256().uppercase()
        val newRequest = request.newBuilder()
            .addHeader("sign", sign)
            .addHeader("signTime", signTime.toEpochMilli().toString())
            .addHeader("deviceId", "android-$publicDeviceId")
            .addHeader("clientVersionName", appVersionName)
            .addHeader("clientVersionCode", appVersionCode)
            .build()

        return chain.proceed(newRequest)
    }
}

class ServerNeedLoginException : RuntimeException()