package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDate

@Serializable
data class AutoScoreStatusResponse(
    val hasActiveTask: Boolean,
    val status: String?,
    val expireDate: XhuLocalDate?,
    val nextCheckTime: XhuInstant?,
    val lastCheckTime: XhuInstant?,
    val lastCheckResult: String?,
)
