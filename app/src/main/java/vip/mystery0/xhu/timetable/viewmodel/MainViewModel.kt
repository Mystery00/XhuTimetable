package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.PoemsApi
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.Config
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.serverExceptionHandler
import vip.mystery0.xhu.timetable.model.Course
import vip.mystery0.xhu.timetable.model.entity.CourseType
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.module.repo
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*
import kotlin.collections.HashMap

class MainViewModel : ComposeViewModel(), KoinComponent {
    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    private val poemsApi: PoemsApi by inject()

    private val courseRepo: CourseRepo by repo()

    private val courseLocalRepo: CourseRepo by localRepo()

    private val _errorMessage = MutableStateFlow("")
    val errorMessage: StateFlow<String> = _errorMessage

    //今日页面标题
    private val _todayTitle = MutableStateFlow("")
    val todayTitle: StateFlow<String> = _todayTitle

    //日期栏开始时间
    private val _dateStart = MutableStateFlow(LocalDate.now())
    val dateStart: StateFlow<LocalDate> = _dateStart

    //当前周
    private val _week = MutableStateFlow(1)
    val week: StateFlow<Int> = _week

    //今日诗词
    private val _poems = MutableStateFlow<Poems?>(null)
    val poems: StateFlow<Poems?> = _poems

    private val _todayCourse = MutableStateFlow<List<Course>>(emptyList())
    val todayCourse: StateFlow<List<Course>> = _todayCourse

    private val _tableCourse = MutableStateFlow<List<List<CourseSheet?>>>(emptyList())
    val tableCourse: StateFlow<List<List<CourseSheet?>>> = _tableCourse

    private val _weekView = MutableStateFlow<List<WeekView>>(emptyList())
    val weekView: StateFlow<List<WeekView>> = _weekView

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _showWeekView = MutableStateFlow(false)
    val showWeekView: StateFlow<Boolean> = _showWeekView

    init {
        showPoems()
        calculateTodayTitle()
        calculateWeek()
        loadCourseList(forceUpdate = false)
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
            val field = WeekFields.of(DayOfWeek.MONDAY, 1).dayOfWeek()
            _dateStart.value = nowDate.with(field, 1)
        }
    }

    private fun calculateWeek() {
        viewModelScope.launch {
            val startDate = LocalDateTime.ofInstant(Config.termStartTime, chinaZone).toLocalDate()
            val days =
                Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays()
            val currentWeek = ((days / 7) + 1).toInt()
            _week.value = currentWeek
            _week.collect { loadCourseToTable(it) }
        }
    }

    fun loadCourseList(forceUpdate: Boolean = true) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load course list failed", throwable)
            _loading.value = false
            _tableCourse.value = emptyList()
            _errorMessage.value = throwable.message ?: throwable.javaClass.simpleName
        }) {
            _loading.value = true
            var loadFromCloud = forceUpdate
            if (!loadFromCloud) {
                loadFromCloud = Config.lastSyncCourse.isBefore(LocalDate.now())
            }
            //获取所有的课程列表
            val courseList = if (loadFromCloud) {
                courseRepo.getCourseList()
            } else {
                courseLocalRepo.getCourseList()
            }

            //计算当前周
            val startDate = LocalDateTime.ofInstant(Config.termStartTime, chinaZone).toLocalDate()
            val days =
                Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays()
            val currentWeek = ((days / 7) + 1).toInt()
            _week.value = currentWeek
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
                    it.type,
                    it.time,
                    timeString,
                    it.time.formatTime(),
                    it.day,
                    thisWeek,
                    ColorPool.hash(it.name),
                )
            }

            //过滤出今日的课程
            val todayCourse = allCourseList.filter { it.thisWeek && it.day == weekIndex }
                .sortedBy { it.timeString }
            //设置数据
            _todayCourse.value = todayCourse
            loadCourseToTable(currentWeek)
            if (loadFromCloud) {
                _errorMessage.emit("${LocalDateTime.now().format(dateTimeFormatter)} 数据同步成功！")
            }
        }
    }

    private fun loadCourseToTable(currentWeek: Int) {
        Log.i(TAG, "loadCourseToTable: $currentWeek")
        viewModelScope.launch {
            //获取所有的课程列表
            val courseList = courseLocalRepo.getCourseList()
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
                    it.type,
                    it.time,
                    timeString,
                    it.time.formatTime(),
                    it.day,
                    thisWeek,
                    ColorPool.hash(it.name),
                )
            }
            //过滤出本周的课程
            val tableCourse = Array<ArrayList<CourseSheet?>>(7) { _ -> arrayListOf() }
            val expandItemMap = HashMap<Pair<Int, Int>, ArrayList<Course>>()
            allCourseList.map { course ->
                course.timeSet.forEach { time ->
                    course.weekSet.forEach { week ->
                        val add = when (course.type) {
                            //所有周都有，那么添加
                            CourseType.ALL -> true
                            //单周才有，判断单周
                            CourseType.SINGLE -> week % 2 == 1
                            //双周才有，判断双周
                            CourseType.DOUBLE -> week % 2 == 0
                        }
                        if (add) {
                            val key = course.day to time
                            val list = expandItemMap.getOrDefault(key, arrayListOf())
                            list.add(course)
                            expandItemMap[key] = list
                        }
                    }
                }
            }
            var lastSheet: CourseSheet? = null
            expandItemMap.entries.sortedWith { o1, o2 ->
                val key1 = o1.key
                val key2 = o2.key
                if (key1.first == key2.first) {
                    key1.second.compareTo(key2.second)
                } else {
                    key1.first.compareTo(key2.first)
                }
            }.forEach { (key, list) ->
                val day = key.first
                list.sortWith { o1, o2 ->
                    if (o1.thisWeek == o2.thisWeek) {
                        o1.weekSet.first().compareTo(o2.weekSet.first())
                    } else {
                        o2.thisWeek.compareTo(o1.thisWeek)
                    }
                }
                val show = list.first()
                val showTitle = "${show.courseName}@${show.location}"
                val thisSheet =
                    CourseSheet(
                        if (show.thisWeek) showTitle else "[非本周]\n${showTitle}",
                        1,
                        show.timeSet.first(),
                        list.distinct().sortedBy { it.weekSet.first() },
                        if (show.thisWeek) ColorPool.hash(show.courseName) else notThisWeekBackgroundColor,
                        if (show.thisWeek) Color.White else Color.Gray,
                    )
                if (lastSheet != thisSheet) {
                    //不相等，直接添加
                    //判断中间需不需要留空
                    lastSheet?.let {
                        val count = thisSheet.startIndex - it.startIndex - it.step
                        for (i in count downTo 1) {
                            tableCourse[day - 1].add(null)
                        }
                    }
                    tableCourse[day - 1].add(thisSheet)
                    lastSheet = thisSheet
                } else {
                    //相等，合并
                    lastSheet!!.step++
                }
            }
            val tableCourseList = tableCourse.map { it }
            //计算周课程视图
            val startDate = LocalDateTime.ofInstant(Config.termStartTime, chinaZone).toLocalDate()
            val days =
                Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays()
            val thisWeek = ((days / 7) + 1).toInt()
            val weekViewArray = Array(20) { index ->
                WeekView(index + 1, thisWeek == index + 1, Array(5) { Array(5) { false } })
            }
            expandItemMap.forEach { (pair, list) ->
                if (pair.second > 10 || pair.first > 5) {
                    //丢弃第11节次的数据
                    return@forEach
                }
                val day = pair.first - 1
                val time = (pair.second - 1) / 2
                val weekSet = list.map { it.weekSet }.flatten().toSet()
                weekSet.forEach {
                    weekViewArray[it - 1].array[day][time] = true
                }
            }
            //设置数据
            _weekView.value = weekViewArray.toList()
            _tableCourse.value = tableCourseList
            _loading.value = false
        }
    }

    fun animeWeekView() {
        _showWeekView.value = !_showWeekView.value
    }

    fun dismissWeekView() {
        _showWeekView.value = false
    }

    fun changeCurrentWeek(currentWeek: Int) {
        _week.value = currentWeek
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

private val notThisWeekBackgroundColor = Color(0xFFe5e5e5)

fun List<Int>.formatTimeString(): String =
    if (size == 1) "第${this[0]}节" else "${first()}-${last()}节"

private val startArray = arrayOf(
    "08:00",
    "08:55",
    "10:00",
    "10:55",
    "14:00",
    "14:55",
    "16:00",
    "16:00",
    "19:00",
    "19:55",
    "20:50",
)

private val endArray = arrayOf(
    "08:45",
    "09:40",
    "10:45",
    "11:40",
    "14:45",
    "15:40",
    "16:45",
    "17:40",
    "19:45",
    "19:40",
    "21:35",
)

fun List<Int>.formatTime(): String = "${startArray[first() - 1]} - ${endArray[last() - 1]}"

data class CourseSheet(
    //显示标题
    val showTitle: String,
    //步长
    var step: Int,
    //开始节次序号
    val startIndex: Int,
    //当前格子的课程列表
    val course: List<Course>,
    //颜色
    val color: Color,
    //文本颜色
    val textColor: Color,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CourseSheet

        if (showTitle != other.showTitle) return false
        if (course != other.course) return false

        return true
    }

    override fun hashCode(): Int {
        var result = showTitle.hashCode()
        result = 31 * result + course.hashCode()
        return result
    }
}

class WeekView(
    val weekNum: Int,
    val thisWeek: Boolean,
    val array: Array<Array<Boolean>>
)
