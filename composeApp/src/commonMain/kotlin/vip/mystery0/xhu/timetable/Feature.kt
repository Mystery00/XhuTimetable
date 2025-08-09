package vip.mystery0.xhu.timetable

import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.base.deviceFactory
import vip.mystery0.xhu.timetable.base.deviceModel
import vip.mystery0.xhu.timetable.base.deviceRom
import vip.mystery0.xhu.timetable.base.packageName
import vip.mystery0.xhu.timetable.base.publicDeviceId
import vip.mystery0.xhu.timetable.base.systemVersion
import vip.mystery0.xhu.timetable.feature.ContextBuilder
import vip.mystery0.xhu.timetable.feature.FeatureHub

expect fun sdkKey(): String

expect fun platform(): String

fun initFeature() {
    FeatureHub.initialize(
        host = "https://fh.api.mystery0.vip",
        sdkKey = sdkKey(),
        pollingIntervalMs = 120000L,
    )
    val context = ContextBuilder()
        .userKey(publicDeviceId())
        .device("mobile")
        .platform(platform())
        .version("${appVersionName()}-${appVersionCode()}")
        .attr("deviceId", publicDeviceId())
        .attr("systemVersion", systemVersion())
        .attr("factory", deviceFactory())
        .attr("model", deviceModel())
        .attr("rom", deviceRom())
        .attr("packageName", packageName())
        .attr("versionName", appVersionName())
        .attr("versionCode", appVersionCode())
        .build()
    FeatureHub.start(context)
}