package vip.mystery0.xhu.timetable.repository.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.model.entity.CourseItem
import vip.mystery0.xhu.timetable.model.entity.CourseSource
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao

class CourseLocalRepo : CourseRepo, KoinComponent {
    private val courseDao: CourseDao by inject()

    override suspend fun getCourseList(
        year: String,
        term: Int
    ): List<CourseResponse> {
        return ArrayList()
    }

    override suspend fun saveCourseList(
        year: String,
        term: Int,
        studentId: String,
        list: List<CourseResponse>
    ) = withContext(Dispatchers.IO) {
        //删除旧数据
        courseDao.queryCourseList(studentId, year, term).forEach {
            courseDao.deleteCourseItem(it)
        }
        list.forEach { course ->
            course.week.forEach { week ->
                courseDao.saveCourseItem(
                    CourseItem(
                        course.name,
                        course.teacher,
                        course.location,
                        week,
                        course.time.joinToString(","),
                        course.day,
                        course.type,
                        CourseSource.JWC,
                        year,
                        term,
                        studentId,
                    )
                )
            }
        }
    }
}