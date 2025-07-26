package vip.mystery0.xhu.timetable.db.dao

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

    @Query("select courseName from tb_custom_course group by courseName")
    suspend fun queryDistinctCourseByUsernameAndTerm(): List<String>

    @Query("select courseName from tb_custom_course where courseName like :keywords group by courseName")
    suspend fun queryDistinctCourseByKeywordsAndUsernameAndTerm(keywords: String): List<String>
}