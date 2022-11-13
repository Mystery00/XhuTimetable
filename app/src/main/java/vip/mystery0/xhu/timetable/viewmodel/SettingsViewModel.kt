package vip.mystery0.xhu.timetable.viewmodel

import android.app.AlarmManager
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.startUniqueWork
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.setConfig
import vip.mystery0.xhu.timetable.contentResolver
import vip.mystery0.xhu.timetable.externalDocumentsDir
import vip.mystery0.xhu.timetable.model.entity.NightMode
import vip.mystery0.xhu.timetable.model.entity.VersionChannel
import vip.mystery0.xhu.timetable.model.response.Splash
import vip.mystery0.xhu.timetable.model.response.TeamMemberResponse
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.module.getRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
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
    private val serverApi: ServerApi by inject()

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

    private val _serverUrl = MutableStateFlow("")
    val serverUrl: StateFlow<String> = _serverUrl

    private val _versionChannel = MutableStateFlow(VersionChannel.STABLE)
    val versionChannel: StateFlow<VersionChannel> = _versionChannel

    val version = MutableStateFlow<Version?>(null)
    private val startRepo: StartRepo = getRepo()

    init {
        viewModelScope.launch {
            Theme.nightMode.value = getConfig { nightMode }
            _notifyTimeData.value = getConfig { notifyTime }
            debugMode.value = getConfig { debugMode }
            _splashList.value = getConfig { splashList }
            _serverUrl.value = getConfig { serverUrl }
            _versionChannel.value = getConfig { versionChannel }
            try {
                _teamMemberData.value = serverApi.getTeamMemberList()
            } catch (e: Exception) {
                Log.w(TAG, "load team member list", e)
            }
        }
    }

    fun updateNightMode(nightMode: NightMode) {
        viewModelScope.launch {
            setConfig {
                this.nightMode = nightMode
            }
            Theme.nightMode.value = getConfig { nightMode }
        }
    }

    fun updateNotifyTime(time: LocalTime?) {
        viewModelScope.launch {
            setConfig {
                notifyTime = time
            }
            _notifyTimeData.value = getConfig { notifyTime }
            setAlarmTrigger(alarmManager)
//            //取消旧的任务
//            workManager.cancelUniqueWork(NotifyWork::class.java.name)
//            setTrigger(workManager)
        }
    }

    fun updateServerUrl(url: String) {
        if (url.isBlank()) {
            return
        }
        viewModelScope.launch {
            setConfig {
                serverUrl = url
            }
            _serverUrl.value = getConfig { serverUrl }
        }
    }

    fun updateVersionChannel(versionChannel: VersionChannel) {
        viewModelScope.launch {
            setConfig {
                this.versionChannel = versionChannel
            }
            _versionChannel.value = getConfig { versionChannel }
        }
    }

    fun enableDebugMode() {
        viewModelScope.launch {
            setConfig { debugMode = true }
            debugMode.value = true
        }
    }

    fun disableDebugMode() {
        viewModelScope.launch {
            setConfig { debugMode = false }
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
                setConfig { customFontFile = null }
                return@launch
            }
            val fontFile = File(externalDocumentsDir, "custom.font")
            contentResolver.openInputStream(uri).use { input ->
                FileOutputStream(fontFile).use { output ->
                    input?.copyTo(output)
                }
            }
            setConfig { customFontFile = fontFile }
        }
    }

    fun checkUpdate() {
        viewModelScope.launch {
            val latestVersion = startRepo.checkVersion()
            DataHolder.version = latestVersion
            version.value = latestVersion
        }
    }

    fun resetPoemsToken(){
        viewModelScope.launch {
            setConfig { poemsToken = null }
        }
    }
}