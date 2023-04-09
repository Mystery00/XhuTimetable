package vip.mystery0.xhu.timetable.config.interceptor

import android.util.Log
import okhttp3.Interceptor
import okhttp3.Response
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.parseServerError

class CheckLoginInterceptor : Interceptor {
    companion object {
        private const val TAG = "CheckLoginInterceptor"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)
        if (response.code == 401) {
            throw ServerNeedLoginException()
        }
        if (response.code >= 400) {
            //判断为请求存在错误
            val responseString =
                response.body?.string()
                    ?: throw ServerError("no response body, code: ${response.code}")
            parseServerError(response.code, responseString)?.let {
                Log.w(TAG, "intercept: response error: $it")
                throw ServerError(it.message)
            }
        }
        return response
    }
}