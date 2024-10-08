package vip.mystery0.xhu.timetable.repository

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import retrofit2.HttpException
import vip.mystery0.xhu.timetable.api.UserApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.parseServerErrorWhenFailed
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.request.LoginRequest
import vip.mystery0.xhu.timetable.model.request.SetCampusRequest
import vip.mystery0.xhu.timetable.model.response.LoginResponse
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

object UserRepo : BaseDataRepo {
    private val userApi: UserApi by inject()

    suspend fun doLogin(user: User): LoginResponse =
        doLogin(user.studentId, user.password)

    suspend fun doLogin(username: String, password: String): LoginResponse {
        val publicKey = withContext(Dispatchers.IO) { userApi.publicKey() }.publicKey
        val encryptPassword = withContext(Dispatchers.Default) {
            val decodedPublicKey = Base64.decode(publicKey, Base64.DEFAULT)
            val key =
                KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(decodedPublicKey))
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            Base64.encodeToString(cipher.doFinal(password.toByteArray()), Base64.DEFAULT)
        }
        val loginRequest = LoginRequest(username, encryptPassword, publicKey)
        try {
            return withContext(Dispatchers.IO) {
                userApi.login(loginRequest)
            }
        } catch (e: HttpException) {
            val resp = e.response() ?: throw e
            resp.parseServerErrorWhenFailed()
        }
    }

    suspend fun getUserInfo(token: String) = withContext(Dispatchers.IO) {
        userApi.getUserInfo(token)
    }

    suspend fun reloadUserInfo(token: String) = withContext(Dispatchers.IO) {
        userApi.reloadUserInfo(token)
    }

    suspend fun getCampusList(token: String) = withContext(Dispatchers.IO) {
        userApi.getCampusList(token)
    }

    suspend fun updateUserCampus(token: String, campus: String) = withContext(Dispatchers.IO) {
        userApi.updateUserCampus(token, SetCampusRequest(campus))
    }
}