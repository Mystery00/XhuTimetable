package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.datetime.DayOfWeek
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.SelectDataLoader
import vip.mystery0.xhu.timetable.base.Selectable
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.ClassroomRepo
import vip.mystery0.xhu.timetable.utils.formatChina

class CourseRoomViewModel : PagingComposeViewModel<ClassroomRequest, ClassroomResponse>(
    {
        ClassroomRepo.getClassroomListStream(it)
    }
) {
    val areaSelect = AreaSelectDataLoader()
    val weekSelect = IntSelectDataLoader()
    val daySelect = IntSelectDataLoader()
    val timeSelect = IntSelectDataLoader()

    val init: Boolean
        get() = pageRequestFlow.value == null

    fun init() {
        viewModelScope.safeLaunch {
            areaSelect.init()

            weekSelect.customInit(1, 20) { "第${it}周" }
            daySelect.customInit(1, 7) {
                DayOfWeek(it).formatChina()
            }
            timeSelect.customInit(1, 12) { "第${it}节" }
        }
    }

    fun changeArea(area: String) {
        viewModelScope.safeLaunch {
            areaSelect.setSelected(area)
        }
    }

    fun changeWeek(week: List<Int>) {
        viewModelScope.safeLaunch {
            weekSelect.multiSetSelected(week)
        }
    }

    fun changeDay(day: List<Int>) {
        viewModelScope.safeLaunch {
            daySelect.multiSetSelected(day)
        }
    }

    fun changeTime(time: List<Int>) {
        viewModelScope.safeLaunch {
            timeSelect.multiSetSelected(time)
        }
    }

    fun search() {
        fun failed(message: String) {
            logger.w("search failed: $message")
            toastMessage(message)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("search failed", throwable)
            failed(throwable.message ?: throwable.desc())
        }) {
            val area = areaSelect.getSelected()?.value ?: ""
            val week = weekSelect.getMultiSelected().map { it.value }
            val day = daySelect.getMultiSelected().map { it.value }
            val time = timeSelect.getMultiSelected().map { it.value }
            val request = ClassroomRequest(area, week, day, time)
            pageRequestFlow.emit(request)
        }
    }
}

data class AreaSelect(
    val value: String,
    override val title: String,
    override val selected: Boolean,
) : Selectable

class AreaSelectDataLoader : SelectDataLoader<AreaSelect, String>() {
    override suspend fun initSelect(): List<AreaSelect> {
        fun buildAreaSelect(title: String) = AreaSelect(title, title, false)

        return listOf(
            buildAreaSelect("一教"),
            buildAreaSelect("二教"),
            buildAreaSelect("三教"),
            buildAreaSelect("四教"),
            buildAreaSelect("五教"),
            buildAreaSelect("六教"),
            buildAreaSelect("八教"),
            buildAreaSelect("艺术大楼"),
            buildAreaSelect("彭州校区"),
            buildAreaSelect("人南校区"),
            buildAreaSelect("宜宾"),
        )
    }

    override fun valueId(value: AreaSelect): String = value.value

    override fun updateSelect(
        t: AreaSelect,
        selected: Boolean
    ): AreaSelect = t.copy(selected = selected)
}

data class IntSelect(
    val value: Int,
    override val title: String,
    override val selected: Boolean,
) : Selectable

class IntSelectDataLoader : SelectDataLoader<IntSelect, Int>() {
    override suspend fun initSelect(): List<IntSelect> = emptyList()

    fun customInit(
        start: Int,
        end: Int,
        title: (Int) -> String,
    ) {
        select.value = (start..end).map { IntSelect(it, title(it), false) }
    }

    override fun valueId(value: IntSelect): Int = value.value

    override fun updateSelect(
        t: IntSelect,
        selected: Boolean
    ): IntSelect = t.copy(selected = selected)
}