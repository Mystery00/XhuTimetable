package vip.mystery0.xhu.timetable.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import vip.mystery0.xhu.timetable.model.entity.CourseConverts
import vip.mystery0.xhu.timetable.model.entity.CourseItem
import vip.mystery0.xhu.timetable.model.entity.Notice
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao
import vip.mystery0.xhu.timetable.repository.db.dao.NoticeDao

@Database(entities = [CourseItem::class, Notice::class], version = 1)
@TypeConverters(
    CourseConverts::class,
)
abstract class DB : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun noticeDao(): NoticeDao
}