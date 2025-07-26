package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalTime
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.startUniqueWork
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.entity.VersionChannel
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.work.DownloadApkWork
import vip.mystery0.xhu.timetable.work.DownloadPatchWork
import vip.mystery0.xhu.timetable.work.NotifySetter

actual class PlatformSettingsViewModel : ComposeViewModel() {
    private val workManager: WorkManager by inject()

    private val _notifyTimeData = MutableStateFlow<LocalTime?>(null)
    val notifyTimeData: StateFlow<LocalTime?> = _notifyTimeData

    private val _versionChannel = MutableStateFlow(VersionChannel.STABLE)
    val versionChannel: StateFlow<VersionChannel> = _versionChannel

    val version = MutableStateFlow<ClientVersion?>(null)

    fun init() {
        viewModelScope.launch {
            _notifyTimeData.value = getConfigStore { notifyTime }
            _versionChannel.value = getConfigStore { versionChannel }
            StartRepo.version.collectLatest {
                if (it == ClientVersion.EMPTY) {
                    version.value = null
                } else {
                    version.value = it
                }
            }
        }
    }

    fun updateNotifyTime(time: LocalTime?) {
        viewModelScope.launch {
            setConfigStore { notifyTime = time }
            _notifyTimeData.value = getConfigStore { notifyTime }
            NotifySetter.setTrigger()
        }
    }

    fun updateVersionChannel(versionChannel: VersionChannel) {
        viewModelScope.launch {
            setConfigStore { this.versionChannel = versionChannel }
            _versionChannel.value = getConfigStore { versionChannel }
        }
    }

    fun checkUpdate(forceBeta: Boolean) {
        viewModelScope.launch {
            StartRepo.checkVersion(forceBeta)
        }
    }

    fun downloadApk() {
        viewModelScope.launch {
            val newVersion = version.value ?: return@launch
            workManager.startUniqueWork<DownloadApkWork>(
                Data.Builder()
                    .putString(DownloadApkWork.ARG_VERSION_ID, newVersion.versionId.toString())
                    .putString(DownloadApkWork.ARG_VERSION_NAME, newVersion.versionName)
                    .putString(DownloadApkWork.ARG_VERSION_CODE, newVersion.versionCode.toString())
                    .putString(
                        DownloadApkWork.ARG_VERSION_CHECK_MD5,
                        newVersion.checkMd5.toString()
                    )
                    .build()
            )
        }
    }

    fun downloadPatch() {
        viewModelScope.launch {
            val newVersion = version.value ?: return@launch
            workManager.startUniqueWork<DownloadPatchWork>(
                Data.Builder()
                    .putString(DownloadPatchWork.ARG_VERSION_ID, newVersion.versionId.toString())
                    .putString(DownloadPatchWork.ARG_VERSION_NAME, newVersion.versionName)
                    .putString(
                        DownloadPatchWork.ARG_VERSION_CODE,
                        newVersion.versionCode.toString()
                    )
                    .build()
            )
        }
    }
}