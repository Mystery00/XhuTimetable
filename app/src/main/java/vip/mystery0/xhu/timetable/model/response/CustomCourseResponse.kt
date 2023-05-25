package vip.mystery0.xhu.timetable.model.response

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class CustomCourseResponse(
    val courseId: Long,
    val courseName: String,
    val weekStr: String,
    val weekList: List<Int>,
    val day: DayOfWeek,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val location: String,
    val teacher: String,
    val extraData: List<String>,
    val createTime: Instant,
) {
    companion object {
        fun init(): CustomCourseResponse {
            val day = LocalDate.now().dayOfWeek
            return CustomCourseResponse(
                0L,
                "",
                "",
                listOf(),
                day,
                day.value,
                1,
                1,
                LocalTime.now(),
                LocalTime.now(),
                "",
                "",
                emptyList(),
                Instant.now(),
            )
        }
    }
}
