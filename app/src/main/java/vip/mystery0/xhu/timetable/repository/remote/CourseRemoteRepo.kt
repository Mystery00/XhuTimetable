package vip.mystery0.xhu.timetable.repository.remote

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CourseApi
import vip.mystery0.xhu.timetable.api.JwcApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.coroutine.RepoCoroutineScope
import vip.mystery0.xhu.timetable.config.coroutine.RetryCallback
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLogin
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.OldCourseResponse
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo
import java.time.LocalDate

class CourseRemoteRepo : CourseRepo {
    private val jwcApi: JwcApi by inject()

    private val courseApi: CourseApi by inject()

    private val local: CourseRepo by localRepo()

    override suspend fun fetchCourseList(user: User, year: Int, term: Int): CourseResponse {
        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        val showCustomCourseOnWeek = getConfigStore { showCustomCourseOnWeek }

        val coroutineExceptionHandler =
            CoroutineExceptionHandler { coroutineContext, throwable ->
                val callback = coroutineContext[RetryCallback]?.callback
                if (throwable is ServerNeedLoginException) {
                    callback?.invoke()
                }
            }
        RepoCoroutineScope.scope.launch(coroutineExceptionHandler + RetryCallback {

        }) {
            courseApi.courseList(it, nowYear, nowTerm, showCustomCourseOnWeek)
        }

        val courseResponse = user.withAutoLoginOnce {
            courseApi.courseList(it, nowYear, nowTerm, showCustomCourseOnWeek)
        }
        local.saveCourseList(user, nowYear, nowTerm, courseResponse)
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