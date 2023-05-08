package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
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
    object Key {
        const val SAVE_AS_COUNT_DOWN = "key_save_as_count_down"
    }
}