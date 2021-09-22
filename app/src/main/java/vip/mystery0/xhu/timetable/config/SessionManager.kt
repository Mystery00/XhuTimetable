package vip.mystery0.xhu.timetable.config

import vip.mystery0.xhu.timetable.model.entity.UserInfo

object SessionManager {
    //用户列表
    private val userMap = HashMap<String, User>(4)

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
        userMap[studentId] = User(studentId, password, token, userInfo)
        writeToCache()
    }

    fun logout(studentId: String) {
        userMap.remove(studentId)
        writeToCache()
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
    val main: Boolean = false,
)