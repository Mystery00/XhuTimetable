package vip.mystery0.xhu.timetable.repository.local

import vip.mystery0.xhu.timetable.config.getNewConfig
import vip.mystery0.xhu.timetable.model.response.ClientInitResponse
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.model.response.XhuStartTime
import vip.mystery0.xhu.timetable.repository.StartRepo

class StartLocalRepo : StartRepo {
    override suspend fun init(): ClientInitResponse {
        val termStartDate = getNewConfig { termStartDate }
        val nowYear = getNewConfig { nowYear }
        val nowTerm = getNewConfig { nowTerm }
        return ClientInitResponse(
            XhuStartTime(termStartDate, nowYear, nowTerm),
            getNewConfig { splashList },
            null,
        )
    }

    override suspend fun checkVersion(): ClientVersion? = null
}