package vip.mystery0.xhu.timetable.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import vip.mystery0.xhu.timetable.model.entity.CourseColor
import vip.mystery0.xhu.timetable.model.entity.CourseConverts
import vip.mystery0.xhu.timetable.model.entity.CourseItem
import vip.mystery0.xhu.timetable.model.entity.Notice
import vip.mystery0.xhu.timetable.repository.db.dao.CourseColorDao
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao
import vip.mystery0.xhu.timetable.repository.db.dao.NoticeDao

@Database(entities = [CourseItem::class, CourseColor::class, Notice::class], version = 2)
@TypeConverters(
    CourseConverts::class,
)
abstract class DB : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun courseColorDao(): CourseColorDao
    abstract fun noticeDao(): NoticeDao
}