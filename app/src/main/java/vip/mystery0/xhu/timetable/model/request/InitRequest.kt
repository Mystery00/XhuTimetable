package vip.mystery0.xhu.timetable.model.request

import android.os.Build
import vip.mystery0.xhu.timetable.BuildConfig
import vip.mystery0.xhu.timetable.appVersionCode
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.publicDeviceId

data class InitRequest(
    val deviceId: String = publicDeviceId,
    val appVersion: String = AppVersion,
    val systemVersion: String = SystemVersion,
    val factory: String = Build.MANUFACTURER,
    val model: String = Build.MODEL,
    val rom: String = Build.DISPLAY,
    val checkBeta: Boolean,
)

private val AppVersion: String
    get() =
        if (BuildConfig.DEBUG)
            "debug"
        else
            "${appVersionName}-${appVersionCode}"

private val SystemVersion = "Android ${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT}"