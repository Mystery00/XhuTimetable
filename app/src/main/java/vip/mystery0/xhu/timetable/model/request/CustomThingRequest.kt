package vip.mystery0.xhu.timetable.model.request

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.module.registerAdapter
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
            Moshi.Builder().registerAdapter().build()
                .adapter(
                    Types.newParameterizedType(
                        Map::class.java,
                        String::class.java,
                        String::class.java,
                    )
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
            startTime.atZone(chinaZone).toInstant().toEpochMilli(),
            endTime.atZone(chinaZone).toInstant().toEpochMilli(),
            remark,
            color.toHexString(),
            extraDataToJson(map),
        )
    }
}