package vip.mystery0.xhu.timetable.repository

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.DelicateCryptographyApi
import dev.whyoleg.cryptography.algorithms.RSA
import dev.whyoleg.cryptography.algorithms.SHA1
import io.ktor.util.decodeBase64String
import io.ktor.util.encodeBase64
import io.ktor.utils.io.core.toByteArray
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.JobApi
import vip.mystery0.xhu.timetable.api.UserApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.request.AutoCheckScoreRequest
import vip.mystery0.xhu.timetable.model.response.JobHistoryResponse

object JobRepo : BaseDataRepo {
    private val jobApi: JobApi by inject()
    private val userApi: UserApi by inject()

    suspend fun fetchHistoryList(): List<JobHistoryResponse> {
        checkForceLoadFromCloud(true)

        val response = mainUser().withAutoLoginOnce {
            jobApi.getHistory(it)
        }
        return response
    }

    suspend fun testPush(registrationId: String) {
        mainUser().withAutoLoginOnce {
            jobApi.pushTest(it, registrationId)
        }
    }

    suspend fun addAutoCheckScoreJob(registrationId: String) {
        val user = mainUser()
        val publicKey = withContext(Dispatchers.IO) { userApi.publicKey() }.publicKey
        val encryptPassword = withContext(Dispatchers.Default) {
            val decodedPublicKey = publicKey.decodeBase64String()
            val key = CryptographyProvider.Default.get(RSA.PKCS1)
                .publicKeyDecoder(SHA1)
                .decodeFromByteArray(RSA.PublicKey.Format.DER, decodedPublicKey.toByteArray())
            key.encryptor().encrypt(user.password.toByteArray()).encodeBase64()
        }
        val year = getConfigStore { nowYear }
        val term = getConfigStore { nowTerm }
        val request = AutoCheckScoreRequest(
            username = user.studentId,
            password = encryptPassword,
            publicKey = publicKey,
            registrationId = registrationId,
            year = year,
            term = term,
        )
        mainUser().withAutoLoginOnce {
            jobApi.autoCheckScore(it, request)
        }
    }
}