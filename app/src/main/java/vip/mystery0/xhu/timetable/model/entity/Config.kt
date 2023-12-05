package vip.mystery0.xhu.timetable.model.entity

import android.os.Build

enum class NightMode(
    val value: Int,
    val title: String
) {
    AUTO(0, "自动"),
    ON(1, "始终开启"),
    OFF(2, "始终关闭"),
    MATERIAL_YOU(3, "Material You"),
    ;

    companion object {
        fun selectList(): List<NightMode> {
            val list = entries.sortedBy { it.value }.toMutableList()
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                list.remove(MATERIAL_YOU)
            }
            return list
        }
    }
}

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