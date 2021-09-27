package vip.mystery0.xhu.timetable.repository

import vip.mystery0.xhu.timetable.model.response.InitResponse
import vip.mystery0.xhu.timetable.module.Repo

interface StartRepo : Repo {
    suspend fun init(): InitResponse
}