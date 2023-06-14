package vip.mystery0.xhu.timetable.model.response

import java.time.Instant
import java.time.LocalDate

data class ClientInitResponse(
    val xhuStartTime: XhuStartTime,
    val splash: List<Splash>,
    val latestVersion: ClientVersion?,
    //节假日信息
    val holiday: Holiday,
    //明日节假日信息
    val tomorrowHoliday: Holiday,
)

data class XhuStartTime(
    val startDate: LocalDate,
    val nowYear: Int,
    val nowTerm: Int,
)

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
    val publishTime: Instant,
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
            publishTime = Instant.EPOCH,
        )
    }
}

data class Holiday(
    val date: LocalDate,
    val isOffDay: Boolean,
    val name: String,
)