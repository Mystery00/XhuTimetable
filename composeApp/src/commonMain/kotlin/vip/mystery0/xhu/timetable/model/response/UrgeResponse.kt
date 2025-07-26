package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant
import vip.mystery0.xhu.timetable.model.transfer.PageResult

@Serializable
data class UrgeResponse(
    val remainCount: Int,
    val page: PageResult<UrgeItem>,
)

@Serializable
data class UrgeItem(
    var urgeId: Long,
    var title: String,
    var description: String,
    var count: Long,
    var rate: Int,
    var complete: Boolean,
    var urged: Boolean,
    var createTime: XhuInstant,
    var updateTime: XhuInstant,
)