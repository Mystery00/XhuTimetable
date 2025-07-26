package vip.mystery0.xhu.timetable.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import vip.mystery0.xhu.timetable.model.entity.PracticalCourseEntity

@Dao
interface PracticalCourseDao {
    @Insert
    suspend fun insert(entity: PracticalCourseEntity)

    @Delete
    suspend fun delete(entity: PracticalCourseEntity)

    @Query("select * from tb_practical_course where studentId = :username and year = :year and term = :term")
    suspend fun queryList(
        username: String,
        year: Int,
        term: Int,
    ): List<PracticalCourseEntity>
}