package vip.mystery0.xhu.timetable.model.response

import java.time.LocalDate

data class ClientInitResponse(
    val xhuStartTime: XhuStartTime,
    val splash: List<Splash>,
)

data class XhuStartTime(
    val startTime: Long,
    val startDate: LocalDate,
    val nowYear: Int,
    val nowTerm: Int,
)