package vip.mystery0.xhu.timetable.repository

import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.JobApi
import vip.mystery0.xhu.timetable.api.UserApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.request.AutoCheckScoreRequest
import vip.mystery0.xhu.timetable.model.response.JobHistoryResponse
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

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
            val decodedPublicKey = Base64.decode(publicKey, Base64.DEFAULT)
            val key =
                KeyFactory.getInstance("RSA").generatePublic(X509EncodedKeySpec(decodedPublicKey))
            val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
            cipher.init(Cipher.ENCRYPT_MODE, key)
            Base64.encodeToString(cipher.doFinal(user.password.toByteArray()), Base64.DEFAULT)
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