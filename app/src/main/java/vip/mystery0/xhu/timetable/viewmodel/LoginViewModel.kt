package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.*
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.repository.doLogin

class LoginViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val eventBus: EventBus by inject()

    private val serverApi: ServerApi by inject()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    fun login(
        username: String,
        password: String
    ) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "login failed", throwable)
            _loginState.value =
                LoginState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _loginState.value = LoginState(loading = true)
            runOnCpu {
                if (SessionManager.getUser(username) != null) {
                    //账号已登录，不允许二次登陆
                    throw CoroutineStopException("该用户已登录！")
                }
            }
            val loginResponse = doLogin(username, password)
            val userInfo = serverApi.userInfo(loginResponse.token)
            SessionManager.login(username, password, loginResponse.token, userInfo)
            if (SessionManager.mainUser().studentId == username) {
                //刚刚登录的账号是主账号，说明是异常情况下登录
                eventBus.post(UIEvent(EventType.CHANGE_MAIN_USER))
            }
            _loginState.value = LoginState(loading = false, success = true)
        }
    }

    private val _updateDialogState = MutableStateFlow(false)
    val updateDialogState: StateFlow<Boolean> = _updateDialogState

    init {
        val newVersionCode = DataHolder.version?.versionCode ?: 0L
//            _updateDialogState.value = newVersionCode <= appVersionCodeNumber
        _updateDialogState.value = true
    }

    fun closeUpdateDialog() {
        _updateDialogState.value = false
    }
}

data class LoginState(
    val loading: Boolean = false,
    val success: Boolean = false,
    val errorMessage: String = "",
)