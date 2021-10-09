package vip.mystery0.xhu.timetable.repository.remote

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.JwcApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.Config
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo
import java.time.LocalDate

class CourseRemoteRepo : CourseRepo, KoinComponent {
    private val jwcApi: JwcApi by inject()

    private val local: CourseRepo by localRepo()

    override suspend fun getCourseList(
        user: User,
        year: String,
        term: Int,
    ): List<CourseResponse> {
        val response = user.withAutoLogin {
            jwcApi.courseList(it, Config.currentYear, Config.currentTerm).checkLogin()
        }
        val courseList = response.first
        courseList.forEach { it.user = user }
        local.saveCourseList(year, term, user.studentId, courseList)
        Config.lastSyncCourse = LocalDate.now()
        return courseList
    }
}