package vip.mystery0.xhu.timetable.ui.theme

actual fun showNightModeSelectList(): List<NightMode> {
    val list = NightMode.entries.sortedBy { it.value }.toMutableList()
    list.remove(NightMode.MATERIAL_YOU)
    return list
}