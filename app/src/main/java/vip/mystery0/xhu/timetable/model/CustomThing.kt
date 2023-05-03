package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import java.time.LocalDate
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