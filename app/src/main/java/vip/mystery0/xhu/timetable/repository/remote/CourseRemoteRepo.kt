package vip.mystery0.xhu.timetable.repository.remote

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.JwcApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo
import java.time.LocalDate

class CourseRemoteRepo : CourseRepo {
    private val jwcApi: JwcApi by inject()

    private val local: CourseRepo by localRepo()

    override suspend fun getCourseList(
        user: User,
        year: String,
        term: Int,
    ): List<CourseResponse> = runOnCpu {
        val response = user.withAutoLogin {
            jwcApi.courseList(
                it,
                getConfig { currentYear },
                getConfig { currentTerm },
                getConfig { showCustomCourseOnWeek },
            ).checkLogin()
        }
        val courseList = response.first
        courseList.forEach { it.user = user }
        local.saveCourseList(year, term, user.studentId, courseList)
        setConfig { lastSyncCourse = LocalDate.now() }
        courseList
    }
}