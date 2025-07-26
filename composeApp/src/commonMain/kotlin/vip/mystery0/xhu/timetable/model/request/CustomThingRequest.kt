package vip.mystery0.xhu.timetable.model.request

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDateTime
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.formatMapToJson
import vip.mystery0.xhu.timetable.utils.toHexString

@Serializable
data class CustomThingRequest(
    val title: String,
    val location: String,
    val allDay: Boolean,
    val startTime: Long,
    var endTime: Long,
    val remark: String?,
    val color: String,
    val metadata: String?,
) {
    companion object {
        fun buildOf(
            title: String,
            location: String,
            allDay: Boolean,
            startTime: XhuLocalDateTime,
            endTime: XhuLocalDateTime,
            remark: String,
            color: Color,
            map: Map<String, String>,
        ) = CustomThingRequest(
            title,
            location,
            allDay,
            startTime.asInstant().toEpochMilliseconds(),
            endTime.asInstant().toEpochMilliseconds(),
            remark,
            color.toHexString(),
            map.formatMapToJson(),
        )
    }
}