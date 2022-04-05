package vip.mystery0.xhu.timetable.repository.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import vip.mystery0.xhu.timetable.model.entity.CourseItem
import vip.mystery0.xhu.timetable.model.entity.CourseSource

@Dao
interface CourseDao {
    @Insert
    fun saveCourseItem(courseItem: CourseItem)

    @Delete
    fun deleteCourseItem(courseItem: CourseItem)

    @Query("select * from tb_course_item where studentId = :username and year = :year and term = :term and source = :courseSource")
    suspend fun queryCourseList(
        username: String,
        year: String,
        term: Int,
        courseSource: CourseSource = CourseSource.JWC,
    ): List<CourseItem>

    @Query("select * from tb_course_item group by courseName")
    suspend fun queryDistinctCourseByUsernameAndTerm(): List<CourseItem>

    @Query("select * from tb_course_item where courseName like :keywords group by courseName")
    suspend fun queryDistinctCourseByKeywordsAndUsernameAndTerm(keywords: String): List<CourseItem>
}