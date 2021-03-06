package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.repository.getCourseRoomList

class CourseRoomViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "CourseRoomViewModel"
    }

    private val _areaSelect = MutableStateFlow(buildAreaSelect(""))
    val areaSelect: StateFlow<List<AreaSelect>> = _areaSelect
    private val _week = MutableStateFlow<List<Int>>(emptyList())
    val week: StateFlow<List<Int>> = _week
    private val _day = MutableStateFlow<List<Int>>(emptyList())
    val day: StateFlow<List<Int>> = _day
    private val _time = MutableStateFlow<List<Int>>(emptyList())
    val time: StateFlow<List<Int>> = _time

    private val _courseRoomListState = MutableStateFlow(CourseRoomListState())
    val courseRoomListState: StateFlow<CourseRoomListState> = _courseRoomListState

    fun changeArea(area: String) {
        viewModelScope.launch {
            if (_areaSelect.value.first { it.selected }.area == area) {
                return@launch
            }
            _areaSelect.value = buildAreaSelect(area)
        }
    }

    fun changeWeek(week: List<Int>) {
        viewModelScope.launch {
            _week.value = week
        }
    }

    fun changeDay(day: List<Int>) {
        viewModelScope.launch {
            _day.value = day
        }
    }

    fun changeTime(time: List<Int>) {
        viewModelScope.launch {
            _time.value = time
        }
    }

    fun search() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "search course room failed", throwable)
            _courseRoomListState.value = CourseRoomListState(
                loading = false,
                errorMessage = throwable.message ?: throwable.javaClass.simpleName
            )
        }) {
            _courseRoomListState.value = CourseRoomListState(loading = true)
            val mainUser = SessionManager.mainUserOrNull()
            if (mainUser == null) {
                _courseRoomListState.value = CourseRoomListState(
                    loading = false,
                    errorMessage = "???????????????"
                )
                return@launch
            }
            getCourseRoomList(
                mainUser,
                _areaSelect.value.first { it.selected }.area,
                _week.value,
                _day.value,
                _time.value
            ).map {
                CourseRoom(
                    it.no,
                    it.name,
                    it.seat,
                    it.region,
                    it.type,
                )
            }.let {
                _courseRoomListState.value = CourseRoomListState(
                    courseRoomList = it,
                    loading = false,
                    errorMessage = if (it.isEmpty()) "????????????" else ""
                )
            }
        }
    }

    private fun buildAreaSelect(selectedArea: String): List<AreaSelect> {
        val list = listOf(
            AreaSelect("??????").check(selectedArea),
            AreaSelect("??????").check(selectedArea),
            AreaSelect("??????").check(selectedArea),
            AreaSelect("??????").check(selectedArea),
            AreaSelect("??????").check(selectedArea),
            AreaSelect("??????").check(selectedArea),
            AreaSelect("??????").check(selectedArea),
            AreaSelect("????????????").check(selectedArea),
            AreaSelect("????????????").check(selectedArea),
            AreaSelect("????????????").check(selectedArea),
            AreaSelect("??????").check(selectedArea),
        )
        if (selectedArea == "") {
            list.first().selected = true
        }
        return list
    }
}

data class AreaSelect(
    val area: String,
    var selected: Boolean = false,
) {
    fun check(selectedArea: String): AreaSelect {
        selected = selectedArea == area
        return this
    }
}

data class CourseRoomListState(
    val loading: Boolean = false,
    val courseRoomList: List<CourseRoom> = emptyList(),
    val errorMessage: String = "",
)

data class CourseRoom(
    var roomNo: String,
    var roomName: String,
    var seat: String,
    var region: String,
    var type: String,
) {
    companion object {
        val PLACEHOLDER =
            CourseRoom("????????????", "????????????", "?????????", "????????????", "????????????")
    }
}