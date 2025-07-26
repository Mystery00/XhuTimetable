package vip.mystery0.xhu.timetable.module

import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.db.AppDatabase

internal const val DATABASE_NAME = "xhu-timetable"

val databaseModule = module {
    single {
        getRoomDatabase(get())
    }
    single { get<AppDatabase>().courseDao() }
    single { get<AppDatabase>().practicalCourseDao() }
    single { get<AppDatabase>().experimentCourseDao() }
    single { get<AppDatabase>().customCourseDao() }
    single { get<AppDatabase>().customThingDao() }
    single { get<AppDatabase>().courseColorDao() }
}

// Room compiler generates the `actual` implementations
@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

private fun getRoomDatabase(
    builder: RoomDatabase.Builder<AppDatabase>
): AppDatabase = builder
    .addMigrations(MIGRATION_1_2)
    .addMigrations(MIGRATION_2_3)
    .addMigrations(MIGRATION_3_4)
    .addMigrations(MIGRATION_4_5)
    .build()


private val MIGRATION_1_2: Migration = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_course_color` (`courseName` TEXT NOT NULL, `color` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
    }
}

private val MIGRATION_2_3: Migration = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_custom_thing` (`thingId` INTEGER NOT NULL, `title` TEXT NOT NULL, `location` TEXT NOT NULL, `allDay` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `remark` TEXT NOT NULL, `color` TEXT NOT NULL, `extraData` TEXT NOT NULL, `year` TEXT NOT NULL, `term` INTEGER NOT NULL, `studentId` TEXT NOT NULL, PRIMARY KEY(`thingId`))")
    }
}

private val MIGRATION_3_4: Migration = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("alter table tb_course_item rename to _temp_tb_course_item")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_course_item` (`courseName` TEXT NOT NULL, `teacherName` TEXT NOT NULL, `location` TEXT NOT NULL, `weekString` TEXT NOT NULL, `weekNum` INTEGER NOT NULL, `time` TEXT NOT NULL, `weekIndex` INTEGER NOT NULL, `type` INTEGER NOT NULL, `source` INTEGER NOT NULL, `extraData` TEXT NOT NULL, `year` TEXT NOT NULL, `term` INTEGER NOT NULL, `studentId` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
        connection.execSQL("insert into tb_course_item select courseName, teacherName, location, weekString, weekNum, time, weekIndex, type, source, '[]', year, term, studentId, id from _temp_tb_course_item")
        connection.execSQL("drop table _temp_tb_course_item")
    }
}

private val MIGRATION_4_5: Migration = object : Migration(4, 5) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("drop table tb_course_item")
        connection.execSQL("drop table tb_custom_thing")
        connection.execSQL("drop table tb_notice")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_course` (`courseName` TEXT NOT NULL, `weekStr` TEXT NOT NULL, `weekList` TEXT NOT NULL, `dayIndex` INTEGER NOT NULL, `startDayTime` INTEGER NOT NULL, `endDayTime` INTEGER NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `location` TEXT NOT NULL, `teacher` TEXT NOT NULL, `extraData` TEXT NOT NULL, `campus` TEXT NOT NULL, `courseType` TEXT NOT NULL, `credit` REAL NOT NULL, `courseCodeType` TEXT NOT NULL, `courseCodeFlag` TEXT NOT NULL, `year` INTEGER NOT NULL, `term` INTEGER NOT NULL, `studentId` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_practical_course` (`courseName` TEXT NOT NULL, `weekStr` TEXT NOT NULL, `weekList` TEXT NOT NULL, `teacher` TEXT NOT NULL, `campus` TEXT NOT NULL, `credit` REAL NOT NULL, `year` INTEGER NOT NULL, `term` INTEGER NOT NULL, `studentId` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_experiment_course` (`courseName` TEXT NOT NULL, `experimentProjectName` TEXT NOT NULL, `teacherName` TEXT NOT NULL, `experimentGroupName` TEXT NOT NULL, `weekStr` TEXT NOT NULL, `weekList` TEXT NOT NULL, `dayIndex` INTEGER NOT NULL, `startDayTime` INTEGER NOT NULL, `endDayTime` INTEGER NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `region` TEXT NOT NULL, `location` TEXT NOT NULL, `year` INTEGER NOT NULL, `term` INTEGER NOT NULL, `studentId` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_custom_course` (`courseId` INTEGER NOT NULL, `courseName` TEXT NOT NULL, `weekStr` TEXT NOT NULL, `weekList` TEXT NOT NULL, `dayIndex` INTEGER NOT NULL, `startDayTime` INTEGER NOT NULL, `endDayTime` INTEGER NOT NULL, `startTime` TEXT NOT NULL, `endTime` TEXT NOT NULL, `location` TEXT NOT NULL, `teacher` TEXT NOT NULL, `extraData` TEXT NOT NULL, `createTime` INTEGER NOT NULL, `year` INTEGER NOT NULL, `term` INTEGER NOT NULL, `studentId` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `tb_custom_thing` (`thingId` INTEGER NOT NULL, `title` TEXT NOT NULL, `location` TEXT NOT NULL, `allDay` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `remark` TEXT NOT NULL, `color` TEXT NOT NULL, `metadata` TEXT NOT NULL, `createTime` INTEGER NOT NULL, `studentId` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT)")
    }
}