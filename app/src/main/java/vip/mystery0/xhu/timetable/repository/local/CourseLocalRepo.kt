package vip.mystery0.xhu.timetable.repository.local

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.model.entity.CourseItem
import vip.mystery0.xhu.timetable.model.entity.CourseSource
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao

class CourseLocalRepo : CourseRepo {
    private val courseDao: CourseDao by inject()

    override suspend fun getCourseList(
        user: User,
        year: String,
        term: Int,
    ): List<CourseResponse> = runOnIo {
        val courseList = courseDao.queryCourseList(user.studentId, year, term)
        val result = ArrayList<CourseResponse>(courseList.size)
        val map = HashMap<String, CourseResponse>()
        val weekMap = HashMap<String, ArrayList<Int>>()
        courseList.forEach { item ->
            val key =
                "${item.courseName}!${item.teacherName}!${item.location}!${item.weekIndex}!${item.time}!${item.type}"
            var courseItem = map[key]
            if (courseItem == null) {
                courseItem = CourseResponse(
                    item.courseName,
                    item.teacherName,
                    item.location,
                    item.weekString,
                    arrayListOf(),
                    item.time.split(",").map { it.toInt() },
                    item.type,
                    item.weekIndex,
                )
                map[key] = courseItem
            }
            val list = weekMap[key] ?: arrayListOf()
            list.add(item.weekNum)
            weekMap[key] = list
        }
        map.forEach { (key, courseResponse) ->
            result.add(
                CourseResponse(
                    courseResponse.name,
                    courseResponse.teacher,
                    courseResponse.location,
                    courseResponse.weekString,
                    weekMap[key]!!,
                    courseResponse.time,
                    courseResponse.type,
                    courseResponse.day,
                )
            )
        }
        result.forEach { it.user = user }
        result
    }

    override suspend fun saveCourseList(
        year: String,
        term: Int,
        studentId: String,
        list: List<CourseResponse>
    ) = runOnIo {
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
                        course.weekString,
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