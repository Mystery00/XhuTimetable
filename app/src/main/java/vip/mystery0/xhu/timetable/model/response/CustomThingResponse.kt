package vip.mystery0.xhu.timetable.model.response

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.toHexString
import java.time.Instant
import java.time.LocalDateTime

data class CustomThingResponse(
    var thingId: Long,
    var title: String,
    var location: String,
    var allDay: Boolean,
    var startTime: Instant,
    var endTime: Instant,
    var remark: String,
    var color: String,
    var metadata: String,
    val createTime: Instant,
){
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
        val data = MOSHI.fromJson(metadata) ?: emptyMap()
        extra = data
        return data
    }

    companion object {
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

        val PLACEHOLDER = CustomThingResponse(
            0L,
            "事项名称",
            "地点",
            false,
            Instant.now(),
            Instant.now(),
            "备注",
            ColorPool.random.toHexString(),
            "",
            Instant.now(),
        )

        fun init(): CustomThingResponse {
            val now = LocalDateTime.now()
            val plus = now.plusHours(1)
            return CustomThingResponse(
                0L,
                "",
                "",
                false,
                now.withMinute(0).withSecond(0).withNano(0).asInstant(),
                plus.withMinute(0).withSecond(0).withNano(0).asInstant(),
                "",
                ColorPool.random.toHexString(),
                "",
                Instant.now(),
            )
        }
    }
}