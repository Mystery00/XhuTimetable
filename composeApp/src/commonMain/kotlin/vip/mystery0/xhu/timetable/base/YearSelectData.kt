package vip.mystery0.xhu.timetable.base

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Month
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore

@Immutable
data class YearSelect(
    val value: Int,
    override val title: String,
    override val selected: Boolean,
) : Selectable

class YearSelectDataLoader : SelectDataLoader<YearSelect, Int>() {
    override suspend fun initSelect(): List<YearSelect> {
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

    suspend fun getSelectedYear(): Int = getSelected()!!.value

    override fun valueId(value: YearSelect): Int = value.value

    override fun updateSelect(
        t: YearSelect,
        selected: Boolean
    ): YearSelect = t.copy(selected = selected)
}