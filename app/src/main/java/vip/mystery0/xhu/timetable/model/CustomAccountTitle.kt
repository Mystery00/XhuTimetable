package vip.mystery0.xhu.timetable.model

data class CustomAccountTitle(
    val todayTemplate: String = "{studentNo}({name})",
    val weekTemplate: String = "{studentNo}({name})",
) {
    companion object {
        val DEFAULT = CustomAccountTitle()
    }

    fun formatToday(userInfo: UserInfo): String {
        var result = todayTemplate
        for (titleTemplate in AccountTitleTemplate.entries) {
            result = result.replace("{${titleTemplate.tpl}}", titleTemplate.action(userInfo))
        }
        return result
    }

    fun formatWeek(userInfo: UserInfo): String {
        var result = weekTemplate
        for (titleTemplate in AccountTitleTemplate.entries) {
            result = result.replace("{${titleTemplate.tpl}}", titleTemplate.action(userInfo))
        }
        return result
    }
}

enum class AccountTitleTemplate(
    val tpl: String,
    val action: (UserInfo) -> String,
) {
    STUDENT_NO(tpl = "studentNo", action = { it.studentNo }),
    NAME(tpl = "name", action = { it.name }),
    NICK_NAME(tpl = "nickName", action = { it.name })
}