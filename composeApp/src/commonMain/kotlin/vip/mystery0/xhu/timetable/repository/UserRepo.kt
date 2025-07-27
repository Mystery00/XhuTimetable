package vip.mystery0.xhu.timetable.repository

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDH
import dev.whyoleg.cryptography.algorithms.HKDF
import dev.whyoleg.cryptography.algorithms.SHA256
import io.ktor.util.decodeBase64Bytes
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
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

object UserRepo : BaseDataRepo {
    private val userApi: UserApi by inject()

    suspend fun doLogin(user: User): LoginResponse =
        doLogin(user.studentId, user.password)

    suspend fun doLogin(username: String, password: String): LoginResponse {
        val resp = withContext(Dispatchers.IO) { userApi.publicKey() }
        val (encryptPassword, clientPublicKey) = withContext(Dispatchers.Default) {
            val clientKeyPair = CryptographyProvider.Default.get(ECDH)
                .keyPairGenerator(EC.Curve.P521)
                .generateKeyBlocking()
            val serverPublicKey = CryptographyProvider.Default.get(ECDH)
                .publicKeyDecoder(curve = EC.Curve.P521)
                .decodeFromByteArrayBlocking(
                    EC.PublicKey.Format.DER,
                    resp.publicKey.decodeBase64Bytes()
                )
            val nonce = resp.nonce.decodeBase64Bytes()
            val sharedSecret = clientKeyPair.privateKey.sharedSecretGenerator()
                .generateSharedSecretToByteArrayBlocking(serverPublicKey)
            val cipher = CryptographyProvider.Default.get(AES.GCM)
                .keyDecoder()
                .decodeFromByteArrayBlocking(
                    AES.Key.Format.RAW, CryptographyProvider.Default.get(HKDF)
                        .secretDerivation(SHA256, AES.Key.Size.B256, nonce)
                        .deriveSecretToByteArrayBlocking(sharedSecret)
                )
                .cipher()
            val encryptedPassword =
                cipher.encryptBlocking(password.toByteArray(), nonce).encodeBase64()
            val clientPublicKey =
                clientKeyPair.publicKey.encodeToByteArrayBlocking(EC.PublicKey.Format.DER)
                    .encodeBase64()
            encryptedPassword to clientPublicKey
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
        userApi.reloadUserInfo(token)
    }

    suspend fun getCampusList(token: String) = withContext(Dispatchers.IO) {
        userApi.getCampusList(token)
    }

    suspend fun updateUserCampus(token: String, campus: String) = withContext(Dispatchers.IO) {
        userApi.updateUserCampus(token, SetCampusRequest(campus))
    }
}