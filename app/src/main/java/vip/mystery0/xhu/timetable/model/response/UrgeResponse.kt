package vip.mystery0.xhu.timetable.model.response

import vip.mystery0.xhu.timetable.model.transfer.PageResult
import java.time.Instant

data class UrgeResponse(
    val remainCount: Int,
    val page: PageResult<UrgeItem>,
)

data class UrgeItem(
    var urgeId: Long,
    var title: String,
    var description: String,
    var count: Long,
    var rate: Int,
    var complete: Boolean,
    var urged: Boolean,
    var createTime: Instant,
    var updateTime: Instant,
)