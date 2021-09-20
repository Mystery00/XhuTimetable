package vip.mystery0.xhu.timetable.model.response

data class InitResponse(
    val version: Version?,
    val splash: List<Splash>,
    val startTime: Long,
)

data class Version(
    val versionId: Long,
    val apkSize: String,
    val apkMd5: String,
    val patchSize: String,
    val patchMd5: String,
    val updateLog: String,
    val versionCode: Long,
    val versionName: String,
    val lastVersionCode: Long,
    val forceUpdate: Boolean,
    val beta: Boolean,
    val publishTime: Long,
)

data class Splash(
    val splashId: Long,
    val imageUrl: String,
    val locationUrl: String,
    val showTime: Int,
    val startShowTime: Long,
    val endShowTime: Long,
)
