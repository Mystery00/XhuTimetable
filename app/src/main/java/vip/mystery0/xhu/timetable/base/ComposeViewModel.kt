package vip.mystery0.xhu.timetable.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore

abstract class ComposeViewModel : ViewModel(), KoinComponent {
    protected suspend fun initUserSelect(): List<UserSelect> {
        val loggedUserList = UserStore.loggedUserList()
        val mainUserId = UserStore.mainUserId()
        return loggedUserList.map {
            UserSelect(it.studentId, it.info.name, it.studentId == mainUserId)
        }.sortedBy { it.selected }
    }

    protected suspend fun getSelectedUser(list: List<UserSelect>): User? {
        if (list.isEmpty()) {
            //如果为空，返回主用户，当然如果用户全部为空，该方法也返回空
            return UserStore.getMainUser()
        }
        val selectedId =
            withContext(Dispatchers.Default) { list.firstOrNull { it.selected }?.studentId }
                ?: return UserStore.getMainUser()
        return UserStore.getUserByStudentId(selectedId)
    }

    protected suspend fun setSelectedUser(
        list: List<UserSelect>,
        studentId: String
    ): Pair<List<UserSelect>, Boolean> {
        val selectedUser = getSelectedUser(list)
        if (selectedUser != null && selectedUser.studentId == studentId) {
            return list to false
        }
        return list.map {
            UserSelect(it.studentId, it.userName, it.studentId == studentId)
        } to true
    }
}

data class UserSelect(
    val studentId: String,
    val userName: String,
    val selected: Boolean,
)