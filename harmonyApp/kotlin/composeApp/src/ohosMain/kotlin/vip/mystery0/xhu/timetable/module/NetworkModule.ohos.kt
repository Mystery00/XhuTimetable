package vip.mystery0.xhu.timetable.module

import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.platform.OhosHttp
import vip.mystery0.xhu.timetable.platform.OhosHttpConfig

actual fun httpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig> = OhosHttp

actual fun httpClientEngineConfig(config: HttpClientEngineConfig, timeout: Long) {
    if (timeout > 0) (config as OhosHttpConfig).timeoutMillis = timeout * 1000
}

actual fun userAgent(): String = "XhuTimetableHarmony/${appVersionName()}"
