package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.response.Course
import vip.mystery0.xhu.timetable.model.response.ExperimentCourse
import vip.mystery0.xhu.timetable.utils.sha512
import java.time.DayOfWeek

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
    //归属用户
    val user: User,
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
        fun valueOf(course: Course, user: User): WeekCourseView {
            val courseDayTime = if (course.startDayTime == course.endDayTime) {
                "第${course.startDayTime}节"
            } else {
                "${course.startDayTime}-${course.endDayTime}节"
            }
            val courseTime =
                "${startArray[course.startDayTime - 1]} - ${endArray[course.endDayTime - 1]}"
            return WeekCourseView(
                courseName = course.courseName,
                weekStr = course.weekStr,
                weekList = course.weekList,
                day = course.day,
                startDayTime = course.startDayTime,
                endDayTime = course.endDayTime,
                courseDayTime = courseDayTime,
                courseTime = courseTime,
                location = course.location,
                teacher = course.teacher,
                extraData = course.extraData,
                user = user,
            )
        }

        fun valueOf(experimentCourse: ExperimentCourse, user: User): WeekCourseView {
            val courseDayTime = if (experimentCourse.startDayTime == experimentCourse.endDayTime) {
                "第${experimentCourse.startDayTime}节"
            } else {
                "${experimentCourse.startDayTime}-${experimentCourse.endDayTime}节"
            }
            val courseTime =
                "${startArray[experimentCourse.startDayTime - 1]} - ${endArray[experimentCourse.endDayTime - 1]}"
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
                courseDayTime = courseDayTime,
                courseTime = courseTime,
                location = experimentCourse.location,
                teacher = experimentCourse.teacherName,
                extraData = extraData,
                user = user,
            )
        }
    }
}

private val startArray = arrayOf(
    "08:00",
    "08:55",
    "10:00",
    "10:55",
    "14:00",
    "14:55",
    "16:00",
    "16:55",
    "19:00",
    "19:55",
    "20:50",
)

private val endArray = arrayOf(
    "08:45",
    "09:40",
    "10:45",
    "11:40",
    "14:45",
    "15:40",
    "16:45",
    "17:40",
    "19:45",
    "20:40",
    "21:35",
)

fun WeekCourseView.format(template: String): String {
    var result = template
    for (titleTemplate in TitleTemplate.values()) {
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
