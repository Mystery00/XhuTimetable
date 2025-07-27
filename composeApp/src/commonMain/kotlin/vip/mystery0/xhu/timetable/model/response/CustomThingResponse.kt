package vip.mystery0.xhu.timetable.model.response

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDateTime
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.parseJsonToMap
import vip.mystery0.xhu.timetable.utils.toHexString
import kotlin.time.Clock

@Serializable
data class CustomThingResponse(
    var thingId: Long,
    var title: String,
    var location: String,
    var allDay: Boolean,
    var startTime: XhuInstant,
    var endTime: XhuInstant,
    var remark: String,
    var color: String,
    var metadata: String,
    val createTime: XhuInstant,
) {
    val saveAsCountDown: Boolean
        get() = parseExtra()[CustomThing.Key.SAVE_AS_COUNT_DOWN]?.toBoolean() ?: false

    private var extra: Map<String, String>? = null

    private fun parseExtra(): Map<String, String> {
        if (extra != null) {
            return extra!!
        }
        if (metadata.isBlank()) {
            return emptyMap()
        }
        val data = metadata.parseJsonToMap<String, String>()
        extra = data
        return data
    }

    companion object {
        fun init(): CustomThingResponse {
            val now = XhuLocalDateTime.now()
            val startTime = now.date.atTime(now.hour, 0)
            val endTime = if (now.hour < 23) now.date.atTime(now.hour, 0)
            else (now.date.plus(1, DateTimeUnit.DAY)).atTime(0, 0)
            val color = ColorPool.random.toHexString()
            return CustomThingResponse(
                0L,
                "",
                "",
                false,
                startTime.asInstant(),
                endTime.asInstant(),
                "",
                color,
                "",
                Clock.System.now(),
            )
        }
    }
}