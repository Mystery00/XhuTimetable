package vip.mystery0.xhu.timetable.model.response

data class AllCourseResponse(
    var name: String,
    var teacher: String,
    var location: String,
    var weekString: String,
    var week: List<Int>,
    var time: List<Int>,
    var day: Int,
    var createTime: Long,
    var updateTime: Long,
)