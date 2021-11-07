package vip.mystery0.xhu.timetable.repository.db.dao

import androidx.room.*
import vip.mystery0.xhu.timetable.model.entity.CourseColor

@Dao
interface CourseColorDao {
    @Insert
    fun save(courseColor: CourseColor)

    @Update
    fun update(courseColor: CourseColor)

    @Delete
    fun delete(courseColor: CourseColor)

    @Query("select * from tb_course_color")
    suspend fun queryAllCourseColorList(): List<CourseColor>

    @Query("select * from tb_course_color where courseName = :courseName limit 1")
    suspend fun selectCourseColor(
        courseName: String,
    ): CourseColor?
}