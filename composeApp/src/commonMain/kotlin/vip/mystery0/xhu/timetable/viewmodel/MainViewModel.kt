package vip.mystery0.xhu.timetable.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.model.CustomUi
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

    val version = MutableStateFlow<ClientVersion?>(null)

    private val isDarkMode = MutableStateFlow<Boolean?>(null)

    //页面标题
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
            if (disable && isDarkMode.value!!) {
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
                //加载今日列表的数据
                loadTodayCourse(
                    currentWeek,
                    data.todayViewList,
                    colorMap,
                )
                //加载今日事项
                loadTodayThing(data.todayThingList)
                //加载周列表的数据
                loadCourseToTable(
                    currentWeek,
                    currentWeek,
                    data.weekViewList,
                    colorMap,
                    changeWeekOnly,
                )
                //加载日历视图的数据
                loadCalendar(currentWeek, calendarData, colorMap)
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
                    //加载今日列表的数据
                    loadTodayCourse(
                        currentWeek,
                        cloudData.todayViewList,
                        colorMap,
                    )
                    //加载今日事项
                    loadTodayThing(cloudData.todayThingList)
                    //加载周列表的数据
                    loadCourseToTable(
                        currentWeek,
                        currentWeek,
                        cloudData.weekViewList,
                        colorMap,
                        changeWeekOnly,
                    )
                    //加载日历视图的数据
                    loadCalendar(currentWeek, cloudCalendarData, colorMap)
                }
                toastMessage("数据同步成功！")
            }
            _loading.value = false
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
            //设置加载状态
            _loading.value = true
            //加载课表相关配置项
            val (currentWeek, _) = loadCourseConfig(forceUpdate = false)
            //从云端加载数据
            val cloudData = getMainPageData(forceLoadFromCloud = true, forceLoadFromLocal = false)
            val cloudCalendarData = AggregationRepo
                .fetchAggregationCalendarPage(forceLoadFromCloud = true, forceLoadFromLocal = false)
            //获取自定义颜色列表
            val colorMap = CourseColorRepo.getRawCourseColorList()
            withContext(Dispatchers.Default) {
                //加载今日列表的数据
                loadTodayCourse(
                    currentWeek,
                    cloudData.todayViewList,
                    colorMap,
                )
                //加载今日事项
                loadTodayThing(cloudData.todayThingList)
                //加载周列表的数据
                loadCourseToTable(currentWeek, currentWeek, cloudData.weekViewList, colorMap, false)
                //加载日历视图的数据
                loadCalendar(currentWeek, cloudCalendarData, colorMap)
            }
            toastMessage("数据同步成功！")
            _loading.value = false
        }
    }

    /**
     * 复用的加载课表相关配置项的方法
     * @return 是否需要从云端加载课表
     */
    private suspend fun loadCourseConfig(forceUpdate: Boolean = true): Pair<Int, Boolean> {
        _multiAccountMode.value = getConfigStore { multiAccountMode }
        var loadFromCloud = forceUpdate
        if (!loadFromCloud) {
            loadFromCloud = getCacheStore { lastSyncCourse } < LocalDate.now()
        }
        val currentWeek = WidgetRepo.calculateWeek()
        _week.value = currentWeek
        val termStartDate = getConfigStore { termStartDate }
        _dateStart.value = termStartDate.plus(currentWeek.toLong() - 1, DateTimeUnit.WEEK)

        return currentWeek to loadFromCloud
    }

    /**
     * 复用的加载今日课表的方法
     * @param currentWeek 当前周
     * @param courseList 课程列表
     */
    private suspend fun loadTodayCourse(
        currentWeek: Int,
        courseList: List<TodayCourseView>,
        colorMap: Map<String, Color>,
    ) {
        val today = LocalDate.now()
        val showTomorrow =
            getConfigStore { showTomorrowCourseTime }?.let { LocalTime.now() > it } ?: false
        val customAccountTitle = getConfigStore { customAccountTitle }
        val showDate: LocalDate
        val showDay: DayOfWeek
        val showCurrentWeek: Int
        if (showTomorrow) {
            //显示明天的课程，那么showDate就是明天对应的日期
            showDate = today.plus(1, DateTimeUnit.DAY)
            showDay = showDate.dayOfWeek
            showCurrentWeek = WidgetRepo.calculateWeek(showDate)
        } else {
            showDate = today
            showDay = today.dayOfWeek
            showCurrentWeek = currentWeek
        }
        //过滤出今日或者明日的课程
        val showList =
            courseList.filter { it.weekList.contains(showCurrentWeek) && it.day == showDay }
                .sortedBy { it.startDayTime }
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
        _todayCourse.value = resultList.sortedBy { it.startDayTime }.map {
            it.updateTime()
            TodayCourseSheet(
                it.courseName,
                it.teacher,
                (it.startDayTime..it.endDayTime).sorted().toMutableSet(),
                it.startTime to it.endTime,
                it.courseDayTime,
                it.location,
                it.backgroundColor,
                showDate,
                customAccountTitle.formatToday(it.user.info),
            ).calc(now)
        }
    }

    /**
     * 复用的加载今日事项的方法
     * @param thingList 事项列表
     */
    private suspend fun loadTodayThing(thingList: List<TodayThingView>) {
        val showTomorrow =
            getConfigStore { showTomorrowCourseTime }?.let { LocalTime.now() > it } ?: false
        val customAccountTitle = getConfigStore { customAccountTitle }
        val showInstant = if (showTomorrow) {
            //显示明天的事项，那么showInstant就是明天开始的时间
            LocalDate.now().plus(1, DateTimeUnit.DAY).atTime(0, 0).asInstant()
        } else {
            Clock.System.now()
        }
        //过滤出今日或者明日的课程
        val showList = thingList.filter { it.showOnDay(showInstant) }.sortedBy { it.sort }
        if (showList.isEmpty()) {
            _todayThing.value = emptyList()
            return
        }
        _todayThing.value = showList.map {
            val startDateTime = it.startTime.asLocalDateTime()
            val endDateTime = it.endTime.asLocalDateTime()
            val timeText = buildString {
                if (it.allDay) {
                    append(startDateTime.date.format(dateFormatter))
                } else {
                    append(startDateTime.format(thingDateTimeFormatter))
                }
                if (it.saveAsCountDown) {
                    return@buildString
                }
                append(" - ")
                if (it.allDay) {
                    append(endDateTime.date.format(dateFormatter))
                } else {
                    append(endDateTime.format(thingDateTimeFormatter))
                }
            }
            val remainDays = betweenDays(LocalDate.now(), startDateTime.date).toLong()
            TodayThingSheet(
                it.title,
                it.location,
                it.allDay,
                timeText,
                it.remark,
                it.color,
                it.saveAsCountDown,
                remainDays,
                customAccountTitle.formatToday(it.user.info),
            )
        }
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
    private suspend fun loadCourseToTable(
        currentWeek: Int,
        showWeek: Int,
        courseList: List<WeekCourseView>,
        colorMap: Map<String, Color>,
        changeWeekOnly: Boolean,
    ) {
        //设置是否本周以及课程颜色
        courseList.forEach {
            it.thisWeek = it.weekList.contains(showWeek)
            it.backgroundColor = colorMap[it.courseName] ?: ColorPool.hash(it.courseName)
            it.generateKey()
        }
        //组建表格的数据结构
        val expandTableCourse = Array(7) { day ->
            Array(11) { index ->
                CourseSheet.empty(1, index + 1, day + 1)
            }
        }
        //平铺课程
        courseList.forEach { course ->
            val day = course.day.isoDayNumber - 1
            (course.startDayTime..course.endDayTime).forEach { time ->
                //填充表格
                expandTableCourse[day][time - 1].course.add(course)
            }
        }
        //使用key判断格子内容是否相同，相同则合并
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
        //处理显示的信息
        tableCourse.forEach { array ->
            array.forEach { sheet ->
                if (sheet.course.isNotEmpty()) {
                    sheet.course.sort()
                    val show = sheet.course.first()
                    sheet.course = ArrayList(sheet.course.sortedBy { it.weekList.first() })
                    if (show.thisWeek) {
                        sheet.showTitle = show.format(_customUi.value.weekTitleTemplate)
                        sheet.color = colorMap[show.courseName] ?: ColorPool.hash(show.courseName)
                        sheet.textColor = Color.White
                    } else {
                        sheet.showTitle = show.format(_customUi.value.weekNotTitleTemplate)
                        sheet.color = XhuColor.notThisWeekBackgroundColor
                        sheet.textColor = Color.Gray
                    }
                }
            }
        }
        //过滤非本周课程
        val showNotThisWeek = getConfigStore { showNotThisWeek }
        if (!showNotThisWeek) {
            tableCourse.forEach { array ->
                array.forEach { sheet ->
                    val temp = sheet.course.filter { it.thisWeek }
                    sheet.course.clear()
                    sheet.course.addAll(temp)
                }
            }
        }
        _tableCourse.value = tableCourse.toList()

        if (changeWeekOnly) {
            //仅仅只是切换周，那么不需要重新渲染周预览
            return
        }

        //计算顶部周视图
        val weekViewArray = Array(20) { index ->
            WeekView(index + 1, currentWeek == index + 1, Array(5) { Array(5) { false } })
        }
        for (dayIndex in 0 until 5) {
            val dayArray = expandTableCourse[dayIndex]
            for (itemIndex in 0 until 10) {
                val sheet = dayArray[itemIndex]
                sheet.course.forEach {
                    it.weekList.forEach { week ->
                        weekViewArray[week - 1].array[dayIndex][itemIndex / 2] = true
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
    ) {
        val now = LocalDate.now()
        var alreadySet = false
        _calendarList.value = calendarList.map { week ->
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
                            CalendarSheetItem(
                                title = item.title,
                                subtitle = item.courseName,
                                text = textBuilder.toString(),
                                color = color,
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
    }

    fun changeCurrentWeek(currentWeek: Int) {
        viewModelScope.safeLaunch {
            _week.value = currentWeek
            val termStartDate = getConfigStore { termStartDate }
            //获取缓存的课程数据
            val data = getMainPageData(forceLoadFromCloud = false, forceLoadFromLocal = true)
            val weekList = data.weekViewList
            //获取自定义颜色列表
            val colorMap = CourseColorRepo.getRawCourseColorList()
            loadCourseToTable(currentWeek, currentWeek, weekList, colorMap, true)
            _dateStart.value = termStartDate.plus(currentWeek.toLong() - 1, DateTimeUnit.WEEK)
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
) {
    lateinit var courseStatus: CourseStatus

    fun calc(now: LocalDateTime): TodayCourseSheet {
        val startTime = showDate.atTime(timeText.first)
        val endTime = showDate.atTime(timeText.second)
        courseStatus = when {
            now < startTime -> CourseStatus.BEFORE
            now > endTime -> CourseStatus.AFTER
            else -> CourseStatus.IN
        }
        return this
    }
}

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

data class CalendarSheetItem(
    val title: String,
    val subtitle: String,
    val text: String,
    val color: Color,
)

enum class CalendarSheetType {
    MONTH_HEADER, DAY_ITEM,
}

enum class CourseStatus(
    val title: String,
) {
    BEFORE("未开始"), IN("开课中"), AFTER("已结束"),
}

class WeekView(
    val weekNum: Int, val thisWeek: Boolean, val array: Array<Array<Boolean>>
)

data class HolidayView(
    val showTitle: String,
    val color: Color,
)
