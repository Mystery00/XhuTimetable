package vip.mystery0.xhu.timetable.model.response

import java.time.Instant

data class VersionUrl(
    val versionId: Long,
    val apkUrl: String,
    val apkMd5: String,
    val patchUrl: String,
    val patchMd5: String,
)

data class Splash(
    val splashId: Long,
    val imageUrl: String,
    var backgroundColor: String,
    val locationUrl: String,
    val showTime: Int,
    val startShowTime: Instant,
    val endShowTime: Instant,
)
