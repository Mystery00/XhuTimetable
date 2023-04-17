package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
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
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.base.YearSelect
import vip.mystery0.xhu.timetable.base.TermSelect
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLogin
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.model.CustomCourse
import vip.mystery0.xhu.timetable.model.request.AllCourseRequest
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.repository.CustomCoursePageSource
import vip.mystery0.xhu.timetable.repository.CustomCourseRepo
import vip.mystery0.xhu.timetable.repository.createCustomCourse
import vip.mystery0.xhu.timetable.repository.deleteCustomCourse
import vip.mystery0.xhu.timetable.repository.getCustomCourseList
import vip.mystery0.xhu.timetable.repository.updateCustomCourse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month

class CustomCourseViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "CustomCourseViewModel"
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect
    private val _yearSelect = MutableStateFlow<List<YearSelect>>(emptyList())
    val yearSelect: StateFlow<List<YearSelect>> = _yearSelect
    private val _termSelect = MutableStateFlow<List<TermSelect>>(emptyList())
    val termSelect: StateFlow<List<TermSelect>> = _termSelect

    private val pageRequestFlow = MutableStateFlow<PageRequest?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _pageState = pageRequestFlow
        .flatMapLatest {
            if (it == null) return@flatMapLatest flowOf(PagingData.empty())
            CustomCourseRepo.getCustomCourseListStream(it.user, it.year, it.term)
        }.cachedIn(viewModelScope)
    val pageState: Flow<PagingData<CustomCourseResponse>> = _pageState

    private val serverApi: ServerApi by inject()

    var changeCustomCourse = false

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

    private val _customCourseListState = MutableStateFlow(CustomCourseListState())
    val customCourseListState: StateFlow<CustomCourseListState> = _customCourseListState

    private val _saveCustomCourseState = MutableStateFlow(SaveCustomCourseState())
    val saveCustomCourseState: StateFlow<SaveCustomCourseState> = _saveCustomCourseState

    private val _searchCourseListState = MutableStateFlow(SearchCourseListState())
    val searchCourseListState: StateFlow<SearchCourseListState> = _searchCourseListState

    init {
        viewModelScope.launch {
            _userSelect.value = initUserSelect()
            _yearSelect.value = initYearSelect()
            _termSelect.value = initTermSelect()
        }
    }

    private fun toastMessage(message: String) {
        _errorMessage.value = System.currentTimeMillis() to message
    }

    fun loadCustomCourseList() {
        viewModelScope.launch {
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                Log.w(TAG, "loadScoreList: empty selected user")
                _customCourseListState.value =
                    CustomCourseListState(errorMessage = "选择用户为空，请重新选择")
                return@launch
            }
            val year = getSelectedYear(_yearSelect.value)
            val term = getSelectedTerm(_termSelect.value)
            pageRequestFlow.emit(PageRequest(selectedUser, year, term))
        }
    }

    fun loadSearchCourseList(
        courseName: String?,
        teacherName: String?,
        courseIndex: Int?,
        day: Int?
    ) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load search course list failed", throwable)
            _searchCourseListState.value = SearchCourseListState()
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
//            _searchCourseListState.value = SearchCourseListState(loading = true)
//            val currentYear = getConfig { currentYear }
//            val currentTerm = getConfig { currentTerm }
//            val request = AllCourseRequest(
//                currentYear,
//                currentTerm,
//                courseName,
//                teacherName,
//                courseIndex,
//                day,
//            )
//            val list = UserStore.mainUser().withAutoLogin {
//                serverApi.selectAllCourse(it, request).checkLogin()
//            }.first
//            val result = list.map {
//                SearchCourse(
//                    it.name,
//                    it.teacher,
//                    it.location,
//                    it.weekString,
//                    it.week,
//                    it.time,
//                    it.day,
//                )
//            }
//            _searchCourseListState.value = SearchCourseListState(
//                searchCourseList = result,
//                loading = false
//            )
        }
    }

    fun saveCustomCourse(
        courseId: Long,
        courseName: String,
        teacherName: String,
        week: List<Int>,
        location: String,
        courseIndex: List<Int>,
        day: Int,
    ) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "save custom course failed", throwable)
            _saveCustomCourseState.value =
                SaveCustomCourseState()
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
//            _saveCustomCourseState.value = SaveCustomCourseState(loading = true)
//
//            val customCourse = CustomCourse(
//                courseId,
//                courseName,
//                teacherName,
//                "",
//                week,
//                location,
//                courseIndex,
//                day,
//                "",
//            )
//            if (courseId == 0L) {
//                createCustomCourse(currentUser, currentYear, currentTerm, customCourse)
//            } else {
//                updateCustomCourse(currentUser, currentYear, currentTerm, customCourse)
//            }
//            _saveCustomCourseState.value = SaveCustomCourseState()
//            toastMessage("《$courseName》保存成功")
//            changeCustomCourse = true
//            loadCustomCourseList()
        }
    }

    fun delete(courseId: Long) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "delete custom course failed", throwable)
            _saveCustomCourseState.value =
                SaveCustomCourseState()
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
//            _saveCustomCourseState.value = SaveCustomCourseState(loading = true)
//
//            deleteCustomCourse(currentUser, courseId)
//            _saveCustomCourseState.value = SaveCustomCourseState()
//            toastMessage("删除成功")
//            changeCustomCourse = true
//            loadCustomCourseList()
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
}

data class PageRequest(
    val user: User,
    val year: Int,
    val term: Int,
)

data class CustomCourseListState(
    val loading: Boolean = false,
    val customCourseList: List<CustomCourse> = emptyList(),
    val errorMessage: String = "",
)

data class SaveCustomCourseState(
    val loading: Boolean = false,
)

data class SearchCourseListState(
    val loading: Boolean = false,
    val searchCourseList: List<SearchCourse> = emptyList(),
)

data class SearchCourse(
    var name: String,
    var teacher: String,
    var location: String,
    var weekString: String,
    var week: List<Int>,
    var time: List<Int>,
    var day: Int,
) {
    companion object {
        val PLACEHOLDER =
            SearchCourse("课程名称", "教师名称", "上课地点", "第1周", listOf(1), listOf(1, 1), 1)
        val EMPTY =
            SearchCourse(
                "",
                "",
                "",
                "",
                listOf(),
                listOf(1, 1),
                LocalDate.now().dayOfWeek.value,
            )
    }
}