package vip.mystery0.xhu.timetable.repository.remote

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.model.request.InitRequest
import vip.mystery0.xhu.timetable.model.response.InitResponse
import vip.mystery0.xhu.timetable.repository.StartRepo

class StartRemoteRepo : StartRepo, KoinComponent {
    private val serverApi by inject<ServerApi>()

    override suspend fun init(): InitResponse = serverApi.initRequest(InitRequest(checkBeta = true))
}