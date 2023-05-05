package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.Selectable
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.repository.ClassroomRepo
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class CourseRoomViewModel : ComposeViewModel() {
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

    // 空闲教室分页数据
    private val pageRequestFlow = MutableStateFlow<ClassroomRequest?>(null)
    private val _pageState = pageRequestFlow
        .flatMapLatest {
            if (it == null) return@flatMapLatest flowOf(PagingData.empty())
            ClassroomRepo.getClassroomListStream(it)
        }.cachedIn(viewModelScope)
    val pageState: Flow<PagingData<ClassroomResponse>> = _pageState
    val init: Boolean
        get() = pageRequestFlow.value == null

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

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

    private fun toastMessage(message: String) {
        _errorMessage.value = System.currentTimeMillis() to message
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