package vip.mystery0.xhu.timetable.model.response

data class UrgeResponse(
    val remainCount: Int,
    val urgeList: List<UrgeItem>
)

data class UrgeItem(
    var urgeId: Long,
    var title: String,
    var description: String,
    var count: Long,
    var rate: Int,
    var complete: Boolean,
    var urged: Boolean,
    var createTime: Long,
    var updateTime: Long,
)