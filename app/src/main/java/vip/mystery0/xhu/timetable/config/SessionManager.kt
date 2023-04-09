package vip.mystery0.xhu.timetable.config

import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.OldUserInfo
import vip.mystery0.xhu.timetable.model.UserInfo

object SessionManager {
    suspend fun readFromCache() {
        val list = getConfig { userList }
        for (user in list) {
            val info = user.info
            UserStore.login(
                User(
                    user.studentId,
                    user.password,
                    user.token,
                    UserInfo(
                        info.studentId,
                        info.userName,
                        Gender.parseOld(info.sex),
                        info.grade.toInt(),
                        info.institute,
                        info.profession,
                        info.className,
                        info.direction,
                    ),
                    user.profileImage,
                )
            )
        }
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