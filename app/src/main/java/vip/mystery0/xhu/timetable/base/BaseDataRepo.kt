package vip.mystery0.xhu.timetable.base

import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.module.NetworkNotConnectException

interface BaseDataRepo : KoinComponent {
    val isOnline: Boolean
        get() = isOnline()

    fun checkForceLoadFromCloud(forceLoadFromCloud: Boolean) {
        if (!forceLoadFromCloud) {
            return
        }
        if (!isOnline) {
            throw NetworkNotConnectException()
        }
    }

    suspend fun requestUserList(): List<User> {
        val multiAccountMode = getConfigStore { multiAccountMode }
        return if (multiAccountMode) loggedUserList() else listOf(mainUser())
    }

    suspend fun loggedUserList() = UserStore.loggedUserList()

    suspend fun mainUser() = UserStore.mainUser()
}