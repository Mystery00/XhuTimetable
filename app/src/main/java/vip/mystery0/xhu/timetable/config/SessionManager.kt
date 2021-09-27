package vip.mystery0.xhu.timetable.config

import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.model.UserInfo
import vip.mystery0.xhu.timetable.repository.doLogin

object SessionManager {
    //用户列表
    private val userMap = HashMap<String, User>(4)

    val mainUser: User
        get() = userMap.values.find { it.main }!!

    fun isLogin(): Boolean = userMap.isNotEmpty()

    fun getUser(studentId: String): User? {
        val user = userMap[studentId]
        if (user == null) {
            logout(studentId)
            return null
        }
        return user
    }

    fun login(
        studentId: String,
        password: String,
        token: String,
        userInfo: UserInfo,
    ) {
        val main = userMap.values.find { it.main }
        userMap[studentId] = User(studentId, password, token, userInfo, main == null)
        writeToCache()
    }

    fun logout(studentId: String) {
        userMap.remove(studentId)
        writeToCache()
    }

    fun changeMainUser(studentId: String): Boolean {
        val user = userMap[studentId] ?: return false
        mainUser.main = false
        user.main = true
        writeToCache()
        return true
    }

    @Synchronized
    private fun writeToCache() {
        Config.userList = ArrayList(userMap.values)
    }

    @Synchronized
    fun readFromCache() {
        userMap.clear()
        Config.userList.forEach {
            userMap[it.studentId] = it
        }
    }

    private fun reLogin(
        user: User,
        newToken: String,
        userInfo: UserInfo,
    ) {
        userMap.remove(user.studentId)
        userMap[user.studentId] = User(user.studentId, user.password, newToken, userInfo, user.main)
        writeToCache()
    }

    suspend fun <R> User.withAutoLogin(block: suspend (String) -> R): Pair<R, Boolean> =
        try {
            block(token) to false
        } catch (exception: ServerNeedLoginException) {
            //做一次登录
            val loginResponse = doLogin(this)
            //获取用户信息
            val serverApi = KoinJavaComponent.get<ServerApi>(ServerApi::class.java)
            val userInfo = serverApi.userInfo(loginResponse.token)
            reLogin(this, loginResponse.token, userInfo)
            block(token) to true
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
    //是否为主用户
    var main: Boolean = false,
)