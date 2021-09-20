package vip.mystery0.xhu.timetable.config.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import vip.mystery0.xhu.timetable.BuildConfig

class LogInterceptor : Interceptor {
    private val interceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response =
        if (BuildConfig.DEBUG)
            interceptor.intercept(chain)
        else
            chain.proceed(chain.request())
}