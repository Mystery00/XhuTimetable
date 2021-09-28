package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.PoemsApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.Config
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.model.Course
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.module.repo
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

class MainViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val poemsApi: PoemsApi by inject()

    private val courseRepo: CourseRepo by repo()

    private val courseLocalRepo: CourseRepo by localRepo()

    private val _todayTitle = MutableStateFlow("")
    val todayTitle: StateFlow<String> = _todayTitle

    private val _week = MutableStateFlow(1)
    val week: StateFlow<Int> = _week

    private val _poems = MutableStateFlow<Poems?>(null)
    val poems: StateFlow<Poems?> = _poems

    private val _tableCourse = MutableStateFlow(CourseListState(loading = true))
    val tableCourse: StateFlow<CourseListState> = _tableCourse

    private val _todayCourse = MutableStateFlow<List<Course>>(emptyList())
    val todayCourse: StateFlow<List<Course>> = _todayCourse

    init {
        calculateTodayTitle()
        calculateWeek()
        showPoems()
        loadCourseList(firstRequest = true, forceRequest = false)
    }

    private fun calculateTodayTitle() {
        viewModelScope.launch {
            val nowDate = LocalDate.now()
            val todayWeekIndex = nowDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
            val startDate = LocalDateTime.ofInstant(Config.termStartTime, chinaZone).toLocalDate()
            if (nowDate.isBefore(startDate)) {
                //开学之前，那么计算剩余时间
                val remainDays =
                    Duration.between(nowDate.atStartOfDay(), startDate.atStartOfDay()).toDays()
                _todayTitle.value = "距离开学还有${remainDays}天 $todayWeekIndex"
            } else {
                val days =
                    Duration.between(startDate.atStartOfDay(), nowDate.atStartOfDay()).toDays()
                val week = (days / 7) + 1
                _todayTitle.value = "第${week}周 $todayWeekIndex"
            }
        }
    }

    private fun calculateWeek() {
        viewModelScope.launch {
            val nowDate = LocalDate.now()
            _week.value = nowDate.dayOfWeek.value
        }
    }

    private fun showPoems() {
        viewModelScope.launch {
            val token = Config.poemsToken
            if (token == null) {
                Config.poemsToken = poemsApi.getToken().data
            }
            _poems.value = poemsApi.getSentence().data
        }
    }

    fun loadCourseList(firstRequest: Boolean = true, forceRequest: Boolean = false) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load course list failed", throwable)
            _tableCourse.value =
                CourseListState(errorMessage = throwable.message ?: throwable.javaClass.simpleName)
        }) {
            //获取所有的课程列表
            val courseList = if (firstRequest || forceRequest) {
                //第一次请求，那么根据网络情况获取课程列表
                courseRepo.getCourseList()
            } else {
                //不是第一次请求，那么获取本地数据
                courseLocalRepo.getCourseList()
            }
            //计算当前周
            val startDate = LocalDateTime.ofInstant(Config.termStartTime, chinaZone).toLocalDate()
            val days =
                Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays()
            val currentWeek = ((days / 7) + 1).toInt()
            val weekIndex = LocalDate.now().dayOfWeek.value
            //转换对象
            val allCourseList = courseList.map {
                val thisWeek = it.week.contains(currentWeek)
                val weekString = it.week.formatWeekString()
                val timeString = it.time.formatTimeString()
                Course(
                    it.name,
                    it.teacher,
                    it.location,
                    it.week,
                    weekString,
                    timeString,
                    timeString,
                    it.day,
                    thisWeek,
                    ColorPool.hash(it.name),
                )
            }
            //过滤出本周的课程
            val weekCourse = allCourseList.filter { it.thisWeek }
            //过滤出今日的课程
            val todayCourse = allCourseList.filter { it.thisWeek && it.day == weekIndex }
                .sortedBy { it.timeString }
            //设置数据
            _tableCourse.value = CourseListState(loading = false, courseList = weekCourse)
            _todayCourse.value = todayCourse
        }
    }
}

fun List<Int>.formatWeekString(): String {
    var start = -1
    var temp = -1
    val stringBuilder = StringBuilder()
    this.forEach {
        if (it == temp + 1) {
            //连续数字
            temp = it
            return@forEach
        }
        //非连续数字
        if (start == -1) {
            //第一次循环
            start = it
            temp = it
            return@forEach
        }
        //非第一次循环
        stringBuilder.append(start)
        if (temp != start) {
            stringBuilder.append("-").append(temp)
        }
        stringBuilder.append("、")
        start = it
        temp = it
    }
    stringBuilder.append(start)
    if (temp != start) {
        stringBuilder.append("-").append(temp)
    }
    return stringBuilder.append("周").toString()
}

fun List<Int>.formatTimeString(): String =
    if (size == 1) "第${this[0]}节" else "${first()}-${last()}节"

data class CourseListState(
    val loading: Boolean = false,
    val courseList: List<Course> = emptyList(),
    val errorMessage: String = "",
)