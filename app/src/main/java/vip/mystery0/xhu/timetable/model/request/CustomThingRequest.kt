package vip.mystery0.xhu.timetable.model.request

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonAdapter
import vip.mystery0.xhu.timetable.module.moshiTypeAdapter
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.toHexString
import java.time.LocalDateTime

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
        private fun extraDataToJson(map: Map<String, String>): String = MOSHI.toJson(map)

        private val MOSHI: JsonAdapter<Map<String, String>> by lazy {
            moshiTypeAdapter(
                Map::class.java,
                String::class.java,
                String::class.java,
            )
        }

        fun buildOf(
            title: String,
            location: String,
            allDay: Boolean,
            startTime: LocalDateTime,
            endTime: LocalDateTime,
            remark: String,
            color: Color,
            map: Map<String, String>,
        ) = CustomThingRequest(
            title,
            location,
            allDay,
            startTime.asInstant().toEpochMilli(),
            endTime.asInstant().toEpochMilli(),
            remark,
            color.toHexString(),
            extraDataToJson(map),
        )
    }
}