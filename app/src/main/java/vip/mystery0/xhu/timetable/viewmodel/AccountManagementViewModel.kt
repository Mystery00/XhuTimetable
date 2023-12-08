package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.event.EventType

class AccountManagementViewModel : ComposeViewModel() {
    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    private val _loggedUserList = MutableStateFlow<List<UserItem>>(emptyList())
    val loggedUserList: StateFlow<List<UserItem>> = _loggedUserList

    init {
        loadLoggedUserList()
    }

    fun loadLoggedUserList() {
        viewModelScope.launch {
            val mainUserId = UserStore.getMainUserId()
            val userList = UserStore.loggedUserList()
            _loggedUserList.value =
                userList.map {
                    UserItem(
                        it.studentId,
                        it.info.name,
                        it.info.gender,
                        it.studentId == mainUserId,
                    )
                }
        }
    }

    fun changeMainUser(studentId: String) {
        viewModelScope.launch {
            UserStore.setMainUser(studentId)
            EventBus.post(EventType.CHANGE_MAIN_USER)
            loadLoggedUserList()
        }
    }

    fun logoutUser(studentId: String) {
        viewModelScope.launch {
            val result = UserStore.logout(studentId)
            if (result) {
                EventBus.post(EventType.MAIN_USER_LOGOUT)
            }
            loadLoggedUserList()
        }
    }

    fun changeMultiAccountMode(enable: Boolean) {
        viewModelScope.launch {
            val multiAccountMode = getConfigStore { multiAccountMode }
            if (multiAccountMode == enable) {
                return@launch
            }
            EventBus.post(EventType.MULTI_MODE_CHANGED)
        }
    }
}

data class UserItem(
    //学号
    val studentId: String,
    //用户姓名
    val userName: String,
    //性别
    val gender: Gender,
    //是否为主用户
    var main: Boolean,
)