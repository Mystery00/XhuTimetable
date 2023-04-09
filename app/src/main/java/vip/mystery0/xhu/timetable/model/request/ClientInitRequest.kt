package vip.mystery0.xhu.timetable.model.request

import android.os.Build
import vip.mystery0.xhu.timetable.config.store.GlobalNewConfig

data class ClientInitRequest(
    val systemVersion: String = "Android ${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT}",
    val factory: String = Build.MANUFACTURER,
    val model: String = Build.MODEL,
    val rom: String = Build.DISPLAY,
    val checkBetaVersion: Boolean = GlobalNewConfig.versionChannel.isBeta(),
)
