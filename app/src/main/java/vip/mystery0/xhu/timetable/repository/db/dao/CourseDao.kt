package vip.mystery0.xhu.timetable.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import vip.mystery0.xhu.timetable.model.entity.CourseEntity

@Dao
interface CourseDao {
    @Insert
    suspend fun insert(entity: CourseEntity)

    @Delete
    suspend fun delete(entity: CourseEntity)

    @Query("select * from tb_course where studentId = :username and year = :year and term = :term")
    suspend fun queryList(
        username: String,
        year: Int,
        term: Int,
    ): List<CourseEntity>

    @Query("select * from tb_course group by courseName limit :size")
    suspend fun queryRandomList(size: Int): List<CourseEntity>

    @Query("select courseName from tb_course group by courseName")
    suspend fun queryDistinctCourseByUsernameAndTerm(): List<String>

    @Query("select courseName from tb_course where courseName like :keywords group by courseName")
    suspend fun queryDistinctCourseByKeywordsAndUsernameAndTerm(keywords: String): List<String>
}