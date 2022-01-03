package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.*
import vip.mystery0.xhu.timetable.model.CustomCourse
import vip.mystery0.xhu.timetable.repository.createCustomCourse
import vip.mystery0.xhu.timetable.repository.getCustomCourseList
import vip.mystery0.xhu.timetable.repository.updateCustomCourse
import java.time.LocalDateTime
import java.time.Month

class CustomCourseViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "CustomCourseViewModel"
    }

    var changeCustomCourse = false

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect
    private val _yearSelect = MutableStateFlow<List<YearSelect>>(emptyList())
    val yearSelect: StateFlow<List<YearSelect>> = _yearSelect
    private val _termSelect = MutableStateFlow<List<TermSelect>>(emptyList())
    val termSelect: StateFlow<List<TermSelect>> = _termSelect

    private val _customCourseListState = MutableStateFlow(CustomCourseListState())
    val customCourseListState: StateFlow<CustomCourseListState> = _customCourseListState

    private val _saveCustomCourseState = MutableStateFlow(SaveCustomCourseState())
    val saveCustomCourseState: StateFlow<SaveCustomCourseState> = _saveCustomCourseState

    init {
        viewModelScope.launch {
            val loggedUserList = SessionManager.loggedUserList()
            _userSelect.value = runOnCpu {
                loggedUserList.map {
                    UserSelect(it.studentId, it.info.userName, it.main)
                }
            }
            _yearSelect.value = buildYearSelect(getConfig { currentYear })
            _termSelect.value = buildTermSelect(getConfig { currentTerm })
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
            val selectUser =
                SessionManager.user(selected)
            val year = runOnCpu { yearSelect.value.first { it.selected }.year }
            val term = runOnCpu { termSelect.value.first { it.selected }.term }
            val response = getCustomCourseList(selectUser, year, term)
            _customCourseListState.value = CustomCourseListState(
                customCourseList = response,
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
            val selected = runOnCpu { _userSelect.value.first { it.selected }.studentId }
            val selectUser = SessionManager.user(selected)
            val year = runOnCpu { yearSelect.value.first { it.selected }.year }
            val term = runOnCpu { termSelect.value.first { it.selected }.term }
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
                createCustomCourse(selectUser, year, term, customCourse)
            } else {
                updateCustomCourse(selectUser, year, term, customCourse)
            }
            _saveCustomCourseState.value = SaveCustomCourseState()
            toastMessage("《$courseName》保存成功")
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