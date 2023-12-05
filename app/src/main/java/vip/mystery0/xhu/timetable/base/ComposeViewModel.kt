package vip.mystery0.xhu.timetable.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import java.time.Month

abstract class ComposeViewModel : ViewModel(), KoinComponent {
    protected suspend fun initUserSelect(): List<UserSelect> {
        val loggedUserList = UserStore.loggedUserList()
        val mainUserId = UserStore.mainUserId()
        return loggedUserList.map {
            UserSelect(it.studentId, it.info.name, it.studentId == mainUserId)
        }.sortedBy { it.selected }
    }

    protected suspend fun initYearSelect(): List<YearSelect> {
        val loggedUserList = UserStore.loggedUserList()
        val termStartDate = getConfigStore { termStartDate }
        val nowYear = getConfigStore { nowYear }
        val minYear = loggedUserList.minByOrNull { it.info.xhuGrade }?.info?.xhuGrade ?: nowYear
        var maxYear =
            if (termStartDate.month < Month.JUNE) termStartDate.year else termStartDate.year - 1
        if (maxYear < nowYear) {
            maxYear = nowYear
        }
        if (minYear > maxYear) {
            maxYear = minYear
        }
        return (minYear..maxYear).map {
            YearSelect(it, "${it}-${it + 1}学年", it == nowYear)
        }.reversed()
    }

    protected suspend fun initTermSelect(): List<TermSelect> {
        val nowTerm = getConfigStore { nowTerm }
        return (1..2).map {
            TermSelect(it, "第${it}学期", it == nowTerm)
        }
    }

    protected suspend fun getSelectedUser(list: List<UserSelect>): User? {
        if (list.isEmpty()) {
            //如果为空，返回主用户，当然如果用户全部为空，该方法也返回空
            return UserStore.getMainUser()
        }
        val selectedId = withContext(Dispatchers.Default) {
            list.firstOrNull { it.selected }?.studentId
        } ?: return UserStore.getMainUser()
        return UserStore.getUserByStudentId(selectedId)
    }

    protected fun getSelectedYear(list: List<YearSelect>): Int =
        getSelected(list)!!.value

    protected fun getSelectedTerm(list: List<TermSelect>): Int =
        getSelected(list)!!.value

    protected fun <T : Selectable> getSelected(list: List<T>): T? {
        return list.firstOrNull { it.selected }
    }

    protected suspend fun setSelectedUser(
        list: List<UserSelect>,
        studentId: String,
    ): Pair<List<UserSelect>, Boolean> {
        val selectedUser = getSelectedUser(list)
        if (selectedUser != null && selectedUser.studentId == studentId) {
            return list to false
        }
        return list.map {
            it.copy(selected = it.studentId == studentId)
        } to true
    }

    protected fun setSelectedYear(
        list: List<YearSelect>,
        year: Int,
    ): List<YearSelect> {
        val selected = getSelectedYear(list)
        if (selected == year) {
            return list
        }
        return list.map {
            it.copy(selected = it.value == year)
        }
    }

    protected fun setSelectedTerm(
        list: List<TermSelect>,
        term: Int,
    ): List<TermSelect> {
        val selected = getSelectedTerm(list)
        if (selected == term) {
            return list
        }
        return list.map {
            it.copy(selected = it.value == term)
        }
    }
}

interface Selectable {
    val title: String
    val selected: Boolean
}

data class UserSelect(
    val studentId: String,
    val userName: String,
    override val selected: Boolean,
    override val title: String = "${userName}(${studentId})",
) : Selectable

data class YearSelect(
    val value: Int,
    override val title: String,
    override val selected: Boolean,
) : Selectable

data class TermSelect(
    val value: Int,
    override val title: String,
    override val selected: Boolean,
) : Selectable