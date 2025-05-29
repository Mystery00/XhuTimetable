package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.TermSelect
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.base.YearSelect
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreResponse
import vip.mystery0.xhu.timetable.repository.ScoreRepo

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

    fun init() {
        viewModelScope.launch {
            _userSelect.value = initUserSelect()
            _yearSelect.value = initYearSelect()
            _termSelect.value = initTermSelect()
            loadExpScoreList()
        }
    }

    fun loadExpScoreList() {
        fun failed(message: String) {
            Log.w(TAG, "load exp score list failed, $message")
            _expScoreListState.value = ExpScoreListState(errorMessage = message)
        }

        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load exp score list failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _expScoreListState.value = ExpScoreListState(loading = true)
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            val year = getSelectedYear(_yearSelect.value)
            val term = getSelectedTerm(_termSelect.value)
            val scoreList = ScoreRepo.fetchExpScoreList(selectedUser, year, term)
            _expScoreListState.value =
                ExpScoreListState(scoreList = scoreList, errorMessage = "数据加载完成！")
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            val (selectList, _) = setSelectedUser(_userSelect.value, studentId)
            _userSelect.value = selectList
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

    fun clearErrorMessage() {
        _expScoreListState.value = _expScoreListState.value.copy(errorMessage = "")
    }
}

data class ExpScoreListState(
    val loading: Boolean = false,
    val scoreList: List<ExperimentScoreResponse> = emptyList(),
    val errorMessage: String = "",
)