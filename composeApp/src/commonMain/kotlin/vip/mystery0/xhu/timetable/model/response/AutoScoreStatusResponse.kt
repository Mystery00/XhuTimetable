package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDate

@Serializable
data class AutoScoreStatusResponse(
    val hasActiveTask: Boolean,
    val status: String?,
    val expireDate: XhuLocalDate?,
    val nextCheckTime: String?,
    val lastCheckTime: String?,
    val lastCheckResult: String?,
)
