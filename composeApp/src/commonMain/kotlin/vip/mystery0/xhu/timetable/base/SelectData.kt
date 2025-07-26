package vip.mystery0.xhu.timetable.base

import com.maxkeppeker.sheets.core.models.base.UseCaseState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext

interface Selectable {
    val title: String
    val selected: Boolean
}

abstract class SelectDataLoader<T : Selectable, ID> {
    val select = MutableStateFlow<List<T>>(emptyList())
    protected val list: List<T>
        get() = select.value

    val selectDialog = MutableStateFlow(UseCaseState())

    suspend fun init() {
        select.value = initSelect()
    }

    abstract suspend fun initSelect(): List<T>

    open suspend fun getSelected(): T? {
        if (list.isEmpty()) {
            return null
        }
        val selected = withContext(Dispatchers.Default) {
            list.firstOrNull { it.selected }
        }
        return selected
    }

    open suspend fun getMultiSelected(): List<T> =
        withContext(Dispatchers.Default) {
            list.filter { it.selected }
        }

    open suspend fun setSelected(value: ID): Boolean {
        val selected = getSelected()
        if (selected != null && valueId(selected) == value) {
            return false
        }
        select.value = list.map {
            updateSelect(it, valueId(it) == value)
        }
        return true
    }

    open suspend fun multiSetSelected(values: List<ID>) {
        select.value = list.map {
            updateSelect(it, values.contains(valueId(it)))
        }
    }

    abstract fun valueId(value: T): ID

    abstract fun updateSelect(t: T, selected: Boolean): T
}