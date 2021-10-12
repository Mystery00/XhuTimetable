package vip.mystery0.xhu.timetable.repository

import android.util.Base64
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.model.request.LoginRequest
import vip.mystery0.xhu.timetable.model.response.LoginResponse
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

suspend fun doLogin(user: User): LoginResponse =
    doLogin(user.studentId, user.password)

suspend fun doLogin(username: String, password: String): LoginResponse {
    val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
    val publicKey = serverApi.publicKey().publicKey
    val encryptPassword = runOnCpu {
        val decodedPublicKey = Base64.decode(publicKey, Base64.DEFAULT)
        val key =
            KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(decodedPublicKey))
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key)
        Base64.encodeToString(cipher.doFinal(password.toByteArray()), Base64.DEFAULT)
    }
    val loginRequest = LoginRequest(username, encryptPassword, publicKey)
    return serverApi.login(loginRequest)
}