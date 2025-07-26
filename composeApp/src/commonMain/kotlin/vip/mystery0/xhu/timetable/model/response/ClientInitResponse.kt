package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDate

@Serializable
data class ClientInitResponse(
    val xhuStartTime: XhuStartTime,
    val splash: List<Splash>,
    val latestVersion: ClientVersion?,
    //节假日信息
    val holiday: Holiday,
    //明日节假日信息
    val tomorrowHoliday: Holiday,
)

@Serializable
data class XhuStartTime(
    val startDate: XhuLocalDate,
    val nowYear: Int,
    val nowTerm: Int,
)

@Serializable
data class ClientVersion(
    val versionId: Long,
    val apkSize: Long,
    val patchSize: Long,
    val updateLog: String,
    val versionName: String,
    val versionCode: Long,
    val lastVersionCode: Long,
    val showPatch: Boolean,
    val forceUpdate: Boolean,
    val publishTime: XhuInstant,
) {
    val checkMd5: Boolean
        get() = lastVersionCode != 0L

    companion object {
        val EMPTY = ClientVersion(
            versionId = 0,
            apkSize = 0,
            patchSize = 0,
            updateLog = "",
            versionName = "",
            versionCode = 0,
            lastVersionCode = 0,
            showPatch = false,
            forceUpdate = false,
            publishTime = XhuInstant.fromEpochMilliseconds(0),
        )
    }
}

@Serializable
data class Holiday(
    val date: XhuLocalDate,
    val isOffDay: Boolean,
    val name: String,
)