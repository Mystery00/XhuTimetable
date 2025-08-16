package vip.mystery0.xhu.timetable.config.store

import com.tencent.mmkv.MMKV
import kotlin.random.Random
import kotlin.time.Clock

private val secret: String
    get() {
        val value = GlobalConfigStore.userStoreSecret
        if (value.isBlank()) {
            val newValue = Random(Clock.System.now().toEpochMilliseconds()).nextLong().toString()
            GlobalConfigStore.userStoreSecret = newValue
            return newValue
        }
        return value
    }

private val CacheStore = MMKV.mmkvWithID("CacheStore")
private val ConfigStore = MMKV.mmkvWithID("ConfigStore")
private val MenuStore = MMKV.mmkvWithID("MenuStore", MMKV.SINGLE_PROCESS_MODE)
private val PoemsStore = MMKV.mmkvWithID("PoemsStore", MMKV.SINGLE_PROCESS_MODE)
private val UserStore = MMKV.mmkvWithID("UserStore", MMKV.SINGLE_PROCESS_MODE, secret)

fun getMMKV(id: String): MMKV =
    when (id) {
        Store.CacheStore.id -> CacheStore
        Store.ConfigStore.id -> ConfigStore
        Store.MenuStore.id -> MenuStore
        Store.PoemsStore.id -> PoemsStore
        Store.UserStore.id -> UserStore
        else -> throw NotImplementedError()
    }

@Suppress("UNCHECKED_CAST")
actual inline fun <reified T> Store.getValue(
    key: String,
    defaultValue: T,
): T {
    val kv = getMMKV(id)
    return when (defaultValue) {
        is Boolean -> kv.decodeBool(key, defaultValue)
        is Int -> kv.decodeInt(key, defaultValue)
        is Long -> kv.decodeLong(key, defaultValue)
        is Float -> kv.decodeFloat(key, defaultValue)
        is String -> kv.decodeString(key, defaultValue)
        is Set<*> -> kv.decodeStringSet(key, defaultValue as Set<String>)
        else -> throw IllegalArgumentException("Unsupported type: ${defaultValue::class.simpleName}")
    } as T
}

@Suppress("UNCHECKED_CAST")
actual inline fun <reified T> Store.setValue(
    key: String,
    value: T,
) {
    val kv = getMMKV(id)
    when (value) {
        is Boolean -> kv.encode(key, value)
        is Int -> kv.encode(key, value)
        is Long -> kv.encode(key, value)
        is Float -> kv.encode(key, value)
        is String -> kv.encode(key, value)
        is Set<*> -> kv.encode(key, value as Set<String>)
        else -> throw IllegalArgumentException("Unsupported type: ${value::class.simpleName}")
    }
}

actual fun Store.removeValue(key: String) {
    val kv = getMMKV(id)
    kv.removeValueForKey(key)
}

actual fun Store.removeAllValue() {
    val kv = getMMKV(id)
    kv.clearAll()
}