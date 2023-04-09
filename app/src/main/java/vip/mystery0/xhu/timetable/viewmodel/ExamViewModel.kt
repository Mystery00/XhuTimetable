package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.repository.getCourseColorByName
import vip.mystery0.xhu.timetable.repository.getExamList
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import java.time.Duration
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
            val loggedUserList = UserStore.loggedUserList()
            val mainUserId = UserStore.mainUserId()
            _userSelect.value = loggedUserList.map {
                UserSelect(it.studentId, it.info.name, it.studentId == mainUserId)
            }
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
                    UserStore.mainUser()
                } else {
                    val studentId = _userSelect.value.first { it.selected }.studentId
                    UserStore.userByStudentId(studentId)
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
                    val showText = when {
                        now.isBefore(startTime) -> {
                            //考试前
                            val duration = Duration.between(now, startTime)
                            val remainDays = duration.toDays()
                            if (remainDays > 0L) {
                                //还有超过一天的时间，那么显示 x天
                                val dayDuration = Duration.between(
                                    now.toLocalDate().atStartOfDay(),
                                    startTime.toLocalDate().atStartOfDay()
                                )
                                //如果在明天之外，那么不计算小时
                                if (dayDuration.toDays() > 1) {
                                    "${remainDays + 1}\n天"
                                } else {
                                    "${remainDays}\n天"
                                }
                            } else {
                                //剩余时间不足一天，显示 x小时
                                val remainHours = duration.toHours()
                                "${remainHours}\n小时后"
                            }
                        }
                        now.isAfter(endTime) -> {
                            //考试后
                            "已结束"
                        }
                        else -> {
                            //考试中
                            "今天"
                        }
                    }
                    Exam(
                        getCourseColorByName(it.courseName),
                        date,
                        it.date,
                        it.examNumber,
                        it.courseName,
                        it.type,
                        it.location,
                        time,
                        it.region,
                        examStatus,
                        showText,
                    )
                }.sortedWith(object : Comparator<Exam> {
                    override fun compare(o1: Exam, o2: Exam): Int {
                        if (o1.examStatus == o2.examStatus) {
                            return o1.date.compareTo(o2.date)
                        }
                        return o1.examStatus.compareTo(o2.examStatus)
                    }
                })
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
                UserStore.loggedUserList().map {
                    UserSelect(it.studentId, it.info.name, it.studentId == studentId)
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
    val courseColor: Color,
    val date: LocalDate,
    val dateString: String,
    val examNumber: String,
    val courseName: String,
    val type: String,
    val location: String,
    val time: String,
    val region: String,
    val examStatus: ExamStatus,
    val showText: String,
) {
    companion object {
        val EMPTY = Exam(
            courseColor = Color.White,
            date = LocalDate.MIN,
            dateString = "",
            examNumber = "座位号",
            courseName = "课程名称",
            type = "考试类型",
            location = "考试地点",
            time = "考试日期",
            region = "地区",
            examStatus = ExamStatus.BEFORE,
            showText = "",
        )
    }
}

enum class ExamStatus(
    val index: Int,
    val title: String,
    val color: Color,
    val strokeWidth: Int,
) {
    BEFORE(1, "未开始", XhuColor.Status.beforeColor, 2),
    IN(0, "进行中", XhuColor.Status.inColor, 3),
    AFTER(2, "已结束", XhuColor.Status.afterColor, 1),
}