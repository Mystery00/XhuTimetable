package vip.mystery0.xhu.timetable.repository.remote

import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CommonApi
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.config.setNewConfig
import vip.mystery0.xhu.timetable.model.request.ClientInitRequest
import vip.mystery0.xhu.timetable.model.response.ClientInitResponse
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
import java.time.Duration

class StartRemoteRepo : StartRepo, KoinComponent {
    private val commonApi: CommonApi by inject()

    private val local: StartRepo by localRepo()

    override suspend fun init(): ClientInitResponse {
        val clientInitResponse = withTimeoutOrNull(Duration.ofSeconds(3).toMillis()) {
            commonApi.clientInit(ClientInitRequest())
        } ?: return local.init()
        val xhuStartTime = clientInitResponse.xhuStartTime
        setNewConfig {
            customTermStartDate = Customisable.serverDetect(xhuStartTime.startDate)
            splashList = clientInitResponse.splash
        }
        return clientInitResponse
    }

    override suspend fun checkVersion(): ClientVersion? {
        return withTimeoutOrNull(Duration.ofSeconds(5).toMillis()) {
            commonApi.checkVersion()
        } ?: return local.checkVersion()
    }
}