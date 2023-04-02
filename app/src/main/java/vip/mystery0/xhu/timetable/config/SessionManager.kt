package vip.mystery0.xhu.timetable.config

import android.widget.Toast
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.config.interceptor.ServerNeedLoginException
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.model.OldUserInfo
import vip.mystery0.xhu.timetable.model.UserInfo
import vip.mystery0.xhu.timetable.repository.doLogin
import java.util.concurrent.ConcurrentHashMap
import kotlin.system.exitProcess

object SessionManager {
    //用户列表
    private val userMap = ConcurrentHashMap<String, User>(4)

    suspend fun mainUserOrNull(): User? = runOnCpu {
        userMap.values.find { it.main } ?: userMap.values.firstOrNull()
    }

    suspend fun mainUser(): User {
        val main = mainUserOrNull()
        if (main != null) return main
        Toast.makeText(context.applicationContext, "检测到异常，即将关闭应用", Toast.LENGTH_LONG)
            .show()
        android.os.Process.killProcess(android.os.Process.myPid())
        exitProcess(10)
    }

    fun isLogin(): Boolean = userMap.isNotEmpty()

    suspend fun getUser(studentId: String): User? {
        val user = userMap[studentId]
        if (user == null) {
            logout(studentId)
            return null
        }
        return user
    }

    suspend fun user(studentId: String): User = getUser(studentId)!!

    suspend fun login(
        studentId: String,
        password: String,
        token: String,
        userInfo: UserInfo,
    ) {
        runOnCpu {
            val main = userMap.values.find { it.main }
            userMap[studentId] = User(studentId, password, token, userInfo, main == null, null)
        }
        writeToCache()
    }

    suspend fun logout(studentId: String): Boolean {
        var result = false
        runOnCpu {
            val user = userMap.remove(studentId)
            user?.let {
                if (it.main) {
                    userMap.values.firstOrNull()?.main = true
                    result = true
                }
            }
        }
        writeToCache()
        return result
    }

    suspend fun changeMainUser(studentId: String): Boolean {
        val user = userMap[studentId] ?: return false
        mainUser().main = false
        user.main = true
        writeToCache()
        return true
    }

    suspend fun loggedUserList(): List<User> {
        return runOnCpu { userMap.values.sortedBy { !it.main }.toList() }
    }

    private suspend fun writeToCache() {
        setConfig { userList = ArrayList(userMap.values) }
    }

    suspend fun readFromCache() {
        val list = getConfig { userList }
        userMap.clear()
        list.forEach {
            userMap[it.studentId] = it
        }
    }

    private suspend fun reLogin(
        user: User,
        newToken: String,
        userInfo: UserInfo,
    ) {
        runOnCpu {
            userMap.remove(user.studentId)
            userMap[user.studentId] =
                User(
                    user.studentId,
                    user.password,
                    newToken,
                    userInfo,
                    user.main,
                    user.profileImage
                )
        }
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
            val userInfo = serverApi.userInfo(loginResponse.sessionToken)
            reLogin(this, loginResponse.sessionToken, userInfo)
            block(loginResponse.sessionToken) to true
        }
}

data class OldUser(
    //用户名
    val studentId: String,
    //密码
    val password: String,
    //token
    val token: String,
    //用户信息
    val info: OldUserInfo,
    //是否为主用户
    var main: Boolean = false,
    //头像
    var profileImage: String?,
)

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
    //头像
    var profileImage: String?,
)