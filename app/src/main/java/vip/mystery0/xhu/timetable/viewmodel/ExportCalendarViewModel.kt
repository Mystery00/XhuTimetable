package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.CalendarAccount
import vip.mystery0.xhu.timetable.repository.CalendarRepo

class ExportCalendarViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "ExportCalendarViewModel"
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect

    private val _calendarAccountListState = MutableStateFlow(CalendarAccountListState())
    val calendarAccountListState: StateFlow<CalendarAccountListState> = _calendarAccountListState

    private val _actionState = MutableStateFlow(ActionState())
    val actionState: StateFlow<ActionState> = _actionState

    init {
        viewModelScope.launch {
            _userSelect.value = initUserSelect()
        }
    }

    fun loadCalendarAccountList() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "load calendar account list failed", throwable)
            _calendarAccountListState.value = CalendarAccountListState(
                errorMessage = throwable.message ?: throwable.javaClass.simpleName
            )
        }) {
            _calendarAccountListState.value = CalendarAccountListState(loading = true)
            val accountList = withContext(Dispatchers.IO) {
                CalendarRepo.getAllCalendarAccount()
            }
            _calendarAccountListState.value = CalendarAccountListState(list = accountList)
        }
    }

    fun exportCalendar(
        includeCustomCourse: Boolean,
        includeCustomThing: Boolean,
        reminderList: List<Int>,
    ) {
        fun failed(message: String) {
            Log.w(TAG, "export calendar failed, $message")
            _actionState.value = ActionState(errorMessage = message, actionSuccess = false)
        }
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "export calendar failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _actionState.value = ActionState(loading = true, actionSuccess = false)
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            val year = getConfigStore { nowYear }
            val term = getConfigStore { nowTerm }
            val eventList = CalendarRepo.getEventList(
                selectedUser,
                year,
                term,
                includeCustomCourse,
                includeCustomThing,
            )
            val account = CalendarAccount.byAccount(selectedUser.studentId, selectedUser.info.name)
            val accountId =
                CalendarRepo.getCalendarIdByAccountName(account.generateAccountName())
            if (accountId != null) {
                CalendarRepo.deleteAllEvent(accountId)
            }
            withContext(Dispatchers.IO) {
                eventList.forEach { event ->
                    event.reminder.addAll(reminderList)
                    CalendarRepo.addEvent(account, event)
                }
            }
            _actionState.value = ActionState(actionSuccess = true)
            loadCalendarAccountList()
        }
    }

    fun deleteCalendarAccount(accountId: Long) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "delete calendar account failed", throwable)
            _actionState.value = ActionState(
                errorMessage = throwable.message ?: throwable.javaClass.simpleName,
                actionSuccess = false,
            )
        }) {
            _actionState.value = ActionState(loading = true, actionSuccess = false)
            CalendarRepo.deleteCalendarAccount(accountId)
            _actionState.value = ActionState(actionSuccess = true)
            loadCalendarAccountList()
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            val result = setSelectedUser(_userSelect.value, studentId)
            if (!result.second) {
                return@launch
            }
            _userSelect.value = result.first
        }
    }
}

data class CalendarAccountListState(
    val loading: Boolean = false,
    val list: List<CalendarAccount> = emptyList(),
    val errorMessage: String = "",
)

data class ActionState(
    val loading: Boolean = false,
    val errorMessage: String = "",
    val actionSuccess: Boolean = true,
)