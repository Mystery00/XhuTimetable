package vip.mystery0.xhu.timetable.model.response

import java.time.Instant

data class NoticeResponse(
    val noticeId: Int,
    //公告标题
    val title: String,
    //公告内容
    val content: String,
    //发布状态
    val released: Boolean,
    val createTime: Instant,
    val updateTime: Instant,
)
