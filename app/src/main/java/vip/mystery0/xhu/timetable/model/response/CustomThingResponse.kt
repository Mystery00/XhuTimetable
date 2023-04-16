package vip.mystery0.xhu.timetable.model.response

import java.time.Instant

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
)