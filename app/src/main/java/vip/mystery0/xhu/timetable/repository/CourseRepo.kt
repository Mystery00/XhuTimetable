package vip.mystery0.xhu.timetable.repository

import vip.mystery0.xhu.timetable.config.Config
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.module.Repo

interface CourseRepo : Repo {
    suspend fun getCourseList(
        user: User = SessionManager.mainUser,
        year: String = Config.currentYear,
        term: Int = Config.currentTerm,
    ): List<CourseResponse>

    suspend fun saveCourseList(
        year: String,
        term: Int,
        studentId: String,
        list: List<CourseResponse>
    ) {
    }
}