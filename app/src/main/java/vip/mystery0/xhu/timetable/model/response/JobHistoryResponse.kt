package vip.mystery0.xhu.timetable.model.response

import java.time.Instant

data class JobHistoryResponse(
    val jobId: Long,
    val prepareExecuteTime: Instant,
    val jobType: String,
    val jobTypeTitle: String,
    val success: Boolean,
    val failed: Boolean,
    val status: String,
    val executeTime: Long,
    val message: String,
    val ext: Map<String, String>,
)
