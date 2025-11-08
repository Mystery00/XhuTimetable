package vip.mystery0.xhu.timetable.base

import androidx.compose.runtime.Immutable
import vip.mystery0.xhu.timetable.config.store.getConfigStore

@Immutable
data class TermSelect(
    val value: Int,
    override val title: String,
    override val selected: Boolean,
) : Selectable

class TermSelectDataLoader : SelectDataLoader<TermSelect, Int>() {
    override suspend fun initSelect(): List<TermSelect> {
        val nowTerm = getConfigStore { nowTerm }
        return (1..2).map {
            TermSelect(it, "第${it}学期", it == nowTerm)
        }
    }

    suspend fun getSelectedTerm(): Int = getSelected()!!.value

    override fun valueId(value: TermSelect): Int = value.value

    override fun updateSelect(
        t: TermSelect,
        selected: Boolean
    ): TermSelect = t.copy(selected = selected)
}