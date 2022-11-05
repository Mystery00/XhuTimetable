package vip.mystery0.xhu.timetable.repository.network

import vip.mystery0.xhu.timetable.model.response.InitResponse
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.module.NetworkRepo
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.module.remoteRepo
import vip.mystery0.xhu.timetable.repository.StartRepo

object StartNetworkRepo : StartRepo, NetworkRepo<StartRepo> {
    override val local: StartRepo by localRepo()
    override val remote: StartRepo by remoteRepo()

    override suspend fun init(): InitResponse =
        dispatch().init()

    override suspend fun checkVersion(): Version? =
        dispatch().checkVersion()
}