package vip.mystery0.xhu.timetable.repository.network

import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.module.NetworkRepo
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.module.remoteRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo

object CourseNetworkRepo : CourseRepo, NetworkRepo<CourseRepo> {
    override val local: CourseRepo by localRepo()
    override val remote: CourseRepo by remoteRepo()

    override suspend fun getCourseList(user: User, year: String, term: Int): List<CourseResponse> =
        dispatch().getCourseList(user, year, term)
}