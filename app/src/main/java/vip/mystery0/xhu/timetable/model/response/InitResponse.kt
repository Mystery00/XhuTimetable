package vip.mystery0.xhu.timetable.model.response

import java.time.Instant

data class InitResponse(
    val version: Version?,
    val splash: List<Splash>,
    val startTime: Long,
    val menu: List<Menu>,
)

data class Version(
    val versionId: Long,
    val apkSize: Long,
    val patchSize: Long,
    val updateLog: String,
    val versionCode: Long,
    val versionName: String,
    val lastVersionCode: Long,
    val forceUpdate: Boolean,
    val beta: Boolean,
    val publishTime: Long,
)

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

data class Menu(
    var title: String,
    var key: String,
    var groupSort: Int,
    var hint: String,
    var link: String?,
)
