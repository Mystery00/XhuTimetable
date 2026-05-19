package vip.mystery0.xhu.timetable.repository

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA1
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
        val publicKey = withContext(Dispatchers.IO) { userApi.publicKey() }.publicKey
        val encryptPassword = withContext(Dispatchers.Default) {
            val decodedPublicKey = Base64.decode(publicKey).decodeToString()
            val key = CryptographyProvider.Default.get(RSA.PKCS1)
                .publicKeyDecoder(SHA1)
                .decodeFromByteArray(RSA.PublicKey.Format.DER, decodedPublicKey.toByteArray())
            Base64.encode(key.encryptor().encrypt(user.password.toByteArray()))
        }
        val request = AutoScoreStartRequest(
            password = encryptPassword,
            publicKey = publicKey,
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
}
