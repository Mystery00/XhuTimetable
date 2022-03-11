package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.*
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.model.CustomCourse
import vip.mystery0.xhu.timetable.model.request.AllCourseRequest
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

    private val serverApi: ServerApi by inject()

    var changeCustomCourse = false

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

    private val _init = MutableStateFlow(false)
    val init: StateFlow<Boolean> = _init

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect
    private val _yearSelect = MutableStateFlow<List<YearSelect>>(emptyList())
    val yearSelect: StateFlow<List<YearSelect>> = _yearSelect
    private val _termSelect = MutableStateFlow<List<TermSelect>>(emptyList())
    val termSelect: StateFlow<List<TermSelect>> = _termSelect

    private lateinit var currentUser: User
    private lateinit var currentYear: String
    private var currentTerm: Int = 1

    private val _customCourseListState = MutableStateFlow(CustomCourseListState())
    val customCourseListState: StateFlow<CustomCourseListState> = _customCourseListState

    private val _saveCustomCourseState = MutableStateFlow(SaveCustomCourseState())
    val saveCustomCourseState: StateFlow<SaveCustomCourseState> = _saveCustomCourseState

    private val _searchCourseListState = MutableStateFlow(SearchCourseListState())
    val searchCourseListState: StateFlow<SearchCourseListState> = _searchCourseListState

    init {
        viewModelScope.launch {
            val loggedUserList = SessionManager.loggedUserList()
            _userSelect.value = runOnCpu {
                loggedUserList.map {
                    UserSelect(it.studentId, it.info.userName, it.main)
                }
            }
            currentUser = loggedUserList.find { it.main }!!
            currentYear = getConfig { currentYear }
            currentTerm = getConfig { currentTerm }

            _yearSelect.value = buildYearSelect(currentYear)
            _termSelect.value = buildTermSelect(currentTerm)
            _init.value = true
        }
    }

    private fun toastMessage(message: String) {
        _errorMessage.value = System.currentTimeMillis() to message
    }

    private suspend fun buildYearSelect(selectedYear: String): List<YearSelect> = runOnCpu {
        val loggedUserList = SessionManager.loggedUserList()
        val startYear = loggedUserList.minByOrNull { it.info.grade }!!.info.grade.toInt()
        val time = LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone)
        val endYear = if (time.month < Month.JUNE) time.year - 1 else time.year
        (startYear..endYear).map {
            val year = "${it}-${it + 1}"
            YearSelect(year, selectedYear == year)
        }
    }

    private suspend fun buildTermSelect(selectedTerm: Int): List<TermSelect> = runOnCpu {
        Array(2) {
            val term = it + 1
            TermSelect(term, term == selectedTerm)
        }.toList()
    }

    fun loadCustomCourseList() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load custom course list failed", throwable)
            _customCourseListState.value = CustomCourseListState()
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _customCourseListState.value = CustomCourseListState(loading = true)
            val selected = runOnCpu { _userSelect.value.first { it.selected }.studentId }
            val selectUser = SessionManager.user(selected)
            val year = runOnCpu { yearSelect.value.first { it.selected }.year }
            val term = runOnCpu { termSelect.value.first { it.selected }.term }

            currentUser = selectUser
            currentYear = year
            currentTerm = term

            val response = getCustomCourseList(currentUser, currentYear, currentTerm)
            _customCourseListState.value = CustomCourseListState(
                customCourseList = response,
                loading = false
            )
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
            _searchCourseListState.value = SearchCourseListState(loading = true)
            val currentYear = getConfig { currentYear }
            val currentTerm = getConfig { currentTerm }
            val request = AllCourseRequest(
                currentYear,
                currentTerm,
                courseName,
                teacherName,
                courseIndex,
                day,
            )
            val list = SessionManager.mainUser().withAutoLogin {
                serverApi.selectAllCourse(it, request).checkLogin()
            }.first
            val result = list.map {
                SearchCourse(
                    it.name,
                    it.teacher,
                    it.location,
                    it.weekString,
                    it.week,
                    it.time,
                    it.day,
                )
            }
            _searchCourseListState.value = SearchCourseListState(
                searchCourseList = result,
                loading = false
            )
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
            _saveCustomCourseState.value = SaveCustomCourseState(loading = true)

            val customCourse = CustomCourse(
                courseId,
                courseName,
                teacherName,
                "",
                week,
                location,
                courseIndex,
                day,
                "",
            )
            if (courseId == 0L) {
                createCustomCourse(currentUser, currentYear, currentTerm, customCourse)
            } else {
                updateCustomCourse(currentUser, currentYear, currentTerm, customCourse)
            }
            _saveCustomCourseState.value = SaveCustomCourseState()
            toastMessage("《$courseName》保存成功")
            changeCustomCourse = true
            loadCustomCourseList()
        }
    }

    fun delete(courseId: Long) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "delete custom course failed", throwable)
            _saveCustomCourseState.value =
                SaveCustomCourseState()
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _saveCustomCourseState.value = SaveCustomCourseState(loading = true)

            deleteCustomCourse(currentUser, courseId)
            _saveCustomCourseState.value = SaveCustomCourseState()
            toastMessage("删除成功")
            changeCustomCourse = true
            loadCustomCourseList()
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            if (_userSelect.value.first { it.selected }.studentId == studentId) {
                return@launch
            }
            _userSelect.value = runOnCpu {
                SessionManager.loggedUserList().map {
                    UserSelect(it.studentId, it.info.userName, it.studentId == studentId)
                }
            }
        }
    }

    fun selectYear(year: String) {
        viewModelScope.launch {
            if (_yearSelect.value.first { it.selected }.year == year) {
                return@launch
            }
            _yearSelect.value = buildYearSelect(year)
        }
    }

    fun selectTerm(term: Int) {
        viewModelScope.launch {
            if (_termSelect.value.first { it.selected }.term == term) {
                return@launch
            }
            _termSelect.value = buildTermSelect(term)
        }
    }
}

data class CustomCourseListState(
    val loading: Boolean = false,
    val customCourseList: List<CustomCourse> = emptyList(),
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