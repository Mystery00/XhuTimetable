package vip.mystery0.xhu.timetable.repository.local

import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.model.response.InitResponse
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.repository.StartRepo

class StartLocalRepo : StartRepo {
    override suspend fun init(): InitResponse =
        InitResponse(
            null,
            getConfig { splashList },
            getConfig { termStartTime }.toEpochMilli(),
            getConfig { menuList },
        )

    override suspend fun checkVersion(): Version? = null
}