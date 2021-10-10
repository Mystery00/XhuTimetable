package vip.mystery0.xhu.timetable.model.response

import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.model.entity.CourseType

data class CourseResponse(
    var name: String,
    var teacher: String,
    var location: String,
    var weekString: String,
    var week: List<Int>,
    var time: List<Int>,
    var type: CourseType,
    var day: Int,
) {
    @Transient
    lateinit var user: User
}