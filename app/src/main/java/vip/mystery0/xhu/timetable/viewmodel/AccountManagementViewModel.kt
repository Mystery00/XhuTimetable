package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.Config
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent

class AccountManagementViewModel : ComposeViewModel(), KoinComponent {
    private val eventBus: EventBus by inject()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _loggedUserList = MutableStateFlow<List<UserItem>>(emptyList())
    val loggedUserList: StateFlow<List<UserItem>> = _loggedUserList

    init {
        loadLoggedUserList()
    }

    fun loadLoggedUserList() {
        viewModelScope.launch {
            _loading.value = true
            val loggedList = SessionManager.loggedUserList()
            _loggedUserList.value =
                loggedList.map { UserItem(it.studentId, it.info.userName, it.info.sex, it.main) }
            _loading.value = false
        }
    }

    fun changeMainUser(studentId: String) {
        viewModelScope.launch {
            SessionManager.changeMainUser(studentId)
            eventBus.post(UIEvent(EventType.CHANGE_MAIN_USER))
            loadLoggedUserList()
        }
    }

    fun logoutUser(studentId: String) {
        viewModelScope.launch {
            val result = SessionManager.logout(studentId)
            if (result) {
                eventBus.post(UIEvent(EventType.MAIN_USER_LOGOUT))
            }
            loadLoggedUserList()
        }
    }

    fun changeMultiAccountMode(enable: Boolean) {
        viewModelScope.launch {
            if (Config.multiAccountMode == enable) {
                return@launch
            }
            Config.multiAccountMode = enable
            eventBus.post(UIEvent(EventType.CHANGE_MAIN_USER))
        }
    }
}

data class UserItem(
    //学号
    val studentId: String,
    //用户姓名
    val userName: String,
    //性别
    val sex: String,
    //是否为主用户
    var main: Boolean,
)