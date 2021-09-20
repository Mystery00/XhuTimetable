package vip.mystery0.xhu.timetable.repository

import vip.mystery0.xhu.timetable.model.response.InitResponse

interface StartRepo {
    suspend fun init(): InitResponse
}