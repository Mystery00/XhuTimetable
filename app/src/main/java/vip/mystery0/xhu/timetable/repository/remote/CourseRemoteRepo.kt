package vip.mystery0.xhu.timetable.repository.remote

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo

class CourseRemoteRepo : CourseRepo, KoinComponent {
    private val serverApi: ServerApi by inject()

    private val local: CourseRepo by localRepo()

    override suspend fun getCourseList(
        year: String,
        term: Int
    ): List<CourseResponse> {
        val response = SessionManager.mainUser.withAutoLogin {
            serverApi.courseList(it)
        }
        val courseList = response.first
        val mainUser = SessionManager.mainUser
        local.saveCourseList(year, term, mainUser.studentId, courseList)
        return courseList
    }
}