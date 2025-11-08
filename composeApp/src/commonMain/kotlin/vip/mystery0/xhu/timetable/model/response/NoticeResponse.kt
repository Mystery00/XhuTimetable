package vip.mystery0.xhu.timetable.model.response

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant

@Immutable
@Serializable
data class NoticeResponse(
    val noticeId: Int,
    //公告标题
    val title: String,
    //公告内容
    val content: String,
    //操作列表
    val actions: List<NoticeAction>,
    //发布状态
    val released: Boolean,
    val createTime: XhuInstant,
    val updateTime: XhuInstant,
)

@Serializable
data class NoticeAction(
    val text: String,
    val actionType: String,
    val metadata: String,
)

enum class NoticeActionType {
    COPY,
    OPEN_URI,
}

fun parseNoticeActionType(actionType: String): NoticeActionType? {
    return when (actionType) {
        "COPY" -> NoticeActionType.COPY
        "OPEN_URI" -> NoticeActionType.OPEN_URI
        else -> null
    }
}
