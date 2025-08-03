package vip.mystery0.xhu.timetable.config.store

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import vip.mystery0.xhu.timetable.utils.mapJson

private val CacheStore = NSUserDefaultsSettings.Factory().create("CacheStore")
private val ConfigStore = NSUserDefaultsSettings.Factory().create("ConfigStore")
private val MenuStore = NSUserDefaultsSettings.Factory().create("MenuStore")
private val PoemsStore = NSUserDefaultsSettings.Factory().create("PoemsStore")
private val UserStore = NSUserDefaultsSettings.Factory().create("UserStore")

fun getSettings(id: String): Settings =
    when (id) {
        Store.CacheStore.id -> CacheStore
        Store.ConfigStore.id -> ConfigStore
        Store.MenuStore.id -> MenuStore
        Store.PoemsStore.id -> PoemsStore
        Store.UserStore.id -> UserStore
        else -> throw NotImplementedError()
    }

actual inline fun <reified T> Store.getConfiguration(key: String, defaultValue: T): T {
    val settings = getSettings(id)
    return when (defaultValue) {
        is Boolean -> settings.getBoolean(key, defaultValue)
        is Int -> settings.getInt(key, defaultValue)
        is Long -> settings.getLong(key, defaultValue)
        is Float -> settings.getFloat(key, defaultValue)
        is String -> settings.getString(key, defaultValue)
        is Set<*> -> {
            val text = settings.getString(key, "")
            if (text.isBlank()) return defaultValue
            return mapJson.decodeFromString<Set<String>>(text) as T
        }
        else -> throw IllegalArgumentException("Unsupported type")
    } as T
}

actual inline fun <reified T> Store.setConfiguration(key: String, value: T) {
    val settings = getSettings(id)
    when (value) {
        is Boolean -> settings[key] = value
        is Int -> settings[key] = value
        is Long -> settings[key] = value
        is Float -> settings[key] = value
        is String -> settings[key] = value
        is Set<*> -> {
            @Suppress("UNCHECKED_CAST")
            val text = mapJson.encodeToString(value as Set<String>)
            settings[key] = text
        }
        else -> throw IllegalArgumentException("Unsupported type")
    }
}

actual fun Store.removeConfiguration(key: String) {
    val settings = getSettings(id)
    settings.remove(key)
}

actual fun Store.removeAll() {
    val settings = getSettings(id)
    settings.clear()
}