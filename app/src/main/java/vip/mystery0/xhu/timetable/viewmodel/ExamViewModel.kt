package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.module.betweenDays
import vip.mystery0.xhu.timetable.repository.ExamRepo
import vip.mystery0.xhu.timetable.repository.getCourseColorByName
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.utils.enTimeFormatter
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class ExamViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "ExamViewModel"
    }

    private val _userSelect = MutableStateFlow<List<UserSelect>>(emptyList())
    val userSelect: StateFlow<List<UserSelect>> = _userSelect

    private val _examListState = MutableStateFlow(ExamListState())
    val examListState: StateFlow<ExamListState> = _examListState

    init {
        viewModelScope.launch {
            _userSelect.value = initUserSelect()
            loadExamList()
        }
    }

    fun loadExamList() {
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load exam list failed", throwable)
            _examListState.value =
                ExamListState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            _examListState.value = ExamListState(loading = true)
            val selectedUser = getSelectedUser(_userSelect.value)
            if (selectedUser == null) {
                Log.w(TAG, "loadExamList: empty selected user")
                _examListState.value = ExamListState(errorMessage = "选择用户为空，请重新选择")
                return@launch
            }
            //TODO 改为分页
            val examList = ExamRepo.fetchExamList(selectedUser).items
            withContext(Dispatchers.Default) {
                val now = Instant.now()
                val resultList = examList.map {
                    val examStatus = when {
                        now.isBefore(it.examStartTimeMills) -> ExamStatus.BEFORE
                        now.isAfter(it.examEndTimeMills) -> ExamStatus.AFTER
                        else -> ExamStatus.IN
                    }
                    val time = buildString {
                        append(enTimeFormatter.format(it.examStartTime))
                        append(" - ")
                        append(enTimeFormatter.format(it.examEndTime))
                    }
                    val statusShowText = when (examStatus) {
                        ExamStatus.BEFORE -> {
                            val duration = Duration.between(now, it.examStartTimeMills)
                            val remainDays = duration.toDays()
                            if (remainDays > 0L) {
                                //还有超过一天的时间，那么显示 x天
                                val dayDuration = betweenDays(LocalDate.now(), it.examDay)
                                //如果在明天之外，那么不计算小时
                                if (dayDuration > 1) {
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

                        ExamStatus.IN -> {
                            //考试中
                            "今天"
                        }

                        ExamStatus.AFTER -> {
                            //考试后
                            "已结束"
                        }
                    }
                    Exam(
                        getCourseColorByName(it.courseName),
                        it.examDay,
                        it.examDay.format(DateTimeFormatter.ISO_LOCAL_DATE),
                        it.seatNo,
                        it.courseName,
                        it.examName,
                        it.location,
                        time,
                        it.examRegion,
                        examStatus,
                        statusShowText,
                    )
                }.sortedWith(object : Comparator<Exam> {
                    override fun compare(o1: Exam, o2: Exam): Int {
                        if (o1.examStatus == o2.examStatus) {
                            return o1.date.compareTo(o2.date)
                        }
                        return o1.examStatus.index.compareTo(o2.examStatus.index)
                    }
                })
                _examListState.value = ExamListState(examList = resultList)
            }
        }
    }

    fun selectUser(studentId: String) {
        viewModelScope.launch {
            val result = setSelectedUser(_userSelect.value, studentId)
            if (!result.second) {
                return@launch
            }
            _userSelect.value = result.first
            loadExamList()
        }
    }
}

data class ExamListState(
    val loading: Boolean = false,
    val examList: List<Exam> = emptyList(),
    val errorMessage: String = "",
)

data class Exam(
    val courseColor: Color,
    val date: LocalDate,
    val dateString: String,
    val seatNo: String,
    val courseName: String,
    val examName: String,
    val location: String,
    val time: String,
    val examRegion: String,
    val examStatus: ExamStatus,
    val showText: String,
) {
    companion object {
        val EMPTY = Exam(
            courseColor = Color.White,
            date = LocalDate.MIN,
            dateString = "",
            seatNo = "座位号",
            courseName = "课程名称",
            examName = "考试类型名称",
            location = "考试地点",
            time = "考试日期",
            examRegion = "地区",
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