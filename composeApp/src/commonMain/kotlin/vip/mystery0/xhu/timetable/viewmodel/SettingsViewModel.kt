package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.response.Splash
import vip.mystery0.xhu.timetable.model.response.TeamMemberResponse
import vip.mystery0.xhu.timetable.repository.JobRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.ui.theme.NightMode
import vip.mystery0.xhu.timetable.ui.theme.Theme
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

    fun init() {
        viewModelScope.launch {
            Theme.nightMode.value = getConfigStore { nightMode }
            debugMode.value = getConfigStore { debugMode }
            _splashList.value = getCacheStore { splashList }
            _teamMemberData.value = StartRepo.loadTeamMemberList()
        }
    }

    fun updateNightMode(nightMode: NightMode) {
        viewModelScope.launch {
            setConfigStore { this.nightMode = nightMode }
            Theme.nightMode.value = getConfigStore { nightMode }
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
        hits = hits.copyInto(hits, 0, 1, hits.size)
        hits[hits.lastIndex] = Clock.System.now().toEpochMilliseconds()
        if (hits[0] >= Clock.System.now().toEpochMilliseconds() - timeout) {
            hits = Array(5) { 0L }
            return true
        }
        return false
    }

    fun resetPoemsToken() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                PoemsStore.token = null
            }
        }
    }

    fun testPushChannel(registrationId: String) {
        viewModelScope.launch {
            JobRepo.testPush(registrationId)
        }
    }
}