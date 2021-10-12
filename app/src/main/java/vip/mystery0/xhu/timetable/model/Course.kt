package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.model.entity.CourseType
import vip.mystery0.xhu.timetable.utils.sha512

data class Course(
    val courseName: String,
    val teacherName: String,
    val location: String,
    val weekSet: List<Int>,
    val weekString: String,
    val type: CourseType,
    val timeSet: List<Int>,
    val timeString: String,
    val time: String,
    val day: Int,
    val thisWeek: Boolean,
    val today: Boolean,
    val tomorrow: Boolean,
    val color: Color,
    val studentId: String,
    val userName: String,
) {
    var key = ""

    fun generateKey() {
        key =
            "${courseName}!${teacherName}!${location}!${weekString}!${type}!${timeString}!${day}".sha512()
    }
}
