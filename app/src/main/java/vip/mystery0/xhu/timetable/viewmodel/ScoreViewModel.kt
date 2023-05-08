package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.PageRequest
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.TermSelect
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.base.YearSelect
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.model.response.ScoreResponse
import vip.mystery0.xhu.timetable.repository.ScoreRepo

class ScoreViewModel : PagingComposeViewModel<PageRequest, ScoreResponse>(
    {
        ScoreRepo.getScoreListStream(it.user, it.year, it.term)
    }
) {
    companion object {
        private const val TAG = "ScoreViewModel"
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect
    private val _yearSelect = MutableStateFlow<List<YearSelect>>(emptyList())
    val yearSelect: StateFlow<List<YearSelect>> = _yearSelect
    private val _termSelect = MutableStateFlow<List<TermSelect>>(emptyList())
    val termSelect: StateFlow<List<TermSelect>> = _termSelect

    init {
        viewModelScope.launch {
            _userSelect.value = initUserSelect()
            _yearSelect.value = initYearSelect()
            _termSelect.value = initTermSelect()
        }
    }

    fun loadScoreList() {
        fun failed(message: String) {
            Log.w(TAG, "load score list failed, $message")
            toastMessage(message)
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load score list failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            val year = getSelectedYear(_yearSelect.value)
            val term = getSelectedTerm(_termSelect.value)
            pageRequestFlow.emit(PageRequest(selectedUser, year, term))
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