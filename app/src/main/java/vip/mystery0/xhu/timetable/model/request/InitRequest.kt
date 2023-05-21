package vip.mystery0.xhu.timetable.model.request

import android.os.Build
import vip.mystery0.xhu.timetable.BuildConfig
import vip.mystery0.xhu.timetable.appVersionCode
import vip.mystery0.xhu.timetable.appVersionName

private val AppVersion: String
    get() =
        if (BuildConfig.DEBUG)
            "debug"
        else
            "${appVersionName}-${appVersionCode}"

private val SystemVersion = "Android ${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT}"