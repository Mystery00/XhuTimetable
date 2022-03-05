package vip.mystery0.xhu.timetable.repository.network

import vip.mystery0.xhu.timetable.model.entity.Notice
import vip.mystery0.xhu.timetable.module.NetworkRepo
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.module.remoteRepo
import vip.mystery0.xhu.timetable.repository.NoticeRepo

object NoticeNetworkRepo : NoticeRepo, NetworkRepo<NoticeRepo> {
    override val local: NoticeRepo by localRepo()
    override val remote: NoticeRepo by remoteRepo()

    override suspend fun queryAllNotice(): List<Notice> =
        dispatch().queryAllNotice()

    override suspend fun hasUnReadNotice(): Boolean =
        dispatch().hasUnReadNotice()
}