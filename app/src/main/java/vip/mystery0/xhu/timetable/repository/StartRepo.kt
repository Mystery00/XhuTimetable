package vip.mystery0.xhu.timetable.repository

import vip.mystery0.xhu.timetable.model.response.ClientInitResponse
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.model.response.InitResponse
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.module.Repo

interface StartRepo : Repo {
    suspend fun init(): ClientInitResponse

    suspend fun checkVersion(): ClientVersion?
}