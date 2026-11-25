package vip.mystery0.xhu.timetable.base

import vip.mystery0.xhu.timetable.config.store.Store
import vip.mystery0.xhu.timetable.config.store.getValue
import vip.mystery0.xhu.timetable.config.store.setValue
import vip.mystery0.xhu.timetable.platform.OhosPlatform
import kotlin.uuid.Uuid

private val installationId by lazy {
    Store.ConfigStore.getValue("harmonyInstallationId", "").ifEmpty {
        Uuid.random().toString().also { Store.ConfigStore.setValue("harmonyInstallationId", it) }
    }
}

actual fun publicDeviceId(): String = installationId
actual fun appName(): String = "西瓜课表"
actual fun packageName(): String = OhosPlatform.info.bundleName
actual fun appVersionName(): String = OhosPlatform.info.versionName
actual fun appVersionCode(): String = OhosPlatform.info.versionCode.toString()
actual fun appVersionCodeNumber(): Long = OhosPlatform.info.versionCode
actual fun systemVersion(): String = OhosPlatform.info.systemVersion
actual fun deviceFactory(): String = OhosPlatform.info.manufacturer
actual fun deviceModel(): String = OhosPlatform.info.model
actual fun deviceRom(): String = OhosPlatform.info.systemVersion
