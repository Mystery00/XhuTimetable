package vip.mystery0.xhu.timetable.viewmodel

import android.util.Base64
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.model.request.LoginRequest
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

class LoginViewModel : ComposeViewModel(), KoinComponent {
    private val serverApi: ServerApi by inject()

    private val _loginState = MutableStateFlow(LoginState())
    val loginState: StateFlow<LoginState> = _loginState

    fun login(
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            _loginState.value = LoginState(loading = true)
            val publicKey = serverApi.publicKey().publicKey
            val decodedPublicKey = Base64.decode(publicKey, Base64.DEFAULT)
            val key =
                KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(decodedPublicKey))
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            val encryptPassword =
                Base64.encodeToString(cipher.doFinal(password.toByteArray()), Base64.DEFAULT)
            val loginRequest = LoginRequest(username, encryptPassword, publicKey)
            val loginResponse = serverApi.login(loginRequest)
            Log.i("TAG", "login: ${loginResponse.token}")
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