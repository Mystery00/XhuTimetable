package vip.mystery0.xhu.timetable.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import vip.mystery0.xhu.timetable.model.entity.CustomCourseEntity

@Dao
interface CustomCourseDao {
    @Insert
    suspend fun insert(entity: CustomCourseEntity)

    @Delete
    suspend fun delete(entity: CustomCourseEntity)

    @Query("select * from tb_custom_course where studentId = :username and year = :year and term = :term")
    suspend fun queryList(
        username: String,
        year: Int,
        term: Int,
    ): List<CustomCourseEntity>
}