package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.WorkManager
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.startUniqueWork
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.work.DownloadApkWork
import vip.mystery0.xhu.timetable.work.DownloadPatchWork

actual class CheckUpdateVewModel : ComposeViewModel() {
    private val workManager: WorkManager by inject()

    fun downloadApk(newVersion: ClientVersion) {
        viewModelScope.safeLaunch {
            workManager.startUniqueWork<DownloadApkWork>(
                Data.Builder()
                    .putString(DownloadApkWork.ARG_VERSION_ID, newVersion.versionId.toString())
                    .putString(DownloadApkWork.ARG_VERSION_NAME, newVersion.versionName)
                    .putString(DownloadApkWork.ARG_VERSION_CODE, newVersion.versionCode.toString())
                    .putString(
                        DownloadApkWork.ARG_VERSION_CHECK_MD5, newVersion.checkMd5.toString()
                    ).build()
            )
        }
    }

    fun downloadPatch(newVersion: ClientVersion) {
        viewModelScope.safeLaunch {
            workManager.startUniqueWork<DownloadPatchWork>(
                Data.Builder()
                    .putString(DownloadPatchWork.ARG_VERSION_ID, newVersion.versionId.toString())
                    .putString(DownloadPatchWork.ARG_VERSION_NAME, newVersion.versionName)
                    .putString(
                        DownloadPatchWork.ARG_VERSION_CODE, newVersion.versionCode.toString()
                    ).build()
            )
        }
    }

    fun ignoreVersion(version: ClientVersion) {
        viewModelScope.safeLaunch {
            val ignore = getCacheStore { ignoreVersionList }
            val list = ignore + "${version.versionName}-${version.versionCode}"
            setCacheStore { ignoreVersionList = list }
        }
    }
}