package vip.mystery0.xhu.timetable.model.request

import android.os.Build
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore

data class ClientInitRequest(
    val versionSys: String = "Android ${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT}",
    val deviceFactory: String = Build.MANUFACTURER,
    val deviceModel: String = Build.MODEL,
    val deviceRom: String = Build.DISPLAY,
    val checkBetaVersion: Boolean = GlobalConfigStore.versionChannel.isBeta(),
    val alwaysShowVersion: Boolean = GlobalCacheStore.alwaysShowNewVersion,
)
