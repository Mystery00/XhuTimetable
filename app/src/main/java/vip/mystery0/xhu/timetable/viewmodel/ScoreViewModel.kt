package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.base.YearSelect
import vip.mystery0.xhu.timetable.base.TermSelect
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.repository.ScoreRepo

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
            _userSelect.value = initUserSelect()
            _yearSelect.value = initYearSelect()
            _termSelect.value = initTermSelect()
        }
    }

    fun loadScoreList() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load score list failed", throwable)
            _scoreListState.value =
                ScoreListState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _scoreListState.value = ScoreListState(loading = true)
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                Log.w(TAG, "loadScoreList: empty selected user")
                _scoreListState.value = ScoreListState(errorMessage = "选择用户为空，请重新选择")
                return@launch
            }
            val scoreList = ScoreRepo.fetchScoreList(selectedUser)
            _scoreListState.value = ScoreListState(scoreList = scoreList)
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            _userSelect.value = setSelectedUser(_userSelect.value, studentId).first
        }
    }

    fun selectYear(year: Int) {
        viewModelScope.launch {
            _yearSelect.value = setSelectedYear(_yearSelect.value, year)
        }
    }

    fun selectTerm(term: Int) {
        viewModelScope.launch {
            _termSelect.value = setSelectedTerm(_termSelect.value, term)
        }
    }
}

data class ScoreListState(
    val loading: Boolean = false,
    val scoreList: List<ScoreResponse> = emptyList(),
    val errorMessage: String = "",
)