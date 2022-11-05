package vip.mystery0.xhu.timetable.repository.remote

import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.model.request.InitRequest
import vip.mystery0.xhu.timetable.model.response.InitResponse
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
import java.time.Duration
import java.time.Instant

class StartRemoteRepo : StartRepo, KoinComponent {
    private val serverApi: ServerApi by inject()

    private val local: StartRepo by localRepo()

    override suspend fun init(): InitResponse {
        val response = withTimeoutOrNull(Duration.ofSeconds(5).toMillis()) {
            serverApi.initRequest(InitRequest())
        } ?: return local.init()
        setConfig {
            customTermStartTime = Instant.ofEpochMilli(response.startTime) to false
            splashList = response.splash
            menuList = response.menu
        }
        return response
    }
}