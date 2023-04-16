package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.module.registerAdapter
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import java.time.Instant

data class TodayThingView(
    val title: String,
    val location: String,
    val allDay: Boolean,
    val startTime: Instant,
    val endTime: Instant,
    val remark: String,
    val color: Color,
    val saveAsCountDown: Boolean,
    //归属用户
    val user: User,
) {
    val sort: Long
        get() {
            if (saveAsCountDown) {
                //倒计时优先
                return 0L
            }
            return startTime.toEpochMilli()
        }

    fun showOnDay(showInstant: Instant): Boolean {
        if (endTime.isBefore(showInstant)) {
            //事项结束时间比需要显示的时间早，说明事件结束了
            return false
        }
        if (saveAsCountDown) {
            //显示为倒计时，那么只要不结束就始终显示
            return true
        }
        //只要是今天的事项就显示，在一天内不计算是否结束
        val startDate = startTime.asLocalDateTime().toLocalDate()
        val endDate = endTime.asLocalDateTime().toLocalDate()
        val showDate = showInstant.asLocalDateTime().toLocalDate()

        return !startDate.isAfter(showDate) && !endDate.isBefore(showDate)
    }

    companion object {
        private val metadataMoshi: JsonAdapter<Map<String, String>> =
            Moshi.Builder().registerAdapter().build()
                .adapter(
                    Types.newParameterizedType(
                        Map::class.java,
                        String::class.java,
                        String::class.java,
                    )
                )

        fun valueOf(thing: CustomThingResponse, user: User): TodayThingView {
            val metadataMap =
                kotlin.runCatching { metadataMoshi.fromJson(thing.metadata) }.getOrNull()
                    ?: emptyMap()
            val saveAsCountDown =
                metadataMap[CustomThing.Key.SAVE_AS_COUNT_DOWN]?.toBoolean() ?: false
            return TodayThingView(
                title = thing.title,
                location = thing.location,
                allDay = thing.allDay,
                startTime = thing.startTime,
                endTime = thing.endTime,
                remark = thing.remark,
                color = Color(android.graphics.Color.parseColor(thing.color)),
                saveAsCountDown,
                user = user,
            )
        }
    }
}
