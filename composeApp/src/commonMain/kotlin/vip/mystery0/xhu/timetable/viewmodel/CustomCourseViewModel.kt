package vip.mystery0.xhu.timetable.viewmodel

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import vip.mystery0.xhu.timetable.base.PageRequest
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.TermSelectDataLoader
import vip.mystery0.xhu.timetable.base.UserSelectDataLoader
import vip.mystery0.xhu.timetable.base.YearSelectDataLoader
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.CustomCourseRepo

class CustomCourseViewModel : PagingComposeViewModel<PageRequest, CustomCourseResponse>(
    {
        CustomCourseRepo.getCustomCourseListStream(it.user, it.year, it.term)
    }
) {
    val userSelect = UserSelectDataLoader()
    val yearSelect = YearSelectDataLoader()
    val termSelect = TermSelectDataLoader()

    private val _saveLoadingState = MutableStateFlow(LoadingState())
    val saveLoadingState: StateFlow<LoadingState> = _saveLoadingState

    fun init() {
        viewModelScope.safeLaunch {
            userSelect.init()
            yearSelect.init()
            termSelect.init()
            loadCustomCourseList()
        }
    }

    fun loadCustomCourseList() {
        fun failed(message: String) {
            logger.w("loadCustomCourseList failed: $message")
            toastMessage(message)
        }

        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load custom course list failed", throwable)
            failed(throwable.desc())
        }) {
            val selectedUser = userSelect.getSelectedUser()
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@safeLaunch
            }
            val year = yearSelect.getSelectedYear()
            val term = termSelect.getSelectedTerm()
            loadData(PageRequest(selectedUser, year, term))
        }
    }

    fun saveCustomCourse(
        courseId: Long?,
        request: CustomCourseRequest,
    ) {
        fun failed(message: String) {
            logger.w("saveCustomCourse failed: $message")
            toastMessage(message)
            _saveLoadingState.value = LoadingState(actionSuccess = false)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("save custom course failed", throwable)
            failed(throwable.desc())
        }) {
            _saveLoadingState.value = LoadingState(loading = true, actionSuccess = false)

            val selectedUser = userSelect.getSelectedUser()
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@safeLaunch
            }
            val year = yearSelect.getSelectedYear()
            val term = termSelect.getSelectedTerm()
            if (request.startDayTime > request.endDayTime) {
                failed("开始节次不能大于结束节次")
                return@safeLaunch
            }
            request.year = year
            request.term = term
            if (courseId == null || courseId == 0L) {
                CustomCourseRepo.createCustomCourse(selectedUser, request)
            } else {
                CustomCourseRepo.updateCustomCourse(selectedUser, courseId, request)
            }
            _saveLoadingState.value = LoadingState(actionSuccess = true)
            toastMessage("《${request.courseName}》保存成功")
            updateChange()
        }
    }

    fun deleteCustomCourse(courseId: Long) {
        fun failed(message: String) {
            logger.w("deleteCustomCourse failed: $message")
            toastMessage(message)
            _saveLoadingState.value = LoadingState(actionSuccess = false)
        }
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("delete custom course failed", throwable)
            failed(throwable.desc())
        }) {
            _saveLoadingState.value = LoadingState(loading = true, actionSuccess = false)

            val selectedUser = userSelect.getSelectedUser()
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@safeLaunch
            }

            CustomCourseRepo.deleteCustomCourse(selectedUser, courseId)
            _saveLoadingState.value = LoadingState(actionSuccess = true)
            toastMessage("删除成功")
            updateChange()
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.safeLaunch {
            userSelect.setSelected(studentId)
        }
    }

    fun selectYear(year: Int) {
        viewModelScope.safeLaunch {
            yearSelect.setSelected(year)
        }
    }

    fun selectTerm(term: Int) {
        viewModelScope.safeLaunch {
            termSelect.setSelected(term)
        }
    }

    private fun updateChange() {
        viewModelScope.safeLaunch {
            EventBus.post(EventType.CHANGE_SHOW_CUSTOM_COURSE)
        }
        loadCustomCourseList()
    }

    data class LoadingState(
        val init: Boolean = false,
        val loading: Boolean = false,
        val actionSuccess: Boolean = true,
    )
}