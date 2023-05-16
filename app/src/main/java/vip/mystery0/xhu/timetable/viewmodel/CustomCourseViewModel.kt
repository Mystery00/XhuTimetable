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
import vip.mystery0.xhu.timetable.base.PageRequest
import vip.mystery0.xhu.timetable.base.PagingComposeViewModel
import vip.mystery0.xhu.timetable.base.TermSelect
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.base.YearSelect
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.request.AllCourseRequest
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.response.AllCourseResponse
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.repository.CustomCourseRepo
import java.time.DayOfWeek

@OptIn(ExperimentalCoroutinesApi::class)
class CustomCourseViewModel : PagingComposeViewModel<PageRequest,CustomCourseResponse>(
    {
        CustomCourseRepo.getCustomCourseListStream(it.user, it.year, it.term)
    }
) {
    companion object {
        private const val TAG = "CustomCourseViewModel"
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect
    private val _yearSelect = MutableStateFlow<List<YearSelect>>(emptyList())
    val yearSelect: StateFlow<List<YearSelect>> = _yearSelect
    private val _termSelect = MutableStateFlow<List<TermSelect>>(emptyList())
    val termSelect: StateFlow<List<TermSelect>> = _termSelect

    //蹭课列表分页数据
    private val allCoursePageRequestFlow = MutableStateFlow<AllCoursePageRequest?>(null)
    private val _allCoursePageState = allCoursePageRequestFlow
        .flatMapLatest {
            if (it == null) return@flatMapLatest flowOf(PagingData.empty())
            val request = AllCourseRequest(
                courseName = it.courseName,
                teacherName = it.teacherName,
                courseIndex = it.courseIndex,
                day = it.day?.value,
            )
            CourseRepo.getAllCourseListStream(it.user, it.year, it.term, request)
        }.cachedIn(viewModelScope)
    val allCoursePageState: Flow<PagingData<AllCourseResponse>> = _allCoursePageState

    var changeCustomCourse = false

    private val _saveLoadingState = MutableStateFlow(LoadingState())
    val saveLoadingState: StateFlow<LoadingState> = _saveLoadingState

    init {
        viewModelScope.launch {
            _userSelect.value = initUserSelect()
            _yearSelect.value = initYearSelect()
            _termSelect.value = initTermSelect()
            loadCustomCourseList()
        }
    }

    fun loadCustomCourseList() {
        fun failed(message: String) {
            Log.w(TAG, "loadCustomCourseList failed: $message")
            toastMessage(message)
        }

        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load custom course list failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            val year = getSelectedYear(_yearSelect.value)
            val term = getSelectedTerm(_termSelect.value)
            loadData(PageRequest(selectedUser, year, term))
        }
    }

    fun loadSearchCourseList(
        courseName: String?,
        teacherName: String?,
        courseIndex: Int?,
        day: DayOfWeek?
    ) {
        fun failed(message: String) {
            Log.w(TAG, "loadSearchCourseList failed: $message")
            toastMessage(message)
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load search course list failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            val year = getSelectedYear(_yearSelect.value)
            val term = getSelectedTerm(_termSelect.value)
            allCoursePageRequestFlow.emit(
                AllCoursePageRequest(
                    selectedUser,
                    year,
                    term,
                    courseName,
                    teacherName,
                    courseIndex,
                    day,
                )
            )
        }
    }

    fun saveCustomCourse(
        courseId: Long?,
        request: CustomCourseRequest,
    ) {
        fun failed(message: String) {
            Log.w(TAG, "saveCustomCourse failed: $message")
            toastMessage(message)
            _saveLoadingState.value = LoadingState()
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "save custom course failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _saveLoadingState.value = LoadingState(loading = true)

            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }
            val year = getSelectedYear(_yearSelect.value)
            val term = getSelectedTerm(_termSelect.value)
            request.year = year
            request.term = term
            if (courseId == null || courseId == 0L) {
                CustomCourseRepo.createCustomCourse(selectedUser, request)
            } else {
                CustomCourseRepo.updateCustomCourse(selectedUser, courseId, request)
            }
            _saveLoadingState.value = LoadingState()
            toastMessage("《${request.courseName}》保存成功")
            changeCustomCourse = true
            loadCustomCourseList()
        }
    }

    fun deleteCustomCourse(courseId: Long) {
        fun failed(message: String) {
            Log.w(TAG, "deleteCustomCourse failed: $message")
            toastMessage(message)
            _saveLoadingState.value = LoadingState()
        }
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "delete custom course failed", throwable)
            failed(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _saveLoadingState.value = LoadingState(loading = true)

            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                failed("选择用户为空，请重新选择")
                return@launch
            }

            CustomCourseRepo.deleteCustomCourse(selectedUser, courseId)
            _saveLoadingState.value = LoadingState()
            toastMessage("删除成功")
            changeCustomCourse = true
            loadCustomCourseList()
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            _userSelect.value = setSelectedUser(_userSelect.value, studentId).first
        }
    }

    fun selectYear(year: Int) {
        viewModelScope.launch {
            _yearSelect.value = setSelectedYear(_yearSelect.value, year)
        }
    }

    fun selectTerm(term: Int) {
        viewModelScope.launch {
            _termSelect.value = setSelectedTerm(_termSelect.value, term)
        }
    }

    internal data class AllCoursePageRequest(
        val user: User,
        val year: Int,
        val term: Int,
        val courseName: String?,
        val teacherName: String?,
        val courseIndex: Int?,
        val day: DayOfWeek?,
    )

    data class LoadingState(
        val init: Boolean = false,
        val loading: Boolean = false,
        val actionSuccess: Boolean = true,
    )
}