package vip.mystery0.xhu.timetable.config

data class Customisable<T>(
    val data: T,
    val custom: Boolean,
) {
    companion object {
        fun <T> custom(data: T) = Customisable(data, true)
        fun <T> serverDetect(data: T) = Customisable(data, false)
        fun <T> clearCustom(data: T) = Customisable(data, false)

        fun customKey(key: String): String = "${key}-custom"
    }

    fun mapKey(key: String): String = if (custom) customKey(key) else key
}