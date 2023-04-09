package vip.mystery0.xhu.timetable.repository.local

import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.response.ClientInitResponse
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.model.response.XhuStartTime
import vip.mystery0.xhu.timetable.repository.StartRepo

class StartLocalRepo : StartRepo {
    override suspend fun init(): ClientInitResponse {
        val termStartDate = getConfigStore { termStartDate }
        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        return ClientInitResponse(
            XhuStartTime(termStartDate, nowYear, nowTerm),
            getConfigStore { splashList },
            null,
        )
    }

    override suspend fun checkVersion(): ClientVersion? = null
}