package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.FeedbackApi
import vip.mystery0.xhu.timetable.api.PoemsApi
import vip.mystery0.xhu.timetable.api.checkLogin
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.*
import vip.mystery0.xhu.timetable.config.SessionManager.withAutoLogin
import vip.mystery0.xhu.timetable.isOnline
import vip.mystery0.xhu.timetable.model.Course
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.Menu
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.module.getRepo
import vip.mystery0.xhu.timetable.module.localRepo
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.repository.CustomThingRepo
import vip.mystery0.xhu.timetable.repository.NoticeRepo
import vip.mystery0.xhu.timetable.repository.getRawCourseColorList
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.work.DownloadApkWork
import vip.mystery0.xhu.timetable.work.DownloadPatchWork
import java.time.*
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.WeekFields
import java.util.*

class MainViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private var lastCheckUnreadTime = Instant.MIN

    private val poemsApi: PoemsApi by inject()

    private val feedbackApi: FeedbackApi by inject()

    private val courseRepo: CourseRepo = getRepo()

    private val courseLocalRepo: CourseRepo by localRepo()

    private val customThingRepo: CustomThingRepo = getRepo()

    private val customThingLocalRepo: CustomThingRepo by localRepo()

    private val noticeRepo: NoticeRepo = getRepo()

    private val workManager: WorkManager by inject()

    val version = MutableStateFlow(DataHolder.version)

    private val isDarkMode = MutableStateFlow(false)

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

    //??????????????????
    private val _todayTitle = MutableStateFlow("")
    val todayTitle: StateFlow<String> = _todayTitle

    //?????????????????????
    private val _dateStart = MutableStateFlow(LocalDate.now())
    val dateStart: StateFlow<LocalDate> = _dateStart

    //?????????
    private val _week = MutableStateFlow(1)
    val week: StateFlow<Int> = _week

    //????????????
    private val _poems = MutableStateFlow<Poems?>(null)
    val poems: StateFlow<Poems?> = _poems

    //??????????????????
    private val _todayThing = MutableStateFlow<List<CustomThingSheet>>(emptyList())
    val todayThing: StateFlow<List<CustomThingSheet>> = _todayThing

    private val _todayCourse = MutableStateFlow<List<TodayCourseSheet>>(emptyList())
    val todayCourse: StateFlow<List<TodayCourseSheet>> = _todayCourse

    private val _tableCourse = MutableStateFlow<List<List<CourseSheet>>>(emptyList())
    val tableCourse: StateFlow<List<List<CourseSheet>>> = _tableCourse

    private val _weekView = MutableStateFlow<List<WeekView>>(emptyList())
    val weekView: StateFlow<List<WeekView>> = _weekView

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    private val _showWeekView = MutableStateFlow(false)
    val showWeekView: StateFlow<Boolean> = _showWeekView

    private val _hasUnReadNotice = MutableStateFlow(false)
    val hasUnReadNotice: StateFlow<Boolean> = _hasUnReadNotice

    private val _hasUnReadFeedback = MutableStateFlow(false)
    val hasUnReadFeedback: StateFlow<Boolean> = _hasUnReadFeedback

    private val _multiAccountMode = MutableStateFlow(false)
    val multiAccountMode: StateFlow<Boolean> = _multiAccountMode

    private val _showStatus = MutableStateFlow(false)
    val showStatus: StateFlow<Boolean> = _showStatus

    private val _showTomorrowCourse = MutableStateFlow(false)
    val showTomorrowCourse: StateFlow<Boolean> = _showTomorrowCourse

    private val _backgroundImage = MutableStateFlow<Any>(Unit)
    val backgroundImage: StateFlow<Any> = _backgroundImage

    private val _emptyUser = MutableStateFlow(false)
    val emptyUser: StateFlow<Boolean> = _emptyUser

    private val _mainUser = MutableStateFlow<User?>(null)
    val mainUser: StateFlow<User?> = _mainUser

    private val _menu = MutableStateFlow<List<Menu>>(emptyList())
    val menu: StateFlow<List<Menu>> = _menu

    private val _customUi = MutableStateFlow(CustomUi.DEFAULT)
    val customUi: StateFlow<CustomUi> = _customUi

    init {
        viewModelScope.launch {
            loadFromConfig()
            val mainUser = SessionManager.mainUserOrNull()
            _mainUser.value = mainUser
            _emptyUser.value = mainUser == null
        }
        showPoems()
        calculateTodayTitle()
        calculateWeek()
        loadCourseList(forceUpdate = false)
    }

    private fun toastMessage(message: String) {
        _errorMessage.value = System.currentTimeMillis() to message
    }

    private suspend fun loadFromConfig() {
        _multiAccountMode.value = getConfig { multiAccountMode }
        _showStatus.value = getConfig { showStatus }
        val time = getConfig { showTomorrowCourseTime }
        _showTomorrowCourse.value = time?.let {
            LocalTime.now().isAfter(it)
        } ?: false
        _menu.value = getConfig { menuList }
        _customUi.value = getConfig { customUi }
    }

    fun loadConfig() {
        viewModelScope.launch {
            loadFromConfig()
        }
    }

    fun loadBackground(isDarkModeValue: Boolean = isDarkMode.value) {
        viewModelScope.launch {
            isDarkMode.value = isDarkModeValue
            val disable = getConfig { disableBackgroundWhenNight }
            val isNight = isDarkMode.value
            if (disable && isNight) {
                _backgroundImage.value = Unit
                return@launch
            } else {
                _backgroundImage.value =
                    getConfig { backgroundImage } ?: XhuImages.defaultBackgroundImage
            }
        }
    }

    fun clearLastCheckUnreadTime() {
        lastCheckUnreadTime = Instant.MIN
    }

    fun checkUnRead(block: () -> Unit) {
        val now = Instant.now()
        if (Duration.between(lastCheckUnreadTime, now) > Duration.ofMinutes(1)) {
            block()
            lastCheckUnreadTime = now
        }
    }

    fun checkMainUser() {
        viewModelScope.launch {
            val mainUser = SessionManager.mainUserOrNull()
            _mainUser.value = mainUser
            _emptyUser.value = mainUser == null
        }
    }

    private fun showPoems() {
        if (!isOnline()) {
            return
        }
        viewModelScope.launch {
            val disablePoems = getConfig { disablePoems }
            if (disablePoems) {
                return@launch
            }
            val token = getConfig { poemsToken }
            if (token == null) {
                setConfig { this.poemsToken = poemsApi.getToken().data }
            }
            val poems = poemsApi.getSentence().data
            val showPoemsTranslate = getConfig { showPoemsTranslate }
            if (!showPoemsTranslate) {
                poems.origin.translate = null
            }
            _poems.value = poems
        }
    }

    fun calculateTodayTitle() {
        viewModelScope.launch {
            val showTomorrow = getConfig { showTomorrowCourseTime }?.let {
                LocalTime.now().isAfter(it)
            } ?: false
            _todayTitle.value = runOnCpu {
                val startDate =
                    LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone).toLocalDate()
                val nowDate = LocalDate.now()
                if (nowDate.isBefore(startDate)) {
                    //???????????????????????????????????????
                    val todayWeekIndex =
                        nowDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
                    val remainDays =
                        Duration.between(nowDate.atStartOfDay(), startDate.atStartOfDay()).toDays()
                    "??????????????????${remainDays}??? $todayWeekIndex"
                } else {
                    val date = if (showTomorrow) nowDate.plusDays(1) else nowDate
                    val days =
                        Duration.between(startDate.atStartOfDay(), date.atStartOfDay()).toDays()
                    val weekIndex = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
                    val week = (days / 7) + 1
                    if (week > 20) {
                        "??????????????????"
                    } else {
                        "???${week}??? $weekIndex"
                    }
                }
            }
        }
    }

    private fun calculateWeek() {
        viewModelScope.launch {
            val startDate = runOnCpu {
                LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone).toLocalDate()
            }
            _week.value = runOnCpu {
                val days =
                    Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay())
                        .toDays()
                ((days / 7) + 1).toInt()
            }
            _week.collect {
                loadCourseToTable(it)
                _dateStart.value = runOnCpu {
                    val date = startDate.plusWeeks(it.toLong() - 1)
                    date.with(WeekFields.of(DayOfWeek.MONDAY, 1).dayOfWeek(), 1)
                }
            }
        }
    }

    private suspend fun getAllCourseList(loadFromCloud: Boolean): List<CourseResponse> {
        val currentYear = getConfig { currentYear }
        val currentTerm = getConfig { currentTerm }
        val courseList: List<CourseResponse> =
            if (getConfig { multiAccountMode }) {
                val list = ArrayList<CourseResponse>()
                SessionManager.loggedUserList().forEach { user ->
                    list.addAll(
                        if (loadFromCloud) {
                            courseRepo.getCourseList(user, currentYear, currentTerm)
                        } else {
                            courseLocalRepo.getCourseList(user, currentYear, currentTerm)
                        }
                    )
                }
                list
            } else {
                val user = SessionManager.mainUser()
                if (loadFromCloud) {
                    courseRepo.getCourseList(user, currentYear, currentTerm)
                } else {
                    courseLocalRepo.getCourseList(user, currentYear, currentTerm)
                }
            }
        return courseList
    }

    private suspend fun getAllThingList(loadFromCloud: Boolean): List<CustomThingSheet> {
        if (!getConfig { showCustomThing }) {
            return emptyList()
        }
        val currentYear = getConfig { currentYear }
        val currentTerm = getConfig { currentTerm }
        val thingList: List<CustomThingSheet> =
            if (getConfig { multiAccountMode }) {
                val list = ArrayList<CustomThingSheet>()
                SessionManager.loggedUserList().forEach { user ->
                    val result = if (loadFromCloud) {
                        customThingRepo.getCustomThingList(user, currentYear, currentTerm)
                    } else {
                        customThingLocalRepo.getCustomThingList(user, currentYear, currentTerm)
                    }
                    list.addAll(result.map {
                        CustomThingSheet(
                            user.studentId,
                            user.info.userName,
                            it
                        )
                    })
                }
                list
            } else {
                val user = SessionManager.mainUser()
                val result = if (loadFromCloud) {
                    customThingRepo.getCustomThingList(user, currentYear, currentTerm)
                } else {
                    customThingLocalRepo.getCustomThingList(user, currentYear, currentTerm)
                }
                result.map { CustomThingSheet(user.studentId, user.info.userName, it) }
            }
        return thingList
    }

    private suspend fun loadTodayThing(customThingList: List<CustomThingSheet>) {
        _todayThing.value = runOnCpu {
            val today = LocalDate.now()
            val tomorrow = today.plusDays(1)
            val nowTime = LocalTime.now()

            //????????????????????????????????????
            val showTomorrowCourse = getConfig { showTomorrowCourseTime }?.let {
                nowTime.isAfter(it)
            } ?: false
            customThingList.filter {
                val thing = it.thing
                if (showTomorrowCourse)
                    thing.showOnToday(tomorrow)
                else
                    thing.showOnToday(today)
            }.sortedBy { it.thing.sort }
        }
    }

    fun loadCourseList(forceUpdate: Boolean = true) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load course list failed", throwable)
            _loading.value = false
            if (!GlobalConfig.showOldCourseWhenFailed) {
                _tableCourse.value = emptyList()
            }
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            fun convertCourseList(
                courseList: List<CourseResponse>,
                colorMap: Map<String, Color>,
                currentWeek: Int,
                today: LocalDate,
            ) = courseList.map {
                val thisWeek = it.week.contains(currentWeek)
                val timeString = it.time.formatTimeString()
                val tomorrowWeek =
                    if (today.dayOfWeek == DayOfWeek.SUNDAY) currentWeek + 1 else currentWeek
                val tomorrow = today.plusDays(1)
                val isToday = thisWeek && it.day == today.dayOfWeek.value
                val isTomorrow =
                    it.week.contains(tomorrowWeek) && it.day == tomorrow.dayOfWeek.value
                Course(
                    it.name,
                    it.teacher,
                    it.location,
                    it.week,
                    it.weekString,
                    it.type,
                    it.time,
                    timeString,
                    it.time.formatTime(),
                    it.day,
                    thisWeek,
                    isToday,
                    isTomorrow,
                    colorMap[it.name] ?: ColorPool.hash(it.name),
                    it.user.studentId,
                    it.user.info.userName,
                )
            }

            suspend fun loadData(
                courseList: List<CourseResponse>,
                colorMap: Map<String, Color>,
                currentWeek: Int,
            ) {
                _todayCourse.value = runOnCpu {
                    val today = LocalDate.now()
                    val nowTime = LocalTime.now()
                    //????????????
                    val allCourseList = convertCourseList(courseList, colorMap, currentWeek, today)

                    //????????????????????????????????????
                    val showTomorrowCourse = getConfig { showTomorrowCourseTime }?.let {
                        nowTime.isAfter(it)
                    } ?: false
                    val todayCourse =
                        allCourseList.filter { if (showTomorrowCourse) it.tomorrow else it.today }
                            .sortedBy { it.timeSet.first() }
                            .groupBy { it.studentId }
                    //?????????????????????
                    val resultCourse = ArrayList<TodayCourseSheet>(todayCourse.size)
                    todayCourse.forEach { (studentId, list) ->
                        val resultMap = HashMap<String, TodayCourseSheet>()
                        list.forEach {
                            val key =
                                "${studentId}:${it.courseName}:${it.teacherName}:${it.location}:${it.type}"
                            var course = resultMap[key]
                            if (course == null) {
                                course = TodayCourseSheet(
                                    it.courseName,
                                    it.teacherName,
                                    TreeSet(it.timeSet),
                                    it.location,
                                    it.studentId,
                                    it.userName,
                                    it.color,
                                    LocalDate.now(),
                                )
                                resultMap[key] = course
                            } else {
                                course.timeSet.addAll(it.timeSet)
                            }
                        }
                        resultCourse.addAll(resultMap.values)
                    }
                    //????????????
                    val now = LocalDateTime.now()
                    resultCourse.sortedBy { it.timeSet.first() }.map { it.calc(now) }
                }

                loadCourseToTable(currentWeek)
            }

            _multiAccountMode.value = getConfig { multiAccountMode }
            _loading.value = true
            var loadFromCloud = forceUpdate
            if (!loadFromCloud) {
                loadFromCloud = getConfig { lastSyncCourse }.isBefore(LocalDate.now())
            }
            val currentWeek = runOnCpu {
                //???????????????
                val startDate =
                    LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone).toLocalDate()
                val days =
                    Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay())
                        .toDays()
                ((days / 7) + 1).toInt()
            }
            _week.value = currentWeek

            //???????????????????????????
            val colorMap = getRawCourseColorList()

            //???????????????????????????
            loadData(getAllCourseList(false), colorMap, currentWeek)
            //?????????????????????
            loadTodayThing(getAllThingList(false))

            //??????????????????
            if (loadFromCloud) {
                loadData(getAllCourseList(true), colorMap, currentWeek)
                //?????????????????????
                loadTodayThing(getAllThingList(true))
                toastMessage("?????????????????????")
            }
            _loading.value = false
        }
    }

    fun loadThingList(forceUpdate: Boolean = true) {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load thing list failed", throwable)
            _todayThing.value = emptyList()
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            //?????????????????????
            loadTodayThing(getAllThingList(false))

            //??????????????????
            if (forceUpdate) {
                //?????????????????????
                loadTodayThing(getAllThingList(true))
            }
        }
    }

    private suspend fun loadCourseToTable(currentWeek: Int) {
        //???????????????????????????
        val colorMap = getRawCourseColorList()
        //???????????????????????????
        val courseList = getAllCourseList(false)
        //????????????
        runOnCpu {
            val allCourseList = courseList.map {
                val thisWeek = it.week.contains(currentWeek)
                val timeString = it.time.formatTimeString()
                Course(
                    courseName = it.name,
                    teacherName = it.teacher,
                    location = it.location,
                    weekSet = it.week,
                    weekString = it.weekString,
                    type = it.type,
                    timeSet = it.time,
                    timeString = timeString,
                    time = it.time.formatTime(),
                    day = it.day,
                    thisWeek = thisWeek,
                    today = false,
                    tomorrow = false,
                    color = colorMap[it.name] ?: ColorPool.hash(it.name),
                    studentId = it.user.studentId,
                    userName = it.user.info.userName,
                )
            }
            //???????????????????????????
            val expandTableCourse = Array(7) { day ->
                Array(11) { index ->
                    CourseSheet.empty(1, index + 1, day + 1)
                }
            }
            //??????????????????key??? ?????????????????????????????????????????????value????????????????????????????????????????????????
            val expandItemMap = HashMap<String, Course>(allCourseList.size)
            //??????key
            allCourseList.forEach {
                it.generateKey()
                expandItemMap[it.key] = it
            }
            //????????????
            allCourseList.forEach { course ->
                course.timeSet.forEach { time ->
                    //????????????
                    expandTableCourse[course.day - 1][time - 1].course.add(course)
                }
            }
            //?????????????????????
            val tableCourse = Array<ArrayList<CourseSheet>>(7) { ArrayList(11) }
            expandTableCourse.forEachIndexed { index, dayArray ->
                val first = dayArray.first()
                var lastKey = first.course.joinToString { it.key }
                var lastSheet = first
                for (i in 1 until dayArray.size) {
                    val thisSheet = dayArray[i]
                    val thisKey = thisSheet.course.joinToString { it.key }
                    if (lastKey != thisKey) {
                        //?????????????????????????????????sheet
                        tableCourse[index].add(lastSheet)
                        lastKey = thisKey
                        lastSheet = thisSheet
                    } else {
                        //??????????????????
                        lastSheet.step++
                    }
                }
                if (tableCourse[index].lastOrNull() != lastSheet) {
                    tableCourse[index].add(lastSheet)
                }
            }
            //?????????????????????
            val tableCourseList = tableCourse.map { array ->
                array.map { courseSheet ->
                    if (courseSheet.course.isNotEmpty()) {
                        val list = courseSheet.course.sortedWith { o1, o2 ->
                            if (o1.thisWeek == o2.thisWeek) {
                                if (o1.type != o2.type) {
                                    o2.type.type.compareTo(o1.type.type)
                                } else {
                                    o1.weekSet.first().compareTo(o2.weekSet.first())
                                }
                            } else {
                                o2.thisWeek.compareTo(o1.thisWeek)
                            }
                        }
                        val show = list.first()
                        courseSheet.showTitle =
                            if (show.thisWeek) show.format(_customUi.value.weekTitleTemplate)
                            else show.format(_customUi.value.weekNotTitleTemplate)
                        courseSheet.course =
                            ArrayList(courseSheet.course.distinct().sortedBy { it.weekSet.first() })
                        courseSheet.color =
                            if (show.thisWeek) colorMap[show.courseName]
                                ?: ColorPool.hash(show.courseName) else XhuColor.notThisWeekBackgroundColor
                        courseSheet.textColor = if (show.thisWeek) Color.White else Color.Gray
                    }
                    courseSheet
                }
            }
            //?????????????????????
            val startDate =
                LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone).toLocalDate()
            val days =
                Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay()).toDays()
            val thisWeek = ((days / 7) + 1).toInt()
            val weekViewArray = Array(20) { index ->
                WeekView(index + 1, thisWeek == index + 1, Array(5) { Array(5) { false } })
            }
            for (dayIndex in 0 until 5) {
                val dayArray = expandTableCourse[dayIndex]
                for (itemIndex in 0 until 10) {
                    val courseSheet = dayArray[itemIndex]
                    val weekSet = courseSheet.course.map { it.weekSet }.flatten().toSet()
                    weekSet.forEach { week ->
                        weekViewArray[week - 1].array[dayIndex][itemIndex / 2] = true
                    }
                }
            }
            //????????????
            _weekView.emit(weekViewArray.toList())
            val showNotThisWeek = getConfig { showNotThisWeek }
            if (!showNotThisWeek) {
                tableCourseList.forEach { array ->
                    array.forEach { sheet ->
                        sheet.course.removeIf { !it.thisWeek }
                    }
                }
            }
            _tableCourse.emit(tableCourseList)
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

    fun checkUnReadNotice() {
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "load notice list failed", throwable)
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            if (SessionManager.mainUserOrNull() == null) return@launch
            _hasUnReadNotice.value = noticeRepo.hasUnReadNotice()
        }
    }

    fun checkUnReadFeedback() {
        if (!isOnline()) {
            return
        }
        viewModelScope.launch(serverExceptionHandler { throwable ->
            Log.w(TAG, "check unread feedback failed", throwable)
        }) {
            if (SessionManager.mainUserOrNull() == null) return@launch
            val firstFeedbackMessageId = getConfig { firstFeedbackMessageId }
            val response = SessionManager.mainUser().withAutoLogin {
                feedbackApi.checkMessage(it, firstFeedbackMessageId).checkLogin()
            }
            _hasUnReadFeedback.value = response.first.newResult
        }
    }

    fun downloadApk() {
        viewModelScope.launch {
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadApkWork>()
                    .build()
            )
        }
    }

    fun downloadPatch() {
        viewModelScope.launch {
            workManager.enqueue(
                OneTimeWorkRequestBuilder<DownloadPatchWork>()
                    .build()
            )
        }
    }

    fun ignoreVersion(version: Version) {
        viewModelScope.launch {
            val ignore = getConfig { ignoreVersionList }
            ignore.add("${version.versionName}-${version.versionCode}")
            setConfig { ignoreVersionList = ignore }
        }
    }
}

fun List<Int>.formatTimeString(): String =
    if (size == 1) "???${this[0]}???" else "${first()}-${last()}???"

private val startArray = arrayOf(
    "08:00",
    "08:55",
    "10:00",
    "10:55",
    "14:00",
    "14:55",
    "16:00",
    "16:55",
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
    "20:40",
    "21:35",
)

fun List<Int>.formatTime(): String = "${startArray[first() - 1]} - ${endArray[last() - 1]}"

data class CustomThingSheet(
    val studentId: String,
    val userName: String,
    val thing: CustomThing,
)

data class CourseSheet(
    //????????????
    var showTitle: String,
    //??????
    var step: Int,
    //??????????????????
    val startIndex: Int,
    //??????
    val day: Int,
    //???????????????????????????
    var course: ArrayList<Course>,
    //??????
    var color: Color,
    //????????????
    var textColor: Color,
) {
    companion object {
        fun empty(step: Int, startIndex: Int, day: Int): CourseSheet = CourseSheet(
            "", step, startIndex, day, arrayListOf(), Color.Unspecified, Color.Unspecified
        )
    }

    fun isEmpty(): Boolean = course.isEmpty()

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

private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

data class TodayCourseSheet(
    val courseName: String,
    val teacherName: String,
    val timeSet: TreeSet<Int>,
    val location: String,
    val studentId: String,
    val userName: String,
    val color: Color,
    val date: LocalDate,
) {
    lateinit var time: String
    lateinit var timeText: Pair<String, String>
    lateinit var timeString: String
    lateinit var courseStatus: CourseStatus

    fun calc(now: LocalDateTime): TodayCourseSheet {
        val list = timeSet.toList()
        time = list.formatTime()
        timeText = startArray[list.first() - 1] to endArray[list.last() - 1]
        timeString = list.formatTimeString()
        val startTime = LocalTime.parse(startArray[list.first() - 1], timeFormatter).atDate(date)
        val endTime = LocalTime.parse(endArray[list.last() - 1], timeFormatter).atDate(date)
        courseStatus = when {
            now.isBefore(startTime) -> CourseStatus.BEFORE
            now.isAfter(endTime) -> CourseStatus.AFTER
            else -> CourseStatus.IN
        }
        return this
    }
}

enum class CourseStatus(
    val title: String,
    val color: Color,
    val backgroundColor: Color,
) {
    BEFORE("?????????", XhuColor.Status.beforeColor, XhuColor.Status.beforeBackgroundColor),
    IN("?????????", XhuColor.Status.inColor, XhuColor.Status.inBackgroundColor),
    AFTER("?????????", XhuColor.Status.afterColor, XhuColor.Status.afterBackgroundColor),
}

class WeekView(
    val weekNum: Int,
    val thisWeek: Boolean,
    val array: Array<Array<Boolean>>
)
