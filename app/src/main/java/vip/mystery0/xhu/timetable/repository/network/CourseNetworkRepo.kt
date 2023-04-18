package vip.mystery0.xhu.timetable.repository.network

import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.OldCourseResponse
import vip.mystery0.xhu.timetable.module.NetworkRepo
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.module.remoteRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo111

object CourseNetworkRepo : CourseRepo111, NetworkRepo<CourseRepo111> {
    override val local: CourseRepo111 by localRepo()
    override val remote: CourseRepo111 by remoteRepo()

    override suspend fun fetchCourseList(user: User, year: Int, term: Int): CourseResponse =
        //TODO 修改为新接口
        remote.fetchCourseList(user, year, term)

    override suspend fun getCourseList(
        user: User,
        year: String,
        term: Int
    ): List<OldCourseResponse> =
        dispatch().getCourseList(user, year, term)
}