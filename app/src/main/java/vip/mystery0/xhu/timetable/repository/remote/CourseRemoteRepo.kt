package vip.mystery0.xhu.timetable.repository.remote

import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CourseApi
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.OldCourseResponse
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo111
import java.time.LocalDate

class CourseRemoteRepo : CourseRepo111 {
    private val courseApi: CourseApi by inject()

    private val local: CourseRepo111 by localRepo()

    override suspend fun fetchCourseList(user: User, year: Int, term: Int): CourseResponse {
        val showCustomCourseOnWeek = getConfigStore { showCustomCourseOnWeek }

        val courseResponse = user.withAutoLoginOnce {
            courseApi.courseList(it, year, term, showCustomCourseOnWeek)
        }
        local.saveCourseList(user, year, term, courseResponse)
        setConfigStore { lastSyncCourse = LocalDate.now() }
        return courseResponse
    }

    override suspend fun getCourseList(
        user: User,
        year: String,
        term: Int,
    ): List<OldCourseResponse> = runOnCpu {
//        val response = user.withAutoLogin {
//            jwcApi.courseList(
//                it,
//                getConfig { currentYear },
//                getConfig { currentTerm },
//                getConfig { showCustomCourseOnWeek },
//                getConfig { autoCacheJwcCourse },
//            ).checkLogin()
//        }
//        val courseList = response.first
//        courseList.forEach { it.user = user }
//        local.saveCourseList(year, term, user.studentId, courseList)
//        setConfig { lastSyncCourse = LocalDate.now() }
//        courseList
        emptyList()
    }
}