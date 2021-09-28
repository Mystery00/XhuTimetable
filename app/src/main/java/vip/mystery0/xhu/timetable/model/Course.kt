package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color

data class Course(
    val courseName: String,
    val teacherName: String,
    val location: String,
    val weekSet: List<Int>,
    val weekString: String,
    val timeString: String,
    val time: String,
    val day: Int,
    val thisWeek: Boolean,
    val color: Color,
)