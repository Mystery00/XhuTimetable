package vip.mystery0.xhu.timetable.repository

import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.module.Repo

interface CourseRepo : Repo {
    suspend fun getCourseList(
        year: String = "",
        term: Int = -1
    ): List<CourseResponse>

    suspend fun saveCourseList(
        year: String,
        term: Int,
        studentId: String,
        list: List<CourseResponse>
    ) {
    }
}