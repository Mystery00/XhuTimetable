package vip.mystery0.xhu.timetable.viewmodel

import android.app.AlarmManager
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CommonApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.startUniqueWork
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.contentResolver
import vip.mystery0.xhu.timetable.externalDocumentsDir
import vip.mystery0.xhu.timetable.model.entity.NightMode
import vip.mystery0.xhu.timetable.model.entity.VersionChannel
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.model.response.Splash
import vip.mystery0.xhu.timetable.model.response.TeamMemberResponse
import vip.mystery0.xhu.timetable.setAlarmTrigger
import vip.mystery0.xhu.timetable.ui.theme.Theme
import vip.mystery0.xhu.timetable.work.DownloadApkWork
import vip.mystery0.xhu.timetable.work.DownloadPatchWork
import java.io.File
import java.io.FileOutputStream
import java.time.LocalTime

class SettingsViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "SettingsViewModel"
        private var hits = Array(5) { 0L }
    }

    private val workManager: WorkManager by inject()
    private val alarmManager: AlarmManager by inject()
    private val commonApi: CommonApi by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    val nightMode: StateFlow<NightMode> = Theme.nightMode

    private val _notifyTimeData = MutableStateFlow<LocalTime?>(null)
    val notifyTimeData: StateFlow<LocalTime?> = _notifyTimeData

    private val _teamMemberData = MutableStateFlow<List<TeamMemberResponse>>(emptyList())
    val teamMemberData: StateFlow<List<TeamMemberResponse>> = _teamMemberData

    val debugMode = MutableStateFlow(false)

    private val _splashList = MutableStateFlow<List<Splash>>(emptyList())
    val splashList: StateFlow<List<Splash>> = _splashList

    private val _versionChannel = MutableStateFlow(VersionChannel.STABLE)
    val versionChannel: StateFlow<VersionChannel> = _versionChannel

    val version = MutableStateFlow<ClientVersion?>(null)

    init {
        viewModelScope.launch {
            Theme.nightMode.value = getConfigStore { nightMode }
            _notifyTimeData.value = getConfigStore { notifyTime }
            debugMode.value = getConfigStore { debugMode }
            _splashList.value = getCacheStore { splashList }
            _versionChannel.value = getConfigStore { versionChannel }
            try {
                _teamMemberData.value = commonApi.getTeamMemberList()
            } catch (e: Exception) {
                Log.w(TAG, "load team member list", e)
            }
        }
    }

    fun updateNightMode(nightMode: NightMode) {
        viewModelScope.launch {
            setConfigStore { this.nightMode = nightMode }
            Theme.nightMode.value = getConfigStore { nightMode }
        }
    }

    fun updateNotifyTime(time: LocalTime?) {
        viewModelScope.launch {
            setConfigStore { notifyTime = time }
            _notifyTimeData.value = getConfigStore { notifyTime }
            setAlarmTrigger(alarmManager)
//            //取消旧的任务
//            workManager.cancelUniqueWork(NotifyWork::class.java.name)
//            setTrigger(workManager)
        }
    }

    fun updateVersionChannel(versionChannel: VersionChannel) {
        viewModelScope.launch {
            setConfigStore { this.versionChannel = versionChannel }
            _versionChannel.value = getConfigStore { versionChannel }
        }
    }

    fun enableDebugMode() {
        viewModelScope.launch {
            setConfigStore { debugMode = true }
            debugMode.value = true
        }
    }

    fun disableDebugMode() {
        viewModelScope.launch {
            setConfigStore { debugMode = false }
            debugMode.value = false
        }
    }

    fun clickVersion(timeout: Long): Boolean {
        System.arraycopy(hits, 1, hits, 0, hits.lastIndex)
        hits[hits.lastIndex] = SystemClock.uptimeMillis()
        if (hits[0] >= SystemClock.uptimeMillis() - timeout) {
            hits = Array(5) { 0L }
            return true
        }
        return false
    }

    fun downloadApk() {
        viewModelScope.launch {
            workManager.startUniqueWork<DownloadApkWork>()
        }
    }

    fun downloadPatch() {
        viewModelScope.launch {
            workManager.startUniqueWork<DownloadPatchWork>()
        }
    }

    fun setCustomFont(uri: Uri?) {
        viewModelScope.launch {
            if (uri == null) {
                setConfigStore { customFontFile = null }
                return@launch
            }
            val fontFile = File(externalDocumentsDir, "custom.font")
            contentResolver.openInputStream(uri).use { input ->
                FileOutputStream(fontFile).use { output ->
                    input?.copyTo(output)
                }
            }
            setConfigStore { customFontFile = fontFile }
        }
    }

    fun checkUpdate() {
//        viewModelScope.launch {
//            val latestVersion = startRepo.checkVersion()
//            DataHolder.version = latestVersion
//            version.value = latestVersion
//        }
    }

    fun resetPoemsToken() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                PoemsStore.token = null
            }
        }
    }
}