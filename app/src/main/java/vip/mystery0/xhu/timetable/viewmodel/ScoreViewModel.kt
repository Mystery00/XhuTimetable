package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.Config
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.model.response.ScoreItem
import vip.mystery0.xhu.timetable.repository.getScoreList
import java.time.LocalDateTime
import java.time.Month

class ScoreViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "ScoreViewModel"
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect
    private val _yearSelect = MutableStateFlow<List<YearSelect>>(emptyList())
    val yearSelect: StateFlow<List<YearSelect>> = _yearSelect
    private val _termSelect = MutableStateFlow<List<TermSelect>>(emptyList())
    val termSelect: StateFlow<List<TermSelect>> = _termSelect

    private val _scoreListState = MutableStateFlow(ScoreListState())
    val scoreListState: StateFlow<ScoreListState> = _scoreListState

    init {
        viewModelScope.launch {
            val loggedUserList = SessionManager.loggedUserList()
            _userSelect.value = loggedUserList.map {
                UserSelect(it.studentId, it.info.userName, it.main)
            }
            _yearSelect.value = buildYearSelect(Config.currentYear)
            _termSelect.value = buildTermSelect(Config.currentTerm)
        }
    }

    private fun buildYearSelect(selectedYear: String): List<YearSelect> {
        val loggedUserList = SessionManager.loggedUserList()
        val startYear = loggedUserList.minByOrNull { it.info.grade }!!.info.grade.toInt()
        val time = LocalDateTime.ofInstant(Config.termStartTime, chinaZone)
        val endYear = if (time.month < Month.JUNE) time.year - 1 else time.year
        return (startYear..endYear).map {
            val year = "${it}-${it + 1}"
            YearSelect(year, selectedYear == year)
        }
    }

    private fun buildTermSelect(selectedTerm: Int): List<TermSelect> = Array(2) {
        val term = it + 1
        TermSelect(term, term == selectedTerm)
    }.toList()

    fun loadScoreList() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load score list failed", throwable)
            _scoreListState.value =
                ScoreListState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _scoreListState.value = ScoreListState(loading = true)
            val selectUser =
                SessionManager.getUser(_userSelect.value.first { it.selected }.studentId)!!
            val year = yearSelect.value.first { it.selected }.year
            val term = termSelect.value.first { it.selected }.term
            val response = getScoreList(selectUser, year, term)
            _scoreListState.value =
                ScoreListState(scoreList = response.list, failedScoreList = response.failedList)
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            if (_userSelect.value.first { it.selected }.studentId == studentId) {
                return@launch
            }
            _userSelect.value = SessionManager.loggedUserList().map {
                UserSelect(it.studentId, it.info.userName, it.studentId == studentId)
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

data class YearSelect(
    val year: String,
    val selected: Boolean,
)

data class TermSelect(
    val term: Int,
    val selected: Boolean,
)

data class ScoreListState(
    val loading: Boolean = false,
    val scoreList: List<ScoreItem> = emptyList(),
    val failedScoreList: List<ScoreItem> = emptyList(),
    val errorMessage: String = "",
)