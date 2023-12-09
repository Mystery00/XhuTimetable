package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.model.response.Course
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.model.response.ExperimentCourse
import vip.mystery0.xhu.timetable.utils.sha512
import java.time.DayOfWeek
import java.time.LocalTime

data class WeekCourseView(
    //课程名称
    val courseName: String,
    //上课周显示字符串
    val weekStr: String,
    //上课周列表
    val weekList: List<Int>,
    //星期
    val day: DayOfWeek,
    //开始节次
    val startDayTime: Int,
    //结束节次
    val endDayTime: Int,
    //开始上课时间
    val startTime: LocalTime,
    //结束上课时间
    var endTime: LocalTime,
    //上课节次
    val courseDayTime: String,
    //上课时间
    val courseTime: String,
    //上课地点
    val location: String,
    //教师姓名
    val teacher: String,
    //备注
    val extraData: List<String>,
    //多用户显示内容
    val accountTitle: String,
) : Comparable<WeekCourseView> {
    //是否是本周课程
    var thisWeek = false

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
            append(weekStr)
            append("!")
            append(courseDayTime)
            append("!")
            append(day)
        }.sha512()
    }

    override fun compareTo(other: WeekCourseView): Int {
        if (this.thisWeek != other.thisWeek) {
            return other.thisWeek.compareTo(this.thisWeek)
        }
        return this.weekList.first().compareTo(other.weekList.first())
    }

    companion object {
        fun valueOf(course: Course, accountTitle: String): WeekCourseView {
            val courseDayTime = if (course.startDayTime == course.endDayTime) {
                "第${course.startDayTime}节"
            } else {
                "${course.startDayTime}-${course.endDayTime}节"
            }
            val courseTime = buildString {
                append(course.startTime.format(Formatter.TIME_NO_SECONDS))
                append(" - ")
                append(course.endTime.format(Formatter.TIME_NO_SECONDS))
            }
            return WeekCourseView(
                courseName = course.courseName,
                weekStr = course.weekStr,
                weekList = course.weekList,
                day = course.day,
                startDayTime = course.startDayTime,
                endDayTime = course.endDayTime,
                startTime = course.startTime,
                endTime = course.endTime,
                courseDayTime = courseDayTime,
                courseTime = courseTime,
                location = course.location,
                teacher = course.teacher,
                extraData = course.extraData,
                accountTitle = accountTitle,
            )
        }

        fun valueOf(experimentCourse: ExperimentCourse, accountTitle: String): WeekCourseView {
            val courseDayTime = if (experimentCourse.startDayTime == experimentCourse.endDayTime) {
                "第${experimentCourse.startDayTime}节"
            } else {
                "${experimentCourse.startDayTime}-${experimentCourse.endDayTime}节"
            }
            val courseTime = buildString {
                append(experimentCourse.startTime.format(Formatter.TIME_NO_SECONDS))
                append(" - ")
                append(experimentCourse.endTime.format(Formatter.TIME_NO_SECONDS))
            }
            val extraData = if (experimentCourse.experimentGroupName.isNotBlank()) {
                listOf("实验分组：${experimentCourse.experimentGroupName}")
            } else {
                emptyList()
            }
            return WeekCourseView(
                courseName = experimentCourse.experimentProjectName,
                weekStr = experimentCourse.weekStr,
                weekList = experimentCourse.weekList,
                day = experimentCourse.day,
                startDayTime = experimentCourse.startDayTime,
                endDayTime = experimentCourse.endDayTime,
                startTime = experimentCourse.startTime,
                endTime = experimentCourse.endTime,
                courseDayTime = courseDayTime,
                courseTime = courseTime,
                location = experimentCourse.location,
                teacher = experimentCourse.teacherName,
                extraData = extraData,
                accountTitle = accountTitle,
            )
        }

        fun valueOf(course: CustomCourseResponse, accountTitle: String): WeekCourseView {
            val courseDayTime = if (course.startDayTime == course.endDayTime) {
                "第${course.startDayTime}节"
            } else {
                "${course.startDayTime}-${course.endDayTime}节"
            }
            val courseTime = buildString {
                append(course.startTime.format(Formatter.TIME_NO_SECONDS))
                append(" - ")
                append(course.endTime.format(Formatter.TIME_NO_SECONDS))
            }
            return WeekCourseView(
                courseName = course.courseName,
                weekStr = course.weekStr,
                weekList = course.weekList,
                day = course.day,
                startDayTime = course.startDayTime,
                endDayTime = course.endDayTime,
                startTime = course.startTime,
                endTime = course.endTime,
                courseDayTime = courseDayTime,
                courseTime = courseTime,
                location = course.location,
                teacher = course.teacher,
                extraData = emptyList(),
                accountTitle = accountTitle,
            )
        }
    }
}

fun WeekCourseView.format(template: String): String {
    var result = template
    for (titleTemplate in TitleTemplate.entries) {
        result = result.replace("{${titleTemplate.tpl}}", titleTemplate.action(this))
    }
    return result
}

enum class TitleTemplate(
    val tpl: String,
    val action: (WeekCourseView) -> String,
) {
    COURSE_NAME(tpl = "courseName", action = {
        it.courseName
    }),
    TEACHER_NAME(tpl = "teacherName", action = {
        it.teacher
    }),
    LOCATION(tpl = "location", action = {
        it.location
    }),
}
