package vip.mystery0.xhu.timetable.repository

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDH
import dev.whyoleg.cryptography.algorithms.HKDF
import dev.whyoleg.cryptography.algorithms.SHA256
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.JobApi
import vip.mystery0.xhu.timetable.api.UserApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.model.request.AutoScoreStartRequest
import vip.mystery0.xhu.timetable.model.request.ClientTestRequest
import vip.mystery0.xhu.timetable.model.response.AutoScoreStartResponse
import vip.mystery0.xhu.timetable.model.response.AutoScoreStatusResponse
import vip.mystery0.xhu.timetable.platform
import vip.mystery0.xhu.timetable.push.pushManager
import kotlin.io.encoding.Base64

object JobRepo : BaseDataRepo {
    private val jobApi: JobApi by inject()
    private val userApi: UserApi by inject()

    suspend fun startAutoScoreJob(): AutoScoreStartResponse {
        val registrationId = pushManager.registrationId()
            ?: pushManager.refreshRegistrationId()
            ?: error("推送服务初始化失败，请稍后重试")
        val user = mainUser()
        val resp = withContext(Dispatchers.IO) { userApi.publicKey() }
        val (encryptPassword, clientPublicKey) = withContext(Dispatchers.Default) {
            val clientKeyPair = CryptographyProvider.Default.get(ECDH)
                .keyPairGenerator(EC.Curve.P521)
                .generateKeyBlocking()
            val serverPublicKey = CryptographyProvider.Default.get(ECDH)
                .publicKeyDecoder(curve = EC.Curve.P521)
                .decodeFromByteArrayBlocking(
                    EC.PublicKey.Format.DER,
                    Base64.decode(resp.publicKey)
                )
            val nonce = Base64.decode(resp.nonce)
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
                Base64.encode(cipher.encryptBlocking(user.password.toByteArray(), nonce))
            val clientPublicKey =
                Base64.encode(
                    clientKeyPair.publicKey.encodeToByteArrayBlocking(EC.PublicKey.Format.DER)
                )
            encryptedPassword to clientPublicKey
        }
        val request = AutoScoreStartRequest(
            password = encryptPassword,
            publicKey = resp.publicKey,
            clientPublicKey = clientPublicKey,
            registrationId = registrationId,
            platform = platform().name,
        )
        return mainUser().withAutoLoginOnce {
            jobApi.startAutoScoreJob(it, request = request)
        }
    }

    suspend fun stopAutoScoreJob() {
        mainUser().withAutoLoginOnce {
            jobApi.stopAutoScoreJob(it)
        }
    }

    suspend fun fetchAutoScoreJobStatus(): AutoScoreStatusResponse {
        return mainUser().withAutoLoginOnce {
            jobApi.getAutoScoreJobStatus(it)
        }
    }

    suspend fun startClientTestJob() {
        val registrationId = pushManager.registrationId()
            ?: pushManager.refreshRegistrationId()
            ?: error("推送服务初始化失败，请稍后重试")
        val request = ClientTestRequest(registrationId = registrationId)
        mainUser().withAutoLoginOnce {
            jobApi.startClientTestJob(it, request = request)
        }
    }
}
