package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDateTime

data class CustomThing(
    var thingId: Long,
    var title: String,
    var location: String,
    var allDay: Boolean,
    var startTime: XhuLocalDateTime,
    var endTime: XhuLocalDateTime,
    var remark: String,
    var colorString: String,
    var color: Color,
    var extraData: String,
) {
    object Key {
        const val SAVE_AS_COUNT_DOWN = "key_save_as_count_down"
    }
}