package vip.mystery0.xhu.timetable.config.store

import co.touchlab.kermit.Logger

private val logger = Logger.withTag("Store")

enum class Store(val id: String) {
    CacheStore("CacheStore"),
    ConfigStore("ConfigStore"),
    MenuStore("MenuStore"),
    PoemsStore("PoemsStore"),
    UserStore("UserStore"),
}

internal inline fun <reified T> Store.getConfiguration(key: String, defaultValue: T): T {
    logger.d("getConfiguration($id): $key, class type: ${defaultValue::class.simpleName}")
    return getValue(key, defaultValue)
}

internal inline fun <reified T> Store.setConfiguration(key: String, value: T) {
    logger.d("setConfiguration($id): $key, class type: ${value::class.simpleName}")
    setValue(key, value)
}

internal fun Store.removeConfiguration(key: String) {
    logger.d("removeConfiguration($id): $key")
    removeValue(key)
}

internal fun Store.removeAll() {
    logger.d("removeAll($id)")
    removeAllValue()
}

expect inline fun <reified T> Store.getValue(key: String, defaultValue: T): T
expect inline fun <reified T> Store.setValue(key: String, value: T)
expect fun Store.removeValue(key: String)
expect fun Store.removeAllValue()