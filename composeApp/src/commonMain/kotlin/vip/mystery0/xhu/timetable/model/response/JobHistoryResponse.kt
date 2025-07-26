package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant

@Serializable
data class JobHistoryResponse(
    val jobId: Long,
    val prepareExecuteTime: XhuInstant,
    val jobType: String,
    val jobTypeTitle: String,
    val success: Boolean,
    val failed: Boolean,
    val status: String,
    val executeTime: Long,
    val message: String,
    val ext: Map<String, String>,
)
