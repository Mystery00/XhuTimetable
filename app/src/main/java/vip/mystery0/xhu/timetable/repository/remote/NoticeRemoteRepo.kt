package vip.mystery0.xhu.timetable.repository.remote

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.Config
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.model.entity.Notice
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.NoticeRepo
import java.time.LocalDate

class NoticeRemoteRepo : NoticeRepo, KoinComponent {
    private val serverApi: ServerApi by inject()

    private val local: NoticeRepo by localRepo()

    private suspend fun updateNoticeList() {
        val response = SessionManager.mainUser.withAutoLogin {
            serverApi.noticeList(it).checkLogin()
        }
        val noticeList = response.first
        local.saveList(noticeList)
    }

    override suspend fun queryAllNotice(): List<Notice> {
        updateNoticeList()
        return local.queryAllNotice()
    }

    override suspend fun hasUnReadNotice(): Boolean {
        if (Config.lastSyncNotice.isBefore(LocalDate.now())) {
            updateNoticeList()
        }
        return local.hasUnReadNotice()
    }
}