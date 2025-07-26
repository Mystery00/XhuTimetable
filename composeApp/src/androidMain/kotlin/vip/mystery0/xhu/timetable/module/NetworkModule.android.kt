package vip.mystery0.xhu.timetable.module

import android.webkit.WebSettings
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpConfig
import vip.mystery0.xhu.timetable.context
import java.util.concurrent.TimeUnit

actual fun httpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = OkHttp

actual fun httpClientEngineConfig(
    config: HttpClientEngineConfig,
    timeout: Long
) {
    val okHttpConfig = config as OkHttpConfig
    okHttpConfig.config {
        if (timeout > 0) {
            connectTimeout(timeout, TimeUnit.SECONDS)
            readTimeout(timeout, TimeUnit.SECONDS)
        }
    }
}

actual fun userAgent(): String = WebSettings.getDefaultUserAgent(context)