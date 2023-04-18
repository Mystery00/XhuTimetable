package vip.mystery0.xhu.timetable.model.response

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate

data class CustomCourseResponse(
    val courseId: Long,
    val courseName: String,
    val weekStr: String,
    val weekList: List<Int>,
    val day: DayOfWeek,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val location: String,
    val teacher: String,
    val extraData: List<String>,
    val createTime: Instant,
) {
    companion object {
        val PLACEHOLDER = CustomCourseResponse(
            0L,
            "课程名称",
            "第1周",
            listOf(1),
            DayOfWeek.MONDAY,
            1,
            1,
            1,
            "上课地点",
            "教师名称",
            emptyList(),
            Instant.now(),
        )

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
                "",
                "",
                emptyList(),
                Instant.now(),
            )
        }
    }
}
