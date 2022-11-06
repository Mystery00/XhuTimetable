package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.model.response.ExpScoreResponse
import vip.mystery0.xhu.timetable.repository.getExpScoreList
import java.time.LocalDateTime
import java.time.Month

class ExpScoreViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "ExpScoreViewModel"
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect
    private val _yearSelect = MutableStateFlow<List<YearSelect>>(emptyList())
    val yearSelect: StateFlow<List<YearSelect>> = _yearSelect
    private val _termSelect = MutableStateFlow<List<TermSelect>>(emptyList())
    val termSelect: StateFlow<List<TermSelect>> = _termSelect

    private val _expScoreListState = MutableStateFlow(ExpScoreListState())
    val expScoreListState: StateFlow<ExpScoreListState> = _expScoreListState

    init {
        viewModelScope.launch {
            val loggedUserList = SessionManager.loggedUserList()
            _userSelect.value = runOnCpu {
                loggedUserList.map {
                    UserSelect(it.studentId, it.info.userName, it.main)
                }
            }
            _yearSelect.value = buildYearSelect(getConfig { currentYear })
            _termSelect.value = buildTermSelect(getConfig { currentTerm })
        }
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

    fun loadScoreList() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load score list failed", throwable)
            _expScoreListState.value =
                ExpScoreListState(
                    errorMessage = throwable.message ?: throwable.javaClass.simpleName
                )
        }) {
            _expScoreListState.value = ExpScoreListState(loading = true)
            val selected = runOnCpu { _userSelect.value.first { it.selected }.studentId }
            val selectUser =
                SessionManager.user(selected)
            val year = runOnCpu { yearSelect.value.first { it.selected }.year }
            val term = runOnCpu { termSelect.value.first { it.selected }.term }
            val response = getExpScoreList(selectUser, year, term)
            _expScoreListState.value =
                ExpScoreListState(scoreList = response)
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

data class ExpScoreListState(
    val loading: Boolean = false,
    val scoreList: List<ExpScoreResponse> = emptyList(),
    val errorMessage: String = "",
)