package vip.mystery0.xhu.timetable.ui.theme

enum class NightMode(
    val value: Int,
    val title: String
) {
    AUTO(0, "自动"),
    ON(1, "始终开启"),
    OFF(2, "始终关闭"),
    MATERIAL_YOU(3, "Material You"),
}

expect fun showNightModeSelectList(): List<NightMode>