package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.response.PracticalCourse
import vip.mystery0.xhu.timetable.ui.theme.ColorPool

data class PracticalCourseView(
    val courseName: String,
    val teacherName: String,
    val showWeek: String,
    val credit: Double,
    val color: Color,
    //归属用户
    val user: User,
) {
    companion object {
        fun valueOf(course: PracticalCourse, user: User): PracticalCourseView =
            PracticalCourseView(
                courseName = course.courseName,
                teacherName = course.teacher,
                showWeek = course.weekStr,
                credit = course.credit,
                color = ColorPool.hash(course.courseName),
                user = user,
            )
    }
}