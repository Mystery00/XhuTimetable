package vip.mystery0.xhu.timetable.config.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import vip.mystery0.xhu.timetable.userAgent

class UserAgentInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val newRequest = request.newBuilder()
            .removeHeader("User-Agent")
            .addHeader("User-Agent", userAgent)
            .build()
        return chain.proceed(newRequest)
    }
}