package vip.mystery0.xhu.timetable.base

import platform.Foundation.NSBundle
import platform.Foundation.NSUUID
import platform.UIKit.UIDevice
import vip.mystery0.xhu.timetable.config.store.Store
import vip.mystery0.xhu.timetable.config.store.getConfiguration
import vip.mystery0.xhu.timetable.config.store.setConfiguration

private fun getOrCreateDeviceUniqueId(): String {
    var uniqueId = Store.CacheStore.getConfiguration<String>("device_unique_id", "")
    if (uniqueId == "") {
        // 首次安装或数据清除后生成新的ID
        // identifierForVendor 是一个很好的选择，因为它在同一厂商的应用间保持一致
        uniqueId = UIDevice.currentDevice.identifierForVendor?.UUIDString ?: NSUUID().UUIDString
        Store.CacheStore.setConfiguration("device_unique_id", uniqueId)
    }
    return uniqueId
}

//设备id
val publicDeviceId: String = getOrCreateDeviceUniqueId()

//应用名称
val appName: String =
    NSBundle.mainBundle.infoDictionary?.get("CFBundleDisplayName") as? String
        ?: NSBundle.mainBundle.infoDictionary?.get("CFBundleName") as? String
        ?: "Unknown"

//应用包名
val packageName: String = NSBundle.mainBundle.bundleIdentifier ?: "Unknown"

//版本名称
val appVersionName: String by lazy {
    NSBundle.mainBundle.infoDictionary?.get("CFBundleShortVersionString") as? String
        ?: "Unknown"
}

//版本号
val appVersionCode: String =
    NSBundle.mainBundle.infoDictionary?.get("CFBundleVersion") as? String ?: "Unknown"

val appVersionCodeNumber: Long
    get() = runCatching { appVersionCode.toLong() }.getOrDefault(1L)

actual fun publicDeviceId(): String = publicDeviceId

actual fun appName(): String = appName

actual fun packageName(): String = packageName

actual fun appVersionName(): String = appVersionName

actual fun appVersionCode(): String = appVersionCode

actual fun appVersionCodeNumber(): Long = appVersionCodeNumber

actual fun systemVersion(): String {
    TODO("Not yet implemented")
}

actual fun deviceFactory(): String {
    TODO("Not yet implemented")
}

actual fun deviceModel(): String {
    TODO("Not yet implemented")
}

actual fun deviceRom(): String {
    TODO("Not yet implemented")
}