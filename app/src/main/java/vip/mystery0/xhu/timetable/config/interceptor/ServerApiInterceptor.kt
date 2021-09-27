package vip.mystery0.xhu.timetable.config.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ServerApiInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}

class ServerNeedLoginException : RuntimeException()