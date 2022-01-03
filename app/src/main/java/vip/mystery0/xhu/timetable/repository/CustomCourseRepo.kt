package vip.mystery0.xhu.timetable.repository

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.config.ServerError
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.model.CustomCourse
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest

suspend fun getCustomCourseList(
    user: User,
    year: String,
    term: Int,
): List<CustomCourse> {
    val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
    val response = user.withAutoLogin {
        serverApi.customCourseList(it, year, term).checkLogin()
    }
    return response.first.onEach {
        if (it.courseIndex.size == 1) {
            it.courseIndex = listOf(it.courseIndex[0], it.courseIndex[0])
        }
    }
}

suspend fun createCustomCourse(
    user: User,
    year: String,
    term: Int,
    customCourse: CustomCourse,
) {
    val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
    val request = CustomCourseRequest(
        customCourse.courseName,
        customCourse.teacherName,
        customCourse.week,
        customCourse.location,
        customCourse.courseIndex[0],
        customCourse.courseIndex[1],
        customCourse.day,
        customCourse.extraData,
        year,
        term,
    )
    val response = user.withAutoLogin {
        serverApi.createCustomCourse(it, request).checkLogin()
    }
    if (!response.first) {
        throw ServerError("创建自定义课程失败")
    }
}

suspend fun updateCustomCourse(
    user: User,
    year: String,
    term: Int,
    customCourse: CustomCourse,
) {
    val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
    val request = CustomCourseRequest(
        customCourse.courseName,
        customCourse.teacherName,
        customCourse.week,
        customCourse.location,
        customCourse.courseIndex[0],
        customCourse.courseIndex[1],
        customCourse.day,
        customCourse.extraData,
        year,
        term,
    )
    val response = user.withAutoLogin {
        serverApi.updateCustomCourse(it, customCourse.courseId, request).checkLogin()
    }
    if (!response.first) {
        throw ServerError("创建自定义课程失败")
    }
}