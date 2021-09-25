package vip.mystery0.xhu.timetable.config.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import vip.mystery0.xhu.timetable.config.Config

class PoemsInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val builder = request.newBuilder()
        if (request.url.toString().contains("one.json")) { //请求今日诗词的接口
            val token = Config.poemsToken
            if (token == null) throw Exception("请求今日诗词出错")
            else builder.addHeader("X-User-Token", token)
        }
        return chain.proceed(builder.build())
    }
}