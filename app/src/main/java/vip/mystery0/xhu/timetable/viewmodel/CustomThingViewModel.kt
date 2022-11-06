package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.module.remoteRepo
import vip.mystery0.xhu.timetable.repository.CustomThingRepo
import java.time.LocalDateTime
import java.time.Month

class CustomThingViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "CustomThingViewModel"
    }

    private val customThingRemoteRepo: CustomThingRepo by remoteRepo()

    var changeCustomThing = false

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

    private val _init = MutableStateFlow(false)
    val init: StateFlow<Boolean> = _init

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect
    private val _yearSelect = MutableStateFlow<List<YearSelect>>(emptyList())
    val yearSelect: StateFlow<List<YearSelect>> = _yearSelect
    private val _termSelect = MutableStateFlow<List<TermSelect>>(emptyList())
    val termSelect: StateFlow<List<TermSelect>> = _termSelect

    private lateinit var currentUser: User
    private lateinit var currentYear: String
    private var currentTerm: Int = 1

    private val _customThingListState = MutableStateFlow(CustomThingListState())
    val customThingListState: StateFlow<CustomThingListState> = _customThingListState

    private val _saveCustomThingState = MutableStateFlow(SaveCustomThingState())
    val saveCustomThingState: StateFlow<SaveCustomThingState> = _saveCustomThingState

    init {
        viewModelScope.launch {
            val loggedUserList = SessionManager.loggedUserList()
            _userSelect.value = runOnCpu {
                loggedUserList.map {
                    UserSelect(it.studentId, it.info.userName, it.main)
                }
            }
            currentUser = loggedUserList.find { it.main }!!
            currentYear = getConfig { currentYear }
            currentTerm = getConfig { currentTerm }

            _yearSelect.value = buildYearSelect(currentYear)
            _termSelect.value = buildTermSelect(currentTerm)
            _init.value = true
        }
    }

    private fun toastMessage(message: String) {
        _errorMessage.value = System.currentTimeMillis() to message
    }

    private suspend fun buildYearSelect(selectedYear: String): List<YearSelect> = runOnCpu {
        val loggedUserList = SessionManager.loggedUserList()
        val startYear = loggedUserList.minByOrNull { it.info.grade }!!.info.grade.toInt()
        val time = LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone)
        val endYear = if (time.month < Month.JUNE) time.year - 1 else time.year
        (startYear..endYear).map {
            val year = "${it}-${it + 1}"
            YearSelect(year, selectedYear == year)
        }
    }

    private suspend fun buildTermSelect(selectedTerm: Int): List<TermSelect> = runOnCpu {
        Array(2) {
            val term = it + 1
            TermSelect(term, term == selectedTerm)
        }.toList()
    }

    fun loadCustomThingList() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load custom thing list failed", throwable)
            _customThingListState.value = CustomThingListState()
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _customThingListState.value = CustomThingListState(loading = true)
            val selected = runOnCpu { _userSelect.value.first { it.selected }.studentId }
            val selectUser = SessionManager.user(selected)
            val year = runOnCpu { yearSelect.value.first { it.selected }.year }
            val term = runOnCpu { termSelect.value.first { it.selected }.term }

            currentUser = selectUser
            currentYear = year
            currentTerm = term

            val response =
                customThingRemoteRepo.getCustomThingList(currentUser, currentYear, currentTerm)
            _customThingListState.value = CustomThingListState(
                customThingList = response,
                loading = false
            )
        }
    }

    fun saveCustomThing(
        thingId: Long,
        title: String,
        location: String,
        allDay: Boolean,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        remark: String,
        color: Color,
        map: Map<String, String>,
    ) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "save custom thing failed", throwable)
            _saveCustomThingState.value =
                SaveCustomThingState()
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _saveCustomThingState.value = SaveCustomThingState(loading = true)

            val start = if (allDay) {
                startTime.toLocalDate().atStartOfDay()
            } else {
                startTime
            }
            val end = if (allDay) {
                endTime.toLocalDate().atStartOfDay()
            } else {
                endTime
            }
            val customThing = CustomThing(
                thingId,
                title,
                location,
                allDay,
                start,
                end,
                remark,
                "",
                color,
                CustomThing.extraDataToJson(map),
            )
            if (thingId == 0L) {
                customThingRemoteRepo.createCustomThing(
                    currentUser,
                    currentYear,
                    currentTerm,
                    customThing
                )
            } else {
                customThingRemoteRepo.updateCustomThing(
                    currentUser,
                    currentYear,
                    currentTerm,
                    customThing
                )
            }
            _saveCustomThingState.value = SaveCustomThingState()
            toastMessage("《$title》保存成功")
            changeCustomThing = true
            loadCustomThingList()
        }
    }

    fun delete(thingId: Long) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "delete custom thing failed", throwable)
            _saveCustomThingState.value =
                SaveCustomThingState()
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _saveCustomThingState.value = SaveCustomThingState(loading = true)

            customThingRemoteRepo.deleteCustomThing(
                currentUser,
                currentYear,
                currentTerm,
                thingId
            )
            _saveCustomThingState.value = SaveCustomThingState()
            toastMessage("删除成功")
            changeCustomThing = true
            loadCustomThingList()
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            if (_userSelect.value.first { it.selected }.studentId == studentId) {
                return@launch
            }
            _userSelect.value = runOnCpu {
                SessionManager.loggedUserList().map {
                    UserSelect(it.studentId, it.info.userName, it.studentId == studentId)
                }
            }
        }
    }

    fun selectYear(year: String) {
        viewModelScope.launch {
            if (_yearSelect.value.first { it.selected }.year == year) {
                return@launch
            }
            _yearSelect.value = buildYearSelect(year)
        }
    }

    fun selectTerm(term: Int) {
        viewModelScope.launch {
            if (_termSelect.value.first { it.selected }.term == term) {
                return@launch
            }
            _termSelect.value = buildTermSelect(term)
        }
    }
}

data class CustomThingListState(
    val loading: Boolean = false,
    val customThingList: List<CustomThing> = emptyList(),
)

data class SaveCustomThingState(
    val loading: Boolean = false,
)