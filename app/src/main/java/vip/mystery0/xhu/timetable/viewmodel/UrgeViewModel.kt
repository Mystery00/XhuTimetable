package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.model.response.UrgeItem
import vip.mystery0.xhu.timetable.repository.doUrge
import vip.mystery0.xhu.timetable.repository.getUrgeList
import vip.mystery0.xhu.timetable.trackEvent
import java.time.format.DateTimeFormatter

class UrgeViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "UrgeViewModel"
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    private val _urgeListState = MutableStateFlow(UrgeListState())
    val urgeListState: StateFlow<UrgeListState> = _urgeListState

    private val _urgeLoading = MutableStateFlow(false)
    val urgeLoading: StateFlow<Boolean> = _urgeLoading

    init {
        loadUrgeList()
    }

    fun loadUrgeList() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load urge list failed", throwable)
            _urgeListState.value =
                UrgeListState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _urgeListState.value = UrgeListState(loading = true)
            val user = UserStore.mainUser()
            val response = getUrgeList(user)
            val urgeList = response.urgeList.filter { !it.complete }.reversed()
            val completeList = response.urgeList.filter { it.complete }.reversed()
            _urgeListState.value =
                UrgeListState(
                    urgeList = ArrayList<UrgeItem>().apply {
                        addAll(urgeList)
                        addAll(completeList)
                    },
                    remainCount = response.remainCount
                )
        }
    }

    fun urge(urgeId: Long) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "urge failed", throwable)
            _urgeLoading.value = false
            _urgeListState.value =
                UrgeListState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _urgeLoading.value = true
            trackEvent("催更")
            val user = UserStore.mainUser()
            doUrge(user, urgeId)
            _urgeLoading.value = false
            loadUrgeList()
        }
    }
}

data class UrgeListState(
    val loading: Boolean = false,
    val urgeList: List<UrgeItem> = emptyList(),
    val completeList: List<UrgeItem> = emptyList(),
    val remainCount: Int = 0,
    val errorMessage: String = "",
)