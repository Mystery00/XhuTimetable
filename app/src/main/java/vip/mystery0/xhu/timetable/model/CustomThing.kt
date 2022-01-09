package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import java.time.LocalDateTime

data class CustomThing(
    var thingId: Long,
    var title: String,
    var location: String,
    var allDay: Boolean,
    var startTime: LocalDateTime,
    var endTime: LocalDateTime,
    var remark: String,
    var colorString: String,
    var color: Color,
    var extraData: String,
) {
    companion object {
        val PLACEHOLDER =
            CustomThing(
                0,
                "事项名称",
                "地点",
                false,
                LocalDateTime.now(),
                LocalDateTime.now(),
                "备注",
                "",
                ColorPool.random,
                ""
            )
        val EMPTY: CustomThing
            get() {
                val now = LocalDateTime.now()
                val plus = now.plusHours(1)
                return CustomThing(
                    0,
                    "",
                    "",
                    false,
                    now.withMinute(0).withSecond(0).withNano(0),
                    plus.withMinute(0).withSecond(0).withNano(0),
                    "",
                    "",
                    ColorPool.hash(now.atZone(chinaZone).toInstant().toEpochMilli().toString()),
                    ""
                )
            }
    }

    fun toEntity(
        studentId: String,
        year: String,
        term: Int,
    ) = vip.mystery0.xhu.timetable.model.entity.CustomThing(
        thingId,
        title,
        location,
        allDay,
        startTime.atZone(chinaZone).toInstant().toEpochMilli(),
        endTime.atZone(chinaZone).toInstant().toEpochMilli(),
        remark,
        colorString,
        extraData,
        year,
        term,
        studentId,
    )
}