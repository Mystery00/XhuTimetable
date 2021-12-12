package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.repository.getSchoolCalendarList
import vip.mystery0.xhu.timetable.repository.getSchoolCalendarUrl

class SchoolCalendarViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "SchoolCalendarViewModel"
    }

    private val _loading = MutableStateFlow(LoadingState())
    val loading: StateFlow<LoadingState> = _loading

    private val _area = MutableStateFlow<List<Area>>(emptyList())
    val area: StateFlow<List<Area>> = _area

    private val _loadImageUrl = MutableStateFlow(Pair("", ""))
    val loadImageUrl: StateFlow<Pair<String, String>> = _loadImageUrl

    init {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "changeArea failed", throwable)
            _loading.value = LoadingState(
                loading = false,
                errorMessage = throwable.message ?: throwable.javaClass.simpleName
            )
        }) {
            _loading.value = LoadingState(true)
            val mainUser = SessionManager.mainUserOrNull()
            if (mainUser == null) {
                _loading.value = LoadingState(loading = false, errorMessage = "用户未登录")
                return@launch
            }
            getSchoolCalendarList(mainUser).map {
                Area(it.area, it.resourceId)
            }.let {
                _area.value = it
                _loading.value = LoadingState(loading = false)
            }
            changeArea(_area.value[0].area)
        }
    }

    fun changeArea(area: String) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "changeArea failed", throwable)
            _loading.value = LoadingState(
                loading = false,
                errorMessage = throwable.message ?: throwable.javaClass.simpleName
            )
        }) {
            _loading.value = LoadingState(true)
            val mainUser = SessionManager.mainUserOrNull()
            if (mainUser == null) {
                _loading.value = LoadingState(loading = false, errorMessage = "用户未登录")
                return@launch
            }
            val pair = _area.value.firstOrNull { it.area == area }
            if (pair == null) {
                _loading.value = LoadingState(loading = false, errorMessage = "无效的校区")
                return@launch
            }
            if (pair.url.isNotBlank()) {
                _loadImageUrl.value = area to pair.url
                _loading.value = LoadingState(false)
                return@launch
            }
            getSchoolCalendarUrl(mainUser, pair.resourceId).let {
                pair.url = it
                _loadImageUrl.value = area to it
            }
            _loading.value = LoadingState(false)
        }
    }
}

data class Area(
    val area: String,
    val resourceId: Long,
    var url: String = "",
)