package vip.mystery0.xhu.timetable.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import vip.mystery0.xhu.timetable.model.entity.ExperimentCourseEntity

@Dao
interface ExperimentCourseDao {
    @Insert
    suspend fun insert(entity: ExperimentCourseEntity)

    @Delete
    suspend fun delete(entity: ExperimentCourseEntity)

    @Query("select * from tb_experiment_course where studentId = :username and year = :year and term = :term")
    suspend fun queryList(
        username: String,
        year: Int,
        term: Int,
    ): List<ExperimentCourseEntity>
}