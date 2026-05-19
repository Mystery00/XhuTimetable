package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDate

@Serializable
data class AutoScoreStartResponse(
    val taskId: Long,
    val expireDate: XhuLocalDate,
)
