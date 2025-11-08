package vip.mystery0.xhu.timetable.base

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore

@Immutable
data class UserSelect(
    val studentId: String,
    val userName: String,
    override val selected: Boolean,
    override val title: String = userName,
) : Selectable

class UserSelectDataLoader : SelectDataLoader<UserSelect, String>() {
    override suspend fun initSelect(): List<UserSelect> {
        val loggedUserList = UserStore.loggedUserList()
        val mainUserId = UserStore.mainUserId()
        return loggedUserList.map {
            UserSelect(it.studentId, it.info.name, it.studentId == mainUserId)
        }.sortedBy { it.selected }
    }

    suspend fun getSelectedUser(): User? {
        val selected = getSelected()
        if (selected == null) {
            //如果为空，返回主用户，当然如果用户全部为空，该方法也返回空
            return UserStore.getMainUser()
        }
        val selectedId = withContext(Dispatchers.Default) {
            list.firstOrNull { it.selected }?.studentId
        } ?: return null
        return UserStore.getUserByStudentId(selectedId)
    }

    override fun valueId(value: UserSelect): String = value.studentId

    override fun updateSelect(
        t: UserSelect,
        selected: Boolean
    ): UserSelect = t.copy(selected = selected)
}