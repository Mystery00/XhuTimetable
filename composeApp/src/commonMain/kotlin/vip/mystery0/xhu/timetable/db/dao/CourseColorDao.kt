package vip.mystery0.xhu.timetable.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import vip.mystery0.xhu.timetable.model.entity.CourseColor

@Dao
interface CourseColorDao {
    @Insert
    suspend fun save(courseColor: CourseColor)

    @Update
    suspend fun update(courseColor: CourseColor)

    @Delete
    suspend fun delete(courseColor: CourseColor)

    @Query("select * from tb_course_color")
    suspend fun queryAllCourseColorList(): List<CourseColor>

    @Query("select * from tb_course_color where courseName = :courseName limit 1")
    suspend fun selectCourseColor(
        courseName: String,
    ): CourseColor?
}