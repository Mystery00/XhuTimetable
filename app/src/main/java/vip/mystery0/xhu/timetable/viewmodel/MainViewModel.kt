package vip.mystery0.xhu.timetable.viewmodel

import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.WorkManager
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.base.ComposeViewModel
import vip.mystery0.xhu.timetable.base.startUniqueWork
import vip.mystery0.xhu.timetable.config.networkErrorHandler
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.config.store.Menu
import vip.mystery0.xhu.timetable.config.store.MenuStore
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
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.model.transfer.AggregationView
import vip.mystery0.xhu.timetable.repository.AggregationRepo
import vip.mystery0.xhu.timetable.repository.CourseColorRepo
import vip.mystery0.xhu.timetable.repository.FeedbackRepo
import vip.mystery0.xhu.timetable.repository.NoticeRepo
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.repository.WidgetRepo
import vip.mystery0.xhu.timetable.trackError
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.thingDateTimeFormatter
import vip.mystery0.xhu.timetable.work.DownloadApkWork
import vip.mystery0.xhu.timetable.work.DownloadPatchWork
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.WeekFields
import java.util.TreeSet

class MainViewModel : ComposeViewModel() {
    companion object {
        private const val TAG = "MainViewModel"
    }

    private val workManager: WorkManager by inject()

    val version = MutableStateFlow<ClientVersion?>(null)

    private val isDarkMode = MutableStateFlow(false)

    private val _errorMessage = MutableStateFlow(Pair(System.currentTimeMillis(), ""))
    val errorMessage: StateFlow<Pair<Long, String>> = _errorMessage

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

    private val _backgroundImageBlur = MutableStateFlow(1)
    val backgroundImageBlur: StateFlow<Int> = _backgroundImageBlur

    private val _emptyUser = MutableStateFlow(false)
    val emptyUser: StateFlow<Boolean> = _emptyUser

    private val _mainUser = MutableStateFlow<User?>(null)
    val mainUser: StateFlow<User?> = _mainUser

    private val _menu = MutableStateFlow<List<List<Menu>>>(emptyList())
    val menu: StateFlow<List<List<Menu>>> = _menu

    private val _customUi = MutableStateFlow(CustomUi.DEFAULT)
    val customUi: StateFlow<CustomUi> = _customUi

    init {
        viewModelScope.launch {
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
        calculateWeek()

        loadLocalDataToState()
        //加载今日节假日信息
        loadTodayHoliday()
    }

    private fun toastMessage(message: String) {
        _errorMessage.value = System.currentTimeMillis() to message
    }

    private suspend fun loadFromConfig() {
        _multiAccountMode.value = getConfigStore { multiAccountMode }
        _showStatus.value = getConfigStore { showStatus }
        _showTomorrowCourse.value = getConfigStore { showTomorrowCourseTime }
            ?.let { LocalTime.now().isAfter(it) } ?: false
        _menu.value = MenuStore.loadAllMenu()
        _customUi.value = getConfigStore { customUi }
    }

    fun loadConfig() {
        viewModelScope.launch {
            loadFromConfig()
        }
    }

    fun loadBackground(isDarkModeValue: Boolean = isDarkMode.value) {
        viewModelScope.launch {
            isDarkMode.value = isDarkModeValue
            val disable = getConfigStore { disableBackgroundWhenNight }
            val isNight = isDarkMode.value
            if (disable && isNight) {
                _backgroundImage.value = Unit
                return@launch
            } else {
                _backgroundImage.value = getConfigStore { backgroundImage }
                    ?: XhuImages.defaultBackgroundImage
            }
            _backgroundImageBlur.value = getConfigStore { customUi }.backgroundImageBlur
        }
    }

    fun checkMainUser() {
        viewModelScope.launch {
            val mainUser = UserStore.getMainUser()
            _mainUser.value = mainUser
            _emptyUser.value = mainUser == null
        }
    }

    private fun showPoems() {
        viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            Log.w(TAG, "showPoems: ", throwable)
            trackError(throwable)
        }) {
            _poems.value = PoemsStore.loadPoems()
        }
    }

    fun calculateTodayTitle() {
        viewModelScope.launch {
            val showTomorrow = getConfigStore { showTomorrowCourseTime }?.let {
                LocalTime.now().isAfter(it)
            } ?: false

            _todayTitle.value = WidgetRepo.calculateDateTitle(showTomorrow)
        }
    }

    private fun calculateWeek() {
        viewModelScope.launch {
            _week.value = WidgetRepo.calculateWeek()
            //切换周时同步切换课表
            _week.collect {
                val termStartDate = getConfigStore { termStartDate }
                val currentWeek = WidgetRepo.calculateWeek()
                //获取缓存的课程数据
                val data = getMainPageData(forceLoadFromCloud = false, forceLoadFromLocal = true)
                val weekList = data.weekViewList
                //获取自定义颜色列表
                val colorMap = CourseColorRepo.getRawCourseColorList()
                loadCourseToTable(currentWeek, it, weekList, colorMap, true)
                _dateStart.value = termStartDate.plusWeeks(it.toLong() - 1)
                    //设置一周开始为周一
                    .with(WeekFields.of(DayOfWeek.MONDAY, 1).dayOfWeek(), 1)
            }
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
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load local course list failed", throwable)
            _loading.value = false
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            //设置加载状态
            _loading.value = true
            //加载课表相关配置项
            val pair = loadCourseConfig(forceUpdate = false)
            val currentWeek = pair.first
            val loadFromCloud = pair.second
            //获取缓存的课程数据
            val data = getMainPageData(forceLoadFromCloud = false, forceLoadFromLocal = true)
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
            }

            if (loadFromCloud) {
                //需要从云端加载数据
                val cloudData =
                    getMainPageData(forceLoadFromCloud = true, forceLoadFromLocal = false)
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
        viewModelScope.launch(networkErrorHandler { throwable ->
            Log.w(TAG, "load course list failed", throwable)
            _loading.value = false
            if (!GlobalConfigStore.showOldCourseWhenFailed) {
                _tableCourse.value = emptyList()
            }
            toastMessage(throwable.message ?: throwable.javaClass.simpleName)
        }) {
            //设置加载状态
            _loading.value = true
            //加载课表相关配置项
            val pair = loadCourseConfig(forceUpdate = false)
            val currentWeek = pair.first
            //从云端加载数据
            val cloudData = getMainPageData(forceLoadFromCloud = true, forceLoadFromLocal = false)
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
            loadFromCloud = getCacheStore { lastSyncCourse }.isBefore(LocalDate.now())
        }
        val currentWeek = WidgetRepo.calculateWeek()
        _week.value = currentWeek

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
        val showTomorrow = getConfigStore { showTomorrowCourseTime }
            ?.let { LocalTime.now().isAfter(it) } ?: false
        val showDate: LocalDate
        val showDay: DayOfWeek
        val showCurrentWeek: Int
        if (showTomorrow) {
            //显示明天的课程，那么showDate就是明天对应的日期
            showDate = today.plusDays(1)
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
        showList.groupBy { it.user.studentId }
            .forEach { (_, list) ->
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
        _todayCourse.value = resultList.sortedBy { it.startDayTime }
            .map {
                it.updateTime()
                TodayCourseSheet(
                    it.courseName,
                    it.teacher,
                    TreeSet<Int>().apply {
                        addAll(it.startDayTime..it.endDayTime)
                    },
                    it.startTime to it.endTime,
                    it.courseDayTime,
                    it.location,
                    it.user.studentId,
                    it.user.info.name,
                    it.backgroundColor,
                    showDate,
                ).calc(now)
            }
    }

    /**
     * 复用的加载今日事项的方法
     * @param thingList 事项列表
     */
    private suspend fun loadTodayThing(thingList: List<TodayThingView>) {
        val showTomorrow = getConfigStore { showTomorrowCourseTime }
            ?.let { LocalTime.now().isAfter(it) } ?: false
        val showInstant: Instant
        if (showTomorrow) {
            //显示明天的事项，那么showInstant就是明天开始的时间
            showInstant = LocalDate.now().plusDays(1).atStartOfDay().asInstant()
        } else {
            showInstant = Instant.now()
        }
        //过滤出今日或者明日的课程
        val showList = thingList
            .filter { it.showOnDay(showInstant) }
            .sortedBy { it.sort }
        if (showList.isEmpty()) {
            _todayThing.value = emptyList()
            return
        }
        _todayThing.value = showList.map {
            val startDateTime = it.startTime.asLocalDateTime()
            val endDateTime = it.endTime.asLocalDateTime()
            val timeText = buildString {
                if (it.allDay) {
                    append(startDateTime.format(dateFormatter))
                } else {
                    append(startDateTime.format(thingDateTimeFormatter))
                }
                if (it.saveAsCountDown) {
                    return@buildString
                }
                append(" - ")
                if (it.allDay) {
                    append(endDateTime.format(dateFormatter))
                } else {
                    append(endDateTime.format(thingDateTimeFormatter))
                }
            }
            val remainDays =
                Duration.between(LocalDate.now().atStartOfDay(), startDateTime).toDays()
            TodayThingSheet(
                it.title,
                it.location,
                it.allDay,
                timeText,
                it.remark,
                it.color,
                it.saveAsCountDown,
                remainDays,
                it.user.studentId,
                it.user.info.name,
            )
        }
    }

    /**
     * 复用的加载今日节假日信息的方法
     */
    fun loadTodayHoliday() {
        viewModelScope.launch {
            val showHoliday = getConfigStore { showHoliday }
            if (!showHoliday) {
                _holiday.value = null
                return@launch
            }
            val holiday = getCacheStore { holiday }
            val showTomorrow = getConfigStore { showTomorrowCourseTime }
                ?.let { LocalTime.now().isAfter(it) } ?: false
            val offDayColor = Color(0xFF03a9f4)
            val workDayColor = Color(0xFFff3d00)
            if (showTomorrow) {
                val h = holiday.second
                val holidayName = h?.name ?: ""
                val isOffDay = h?.isOffDay ?: false
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
                val h = holiday.first
                val holidayName = h?.name ?: ""
                val isOffDay = h?.isOffDay ?: false
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
            val day = course.day.value - 1
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
                    sheet.course.removeIf { !it.thisWeek }
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
        viewModelScope.launch(networkErrorHandler {
            Log.w(TAG, "check unread notice failed", it)
        }) {
            if (!UserStore.isLogin()) return@launch
            _hasUnReadNotice.value = NoticeRepo.checkNotice()
        }
    }

    fun checkUnReadFeedback() {
        viewModelScope.launch(networkErrorHandler {
            Log.w(TAG, "check unread feedback failed", it)
        }) {
            if (!UserStore.isLogin()) return@launch
            _hasUnReadFeedback.value = FeedbackRepo.checkUnReadFeedback()
        }
    }

    fun downloadApk(newVersion: ClientVersion) {
        viewModelScope.launch {
            workManager.startUniqueWork<DownloadApkWork>(
                Data.Builder()
                    .putString(DownloadApkWork.ARG_VERSION_ID, newVersion.versionId.toString())
                    .putString(DownloadApkWork.ARG_VERSION_NAME, newVersion.versionName)
                    .putString(DownloadApkWork.ARG_VERSION_CODE, newVersion.versionCode.toString())
                    .putString(
                        DownloadApkWork.ARG_VERSION_CHECK_MD5,
                        newVersion.checkMd5.toString()
                    )
                    .build()
            )
        }
    }

    fun downloadPatch(newVersion: ClientVersion) {
        viewModelScope.launch {
            workManager.startUniqueWork<DownloadPatchWork>(
                Data.Builder()
                    .putString(DownloadPatchWork.ARG_VERSION_ID, newVersion.versionId.toString())
                    .putString(DownloadPatchWork.ARG_VERSION_NAME, newVersion.versionName)
                    .putString(
                        DownloadPatchWork.ARG_VERSION_CODE,
                        newVersion.versionCode.toString()
                    )
                    .build()
            )
        }
    }

    fun ignoreVersion(version: ClientVersion) {
        viewModelScope.launch {
            val ignore = getCacheStore { ignoreVersionList }
            val list = ignore + "${version.versionName}-${version.versionCode}"
            setCacheStore { ignoreVersionList = list }
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

    fun isEmpty(): Boolean = course.isEmpty()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

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
    val timeSet: TreeSet<Int>,
    val timeText: Pair<LocalTime, LocalTime>,
    val timeString: String,
    val location: String,
    val studentId: String,
    val userName: String,
    val color: Color,
    val showDate: LocalDate,
) {
    lateinit var courseStatus: CourseStatus

    fun calc(now: LocalDateTime): TodayCourseSheet {
        val startTime = timeText.first.atDate(showDate)
        val endTime = timeText.second.atDate(showDate)
        courseStatus = when {
            now.isBefore(startTime) -> CourseStatus.BEFORE
            now.isAfter(endTime) -> CourseStatus.AFTER
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
    val studentId: String,
    val userName: String,
)

enum class CourseStatus(
    val title: String,
    val color: Color,
    val backgroundColor: Color,
) {
    BEFORE("未开始", XhuColor.Status.beforeColor, XhuColor.Status.beforeBackgroundColor),
    IN("开课中", XhuColor.Status.inColor, XhuColor.Status.inBackgroundColor),
    AFTER("已结束", XhuColor.Status.afterColor, XhuColor.Status.afterBackgroundColor),
}

class WeekView(
    val weekNum: Int,
    val thisWeek: Boolean,
    val array: Array<Array<Boolean>>
)

data class HolidayView(
    val showTitle: String,
    val color: Color,
)
