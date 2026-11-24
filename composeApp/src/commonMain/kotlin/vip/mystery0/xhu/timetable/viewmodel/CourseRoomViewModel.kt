package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.SelectDataLoader
import vip.mystery0.xhu.timetable.base.Selectable
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.ClassroomRepo
import vip.mystery0.xhu.timetable.repository.WidgetRepo
import vip.mystery0.xhu.timetable.utils.formatChina
import vip.mystery0.xhu.timetable.utils.now
import kotlin.time.Clock

data class ClassroomSearchParam(
    val request: ClassroomRequest,
    val requestTime: Long = Clock.System.now().toEpochMilliseconds(),
)

class CourseRoomViewModel : PagingComposeViewModel<ClassroomSearchParam, ClassroomResponse>(
    { param ->
        ClassroomRepo.getClassroomListStream(param.request)
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
            if (areaSelect.select.value.isNotEmpty()) return@safeLaunch
            areaSelect.init()
            weekSelect.customInit(1, 20) { "第${it}周" }
            daySelect.customInit(1, 7) { DayOfWeek(it).formatChina() }
            timeSelect.customInit(1, 11) { "第${it}节" }

            // 默认填充当前周、今天星期与当前/下一时段节次，避免首次查询因必选参数为空报错
            applyTodayDefault()
        }
    }

    fun selectTodayDefault() {
        viewModelScope.safeLaunch {
            applyTodayDefault()
        }
    }

    private suspend fun applyTodayDefault() {
        val currentWeek = WidgetRepo.calculateWeek().coerceIn(1, 20)
        val todayDay = LocalDate.now().dayOfWeek.isoDayNumber
        val defaultPeriods = suggestCurrentPeriods(LocalTime.now())
        weekSelect.multiSetSelected(listOf(currentWeek))
        daySelect.multiSetSelected(listOf(todayDay))
        timeSelect.multiSetSelected(defaultPeriods)
    }

    private fun suggestCurrentPeriods(now: LocalTime): List<Int> = when {
        now < LocalTime(10, 5) -> listOf(1, 2)
        now < LocalTime(12, 0) -> listOf(3, 4)
        now < LocalTime(15, 35) -> listOf(5, 6)
        now < LocalTime(17, 30) -> listOf(7, 8)
        else -> listOf(9, 10)
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

    fun search(): Boolean {
        val area = areaSelect.getSelectedSnapshot()?.value ?: ""
        val week = weekSelect.getMultiSelectedSnapshot().map { it.value }
        val day = daySelect.getMultiSelectedSnapshot().map { it.value }
        val time = timeSelect.getMultiSelectedSnapshot().map { it.value }

        when {
            week.isEmpty() -> {
                toastMessage("请至少选择一个查询周次")
                return false
            }

            day.isEmpty() -> {
                toastMessage("请至少选择一个查询星期")
                return false
            }

            time.isEmpty() -> {
                toastMessage("请至少选择一个查询节次")
                return false
            }
        }

        fun failed(message: String) {
            logger.w("search failed: $message")
            toastMessage(message)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("search failed", throwable)
            failed(throwable.desc())
        }) {
            val request = ClassroomRequest(area, week, day, time)
            loadData(ClassroomSearchParam(request))
        }
        return true
    }
}

@Immutable
data class AreaSelect(
    val value: String,
    override val title: String,
    override val selected: Boolean,
) : Selectable

class AreaSelectDataLoader : SelectDataLoader<AreaSelect, String>() {
    override suspend fun initSelect(): List<AreaSelect> {
        fun buildAreaSelect(title: String, value: String = title, selected: Boolean = false) =
            AreaSelect(value, title, selected)

        return listOf(
            buildAreaSelect("全部区域", value = "", selected = true),
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

    fun getSelectedSnapshot(): AreaSelect? = select.value.firstOrNull { it.selected }

    override fun valueId(value: AreaSelect): String = value.value

    override fun updateSelect(
        t: AreaSelect,
        selected: Boolean
    ): AreaSelect = t.copy(selected = selected)
}

@Immutable
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

    fun getMultiSelectedSnapshot(): List<IntSelect> = select.value.filter { it.selected }

    override fun valueId(value: IntSelect): Int = value.value

    override fun updateSelect(
        t: IntSelect,
        selected: Boolean
    ): IntSelect = t.copy(selected = selected)
}
