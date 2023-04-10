package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.response.Course
import vip.mystery0.xhu.timetable.model.response.ExperimentCourse
import java.time.DayOfWeek

data class TodayCourseView(
    //课程名称
    val courseName: String,
    //上课周列表
    val weekList: List<Int>,
    //星期
    val day: DayOfWeek,
    //开始节次
    val startDayTime: Int,
    //结束节次
    val endDayTime: Int,
    //上课节次
    val courseDayTime: String,
    //上课时间
    val courseTime: String,
    //上课地点
    val location: String,
    //教师姓名
    val teacher: String,
    //归属用户
    val user: User,
) {
    //是否是今日课程
    var today = false

    //是否是明日课程
    var tomorrow = false

    //课程颜色
    var backgroundColor = Color.Transparent

    companion object {
        fun valueOf(course: Course, user: User): TodayCourseView {
            val courseDayTime = if (course.startDayTime == course.endDayTime) {
                "第${course.startDayTime}节"
            } else {
                "${course.startDayTime}-${course.endDayTime}节"
            }
            val courseTime =
                "${courseTimeStartArray[course.startDayTime - 1]} - ${courseTimeEndArray[course.endDayTime - 1]}"
            return TodayCourseView(
                courseName = course.courseName,
                weekList = course.weekList,
                day = course.day,
                startDayTime = course.startDayTime,
                endDayTime = course.endDayTime,
                courseDayTime = courseDayTime,
                courseTime = courseTime,
                location = course.location,
                teacher = course.teacher,
                user = user,
            )
        }

        fun valueOf(experimentCourse: ExperimentCourse, user: User): TodayCourseView {
            val courseDayTime = if (experimentCourse.startDayTime == experimentCourse.endDayTime) {
                "第${experimentCourse.startDayTime}节"
            } else {
                "${experimentCourse.startDayTime}-${experimentCourse.endDayTime}节"
            }
            val courseTime =
                "${courseTimeStartArray[experimentCourse.startDayTime - 1]} - ${courseTimeEndArray[experimentCourse.endDayTime - 1]}"
            return TodayCourseView(
                courseName = experimentCourse.experimentProjectName,
                weekList = experimentCourse.weekList,
                day = experimentCourse.day,
                startDayTime = experimentCourse.startDayTime,
                endDayTime = experimentCourse.endDayTime,
                courseDayTime = courseDayTime,
                courseTime = courseTime,
                location = experimentCourse.location,
                teacher = experimentCourse.teacherName,
                user = user,
            )
        }
    }
}
