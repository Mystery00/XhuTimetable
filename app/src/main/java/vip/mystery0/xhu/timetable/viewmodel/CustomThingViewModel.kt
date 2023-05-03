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
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.repository.CustomThingRepo
import java.time.Instant
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalCoroutinesApi::class)
class CustomThingViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "CustomThingViewModel"
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect

    // 自定义课程分页数据
    private val pageRequestFlow = MutableStateFlow<PageRequest?>(null)
    private val _pageState = pageRequestFlow
        .flatMapLatest {
            if (it == null) return@flatMapLatest flowOf(PagingData.empty())
            CustomThingRepo.getCustomThingListStream(it.user)
        }.cachedIn(viewModelScope)
    val pageState: Flow<PagingData<CustomThingResponse>> = _pageState

    var changeCustomThing = false

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

    private val _saveLoadingState = MutableStateFlow(LoadingState(init = true))
    val saveLoadingState: StateFlow<LoadingState> = _saveLoadingState

    init {
        viewModelScope.launch {
            _userSelect.value = initUserSelect()
            loadCustomThingList()
        }
    }

    private fun toastMessage(message: String) {
        _errorMessage.value = System.currentTimeMillis() to message
    }

    fun loadCustomThingList() {
        fun failed(message: String) {
            Log.w(TAG, "loadCustomThingList failed: $message")
            toastMessage(message)
        }

        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load custom thing list failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            pageRequestFlow.emit(PageRequest(selectedUser))
        }
    }

    fun saveCustomThing(
        thingId: Long?,
        request: CustomThingRequest,
        saveAsCountdown: Boolean,
    ) {
        fun failed(message: String) {
            Log.w(TAG, "saveCustomThing failed: $message")
            toastMessage(message)
            _saveLoadingState.value = LoadingState(actionSuccess = false)
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "save custom thing failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _saveLoadingState.value = LoadingState(loading = true)
            if (saveAsCountdown) {
                //存储为倒计时，那么持续时间为一天
                request.endTime =
                    Instant.ofEpochMilli(request.startTime).plus(1, ChronoUnit.DAYS).toEpochMilli()
            }
            if (request.startTime > request.endTime) {
                failed("开始时间不能晚于结束时间")
                return@launch
            }

            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            if (thingId == null || thingId == 0L) {
                CustomThingRepo.createCustomThing(selectedUser, request)
            } else {
                CustomThingRepo.updateCustomThing(selectedUser, thingId, request)
            }
            _saveLoadingState.value = LoadingState()
            toastMessage("《${request.title}》保存成功")
            changeCustomThing = true
            loadCustomThingList()
        }
    }

    fun deleteCustomThing(thingId: Long) {
        fun failed(message: String) {
            Log.w(TAG, "deleteCustomThing failed: $message")
            toastMessage(message)
            _saveLoadingState.value = LoadingState(actionSuccess = false)
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "delete custom thing failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _saveLoadingState.value = LoadingState(loading = true)

            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }

            CustomThingRepo.deleteCustomThing(selectedUser, thingId)
            _saveLoadingState.value = LoadingState()
            toastMessage("删除成功")
            changeCustomThing = true
            loadCustomThingList()
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            _userSelect.value = setSelectedUser(_userSelect.value, studentId).first
        }
    }

    internal data class PageRequest(
        val user: User,
        val requestTime: Long = System.currentTimeMillis(),
    )

    data class LoadingState(
        val init: Boolean = false,
        val loading: Boolean = false,
        val actionSuccess: Boolean = true,
    )
}