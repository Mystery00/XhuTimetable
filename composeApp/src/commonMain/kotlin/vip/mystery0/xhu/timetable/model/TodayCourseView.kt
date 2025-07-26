package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import kotlinx.datetime.DayOfWeek
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalTime
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.response.Course
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.model.response.ExperimentCourse
import vip.mystery0.xhu.timetable.utils.sha512

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
    var endDayTime: Int,
    //开始上课时间
    val startTime: XhuLocalTime,
    //结束上课时间
    var endTime: XhuLocalTime,
    //上课地点
    val location: String,
    //教师姓名
    val teacher: String,
    //归属用户
    val user: User,
) {
    //上课节次
    var courseDayTime: String = ""

    //课程颜色
    var backgroundColor = Color.Transparent

    //用来区分课程是否相同的key
    var key = ""

    fun generateKey() {
        key = buildString {
            append(courseName)
            append("!")
            append(teacher)
            append("!")
            append(location)
            append("!")
            append(weekList)
            append("!")
            append(day)
        }.sha512()
    }

    fun updateTime() {
        courseDayTime = if (startDayTime == endDayTime) {
            "第${startDayTime}节"
        } else {
            "${startDayTime}-${endDayTime}节"
        }
    }

    companion object {
        fun valueOf(course: Course, user: User): TodayCourseView {
            return TodayCourseView(
                courseName = course.courseName,
                weekList = course.weekList,
                day = course.day,
                startDayTime = course.startDayTime,
                endDayTime = course.endDayTime,
                startTime = course.startTime,
                endTime = course.endTime,
                location = course.location,
                teacher = course.teacher,
                user = user,
            )
        }

        fun valueOf(experimentCourse: ExperimentCourse, user: User): TodayCourseView {
            return TodayCourseView(
                courseName = experimentCourse.experimentProjectName,
                weekList = experimentCourse.weekList,
                day = experimentCourse.day,
                startDayTime = experimentCourse.startDayTime,
                endDayTime = experimentCourse.endDayTime,
                startTime = experimentCourse.startTime,
                endTime = experimentCourse.endTime,
                location = experimentCourse.location,
                teacher = experimentCourse.teacherName,
                user = user,
            )
        }

        fun valueOf(customCourseResponse: CustomCourseResponse, user: User): TodayCourseView {
            return TodayCourseView(
                courseName = customCourseResponse.courseName,
                weekList = customCourseResponse.weekList,
                day = customCourseResponse.day,
                startDayTime = customCourseResponse.startDayTime,
                endDayTime = customCourseResponse.endDayTime,
                startTime = customCourseResponse.startTime,
                endTime = customCourseResponse.endTime,
                location = customCourseResponse.location,
                teacher = customCourseResponse.teacher,
                user = user,
            )
        }
    }
}
