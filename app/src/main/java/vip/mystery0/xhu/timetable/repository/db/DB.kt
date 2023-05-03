package vip.mystery0.xhu.timetable.repository.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import vip.mystery0.xhu.timetable.model.entity.CourseColor
import vip.mystery0.xhu.timetable.model.entity.CourseEntity
import vip.mystery0.xhu.timetable.model.entity.CustomCourseEntity
import vip.mystery0.xhu.timetable.model.entity.CustomThingEntity
import vip.mystery0.xhu.timetable.model.entity.ExperimentCourseEntity
import vip.mystery0.xhu.timetable.model.entity.PracticalCourseEntity
import vip.mystery0.xhu.timetable.repository.db.dao.CourseColorDao
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao
import vip.mystery0.xhu.timetable.repository.db.dao.CustomCourseDao
import vip.mystery0.xhu.timetable.repository.db.dao.CustomThingDao
import vip.mystery0.xhu.timetable.repository.db.dao.ExperimentCourseDao
import vip.mystery0.xhu.timetable.repository.db.dao.PracticalCourseDao

@Database(
    entities = [
        CourseEntity::class,
        PracticalCourseEntity::class,
        ExperimentCourseEntity::class,
        CustomCourseEntity::class,
        CustomThingEntity::class,
        CourseColor::class,
    ],
    version = 5
)
@TypeConverters(Converters::class)
abstract class DB : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun practicalCourseDao(): PracticalCourseDao
    abstract fun experimentCourseDao(): ExperimentCourseDao
    abstract fun customCourseDao(): CustomCourseDao
    abstract fun customThingDao(): CustomThingDao
    abstract fun courseColorDao(): CourseColorDao
}

object Converters {
    @JvmStatic
    @TypeConverter
    fun fromList(list: List<Int>): String = list.joinToString(",")

    @JvmStatic
    @TypeConverter
    fun toList(str: String): List<Int> = str.split(",").map { it.toInt() }

    @JvmStatic
    @TypeConverter
    fun fromList2(list: List<String>): String = list.joinToString(",")

    @JvmStatic
    @TypeConverter
    fun toList2(str: String): List<String> = str.split(",")
}