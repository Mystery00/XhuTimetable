package vip.mystery0.xhu.timetable.model.request

import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.model.CustomThing

data class CustomThingRequest(
    val title: String,
    val location: String,
    val allDay: Boolean,
    val startTime: Long,
    val endTime: Long,
    val remark: String,
    val color: String,
    val extraData: String,
    val year: String,
    val term: Int,
) {
    companion object {
        fun valueOf(
            customThing: CustomThing,
            color: String,
            year: String,
            term: Int,
        ) = CustomThingRequest(
            customThing.title,
            customThing.location,
            customThing.allDay,
            customThing.startTime.atZone(chinaZone).toInstant().toEpochMilli(),
            customThing.endTime.atZone(chinaZone).toInstant().toEpochMilli(),
            customThing.remark,
            color,
            customThing.extraData,
            year,
            term
        )
    }
}