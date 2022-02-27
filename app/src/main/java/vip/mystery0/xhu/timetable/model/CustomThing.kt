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

    companion object {
        fun extraDataToJson(map: Map<String, String>): String = MOSHI.toJson(map)

        private val MOSHI: JsonAdapter<Map<String, String>> by lazy {
            Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
                .adapter(
                    Types.newParameterizedType(
                        Map::class.java,
                        String::class.java,
                        String::class.java,
                    )
                )
        }
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

    val sort: Long
        get() {
            if (saveAsCountDown) {
                //倒计时优先
                return 0L
            }
            return startTime.atZone(chinaZone).toInstant().toEpochMilli()
        }

    fun showOnToday(showDate: LocalDate): Boolean {
        if (saveAsCountDown) {
            return true
        }
        return !startTime.toLocalDate().isAfter(showDate) &&
                !endTime.toLocalDate().isBefore(showDate)
    }

    val saveAsCountDown: Boolean
        get() = parseExtra()[Key.SAVE_AS_COUNT_DOWN]?.toBoolean() ?: false

    private var extra: Map<String, String>? = null

    private fun parseExtra(): Map<String, String> {
        if (extra != null) {
            return extra!!
        }
        if (extraData.isBlank()) {
            return emptyMap()
        }
        val data = MOSHI.fromJson(extraData) ?: emptyMap()
        extra = data
        return data
    }
}