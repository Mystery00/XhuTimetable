package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.FeatureString
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.UserRepo

class LoginViewModel : ComposeViewModel() {
    private val _loginLabel = MutableStateFlow("")
    val loginLabel: StateFlow<String> = _loginLabel

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    fun init() {
        viewModelScope.safeLaunch {
            _loginLabel.value = FeatureString.LOGIN_LABEL.getValue()
        }
    }

    fun login(
        username: String,
        password: String,
    ) {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("login failed", throwable)
            _loginState.value =
                LoginState(errorMessage = throwable.desc())
        }) {
            _loginState.value = LoginState(loading = true)
            withContext(Dispatchers.Default) {
                if (UserStore.getUserByStudentId(username) != null) {
                    //账号已登录，不允许二次登陆
                    throw RuntimeException("该用户已登录！")
                }
            }
            val loginResponse = UserRepo.doLogin(username, password)
            val userInfo = UserRepo.getUserInfo(loginResponse.sessionToken)
            val user = User(
                studentId = username,
                password = password,
                token = loginResponse.sessionToken,
                info = userInfo,
                null,
            )
            UserStore.login(user)
            if (UserStore.mainUser().studentId == username) {
                //刚刚登录的账号是主账号，说明是异常情况下登录
                EventBus.post(EventType.CHANGE_MAIN_USER)
            }
            _loginState.value = LoginState(loading = false, success = true)
        }
    }
}

data class LoginState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String = "",
)