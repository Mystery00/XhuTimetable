package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.setConfigStore
import vip.mystery0.xhu.timetable.model.CustomAccountTitle
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.repository.UserRepo

class AccountManagementViewModel : ComposeViewModel() {
    private val _loggedUserList = MutableStateFlow<List<UserItem>>(emptyList())
    val loggedUserList: StateFlow<List<UserItem>> = _loggedUserList

    private val _customAccountTitle = MutableStateFlow(GlobalConfigStore.customAccountTitle)
    val customAccountTitle: StateFlow<CustomAccountTitle> = _customAccountTitle

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
                        it.info.xhuGrade,
                        it.info.college,
                        it.info.majorName,
                        it.info.majorDirection,
                    )
                }.sortedBy { it.studentId }
        }
    }

    fun changeMainUser(studentId: String) {
        viewModelScope.launch {
            UserStore.setMainUser(studentId)
            EventBus.post(EventType.CHANGE_MAIN_USER)
            loadLoggedUserList()
        }
    }

    fun reloadUserInfo(studentId: String) {
        viewModelScope.launch {
            var loginUser = UserStore.getUserByStudentId(studentId)!!
            val userInfo = UserRepo.reloadUserInfo(loginUser.token)
            //再获取一次数据，避免更新用户信息的请求时token变了
            loginUser = UserStore.getUserByStudentId(studentId)!!
            val user = loginUser.copy(info = userInfo)
            UserStore.updateUser(user)
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

    fun updateAccountTitleTemplate(customAccountTitle: CustomAccountTitle) {
        viewModelScope.launch {
            setConfigStore { this.customAccountTitle = customAccountTitle }
            _customAccountTitle.value = customAccountTitle
            EventBus.post(EventType.CHANGE_CUSTOM_ACCOUNT_TITLE)
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
    var xhuGrade: Int,
    var college: String,
    var majorName: String,
    var majorDirection: String,
)