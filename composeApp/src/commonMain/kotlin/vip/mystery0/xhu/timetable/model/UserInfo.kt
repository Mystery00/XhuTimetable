package vip.mystery0.xhu.timetable.model

import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    var studentNo: String,
    var name: String,
    var gender: Gender,
    var xhuGrade: Int,
    var college: String,
    var majorName: String,
    var className: String,
    var majorDirection: String,
)

enum class Gender(val showTitle: String) {
    MALE("男"), FEMALE("女"), UNKNOWN("未知");

    companion object {
        fun parseOld(sex: String): Gender =
            when (sex) {
                "男" -> MALE
                "女" -> FEMALE
                else -> UNKNOWN
            }
    }
}
