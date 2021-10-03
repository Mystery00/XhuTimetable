package vip.mystery0.xhu.timetable.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import vip.mystery0.xhu.timetable.model.entity.Notice

@Dao
interface NoticeDao {
    @Insert
    fun saveNotice(notice: Notice)

    @Delete
    fun deleteNotice(notice: Notice)

    @Query("select * from tb_notice")
    suspend fun queryAllNoticeList(): List<Notice>

    @Query("select * from tb_notice where read = :read")
    suspend fun queryNoticeList(read: Boolean = false): List<Notice>
}