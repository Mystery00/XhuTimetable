package vip.mystery0.xhu.timetable.repository

import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.OldCourseResponse
import vip.mystery0.xhu.timetable.module.Repo

interface CourseRepo : Repo {
    suspend fun fetchCourseList(
        user: User,
        year: Int,
        term: Int
    ): CourseResponse

    suspend fun saveCourseList(
        user: User,
        year: Int,
        term: Int,
        response: CourseResponse,
    ) {
    }

    suspend fun getCourseList(
        user: User,
        year: String,
        term: Int,
    ): List<OldCourseResponse>

    suspend fun saveCourseList(
        year: String,
        term: Int,
        studentId: String,
        list: List<OldCourseResponse>
    ) {
    }

    suspend fun getRandomCourseList(size: Int): List<OldCourseResponse> = emptyList()
}