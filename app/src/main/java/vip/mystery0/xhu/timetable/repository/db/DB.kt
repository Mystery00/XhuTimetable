package vip.mystery0.xhu.timetable.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import vip.mystery0.xhu.timetable.model.entity.CourseConverts
import vip.mystery0.xhu.timetable.model.entity.CourseItem
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao

@Database(entities = [CourseItem::class], version = 1)
@TypeConverters(
    CourseConverts::class,
)
abstract class DB : RoomDatabase() {
    abstract fun courseDao(): CourseDao
}