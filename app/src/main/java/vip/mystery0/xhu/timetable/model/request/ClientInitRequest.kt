package vip.mystery0.xhu.timetable.model.request

import android.os.Build
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.publicDeviceId

data class ClientInitRequest(
    val appVersion: String = "$appVersionName-${vip.mystery0.xhu.timetable.appVersionCode}",
    val appVersionCode: String = vip.mystery0.xhu.timetable.appVersionCode,
    val systemVersion: String = "Android ${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT}",
    val factory: String = Build.MANUFACTURER,
    val model: String = Build.MODEL,
    val rom: String = Build.DISPLAY,
    val betaVersion: Boolean = GlobalConfig.versionChannel.isBeta(),
)
