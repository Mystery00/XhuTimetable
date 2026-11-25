package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlinx.datetime.format
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.PracticalCourseView
import vip.mystery0.xhu.timetable.model.TodayCourseView
import vip.mystery0.xhu.timetable.model.TodayThingView
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.format
import vip.mystery0.xhu.timetable.model.response.CalendarDayItemType
import vip.mystery0.xhu.timetable.model.response.CalendarWeekResponse
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.model.transfer.AggregationView
import vip.mystery0.xhu.timetable.module.desc
import vip.mystery0.xhu.timetable.repository.AggregationRepo
import vip.mystery0.xhu.timetable.repository.CourseColorRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.repository.WidgetRepo
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.atStartWeek
import vip.mystery0.xhu.timetable.utils.betweenDays
import vip.mystery0.xhu.timetable.utils.calendarMonthFormatter
import vip.mystery0.xhu.timetable.utils.calendarTimeFormatter
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.formatChina
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.parseColorHexString
import vip.mystery0.xhu.timetable.utils.thingDateTimeFormatter
import kotlin.time.Clock

class MainViewModel : ComposeViewModel() {

    private val courseLoadMutex = Mutex()

    val version = MutableStateFlow<ClientVersion?>(null)

    private val isDarkMode = MutableStateFlow<Boolean?>(null)

    //页面标题
    private val _todayTitle = MutableStateFlow("")
    val todayTitle: StateFlow<String> = _todayTitle

    //日期栏开始时间
    private val _dateStart = MutableStateFlow(LocalDate.now().atStartWeek())
    val dateStart: StateFlow<LocalDate> = _dateStart

    //当前显示的周（随用户切换变化）
    private val _week = MutableStateFlow(1)
    val week: StateFlow<Int> = _week

    //真实当前周（由开学日期计算，不随手动切周变化）
    private val _actualCurrentWeek = MutableStateFlow(1)
    val actualCurrentWeek: StateFlow<Int> = _actualCurrentWeek

    //开学日期（供周课表按周次直接计算日期头）
    private val _termStartDate = MutableStateFlow(LocalDate.now().atStartWeek())
    val termStartDate: StateFlow<LocalDate> = _termStartDate

    //1..20 周预计算课表缓存，支撑本周课表左右丝滑翻页
    private val _allWeekTables = MutableStateFlow<Map<Int, List<List<CourseSheet>>>>(emptyMap())
    val allWeekTables: StateFlow<Map<Int, List<List<CourseSheet>>>> = _allWeekTables

    //1..11 节起止时间（默认西华作息，并优先以本学期真实课程时间校准）
    private val _classTimeSlots = MutableStateFlow(DEFAULT_CLASS_TIME_SLOTS)
    val classTimeSlots: StateFlow<List<Pair<String, String>>> = _classTimeSlots

    //当前学年学期文案（如 2026-2027 第1学期）
    private val _currentYearTermLabel = MutableStateFlow("")
    val currentYearTermLabel: StateFlow<String> = _currentYearTermLabel

    //今日页面当前选中的日期与该周 7 天日期条
    private val _selectedTodayDate = MutableStateFlow(LocalDate.now())
    val selectedTodayDate: StateFlow<LocalDate> = _selectedTodayDate

    private val _isTodayDateCustomSelected = MutableStateFlow(false)
    val isTodayDateCustomSelected: StateFlow<Boolean> = _isTodayDateCustomSelected

    private val _todayWeekDays = MutableStateFlow<List<TodayWeekDayItem>>(emptyList())
    val todayWeekDays: StateFlow<List<TodayWeekDayItem>> = _todayWeekDays

    private val _selectedTodayHeaderText = MutableStateFlow("")
    val selectedTodayHeaderText: StateFlow<String> = _selectedTodayHeaderText

    private var cachedTodayViewList: List<TodayCourseView> = emptyList()
    private var cachedWeekViewList: List<WeekCourseView> = emptyList()
    private var cachedTodayThingList: List<TodayThingView> = emptyList()
    private var cachedColorMap: Map<String, Color> = emptyMap()

    //今日诗词
    private val _poems = MutableStateFlow<Poems?>(null)
    val poems: StateFlow<Poems?> = _poems

    //今日节假日信息
    private val _holiday = MutableStateFlow<HolidayView?>(null)
    val holiday: StateFlow<HolidayView?> = _holiday

    //今日事项列表
    private val _todayThing = MutableStateFlow<List<TodayThingSheet>>(emptyList())
    val todayThing: StateFlow<List<TodayThingSheet>> = _todayThing

    //今日课程列表
    private val _todayCourse = MutableStateFlow<List<TodayCourseSheet>>(emptyList())
    val todayCourse: StateFlow<List<TodayCourseSheet>> = _todayCourse

    //本周课程列表
    private val _tableCourse = MutableStateFlow<List<List<CourseSheet>>>(emptyList())
    val tableCourse: StateFlow<List<List<CourseSheet>>> = _tableCourse

    //日历视图数据
    private val _calendarList = MutableStateFlow<List<CalendarSheetWeek>>(emptyList())
    val calendarList: StateFlow<List<CalendarSheetWeek>> = _calendarList

    private val _weekView = MutableStateFlow<List<WeekView>>(emptyList())
    val weekView: StateFlow<List<WeekView>> = _weekView

    private val _practicalCourseList = MutableStateFlow<List<PracticalCourseShowView>>(emptyList())
    val practicalCourseList: StateFlow<List<PracticalCourseShowView>> = _practicalCourseList

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading


    private val _multiAccountMode = MutableStateFlow(false)
    val multiAccountMode: StateFlow<Boolean> = _multiAccountMode

    private val _enableCalendarView = MutableStateFlow(false)
    val enableCalendarView: StateFlow<Boolean> = _enableCalendarView

    private val _showStatus = MutableStateFlow(false)
    val showStatus: StateFlow<Boolean> = _showStatus

    private val _showTomorrowCourse = MutableStateFlow(false)
    val showTomorrowCourse: StateFlow<Boolean> = _showTomorrowCourse

    private val _backgroundImage = MutableStateFlow<Any>(Unit)
    val backgroundImage: StateFlow<Any> = _backgroundImage

    private val _backgroundImageBlur = MutableStateFlow(1F)
    val backgroundImageBlur: StateFlow<Float> = _backgroundImageBlur

    private val _emptyUser = MutableStateFlow(false)
    val emptyUser: StateFlow<Boolean> = _emptyUser

    private val _mainUser = MutableStateFlow<User?>(null)
    val mainUser: StateFlow<User?> = _mainUser

    private val _customUi = MutableStateFlow(CustomUi.DEFAULT)
    val customUi: StateFlow<CustomUi> = _customUi

    init {
        logger.i(
            "Week header initialized: viewModel=${hashCode()}, today=${LocalDate.now()}, " +
                "dateStart=${_dateStart.value}, weekday=${_dateStart.value.dayOfWeek.isoDayNumber}"
        )
        viewModelScope.safeLaunch {
            loadFromConfig()
            val mainUser = UserStore.getMainUser()
            _mainUser.value = mainUser
            _emptyUser.value = mainUser == null
            StartRepo.version.collectLatest {
                if (it == ClientVersion.EMPTY) {
                    version.value = null
                } else {
                    version.value = it
                }
            }
        }
        showPoems()
        calculateTodayTitle()

        loadLocalDataToState()
        //加载今日节假日信息
        loadTodayHoliday()
    }

    private suspend fun loadFromConfig() {
        _multiAccountMode.value = getConfigStore { multiAccountMode }
        _enableCalendarView.value = getConfigStore { enableCalendarView && !multiAccountMode }
        _showStatus.value = getConfigStore { showStatus }
        _showTomorrowCourse.value =
            getConfigStore { showTomorrowCourseTime }?.let { LocalTime.now() > it } ?: false
        _customUi.value = getConfigStore { customUi }
        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        _currentYearTermLabel.value = "${nowYear}-${nowYear + 1} 第${nowTerm}学期"
    }

    fun loadConfig() {
        viewModelScope.safeLaunch {
            loadFromConfig()
        }
    }

    fun loadBackground(isDarkModeValue: Boolean? = null) {
        if (isDarkModeValue != null && isDarkModeValue == isDarkMode.value) {
            return
        }
        if (isDarkMode.value == null && isDarkModeValue == null) return
        viewModelScope.safeLaunch {
            if (isDarkModeValue != null) {
                isDarkMode.value = isDarkModeValue
            }
            val disable = getConfigStore { disableBackgroundWhenNight }
            if (disable && isDarkMode.value == true) {
                _backgroundImage.value = Unit
                return@safeLaunch
            } else {
                _backgroundImage.value =
                    getConfigStore { backgroundImage } ?: XhuImages.defaultBackgroundImageUri
            }
            _backgroundImageBlur.value = getConfigStore { customUi }.backgroundImageBlur
        }
    }

    fun checkMainUser() {
        viewModelScope.safeLaunch {
            val mainUser = UserStore.getMainUser()
            _mainUser.value = mainUser
            _emptyUser.value = mainUser == null
        }
    }

    fun reloadPoems() {
        showPoems()
    }

    private fun showPoems() {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("showPoems: ", throwable)
        }) {
            _poems.value = PoemsStore.loadPoems()
        }
    }

    fun calculateTodayTitle() {
        viewModelScope.safeLaunch {
            val showTomorrow = getConfigStore { showTomorrowCourseTime }?.let {
                LocalTime.now() > it
            } ?: false

            _todayTitle.value = WidgetRepo.calculateDateTitle(showTomorrow)
        }
    }

    private suspend fun getMainPageData(
        forceLoadFromCloud: Boolean,
        forceLoadFromLocal: Boolean,
    ): AggregationView {
        val showCustomCourse = getConfigStore { showCustomCourseOnWeek }
        val showCustomThing = getConfigStore { showCustomThing }
        val view = AggregationRepo.fetchAggregationMainPage(
            forceLoadFromCloud,
            forceLoadFromLocal,
            showCustomCourse,
            showCustomThing,
        )
        setCacheStore { lastSyncCourse = LocalDate.now() }
        if (view.loadWarning.isNotBlank()) {
            toastMessage(view.loadWarning)
        }
        return view
    }

    /**
     * 初始化时候加载数据的方法
     */
    fun loadLocalDataToState(changeWeekOnly: Boolean = false) {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load local course list failed", throwable)
            _loading.value = false
            toastMessage(throwable.desc())
        }) {
            courseLoadMutex.withLock {
                try {
                    //设置加载状态
                    _loading.value = true
                    //加载课表相关配置项
                    val (currentWeek, loadFromCloud) = loadCourseConfig(forceUpdate = false)
                    //获取缓存的课程数据
                    val data = getMainPageData(forceLoadFromCloud = false, forceLoadFromLocal = true)
                    val calendarData = AggregationRepo.fetchAggregationCalendarPage(
                        forceLoadFromCloud = false,
                        forceLoadFromLocal = true
                    )
                    //获取自定义颜色列表
                    val colorMap = CourseColorRepo.getRawCourseColorList()
                    withContext(Dispatchers.Default) {
                        cachedTodayViewList = data.todayViewList
                        cachedWeekViewList = data.weekViewList
                        cachedTodayThingList = data.todayThingList
                        cachedColorMap = colorMap
                        updateClassTimeSlots(data.weekViewList)
                        val targetDate = resolveTargetTodayDate()
                        //加载今日列表的数据
                        loadTodayCourseForDate(
                            targetDate = targetDate,
                            courseList = data.todayViewList,
                            weekViewList = data.weekViewList,
                            colorMap = colorMap,
                        )
                        //加载今日事项
                        loadTodayThingForDate(targetDate, data.todayThingList)
                        //加载周列表的数据
                        loadCourseToTable(
                            currentWeek,
                            currentWeek,
                            data.weekViewList,
                            colorMap,
                            changeWeekOnly,
                        )
                        //加载日历视图的数据
                        loadCalendar(
                            currentWeek = currentWeek,
                            calendarList = calendarData,
                            colorMap = colorMap,
                            weekViewList = data.weekViewList,
                            todayThingList = data.todayThingList,
                        )
                        //加载实践课程
                        loadPracticalCourse(data.practicalCourseList)
                    }

                    if (loadFromCloud) {
                        //需要从云端加载数据
                        val cloudData =
                            getMainPageData(forceLoadFromCloud = true, forceLoadFromLocal = false)
                        val cloudCalendarData = AggregationRepo.fetchAggregationCalendarPage(
                            forceLoadFromCloud = true,
                            forceLoadFromLocal = false
                        )
                        withContext(Dispatchers.Default) {
                            cachedTodayViewList = cloudData.todayViewList
                            cachedWeekViewList = cloudData.weekViewList
                            cachedTodayThingList = cloudData.todayThingList
                            cachedColorMap = colorMap
                            updateClassTimeSlots(cloudData.weekViewList)
                            val targetDate = resolveTargetTodayDate()
                            //加载今日列表的数据
                            loadTodayCourseForDate(
                                targetDate = targetDate,
                                courseList = cloudData.todayViewList,
                                weekViewList = cloudData.weekViewList,
                                colorMap = colorMap,
                            )
                            //加载今日事项
                            loadTodayThingForDate(targetDate, cloudData.todayThingList)
                            //加载周列表的数据
                            loadCourseToTable(
                                currentWeek,
                                currentWeek,
                                cloudData.weekViewList,
                                colorMap,
                                changeWeekOnly,
                            )
                            //加载日历视图的数据
                            loadCalendar(
                                currentWeek = currentWeek,
                                calendarList = cloudCalendarData,
                                colorMap = colorMap,
                                weekViewList = cloudData.weekViewList,
                                todayThingList = cloudData.todayThingList,
                            )
                            //加载实践课程
                            loadPracticalCourse(cloudData.practicalCourseList)
                        }
                        toastMessage("数据同步成功！")
                    }
                } finally {
                    _loading.value = false
                }
            }
        }
    }

    /**
     * 手动刷新加载数据的方法
     */
    fun refreshCloudDataToState() {
        viewModelScope.safeLaunch(onException = networkErrorHandler { throwable ->
            logger.w("load course list failed", throwable)
            _loading.value = false
            if (!GlobalConfigStore.showOldCourseWhenFailed) {
                _tableCourse.value = emptyList()
            }
            toastMessage(throwable.desc())
        }) {
            courseLoadMutex.withLock {
                try {
                    //设置加载状态
                    _loading.value = true
                    //加载课表相关配置项
                    val (currentWeek, _) = loadCourseConfig(forceUpdate = false)
                    //从云端加载数据
                    val cloudData = getMainPageData(
                        forceLoadFromCloud = true,
                        forceLoadFromLocal = false,
                    )
                    val cloudCalendarData = AggregationRepo.fetchAggregationCalendarPage(
                        forceLoadFromCloud = true,
                        forceLoadFromLocal = false,
                    )
                    //获取自定义颜色列表
                    val colorMap = CourseColorRepo.getRawCourseColorList()
                    withContext(Dispatchers.Default) {
                        cachedTodayViewList = cloudData.todayViewList
                        cachedWeekViewList = cloudData.weekViewList
                        cachedTodayThingList = cloudData.todayThingList
                        cachedColorMap = colorMap
                        updateClassTimeSlots(cloudData.weekViewList)
                        val targetDate = resolveTargetTodayDate()
                        //加载今日列表的数据
                        loadTodayCourseForDate(
                            targetDate = targetDate,
                            courseList = cloudData.todayViewList,
                            weekViewList = cloudData.weekViewList,
                            colorMap = colorMap,
                        )
                        //加载今日事项
                        loadTodayThingForDate(targetDate, cloudData.todayThingList)
                        //加载周列表的数据
                        loadCourseToTable(
                            currentWeek,
                            currentWeek,
                            cloudData.weekViewList,
                            colorMap,
                            false,
                        )
                        //加载日历视图的数据
                        loadCalendar(
                            currentWeek = currentWeek,
                            calendarList = cloudCalendarData,
                            colorMap = colorMap,
                            weekViewList = cloudData.weekViewList,
                            todayThingList = cloudData.todayThingList,
                        )
                        //加载实践课程
                        loadPracticalCourse(cloudData.practicalCourseList)
                    }
                    toastMessage("数据同步成功！")
                } finally {
                    _loading.value = false
                }
            }
        }
    }
    /**
     * 复用的加载课表相关配置项的方法
     * @return 是否需要从云端加载课表
     */
    private suspend fun loadCourseConfig(forceUpdate: Boolean = true): Pair<Int, Boolean> {
        _multiAccountMode.value = getConfigStore { multiAccountMode }
        val nowYear = getConfigStore { nowYear }
        val nowTerm = getConfigStore { nowTerm }
        _currentYearTermLabel.value = "${nowYear}-${nowYear + 1} 第${nowTerm}学期"
        var loadFromCloud = forceUpdate
        if (!loadFromCloud) {
            loadFromCloud = getCacheStore { lastSyncCourse } < LocalDate.now()
        }
        val currentWeek = WidgetRepo.calculateWeek()
        _actualCurrentWeek.value = currentWeek
        _week.value = currentWeek
        val termStartDate = getConfigStore { termStartDate }
        _termStartDate.value = termStartDate
        _dateStart.value = termStartDate.plus(currentWeek.toLong() - 1, DateTimeUnit.WEEK)
        logger.i(
            "Week header loaded: viewModel=${hashCode()}, today=${LocalDate.now()}, " +
                "termStartDate=$termStartDate, week=$currentWeek, dateStart=${_dateStart.value}, " +
                "weekday=${_dateStart.value.dayOfWeek.isoDayNumber}"
        )

        return currentWeek to loadFromCloud
    }

    private suspend fun resolveDefaultTodayDate(): LocalDate {
        val today = LocalDate.now()
        val showTomorrow =
            getConfigStore { showTomorrowCourseTime }?.let { LocalTime.now() > it } ?: false
        return if (showTomorrow) today.plus(1, DateTimeUnit.DAY) else today
    }

    private suspend fun resolveTargetTodayDate(): LocalDate {
        val defaultDate = resolveDefaultTodayDate()
        return if (_isTodayDateCustomSelected.value) {
            _selectedTodayDate.value
        } else {
            _selectedTodayDate.value = defaultDate
            defaultDate
        }
    }

    fun selectTodayDate(date: LocalDate) {
        viewModelScope.safeLaunch {
            val defaultDate = resolveDefaultTodayDate()
            _selectedTodayDate.value = date
            _isTodayDateCustomSelected.value = date != defaultDate
            withContext(Dispatchers.Default) {
                loadTodayCourseForDate(
                    targetDate = date,
                    courseList = cachedTodayViewList,
                    weekViewList = cachedWeekViewList,
                    colorMap = cachedColorMap,
                )
                loadTodayThingForDate(date, cachedTodayThingList)
            }
        }
    }

    fun resetTodayDateToDefault() {
        viewModelScope.safeLaunch {
            val defaultDate = resolveDefaultTodayDate()
            _selectedTodayDate.value = defaultDate
            _isTodayDateCustomSelected.value = false
            withContext(Dispatchers.Default) {
                loadTodayCourseForDate(
                    targetDate = defaultDate,
                    courseList = cachedTodayViewList,
                    weekViewList = cachedWeekViewList,
                    colorMap = cachedColorMap,
                )
                loadTodayThingForDate(defaultDate, cachedTodayThingList)
            }
        }
    }

    private fun updateClassTimeSlots(weekViewList: List<WeekCourseView>) {
        val starts = DEFAULT_CLASS_TIME_SLOTS.map { it.first }.toMutableList()
        val ends = DEFAULT_CLASS_TIME_SLOTS.map { it.second }.toMutableList()
        val startSet = BooleanArray(11)
        val endSet = BooleanArray(11)
        weekViewList.forEach { course ->
            val s = course.startDayTime
            val e = course.endDayTime
            if (s in 1..11 && !startSet[s - 1] && (course.startTime.hour > 0 || course.startTime.minute > 0)) {
                starts[s - 1] = course.startTime.format(Formatter.TIME_NO_SECONDS)
                startSet[s - 1] = true
            }
            if (e in 1..11 && !endSet[e - 1] && (course.endTime.hour > 0 || course.endTime.minute > 0)) {
                ends[e - 1] = course.endTime.format(Formatter.TIME_NO_SECONDS)
                endSet[e - 1] = true
            }
        }
        _classTimeSlots.value = (0 until 11).map { starts[it] to ends[it] }
    }

    /**
     * 按指定日期计算今日课表、周日期条与标题信息
     */
    private suspend fun loadTodayCourseForDate(
        targetDate: LocalDate,
        courseList: List<TodayCourseView>,
        weekViewList: List<WeekCourseView>,
        colorMap: Map<String, Color>,
    ) {
        val today = LocalDate.now()
        val customAccountTitle = getConfigStore { customAccountTitle }
        val showDay = targetDate.dayOfWeek
        val showCurrentWeek = WidgetRepo.calculateWeek(targetDate)

        // 更新顶部日期头文案，如：11月25日 第12周 | 周三
        val weekLabel = when {
            showCurrentWeek <= 0 -> "未开学"
            showCurrentWeek > 20 -> "假期"
            else -> "第${showCurrentWeek}周"
        }
        _selectedTodayHeaderText.value =
            "${targetDate.month.number}月${targetDate.day}日 $weekLabel | ${targetDate.dayOfWeek.formatChina()}"

        // 更新本周 7 天日期选择器数据（周一至周日）
        val weekMonday = targetDate.atStartWeek()
        _todayWeekDays.value = (0..6).map { offset ->
            val date = weekMonday.plus(offset, DateTimeUnit.DAY)
            val dateWeek = WidgetRepo.calculateWeek(date)
            val hasCourse = courseList.any {
                it.weekList.contains(dateWeek) && it.day == date.dayOfWeek
            }
            TodayWeekDayItem(
                date = date,
                weekShort = date.dayOfWeek.formatShortEnglish(),
                dayNumber = date.day.toString(),
                isToday = date == today,
                isSelected = date == targetDate,
                hasCourse = hasCourse,
            )
        }

        // 过滤出目标日期的课程（使用 copy 避免污染缓存的 endDayTime）
        val showList = courseList
            .filter { it.weekList.contains(showCurrentWeek) && it.day == showDay }
            .sortedBy { it.startDayTime }
            .map { it.copy() }
        if (showList.isEmpty()) {
            _todayCourse.value = emptyList()
            return
        }
        //合并相同课程
        val resultList = ArrayList<TodayCourseView>(showList.size)
        //计算key与设置颜色
        showList.forEach {
            it.backgroundColor = colorMap[it.courseName] ?: ColorPool.hash(it.courseName)
            it.generateKey()
        }
        showList.groupBy { it.user.studentId }.forEach { (_, list) ->
            var last = list.first()
            var lastKey = last.key
            list.forEachIndexed { index, todayCourseView ->
                if (index == 0) {
                    resultList.add(todayCourseView)
                    return@forEachIndexed
                }
                val thisKey = todayCourseView.key
                if (lastKey == thisKey) {
                    if (last.endDayTime == todayCourseView.startDayTime - 1) {
                        //合并两个课程
                        last.endDayTime = todayCourseView.endDayTime
                        last.endTime = todayCourseView.endTime
                    } else {
                        //两个课程相同但是节次不连续，不合并
                        resultList.add(todayCourseView)
                        last = todayCourseView
                        lastKey = thisKey
                    }
                } else {
                    resultList.add(todayCourseView)
                    last = todayCourseView
                    lastKey = thisKey
                }
            }
            if (resultList.last() != last) {
                resultList.add(last)
            }
        }
        //最后按照开始节次排序
        val now = LocalDateTime.now()
        val sheets = resultList.sortedBy { it.startDayTime }.map { item ->
            item.updateTime()
            val matchedWeekCourse = weekViewList.firstOrNull { w ->
                w.courseName == item.courseName &&
                    w.day == item.day &&
                    w.weekList.contains(showCurrentWeek) &&
                    w.startDayTime == item.startDayTime &&
                    w.location == item.location
            } ?: weekViewList.firstOrNull { w ->
                w.courseName == item.courseName &&
                    w.day == item.day &&
                    w.weekList.contains(showCurrentWeek)
            }
            TodayCourseSheet(
                courseName = item.courseName,
                teacherName = item.teacher,
                timeSet = (item.startDayTime..item.endDayTime).sorted().toMutableSet(),
                timeText = item.startTime to item.endTime,
                timeString = item.courseDayTime,
                location = item.location,
                color = item.backgroundColor,
                showDate = targetDate,
                accountTitle = customAccountTitle.formatToday(item.user.info),
                weekStr = matchedWeekCourse?.weekStr.orEmpty(),
                extraData = matchedWeekCourse?.extraData.orEmpty(),
            ).calc(now)
        }
        // 标记当天的“下一节课”（仅查看今天时，找到最早一门未开始的课程）
        val nextCourseIndex = if (targetDate == today) {
            sheets.indexOfFirst { it.courseStatus == CourseStatus.BEFORE }
        } else {
            -1
        }
        _todayCourse.value = sheets.mapIndexed { idx, sheet ->
            if (idx == nextCourseIndex) sheet.copy(isNextCourse = true) else sheet
        }
    }

    /**
     * 按指定日期加载自定义事项
     */
    private suspend fun loadTodayThingForDate(
        targetDate: LocalDate,
        thingList: List<TodayThingView>,
    ) {
        val today = LocalDate.now()
        val customAccountTitle = getConfigStore { customAccountTitle }
        val showInstant = if (targetDate == today) {
            Clock.System.now()
        } else {
            targetDate.atTime(0, 0).asInstant()
        }
        val showList = thingList.filter { it.showOnDay(showInstant) }.sortedBy { it.sort }
        if (showList.isEmpty()) {
            _todayThing.value = emptyList()
            return
        }
        _todayThing.value = showList.map {
            buildTodayThingSheet(it, customAccountTitle.formatToday(it.user.info))
        }
    }

    private fun buildTodayThingSheet(
        thing: TodayThingView,
        accountTitle: String,
    ): TodayThingSheet {
        val startDateTime = thing.startTime.asLocalDateTime()
        val endDateTime = thing.endTime.asLocalDateTime()
        val timeText = buildString {
            if (thing.allDay) {
                append(startDateTime.date.format(dateFormatter))
            } else {
                append(startDateTime.format(thingDateTimeFormatter))
            }
            if (thing.saveAsCountDown) {
                return@buildString
            }
            append(" - ")
            if (thing.allDay) {
                append(endDateTime.date.format(dateFormatter))
            } else {
                append(endDateTime.format(thingDateTimeFormatter))
            }
        }
        val remainDays = betweenDays(LocalDate.now(), startDateTime.date)
        return TodayThingSheet(
            thing.title,
            thing.location,
            thing.allDay,
            timeText,
            thing.remark,
            thing.color,
            thing.saveAsCountDown,
            remainDays,
            accountTitle,
        )
    }

    /**
     * 复用的加载今日节假日信息的方法
     */
    fun loadTodayHoliday() {
        viewModelScope.safeLaunch {
            val showHoliday = getConfigStore { showHoliday }
            if (!showHoliday) {
                _holiday.value = null
                return@safeLaunch
            }
            val (nowHoliday, tomorrowHoliday) = getCacheStore { holiday }
            val showTomorrow =
                getConfigStore { showTomorrowCourseTime }?.let { LocalTime.now() > it }
                    ?: false
            val offDayColor = Color(0xFF03a9f4)
            val workDayColor = Color(0xFFff3d00)
            if (showTomorrow) {
                val holidayName = tomorrowHoliday?.name ?: ""
                val isOffDay = tomorrowHoliday?.isOffDay ?: false
                val special = holidayName.isNotBlank()
                val s = if (isOffDay) "~" else "，但是要上班，别忘记上课哦~"
                if (!special) {
                    _holiday.value = null
                } else {
                    _holiday.value = HolidayView(
                        "明天是 $holidayName$s",
                        if (isOffDay) offDayColor else workDayColor,
                    )
                }
            } else {
                val holidayName = nowHoliday?.name ?: ""
                val isOffDay = nowHoliday?.isOffDay ?: false
                val special = holidayName.isNotBlank()
                val s = if (isOffDay) "~" else "，但是要上班，别忘记上课哦~"
                if (!special) {
                    _holiday.value = null
                } else {
                    _holiday.value = HolidayView(
                        "今天是 $holidayName$s",
                        if (isOffDay) offDayColor else workDayColor,
                    )
                }
            }
        }
    }

    /**
     * 复用的加载本周课表的方法
     * @param currentWeek 当前周
     * @param showWeek 需要显示周
     * @param courseList 课程列表
     * @param changeWeekOnly 是否只是切换周数
     */
    private fun buildTableForWeek(
        showWeek: Int,
        courseList: List<WeekCourseView>,
        colorMap: Map<String, Color>,
        showNotThisWeek: Boolean,
        customUi: CustomUi,
    ): List<List<CourseSheet>> {
        val cloned = courseList.map { course ->
            course.copy().apply {
                thisWeek = weekList.contains(showWeek)
                backgroundColor = colorMap[courseName] ?: ColorPool.hash(courseName)
                generateKey()
            }
        }
        val expandTableCourse = Array(7) { day ->
            Array(11) { index ->
                CourseSheet.empty(1, index + 1, day + 1)
            }
        }
        cloned.forEach { course ->
            val day = course.day.isoDayNumber - 1
            if (day in 0..6) {
                (course.startDayTime..course.endDayTime).forEach { time ->
                    if (time in 1..11) {
                        expandTableCourse[day][time - 1].course.add(course)
                    }
                }
            }
        }
        val tableCourse = Array(7) { day ->
            val dayArray = expandTableCourse[day]
            val first = dayArray.first()
            val result = ArrayList<CourseSheet>(dayArray.size)
            var last = first
            var lastKey = first.course.sortedBy { it.key }.joinToString { it.key }
            dayArray.forEachIndexed { index, courseSheet ->
                if (index == 0) {
                    return@forEachIndexed
                }
                val thisKey = courseSheet.course.sortedBy { it.key }.joinToString { it.key }
                if (lastKey == thisKey) {
                    last.step++
                } else {
                    result.add(last)
                    last = courseSheet
                    lastKey = thisKey
                }
            }
            if (result.lastOrNull() != last) {
                result.add(last)
            }
            result
        }
        tableCourse.forEach { array ->
            array.forEach { sheet ->
                if (sheet.course.isNotEmpty()) {
                    sheet.course.sort()
                    val show = sheet.course.first()
                    sheet.course = ArrayList(sheet.course.sortedBy { it.weekList.firstOrNull() ?: 0 })
                    if (show.thisWeek) {
                        sheet.showTitle = show.format(customUi.weekTitleTemplate)
                        sheet.color = colorMap[show.courseName] ?: ColorPool.hash(show.courseName)
                        sheet.textColor = Color.White
                    } else {
                        sheet.showTitle = show.format(customUi.weekNotTitleTemplate)
                        sheet.color = XhuColor.notThisWeekBackgroundColor
                        sheet.textColor = Color.Gray
                    }
                }
            }
        }
        if (!showNotThisWeek) {
            tableCourse.forEach { array ->
                array.forEach { sheet ->
                    val temp = sheet.course.filter { it.thisWeek }
                    sheet.course.clear()
                    sheet.course.addAll(temp)
                }
            }
        }
        return tableCourse.toList()
    }

    private suspend fun loadCourseToTable(
        currentWeek: Int,
        showWeek: Int,
        courseList: List<WeekCourseView>,
        colorMap: Map<String, Color>,
        changeWeekOnly: Boolean,
    ) {
        val showNotThisWeek = getConfigStore { showNotThisWeek }
        val customUi = _customUi.value
        val currentTable = buildTableForWeek(showWeek, courseList, colorMap, showNotThisWeek, customUi)
        _tableCourse.value = currentTable

        val allTables = LinkedHashMap<Int, List<List<CourseSheet>>>(20)
        for (w in 1..20) {
            allTables[w] = if (w == showWeek) {
                currentTable
            } else {
                buildTableForWeek(w, courseList, colorMap, showNotThisWeek, customUi)
            }
        }
        _allWeekTables.value = allTables

        if (changeWeekOnly) {
            return
        }

        val weekViewArray = Array(20) { index ->
            WeekView(index + 1, currentWeek == index + 1, Array(5) { Array(5) { false } })
        }
        courseList.forEach { course ->
            val dayIndex = course.day.isoDayNumber - 1
            if (dayIndex in 0 until 5) {
                (course.startDayTime..course.endDayTime).forEach { time ->
                    val itemIndex = time - 1
                    if (itemIndex in 0 until 10) {
                        course.weekList.forEach { week ->
                            if (week in 1..20) {
                                weekViewArray[week - 1].array[dayIndex][itemIndex / 2] = true
                            }
                        }
                    }
                }
            }
        }
        _weekView.value = weekViewArray.toList()
    }
    /**
     * 复用的加载本周课表的方法
     * @param currentWeek 当前周
     * @param calendarList 日历列表
     */
    private fun loadCalendar(
        currentWeek: Int,
        calendarList: List<CalendarWeekResponse>,
        colorMap: Map<String, Color>,
        weekViewList: List<WeekCourseView>,
        todayThingList: List<TodayThingView>,
    ) {
        val now = LocalDate.now()
        var alreadySet = false
        val resultList = calendarList.map { week ->
            val titleBuilder = StringBuilder()
            titleBuilder.append("第").append(week.weekNum).append("周")
            titleBuilder.append("  ")
            titleBuilder.append(week.startDate.month.number).append("月")
            titleBuilder.append(week.startDate.day).append("日")
            titleBuilder.append("至")
            if (week.startDate.month.number != week.endDate.month.number) {
                titleBuilder.append(week.endDate.month.number).append("月")
            }
            titleBuilder.append(week.endDate.day).append("日")
            val dayItems = ArrayList<CalendarSheet>()
            week.items.forEach { day ->
                val dayTitleBuilder = StringBuilder()
                dayTitleBuilder.append(
                    day.showDate.dayOfWeek.formatChina()
                )
                dayTitleBuilder.append("\n")
                dayTitleBuilder.append(day.showDate.day)
                var today = false
                if (day.showDate >= now) {
                    if (!alreadySet) {
                        today = true
                        alreadySet = true
                    }
                }
                dayItems.add(
                    CalendarSheet.day(
                        date = day.showDate,
                        title = dayTitleBuilder.toString(),
                        today = today,
                        items = day.items.map { item ->
                            val textBuilder = StringBuilder()
                            textBuilder.append(item.startTime.format(calendarTimeFormatter))
                            textBuilder.append("-")
                            textBuilder.append(item.endTime.format(calendarTimeFormatter))
                            val color = when (item.type) {
                                CalendarDayItemType.COURSE, CalendarDayItemType.CUSTOM_COURSE ->
                                    colorMap[item.title]
                                        ?: ColorPool.hash(item.title)

                                CalendarDayItemType.EXPERIMENT_COURSE ->
                                    colorMap[item.courseName]
                                        ?: ColorPool.hash(item.courseName)

                                CalendarDayItemType.CUSTOM_THING ->
                                    item.customThingColor.parseColorHexString()
                            }
                            if (item.location.isNotBlank()) {
                                textBuilder.append("，").append(item.location)
                            }

                            val matchedCourse = if (item.type != CalendarDayItemType.CUSTOM_THING) {
                                val exact = weekViewList.firstOrNull { course ->
                                    course.day == day.showDate.dayOfWeek &&
                                        course.weekList.contains(week.weekNum) &&
                                        course.courseName == item.title &&
                                        course.startTime == item.startTime &&
                                        course.endTime == item.endTime
                                } ?: weekViewList.firstOrNull { course ->
                                    course.day == day.showDate.dayOfWeek &&
                                        course.weekList.contains(week.weekNum) &&
                                        course.courseName == item.title &&
                                        course.location == item.location
                                } ?: weekViewList.firstOrNull { course ->
                                    course.day == day.showDate.dayOfWeek &&
                                        course.weekList.contains(week.weekNum) &&
                                        course.courseName == item.title
                                }
                                exact?.copy()?.apply {
                                    thisWeek = week.weekNum == currentWeek
                                    backgroundColor = color
                                }
                            } else null

                            val matchedThing = if (item.type == CalendarDayItemType.CUSTOM_THING) {
                                todayThingList.firstOrNull { thing ->
                                    thing.title == item.title && thing.location == item.location
                                }?.let { buildTodayThingSheet(it, "") }
                            } else null

                            CalendarSheetItem(
                                title = item.title,
                                subtitle = item.courseName,
                                text = textBuilder.toString(),
                                color = color,
                                type = item.type,
                                date = day.showDate,
                                weekNum = week.weekNum,
                                startTime = item.startTime,
                                endTime = item.endTime,
                                location = item.location,
                                courseDetail = matchedCourse,
                                thingDetail = matchedThing,
                            )
                        }
                    )
                )
            }
            if (week.startDate.month.number != week.endDate.month.number) {
                //跨月了，那么需要补充月份标题
                if (dayItems.isEmpty()) {
                    //这个周没有课，直接加一个月标题
                    dayItems.add(CalendarSheet.monthHeader(week.endDate))
                } else {
                    //这个周有课，找出第一个属于下个月的坐标
                    val indexOfFirst =
                        dayItems.indexOfFirst { it.date.month.number == week.endDate.month.number }
                    if (indexOfFirst == -1) {
                        //没有找到，那么直接加到后面
                        dayItems.add(CalendarSheet.monthHeader(week.endDate))
                    } else {
                        //找到了，那么加到前面
                        dayItems.add(indexOfFirst, CalendarSheet.monthHeader(week.endDate))
                    }
                }
            } else {
                //没有跨月，那么判断第一天是不是1号
                if (week.startDate.day == 1) {
                    //是1号，那么加一个月标题
                    dayItems.add(0, CalendarSheet.monthHeader(week.startDate))
                }
            }
            CalendarSheetWeek(
                weekNum = week.weekNum,
                cursor = week.weekNum >= currentWeek,
                title = titleBuilder.toString(),
                items = dayItems,
            )
        }
        _calendarList.value = resultList
    }

    /**
     * 复用的加载实践课程数据的方法
     */
    private suspend fun loadPracticalCourse(courseList: List<PracticalCourseView>) {
        val customAccountTitle = getConfigStore { customAccountTitle }
        val resultList = courseList.map {
            PracticalCourseShowView(
                it.courseName,
                it.teacherName,
                it.showWeek,
                it.credit,
                it.color,
                customAccountTitle.formatPracticalCourse(it.user.info),
            )
        }
        _practicalCourseList.value = resultList
    }

    fun changeCurrentWeek(currentWeek: Int) {
        viewModelScope.safeLaunch {
            _week.value = currentWeek
            val termStartDate = getConfigStore { termStartDate }
            _termStartDate.value = termStartDate
            _dateStart.value = termStartDate.plus(currentWeek.toLong() - 1, DateTimeUnit.WEEK)
            val cached = _allWeekTables.value[currentWeek]
            if (cached != null) {
                _tableCourse.value = cached
            } else {
                val data = getMainPageData(forceLoadFromCloud = false, forceLoadFromLocal = true)
                val weekList = data.weekViewList
                val colorMap = CourseColorRepo.getRawCourseColorList()
                loadCourseToTable(_actualCurrentWeek.value, currentWeek, weekList, colorMap, true)
            }
            logger.i(
                "Week header changed: viewModel=${hashCode()}, today=${LocalDate.now()}, " +
                    "termStartDate=$termStartDate, week=$currentWeek, dateStart=${_dateStart.value}, " +
                    "weekday=${_dateStart.value.dayOfWeek.isoDayNumber}"
            )
        }
    }
}

data class CourseSheet(
    //显示标题
    var showTitle: String,
    //步长
    var step: Int,
    //开始节次序号
    val startIndex: Int,
    //周几
    val day: Int,
    //当前格子的课程列表
    var course: ArrayList<WeekCourseView>,
    //颜色
    var color: Color,
    //文本颜色
    var textColor: Color,
) {
    companion object {
        fun empty(step: Int, startIndex: Int, day: Int): CourseSheet = CourseSheet(
            "", step, startIndex, day, arrayListOf(), Color.Unspecified, Color.Unspecified
        )
    }

    val isEmptyInstance: Boolean
        get() = showTitle == "" && color == Color.Unspecified && textColor == Color.Unspecified

    fun isEmpty(): Boolean = course.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        other as CourseSheet

        if (showTitle != other.showTitle) return false
        if (color != other.color) return false
        return course == other.course
    }

    override fun hashCode(): Int {
        var result = showTitle.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + course.hashCode()
        return result
    }
}

@Immutable
data class TodayCourseSheet(
    val courseName: String,
    val teacherName: String,
    val timeSet: MutableSet<Int>,
    val timeText: Pair<LocalTime, LocalTime>,
    val timeString: String,
    val location: String,
    val color: Color,
    val showDate: LocalDate,
    val accountTitle: String,
    val courseStatus: CourseStatus = CourseStatus.IN,
    val isNextCourse: Boolean = false,
    val weekStr: String = "",
    val extraData: List<String> = emptyList(),
) {
    fun calc(now: LocalDateTime): TodayCourseSheet {
        val startTime = showDate.atTime(timeText.first)
        val endTime = showDate.atTime(timeText.second)
        val courseStatus = when {
            now < startTime -> CourseStatus.BEFORE
            now > endTime -> CourseStatus.AFTER
            else -> CourseStatus.IN
        }
        return copy(courseStatus = courseStatus)
    }
}

@Immutable
data class TodayWeekDayItem(
    val date: LocalDate,
    val weekShort: String,
    val dayNumber: String,
    val isToday: Boolean,
    val isSelected: Boolean,
    val hasCourse: Boolean,
)

private fun DayOfWeek.formatShortEnglish(): String = when (this) {
    DayOfWeek.MONDAY -> "Mon"
    DayOfWeek.TUESDAY -> "Tue"
    DayOfWeek.WEDNESDAY -> "Wed"
    DayOfWeek.THURSDAY -> "Thu"
    DayOfWeek.FRIDAY -> "Fri"
    DayOfWeek.SATURDAY -> "Sat"
    DayOfWeek.SUNDAY -> "Sun"
}

val DEFAULT_CLASS_TIME_SLOTS: List<Pair<String, String>> = listOf(
    "08:30" to "09:10",
    "09:15" to "09:55",
    "10:15" to "10:55",
    "11:00" to "11:40",
    "14:00" to "14:40",
    "14:45" to "15:25",
    "15:30" to "16:10",
    "16:15" to "16:55",
    "19:00" to "19:40",
    "19:45" to "20:25",
    "20:30" to "21:10",
)

data class TodayThingSheet(
    val title: String,
    val location: String,
    val allDay: Boolean,
    val timeText: String,
    val remark: String,
    val color: Color,
    val saveAsCountDown: Boolean,
    val remainDays: Long,
    val accountTitle: String,
)

data class CalendarSheetWeek(
    val weekNum: Int,
    val cursor: Boolean,
    val title: String,
    val items: List<CalendarSheet>,
)

@Immutable
data class CalendarSheet(
    val date: LocalDate,
    val title: String,
    val today: Boolean,
    val type: CalendarSheetType,
    val items: List<CalendarSheetItem>,
) {
    companion object {
        fun day(
            date: LocalDate,
            title: String,
            today: Boolean,
            items: List<CalendarSheetItem>,
        ) = CalendarSheet(
            date = date,
            title = title,
            today = today,
            type = CalendarSheetType.DAY_ITEM,
            items = items,
        )

        fun monthHeader(endDate: LocalDate) =
            CalendarSheet(
                date = endDate.minus(endDate.day.toLong() - 1, DateTimeUnit.DAY),
                title = endDate.format(calendarMonthFormatter),
                today = false,
                type = CalendarSheetType.MONTH_HEADER,
                items = emptyList(),
            )
    }
}

@Immutable
data class CalendarSheetItem(
    val title: String,
    val subtitle: String,
    val text: String,
    val color: Color,
    val type: CalendarDayItemType = CalendarDayItemType.COURSE,
    val date: LocalDate = LocalDate.now(),
    val weekNum: Int = 1,
    val startTime: LocalTime = LocalTime(0, 0),
    val endTime: LocalTime = LocalTime(0, 0),
    val location: String = "",
    val courseDetail: WeekCourseView? = null,
    val thingDetail: TodayThingSheet? = null,
)

enum class CalendarSheetType {
    MONTH_HEADER, DAY_ITEM,
}

enum class CourseStatus(
    val title: String,
) {
    BEFORE("未开始"), IN("开课中"), AFTER("已结束"),
}

@Immutable
data class WeekView(
    val weekNum: Int,
    val thisWeek: Boolean,
    val array: Array<Array<Boolean>>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as WeekView

        if (weekNum != other.weekNum) return false
        if (thisWeek != other.thisWeek) return false
        if (!array.contentDeepEquals(other.array)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = weekNum
        result = 31 * result + thisWeek.hashCode()
        result = 31 * result + array.contentDeepHashCode()
        return result
    }
}

data class HolidayView(
    val showTitle: String,
    val color: Color,
)

data class PracticalCourseShowView(
    val courseName: String,
    val teacherName: String,
    val showWeek: String,
    val credit: Double,
    val color: Color,
    val accountTitle: String,
)
