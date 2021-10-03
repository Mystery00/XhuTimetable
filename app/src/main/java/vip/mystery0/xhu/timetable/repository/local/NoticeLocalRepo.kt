package vip.mystery0.xhu.timetable.repository.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.model.entity.Notice
import vip.mystery0.xhu.timetable.model.response.NoticeResponse
import vip.mystery0.xhu.timetable.repository.NoticeRepo
import vip.mystery0.xhu.timetable.repository.db.dao.NoticeDao

class NoticeLocalRepo : NoticeRepo, KoinComponent {
    private val noticeDao: NoticeDao by inject()

    override suspend fun queryAllNotice(): List<Notice> =
        withContext(Dispatchers.IO) { noticeDao.queryAllNoticeList() }

    override suspend fun hasUnReadNotice(): Boolean =
        withContext(Dispatchers.IO) { noticeDao.queryNoticeList().isNotEmpty() }

    override suspend fun saveList(noticeList: List<NoticeResponse>): Unit =
        withContext(Dispatchers.IO) {
            val save = HashMap<String, Notice>()
            val saveList = queryAllNotice()
            saveList.forEach {
                save[it.serverId] = it
            }
            //删除所有历史数据
            saveList.forEach {
                noticeDao.deleteNotice(it)
            }
            //组建新数据
            val newList = noticeList.map {
                Notice(it.id, it.title, it.content, it.createTime, save[it.id]?.read ?: false)
            }
            //存储新数据
            newList.forEach {
                noticeDao.saveNotice(it)
            }
        }

    override suspend fun markAllAsRead(): Unit =
        withContext(Dispatchers.IO) {
            noticeDao.queryNoticeList(read = false).forEach {
                it.read = true
                noticeDao.updateNotice(it)
            }
        }
}