package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.Selectable
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.repository.ClassroomRepo
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

class CourseRoomViewModel : PagingComposeViewModel<ClassroomRequest,ClassroomResponse>(
    {
        ClassroomRepo.getClassroomListStream(it)
    }
) {
    companion object {
        private const val TAG = "CourseRoomViewModel"
    }

    private val _areaSelect = MutableStateFlow<List<AreaSelect>>(emptyList())
    val areaSelect: StateFlow<List<AreaSelect>> = _areaSelect
    private val _weekSelect = MutableStateFlow<List<IntSelect>>(emptyList())
    val weekSelect: StateFlow<List<IntSelect>> = _weekSelect
    private val _daySelect = MutableStateFlow<List<IntSelect>>(emptyList())
    val daySelect: StateFlow<List<IntSelect>> = _daySelect
    private val _timeSelect = MutableStateFlow<List<IntSelect>>(emptyList())
    val timeSelect: StateFlow<List<IntSelect>> = _timeSelect

    val init: Boolean
        get() = pageRequestFlow.value == null

    init {
        viewModelScope.launch {
            _areaSelect.value = initAreaSelect()
            _weekSelect.value = initIntSelect(1, 20) { "第${it}周" }
            _daySelect.value = initIntSelect(1, 7) {
                DayOfWeek.of(it).getDisplayName(TextStyle.SHORT, Locale.CHINA)
            }
            _timeSelect.value = initIntSelect(1, 12) { "第${it}节" }
        }
    }

    fun changeArea(area: String) {
        viewModelScope.launch {
            val selected = getSelected(_areaSelect.value)
            if (selected?.value == area) {
                return@launch
            }
            _areaSelect.value = _areaSelect.value.map {
                it.copy(selected = it.value == area)
            }
        }
    }

    fun changeWeek(week: List<Int>) {
        viewModelScope.launch {
            _weekSelect.value = _weekSelect.value.map {
                it.copy(selected = week.contains(it.value))
            }
        }
    }

    fun changeDay(day: List<Int>) {
        viewModelScope.launch {
            _daySelect.value = _daySelect.value.map {
                it.copy(selected = day.contains(it.value))
            }
        }
    }

    fun changeTime(time: List<Int>) {
        viewModelScope.launch {
            _timeSelect.value = _timeSelect.value.map {
                it.copy(selected = time.contains(it.value))
            }
        }
    }

    fun search() {
        fun failed(message: String) {
            Log.w(TAG, "search failed: $message")
            toastMessage(message)
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "search failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            val area = getSelected(_areaSelect.value)?.value ?: ""
            val week = _weekSelect.value.filter { it.selected }.map { it.value }
            val day = _daySelect.value.filter { it.selected }.map { it.value }
            val time = _timeSelect.value.filter { it.selected }.map { it.value }
            val request = ClassroomRequest(area, week, day, time)
            pageRequestFlow.emit(request)
        }
    }

    private fun initAreaSelect(selectedArea: String = ""): List<AreaSelect> {
        fun buildAreaSelect(title: String): AreaSelect {
            return AreaSelect(title, title, title == selectedArea)
        }

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

    private fun initIntSelect(
        start: Int,
        end: Int,
        title: (Int) -> String,
    ): List<IntSelect> {
        return (start..end).map {
            IntSelect(it, title(it), false)
        }
    }
}

data class AreaSelect(
    val value: String,
    override val title: String,
    override val selected: Boolean,
) : Selectable

data class IntSelect(
    val value: Int,
    override val title: String,
    override val selected: Boolean,
) : Selectable