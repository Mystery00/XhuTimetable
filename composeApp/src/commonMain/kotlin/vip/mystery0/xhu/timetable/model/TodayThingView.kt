package vip.mystery0.xhu.timetable.model

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.parseColorHexString
import vip.mystery0.xhu.timetable.utils.parseJsonToMap

data class TodayThingView(
    val title: String,
    val location: String,
    val allDay: Boolean,
    val startTime: XhuInstant,
    val endTime: XhuInstant,
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
            return startTime.toEpochMilliseconds()
        }

    fun showOnDay(showInstant: XhuInstant): Boolean {
        if (endTime < showInstant) {
            //事项结束时间比需要显示的时间早，说明事件结束了
            return false
        }
        if (saveAsCountDown) {
            //显示为倒计时，那么只要不结束就始终显示
            return true
        }
        //只要是今天的事项就显示，在一天内不计算是否结束
        val startDate = startTime.asLocalDateTime().date
        val endDate = endTime.asLocalDateTime().date
        val showDate = showInstant.asLocalDateTime().date

        return startDate <= showDate && endDate >= showDate
    }

    companion object {
        fun valueOf(thing: CustomThingResponse, user: User): TodayThingView {
            val metadataMap = thing.metadata.ifBlank { "{}" }.parseJsonToMap<String, String>()
            val saveAsCountDown =
                metadataMap[CustomThing.Key.SAVE_AS_COUNT_DOWN]?.toBoolean() ?: false
            return TodayThingView(
                title = thing.title,
                location = thing.location,
                allDay = thing.allDay,
                startTime = thing.startTime,
                endTime = thing.endTime,
                remark = thing.remark,
                color = thing.color.parseColorHexString(),
                saveAsCountDown,
                user = user,
            )
        }
    }
}
