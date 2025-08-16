package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vip.mystery0.xhu.timetable.base.PageRequest
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.UserSelectDataLoader
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.CustomThingRepo
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

class CustomThingViewModel : PagingComposeViewModel<PageRequest, CustomThingResponse>(
    {
        CustomThingRepo.getCustomThingListStream(it.user)
    }
) {
    val userSelect = UserSelectDataLoader()

    private val _saveLoadingState = MutableStateFlow(LoadingState(init = true))
    val saveLoadingState: StateFlow<LoadingState> = _saveLoadingState

    fun init() {
        viewModelScope.safeLaunch {
            userSelect.init()
            loadCustomThingList()
        }
    }

    fun loadCustomThingList() {
        fun failed(message: String) {
            logger.w("loadCustomThingList failed: $message")
            toastMessage(message)
        }

        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load custom thing list failed", throwable)
            failed(throwable.desc())
        }) {
            val selectedUser = userSelect.getSelectedUser()
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@safeLaunch
            }
            loadData(PageRequest(selectedUser, 1, 1))
        }
    }

    fun saveCustomThing(
        thingId: Long?,
        request: CustomThingRequest,
        saveAsCountdown: Boolean,
    ) {
        fun failed(message: String) {
            logger.w("saveCustomThing failed: $message")
            toastMessage(message)
            _saveLoadingState.value = LoadingState(actionSuccess = false)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("save custom thing failed", throwable)
            failed(throwable.desc())
        }) {
            _saveLoadingState.value = LoadingState(loading = true)
            if (saveAsCountdown) {
                //存储为倒计时，那么持续时间为一天
                request.endTime = Instant.fromEpochMilliseconds(request.startTime)
                    .plus(1.toDuration(DurationUnit.DAYS))
                    .toEpochMilliseconds()

            }
            if (request.startTime > request.endTime) {
                failed("开始时间不能晚于结束时间")
                return@safeLaunch
            }

            val selectedUser = userSelect.getSelectedUser()
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@safeLaunch
            }
            if (thingId == null || thingId == 0L) {
                CustomThingRepo.createCustomThing(selectedUser, request)
            } else {
                CustomThingRepo.updateCustomThing(selectedUser, thingId, request)
            }
            _saveLoadingState.value = LoadingState()
            toastMessage("《${request.title}》保存成功")
            updateChange()
        }
    }

    fun deleteCustomThing(thingId: Long) {
        fun failed(message: String) {
            logger.w("deleteCustomThing failed: $message")
            toastMessage(message)
            _saveLoadingState.value = LoadingState(actionSuccess = false)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("delete custom thing failed", throwable)
            failed(throwable.desc())
        }) {
            _saveLoadingState.value = LoadingState(loading = true)

            val selectedUser = userSelect.getSelectedUser()
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@safeLaunch
            }

            CustomThingRepo.deleteCustomThing(selectedUser, thingId)
            _saveLoadingState.value = LoadingState()
            toastMessage("删除成功")
            updateChange()
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.safeLaunch {
            userSelect.setSelected(studentId)
        }
    }

    private fun updateChange() {
        viewModelScope.safeLaunch {
            EventBus.post(EventType.CHANGE_SHOW_CUSTOM_THING)
        }
        loadCustomThingList()
    }

    data class LoadingState(
        val init: Boolean = false,
        val loading: Boolean = false,
        val actionSuccess: Boolean = true,
    )
}