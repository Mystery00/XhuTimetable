package vip.mystery0.xhu.timetable.config

import vip.mystery0.xhu.timetable.feature.FeatureHub

enum class Feature(val key: String, private val defaultValue: Boolean) {
    JRSC("switch_jinrishici", false),
    ;

    fun isEnabled(): Boolean = FeatureHub.isEnabled(key, defaultValue)
}

enum class FeatureString(val key: String, private val defaultValue: String) {
    JPUSH_APP_KEY("jpush_api_key", "disable"),
    LOGIN_LABEL("data_login_label", ""),
    ;

    fun getValue(): String = FeatureHub.getValue(key, defaultValue)
}

fun trackEvent(event: String) {
}

fun trackError(error: Throwable) {
}