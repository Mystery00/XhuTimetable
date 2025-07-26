package vip.mystery0.xhu.timetable.model.entity

enum class VersionChannel(
    val value: Int,
    val title: String
) {
    STABLE(1, "稳定版"),
    BETA(2, "测试版"),
    ;

    fun isBeta() = this == BETA

    companion object {
        fun parse(value: Int): VersionChannel =
            values().firstOrNull { it.value == value } ?: STABLE

        fun selectList(): List<VersionChannel> =
            values().sortedBy { it.value }
    }
}