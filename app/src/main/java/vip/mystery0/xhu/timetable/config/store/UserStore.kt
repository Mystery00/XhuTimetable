package vip.mystery0.xhu.timetable.config.store

import android.util.Log
import android.widget.Toast
import com.squareup.moshi.Moshi
import com.tencent.mmkv.MMKV
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.UserApi
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.model.UserInfo
import vip.mystery0.xhu.timetable.module.registerAdapter
import vip.mystery0.xhu.timetable.repository.doLogin
import kotlin.random.Random
import kotlin.system.exitProcess

object UserStore {
    private const val TAG = "UserStore"
    private const val LOGGED_USER_LIST = "loggedUserList"
    private const val MAIN_USER = "mainUser"
    private val secret: String
        get() {
            val value = GlobalNewConfig.userStoreSecret
            if (value.isBlank()) {
                val newValue = Random(System.currentTimeMillis()).nextLong().toString()
                GlobalNewConfig.userStoreSecret = newValue
                return newValue
            }
            return value
        }
    private val kv = MMKV.mmkvWithID("UserStore", MMKV.SINGLE_PROCESS_MODE, secret)
    private val userMoshi = Moshi.Builder().registerAdapter().build().adapter(User::class.java)

    suspend fun isLogin(): Boolean = getMainUser() != null

    suspend fun setMainUser(user: User) {
        setMainUser(user.studentId)
    }

    suspend fun setMainUser(studentId: String) {
        Log.i(TAG, "setMainUser: $studentId")
        runOnIo { kv.encode(MAIN_USER, studentId) }
    }

    suspend fun getMainUserId(): String? = runOnIo { kv.decodeString(MAIN_USER) }

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
        Toast.makeText(context.applicationContext, "检测到异常，即将关闭应用", Toast.LENGTH_LONG)
            .show()
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(10)
    }

    suspend fun getUserByStudentId(studentId: String): User? {
        val key = studentId.userMapKey()
        val json = runOnIo { kv.decodeString(key) } ?: return null
        return userMoshi.fromJson(json)
    }

    suspend fun userByStudentId(studentId: String): User = getUserByStudentId(studentId)!!

    suspend fun loggedUserList(): List<User> {
        val list = runOnIo { kv.decodeStringSet(LOGGED_USER_LIST) } ?: emptySet()
        if (list.isEmpty()) return emptyList()
        return list.mapNotNull { studentId ->
            val key = studentId.userMapKey()
            val json = runOnIo { kv.decodeString(key) } ?: return@mapNotNull null
            val user = userMoshi.fromJson(json) ?: return@mapNotNull null
            user
        }
    }

    suspend fun login(user: User) {
        Log.i(TAG, "login: $user")
        val key = user.mapKey()
        val json = userMoshi.toJson(user)
        runOnIo {
            kv.encode(key, json)
            updateLoggedUserList { it + user.studentId }
        }
    }

    /**
     * 登出用户
     * @return 登出的是否是主用户
     */
    suspend fun logout(studentId: String): Boolean {
        Log.i(TAG, "logout: $studentId")
        val mainUserId = getMainUserId()
        val key = studentId.userMapKey()
        runOnIo {
            kv.removeValueForKey(key)
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

    private suspend fun updateUser(user: User) {
        Log.i(TAG, "updateUser: $user")
        val key = user.mapKey()
        val json = userMoshi.toJson(user)
        runOnIo { kv.encode(key, json) }
    }

    private suspend fun updateLoggedUserList(func: (Set<String>) -> Set<String>) = runOnIo {
        val list = kv.decodeStringSet(LOGGED_USER_LIST) ?: emptySet()
        kv.encode(LOGGED_USER_LIST, func(list))
    }

    private fun String.userMapKey() = "user_$this"
    private fun User.mapKey() = studentId.userMapKey()

    suspend fun <R> User.withAutoLoginOnce(block: suspend (String) -> R): R =
        try {
            block(token)
        } catch (exception: ServerNeedLoginException) {
            //做一次登录
            val loginResponse = doLogin(this)
            //获取用户信息
            val userApi = KoinJavaComponent.get<UserApi>(UserApi::class.java)
            val userInfo = userApi.getUserInfo(loginResponse.sessionToken)
            val user = this.copy(token = loginResponse.sessionToken, info = userInfo)
            updateUser(user)
            block(loginResponse.sessionToken)
        }

    suspend fun <R> User.withAutoLogin(block: suspend (String) -> R): Pair<R, Boolean> =
        try {
            block(token) to false
        } catch (exception: ServerNeedLoginException) {
            //做一次登录
            val loginResponse = doLogin(this)
            //获取用户信息
            val userApi = KoinJavaComponent.get<UserApi>(UserApi::class.java)
            val userInfo = userApi.getUserInfo(loginResponse.sessionToken)
            val user = this.copy(token = loginResponse.sessionToken, info = userInfo)
            updateUser(user)
            block(loginResponse.sessionToken) to true
        }
}

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
    var profileImage: String?,
)