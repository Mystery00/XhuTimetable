package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.repository.getExamList
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class ExamViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "ExamViewModel"
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    }

    val selectUser = MutableStateFlow(SessionManager.mainUser)

    private val _examListState = MutableStateFlow(ExamListState())
    val examListState: StateFlow<ExamListState> = _examListState

    init {
        loadExamList()
    }

    fun loadExamList() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load exam list failed", throwable)
            _examListState.value =
                ExamListState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _examListState.value = ExamListState(loading = true)
            val response = getExamList(selectUser.value)
            val now = LocalDateTime.now()
            val examList = response.list.map {
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
                val time = "${timeFormatter.format(startTime)} - ${timeFormatter.format(endTime)}"
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
            _examListState.value =
                ExamListState(examList = examList, examHtml = response.html)
        }
    }
}

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