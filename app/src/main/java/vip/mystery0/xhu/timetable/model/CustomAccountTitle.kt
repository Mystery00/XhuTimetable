package vip.mystery0.xhu.timetable.model

class CustomAccountTitle {
    val todayTemplate: String = "{studentNo}({name})"
    val weekTemplate: String = "{studentNo}({name})"

    companion object {
        val DEFAULT = CustomAccountTitle()
    }

    fun formatToday(userInfo: UserInfo) =
        todayTemplate.replace("{studentNo}", userInfo.studentNo)
            .replace("{name}", userInfo.name)

    fun formatWeek(userInfo: UserInfo) =
        weekTemplate.replace("{studentNo}", userInfo.studentNo)
            .replace("{name}", userInfo.name)
}