package vip.mystery0.xhu.timetable.config.store

enum class Store(val id: String) {
    CacheStore("CacheStore"),
    ConfigStore("ConfigStore"),
    MenuStore("MenuStore"),
    PoemsStore("PoemsStore"),
    UserStore("UserStore"),
}

expect inline fun <reified T> Store.getConfiguration(key: String, defaultValue: T): T
expect inline fun <reified T> Store.setConfiguration(key: String, value: T)
expect fun Store.removeConfiguration(key: String)
expect fun Store.removeAll()