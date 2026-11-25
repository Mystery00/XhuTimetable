package vip.mystery0.xhu.timetable.repository

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.UserApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.request.LoginRequest
import vip.mystery0.xhu.timetable.model.request.SetCampusRequest
import vip.mystery0.xhu.timetable.model.response.LoginResponse
import vip.mystery0.xhu.timetable.utils.encryptLoginPassword

object UserRepo : BaseDataRepo {
    private val userApi: UserApi by inject()

    suspend fun doLogin(user: User): LoginResponse =
        doLogin(user.studentId, user.password)

    suspend fun doLogin(username: String, password: String): LoginResponse {
        Logger.d("doLogin: $username")
        val resp = withContext(Dispatchers.IO) { userApi.publicKey() }
        val (encryptPassword, clientPublicKey) = withContext(Dispatchers.Default) {
            encryptLoginPassword(password, resp.publicKey, resp.nonce)
        }
        val loginRequest = LoginRequest(username, encryptPassword, resp.publicKey, clientPublicKey)
        return withContext(Dispatchers.IO) {
            userApi.login(loginRequest)
        }
    }

    suspend fun getUserInfo(token: String) = withContext(Dispatchers.IO) {
        userApi.getUserInfo(token)
    }

    suspend fun reloadUserInfo(token: String) = withContext(Dispatchers.IO) {
        Logger.d("reload user info")
        userApi.reloadUserInfo(token)
    }

    suspend fun getCampusList(token: String) = withContext(Dispatchers.IO) {
        userApi.getCampusList(token)
    }

    suspend fun updateUserCampus(token: String, campus: String) = withContext(Dispatchers.IO) {
        userApi.updateUserCampus(token, SetCampusRequest(campus))
    }
}