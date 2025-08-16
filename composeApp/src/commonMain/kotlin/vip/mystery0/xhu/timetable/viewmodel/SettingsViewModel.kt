package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.shareFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import kotlinx.datetime.format
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.fileLogWriter
import vip.mystery0.xhu.timetable.model.response.Splash
import vip.mystery0.xhu.timetable.model.response.TeamMemberResponse
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.JobRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.ui.theme.NightMode
import vip.mystery0.xhu.timetable.ui.theme.Theme
import vip.mystery0.xhu.timetable.utils.chinaDateTime
import kotlin.time.Clock


class SettingsViewModel : ComposeViewModel() {
    companion object {
        private var hits = Array(5) { 0L }
    }

    val nightMode: StateFlow<NightMode> = Theme.nightMode

    private val _teamMemberData = MutableStateFlow<List<TeamMemberResponse>>(emptyList())
    val teamMemberData: StateFlow<List<TeamMemberResponse>> = _teamMemberData

    val debugMode = MutableStateFlow(false)

    private val _splashList = MutableStateFlow<List<Splash>>(emptyList())
    val splashList: StateFlow<List<Splash>> = _splashList

    private val _featurePullTimeList = MutableStateFlow<List<String>>(emptyList())
    val featurePullTimeList: StateFlow<List<String>> = _featurePullTimeList

    fun init() {
        viewModelScope.safeLaunch {
            Theme.nightMode.value = getConfigStore { nightMode }
            debugMode.value = getConfigStore { debugMode }
            _splashList.value = getCacheStore { splashList }
            _teamMemberData.value = StartRepo.loadTeamMemberList()
            _featurePullTimeList.value = getCacheStore { featurePullLastExecuteTime }
                .map { it.format(chinaDateTime) }
        }
    }

    fun updateNightMode(nightMode: NightMode) {
        viewModelScope.safeLaunch {
            setConfigStore { this.nightMode = nightMode }
            Theme.nightMode.value = getConfigStore { nightMode }
        }
    }

    fun enableDebugMode() {
        viewModelScope.safeLaunch {
            setConfigStore { debugMode = true }
            debugMode.value = true
        }
    }

    fun disableDebugMode() {
        viewModelScope.safeLaunch {
            setConfigStore { debugMode = false }
            debugMode.value = false
        }
    }

    fun clickVersion(timeout: Long): Boolean {
        hits = hits.copyInto(hits, 0, 1, hits.size)
        hits[hits.lastIndex] = Clock.System.now().toEpochMilliseconds()
        if (hits[0] >= Clock.System.now().toEpochMilliseconds() - timeout) {
            hits = Array(5) { 0L }
            return true
        }
        return false
    }

    fun resetPoemsToken() {
        viewModelScope.safeLaunch {
            withContext(Dispatchers.IO) {
                PoemsStore.token = null
            }
        }
    }

    fun updateFeaturePullTime() {
        viewModelScope.safeLaunch {
            _featurePullTimeList.value = getCacheStore { featurePullLastExecuteTime }
                .map { it.format(chinaDateTime) }
        }
    }

    fun reportLog() {
        viewModelScope.safeLaunch(onException = {
            Logger.e("generate report log failed", it)
            toastMessage(it.message ?: it.desc())
            true
        }) {
            val file = fileLogWriter.prepareSendFile()
            FileKit.shareFile(file)
        }
    }

    fun testPushChannel(registrationId: String) {
        viewModelScope.safeLaunch {
            JobRepo.testPush(registrationId)
        }
    }
}