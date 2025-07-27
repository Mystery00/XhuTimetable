package vip.mystery0.xhu.timetable.config.store

import co.touchlab.kermit.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.config.ktor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.model.UserInfo
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.repository.UserRepo
import vip.mystery0.xhu.timetable.utils.forceExit

object UserStore {
    private val logger = Logger.withTag(tag = "UserStore")
    private const val LOGGED_USER_LIST = "loggedUserList"
    private const val MAIN_USER = "mainUser"
    private val userJson = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    private val mutex = Mutex()

    suspend fun isLogin(): Boolean = getMainUser() != null

    suspend fun setMainUser(user: User) {
        setMainUser(user.studentId)
    }

    suspend fun setMainUser(studentId: String) {
        logger.i("setMainUser: $studentId")
        withContext(Dispatchers.IO) { Store.UserStore.setConfiguration(MAIN_USER, studentId) }
    }

    suspend fun getMainUserId(): String? =
        withContext(Dispatchers.IO) {
            Store.UserStore.getConfiguration(MAIN_USER, "")
                .ifBlank { null }
        }

    suspend fun mainUserId(): String = getMainUserId()!!

    suspend fun getMainUser(): User? {
        val mainUserId = getMainUserId()
        if (mainUserId.isNullOrBlank()) {
            //主用户不存在，返回第一个登录的用户
            val list = loggedUserList()
            if (list.isEmpty()) return null
            val mainUser = list.first()
            return mainUser.also { setMainUser(it) }
        }
        return getUserByStudentId(mainUserId)
    }

    suspend fun mainUser(): User {
        val main = getMainUser()
        if (main != null) return main
        forceExit()
        throw NotImplementedError()
    }

    suspend fun getUserByStudentId(studentId: String): User? {
        val key = studentId.userMapKey()
        val json = withContext(Dispatchers.IO) { Store.UserStore.getConfiguration(key, "") }
        if (json.isBlank()) return null
        return userJson.decodeFromString(json)
    }

    suspend fun userByStudentId(studentId: String): User = getUserByStudentId(studentId)!!

    suspend fun loggedUserList(): List<User> {
        val list = withContext(Dispatchers.IO) {
            Store.UserStore.getConfiguration(LOGGED_USER_LIST, emptySet<String>())
        }
        if (list.isEmpty()) return emptyList()
        return list.mapNotNull { studentId ->
            val key = studentId.userMapKey()
            val json: String = withContext(Dispatchers.IO) {
                Store.UserStore.getConfiguration(key, "")
            }
            if (json.isBlank()) return@mapNotNull null
            userJson.decodeFromString(json)
        }
    }

    suspend fun login(user: User) {
        logger.i("login: ${user.studentId}")
        val key = user.mapKey()
        val json = userJson.encodeToString(user)
        withContext(Dispatchers.IO) {
            Store.UserStore.setConfiguration(key, json)
            updateLoggedUserList { it + user.studentId }
        }
    }

    /**
     * 登出用户
     * @return 登出的是否是主用户
     */
    suspend fun logout(studentId: String): Boolean {
        logger.i("logout: $studentId")
        val mainUserId = getMainUserId()
        val key = studentId.userMapKey()
        withContext(Dispatchers.IO) {
            Store.UserStore.removeConfiguration(key)
            updateLoggedUserList { it - studentId }
        }
        if (mainUserId == studentId) {
            //登出的是主用户，需要重新设置主用户
            val list = loggedUserList()
            if (list.isEmpty()) {
                return true
            }
            val newMainUser = list.first()
            setMainUser(newMainUser)
        }
        return mainUserId == studentId
    }

    suspend fun updateUser(user: User) {
        logger.i("updateUser: ${user.studentId}")
        val key = user.mapKey()
        val json = userJson.encodeToString(user)
        withContext(Dispatchers.IO) {
            Store.UserStore.setConfiguration(key, json)
        }
    }

    private suspend fun updateLoggedUserList(func: (Set<String>) -> Set<String>) {
        withContext(Dispatchers.IO) {
            val list = Store.UserStore.getConfiguration(LOGGED_USER_LIST, emptySet<String>())
            Store.UserStore.setConfiguration(LOGGED_USER_LIST, func(list))
        }
        EventBus.post(EventType.USER_LIST_CHANGED)
    }

    private fun String.userMapKey() = "user_$this"
    private fun User.mapKey() = studentId.userMapKey()

    suspend fun <R> User.withAutoLoginOnce(block: suspend (String) -> R): R =
        withContext(Dispatchers.Default) {
            try {
                return@withContext withContext(Dispatchers.IO) {
                    block(token)
                }
            } catch (_: ServerNeedLoginException) {
                val sessionToken = mutex.withLock {
                    val newUser = userByStudentId(studentId)
                    val updated = this@withAutoLoginOnce.token != newUser.token
                    if (!updated) {
                        //做一次登录
                        val loginResponse = UserRepo.doLogin(this@withAutoLoginOnce)
                        //获取用户信息
                        val userInfo = UserRepo.getUserInfo(loginResponse.sessionToken)
                        val user =
                            this@withAutoLoginOnce.copy(
                                token = loginResponse.sessionToken,
                                info = userInfo
                            )
                        updateUser(user)
                        loginResponse.sessionToken
                    } else {
                        newUser.token
                    }
                }
                return@withContext withContext(Dispatchers.IO) {
                    block(sessionToken)
                }
            }
        }
}

@Serializable
data class User(
    //用户名
    val studentId: String,
    //密码
    val password: String,
    //token
    val token: String,
    //用户信息
    val info: UserInfo,
    //头像
    var profileImage: String? = null,
)