package vip.mystery0.xhu.timetable.model.response

data class CustomThingResponse(
    var thingId: Long,
    var title: String,
    var location: String,
    var allDay: Boolean,
    var startTime: Long,
    var endTime: Long,
    var remark: String,
    var color: String,
    var extraData: String,
)