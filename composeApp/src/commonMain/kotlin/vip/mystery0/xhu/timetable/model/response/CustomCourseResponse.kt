package vip.mystery0.xhu.timetable.model.response

import kotlin.time.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDate
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalTime
import vip.mystery0.xhu.timetable.utils.now

@Serializable
data class CustomCourseResponse(
    val courseId: Long,
    val courseName: String,
    val weekStr: String,
    val weekList: List<Int>,
    val day: DayOfWeek,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val startTime: XhuLocalTime,
    val endTime: XhuLocalTime,
    val location: String,
    val teacher: String,
    val extraData: List<String>,
    val createTime: XhuInstant,
) {
    companion object {
        fun init(): CustomCourseResponse {
            val day = XhuLocalDate.now().dayOfWeek
            return CustomCourseResponse(
                0L,
                "",
                "",
                listOf(),
                day,
                day.isoDayNumber,
                1,
                1,
                XhuLocalTime.now(),
                XhuLocalTime.now(),
                "",
                "",
                emptyList(),
                Clock.System.now(),
            )
        }
    }
}
