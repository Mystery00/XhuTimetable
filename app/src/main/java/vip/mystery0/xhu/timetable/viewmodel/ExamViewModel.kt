package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.repository.getExamList
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ExamViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "ExamViewModel"
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect

    private val _examListState = MutableStateFlow(ExamListState())
    val examListState: StateFlow<ExamListState> = _examListState

    init {
        viewModelScope.launch {
            val list = runOnCpu {
                SessionManager.loggedUserList().map {
                    UserSelect(it.studentId, it.info.userName, it.main)
                }
            }
            _userSelect.value = list
        }
        loadExamList()
    }

    fun loadExamList() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load exam list failed", throwable)
            _examListState.value =
                ExamListState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _examListState.value = ExamListState(loading = true)
            val selectUser = runOnCpu {
                if (_userSelect.value.isEmpty()) {
                    SessionManager.mainUser()
                } else {
                    val studentId = _userSelect.value.first { it.selected }.studentId
                    SessionManager.user(studentId)
                }
            }
            val response = getExamList(selectUser)
            val examList = runOnCpu {
                val now = LocalDateTime.now()
                response.list.map {
                    val date = LocalDate.parse(it.date, dateFormatter)
                    val startTime =
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(it.startTime), chinaZone)
                    val endTime =
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(it.endTime), chinaZone)
                    val examStatus = when {
                        now.isBefore(startTime) -> ExamStatus.BEFORE
                        now.isAfter(endTime) -> ExamStatus.AFTER
                        else -> ExamStatus.IN
                    }
                    val time =
                        "${timeFormatter.format(startTime)} - ${timeFormatter.format(endTime)}"
                    Exam(
                        date,
                        it.date,
                        it.examNumber,
                        it.courseName,
                        it.type,
                        it.location,
                        time,
                        it.region,
                        examStatus,
                    )
                }.sortedBy { it.examStatus.index }
            }
            _examListState.value =
                ExamListState(examList = examList, examHtml = response.html)
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            val selected = runOnCpu {
                _userSelect.value.first { it.selected }.studentId
            }
            if (selected == studentId) {
                return@launch
            }
            _userSelect.value = runOnCpu {
                SessionManager.loggedUserList().map {
                    UserSelect(it.studentId, it.info.userName, it.studentId == studentId)
                }
            }
            loadExamList()
        }
    }
}

data class UserSelect(
    val studentId: String,
    val userName: String,
    val selected: Boolean,
)

data class ExamListState(
    val loading: Boolean = false,
    val examList: List<Exam> = emptyList(),
    val examHtml: String = "",
    val errorMessage: String = "",
)

data class Exam(
    val date: LocalDate,
    val dateString: String,
    val examNumber: String,
    val courseName: String,
    val type: String,
    val location: String,
    val time: String,
    val region: String,
    val examStatus: ExamStatus,
)

enum class ExamStatus(
    val index: Int,
    val title: String,
    val color: Color,
) {
    BEFORE(1, "未开始", XhuColor.Status.beforeColor),
    IN(2, "进行中", XhuColor.Status.inColor),
    AFTER(0, "已结束", XhuColor.Status.afterColor),
}