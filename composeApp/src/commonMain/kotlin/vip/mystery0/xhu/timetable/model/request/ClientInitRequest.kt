package vip.mystery0.xhu.timetable.model.request

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.base.deviceFactory
import vip.mystery0.xhu.timetable.base.deviceModel
import vip.mystery0.xhu.timetable.base.deviceRom
import vip.mystery0.xhu.timetable.base.systemVersion
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore

@Serializable
data class ClientInitRequest(
    val versionSys: String = systemVersion(),
    val deviceFactory: String = deviceFactory(),
    val deviceModel: String = deviceModel(),
    val deviceRom: String = deviceRom(),
    val checkBetaVersion: Boolean = GlobalConfigStore.versionChannel.isBeta(),
    val alwaysShowVersion: Boolean = GlobalCacheStore.alwaysShowNewVersion,
)
