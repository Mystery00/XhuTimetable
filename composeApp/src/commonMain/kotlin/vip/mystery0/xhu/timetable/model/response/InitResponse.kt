package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant


@Serializable
data class VersionUrl(
    val versionId: Long,
    val apkUrl: String,
    val apkMd5: String,
    val patchUrl: String,
    val patchMd5: String,
)

@Serializable
data class Splash(
    val splashId: Long,
    val imageUrl: String,
    var backgroundColor: String,
    val locationUrl: String,
    val showTime: Int,
    val startShowTime: XhuInstant,
    val endShowTime: XhuInstant,
)
