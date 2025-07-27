package vip.mystery0.xhu.timetable.config.store

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import platform.Foundation.NSUserDefaults

val delegate: NSUserDefaults = NSUserDefaults.standardUserDefaults()
val settings: Settings = NSUserDefaultsSettings(delegate)

actual inline fun <reified T> Store.getConfiguration(key: String, defaultValue: T): T =
    //TODO 分文件存储
    when (defaultValue) {
        is Boolean -> settings.getBoolean(key, defaultValue)
        is Int -> settings.getInt(key, defaultValue)
        is Long -> settings.getLong(key, defaultValue)
        is Float -> settings.getFloat(key, defaultValue)
        is String -> settings.getString(key, defaultValue)
        else -> throw IllegalArgumentException("Unsupported type")
    } as T

actual inline fun <reified T> Store.setConfiguration(key: String, value: T) {
    //TODO 分文件存储
    when (value) {
        is Boolean -> settings[key] = value
        is Int -> settings[key] = value
        is Long -> settings[key] = value
        is Float -> settings[key] = value
        is String -> settings[key] = value
        else -> throw IllegalArgumentException("Unsupported type")
    }
}