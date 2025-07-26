package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.TermSelectDataLoader
import vip.mystery0.xhu.timetable.base.UserSelectDataLoader
import vip.mystery0.xhu.timetable.base.YearSelectDataLoader
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreResponse
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.ScoreRepo

class ExpScoreViewModel : ComposeViewModel(), KoinComponent {
    val userSelect = UserSelectDataLoader()
    val yearSelect = YearSelectDataLoader()
    val termSelect = TermSelectDataLoader()

    private val _expScoreListState = MutableStateFlow(ExpScoreListState())
    val expScoreListState: StateFlow<ExpScoreListState> = _expScoreListState

    fun init() {
        viewModelScope.launch {
            userSelect.init()
            yearSelect.init()
            termSelect.init()
            loadExpScoreList()
        }
    }

    fun loadExpScoreList() {
        fun failed(message: String) {
            logger.w("load exp score list failed, $message")
            _expScoreListState.value = ExpScoreListState(errorMessage = message)
        }

        viewModelScope.launch(networkErrorHandler { throwable ->
            logger.w("load exp score list failed", throwable)
            failed(throwable.message ?: throwable.desc())
        }) {
            _expScoreListState.value = ExpScoreListState(loading = true)
            val selectedUser = userSelect.getSelectedUser()
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            val year = yearSelect.getSelectedYear()
            val term = termSelect.getSelectedTerm()
            val scoreList = ScoreRepo.fetchExpScoreList(selectedUser, year, term)
            _expScoreListState.value =
                ExpScoreListState(scoreList = scoreList, errorMessage = "数据加载完成！")
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            userSelect.setSelected(studentId)
        }
    }

    fun selectYear(year: Int) {
        viewModelScope.launch {
            yearSelect.setSelected(year)
        }
    }

    fun selectTerm(term: Int) {
        viewModelScope.launch {
            termSelect.setSelected(term)
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