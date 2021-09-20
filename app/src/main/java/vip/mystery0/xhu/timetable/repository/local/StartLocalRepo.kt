package vip.mystery0.xhu.timetable.repository.local

import vip.mystery0.xhu.timetable.model.response.InitResponse
import vip.mystery0.xhu.timetable.repository.StartRepo

class StartLocalRepo : StartRepo {
    override suspend fun init(): InitResponse {
        return InitResponse(null, emptyList(), 1L)
    }
}