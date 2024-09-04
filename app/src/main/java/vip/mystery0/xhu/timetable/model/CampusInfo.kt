package vip.mystery0.xhu.timetable.model

data class CampusInfo(
    val selected: String,
    val items: List<String>,
) {
    companion object {
        val EMPTY = CampusInfo("未知", emptyList())
    }
}