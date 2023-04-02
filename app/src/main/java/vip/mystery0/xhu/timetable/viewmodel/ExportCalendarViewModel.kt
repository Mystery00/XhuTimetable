package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.CalendarAccount
import vip.mystery0.xhu.timetable.model.CalendarAttender
import vip.mystery0.xhu.timetable.model.CalendarEvent
import vip.mystery0.xhu.timetable.module.HINT_NETWORK
import vip.mystery0.xhu.timetable.repository.CalendarRepo
import java.time.Instant

class ExportCalendarViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "ExportCalendarViewModel"
    }

    private val serverApi: ServerApi by inject()

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect

    private val _calendarAccountListState = MutableStateFlow(CalendarAccountListState())
    val calendarAccountListState: StateFlow<CalendarAccountListState> = _calendarAccountListState

    private val _actionState = MutableStateFlow(ActionState())
    val actionState: StateFlow<ActionState> = _actionState

    init {
        viewModelScope.launch {
            val loggedUserList = SessionManager.loggedUserList()
            _userSelect.value = runOnCpu {
                loggedUserList.map {
                    UserSelect(it.studentId, it.info.name, it.main)
                }
            }
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
            val accountList = runOnIo {
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
        if (!isOnline()) {
            _actionState.value = ActionState(errorMessage = HINT_NETWORK)
            return
        }
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "export calendar failed", throwable)
            _actionState.value = ActionState(
                errorMessage = throwable.message ?: throwable.javaClass.simpleName
            )
        }) {
            _actionState.value = ActionState(loading = true)
            val selected = runOnCpu { _userSelect.value.first { it.selected }.studentId }
            val selectUser = SessionManager.user(selected)
            val year = getConfig { currentYear }
            val term = getConfig { currentTerm }
            val eventList = selectUser.withAutoLogin {
                serverApi.getCalendarEventList(
                    it,
                    year,
                    term,
                    includeCustomCourse,
                    includeCustomThing,
                ).checkLogin()
            }.first
            val account = CalendarAccount.byAccount(selectUser.studentId, selectUser.info.name)
            val accountId =
                CalendarRepo.getCalendarIdByAccountName(account.generateAccountName())
            if (accountId != null) {
                CalendarRepo.deleteAllEvent(accountId)
            }
            runOnIo {
                eventList.forEach { response ->
                    val event = CalendarEvent(
                        response.title,
                        Instant.ofEpochMilli(response.startTime),
                        Instant.ofEpochMilli(response.endTime),
                        response.location,
                        response.description,
                        response.allDay,
                    )
                    event.attenderList.addAll(response.attenders.map { CalendarAttender(it) })
                    event.reminder.addAll(reminderList)
                    CalendarRepo.addEvent(account, event)
                }
            }
            _actionState.value = ActionState()
            loadCalendarAccountList()
        }
    }

    fun deleteCalendarAccount(accountId: Long) {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "delete calendar account failed", throwable)
            _actionState.value = ActionState(
                errorMessage = throwable.message ?: throwable.javaClass.simpleName
            )
        }) {
            _actionState.value = ActionState(loading = true)
            CalendarRepo.deleteCalendarAccount(accountId)
            _actionState.value = ActionState()
            loadCalendarAccountList()
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            if (_userSelect.value.first { it.selected }.studentId == studentId) {
                return@launch
            }
            _userSelect.value = runOnCpu {
                SessionManager.loggedUserList().map {
                    UserSelect(it.studentId, it.info.name, it.studentId == studentId)
                }
            }
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
)