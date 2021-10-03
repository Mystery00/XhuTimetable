package vip.mystery0.xhu.timetable.repository

import vip.mystery0.xhu.timetable.model.entity.Notice
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.module.Repo

interface NoticeRepo : Repo {
    suspend fun queryAllNotice(): List<Notice>

    suspend fun hasUnReadNotice(): Boolean

    suspend fun saveList(noticeList: List<NoticeResponse>) {}

    suspend fun markAllAsRead() {}
}