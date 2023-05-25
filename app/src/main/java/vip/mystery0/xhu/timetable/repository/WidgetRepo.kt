package vip.mystery0.xhu.timetable.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.TodayCourseView
import vip.mystery0.xhu.timetable.model.TodayThingView
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.WidgetTodayItem
import vip.mystery0.xhu.timetable.model.WidgetWeekItem
import vip.mystery0.xhu.timetable.model.format
import vip.mystery0.xhu.timetable.module.betweenDays
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import java.time.DayOfWeek
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale

object WidgetRepo {
    /**
     * 计算当前周数，如果未开学，该方法返回0
     */
    suspend fun calculateWeek(date: LocalDate = LocalDate.now()): Int {
        val termStartDate = getConfigStore { termStartDate }
        return withContext(Dispatchers.Default) {
            val days = betweenDays(termStartDate, date)
            var week = ((days / 7) + 1)
            if (days < 0 && week > 0) {
                week = 0
            }
            return@withContext week
        }
    }

    suspend fun calculateDateTitle(showTomorrow: Boolean): String {
        val termStartDate = getConfigStore { termStartDate }
        return withContext(Dispatchers.Default) {
            val now = LocalDate.now()
            val todayWeekIndex =
                now.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
            if (now.isBefore(termStartDate)) {
                //开学之前，那么计算剩余时间
                val remainDays = betweenDays(now, termStartDate)
                return@withContext "距离开学还有${remainDays}天 $todayWeekIndex"
            }
            val date = if (showTomorrow) now.plusDays(1) else now
            val days = betweenDays(termStartDate, date)
            val weekIndex = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
            val week = (days / 7) + 1
            if (week > 20) {
                return@withContext "本学期已结束"
            } else {
                return@withContext "第${week}周 $weekIndex"
            }
        }
    }

    suspend fun getTodayList(currentWeek: Int): List<WidgetTodayItem> {
        val showCustomCourse = getConfigStore { showCustomCourseOnWeek }
        val showCustomThing = getConfigStore { showCustomThing }
        val view =
            AggregationRepo.fetchAggregationMainPage(
                forceLoadFromCloud = false,
                forceLoadFromLocal = true,
                showCustomCourse,
                showCustomThing,
            )

        //获取自定义颜色列表
        val colorMap = CourseColorRepo.getRawCourseColorList()
        val resultList = ArrayList<WidgetTodayItem>()

        resultList.addAll(loadTodayCourseList(currentWeek, view.todayViewList, colorMap))
        resultList.addAll(loadTodayThingList(view.todayThingList))
        return resultList
    }

    private suspend fun loadTodayCourseList(
        currentWeek: Int,
        list: List<TodayCourseView>,
        colorMap: Map<String, Color>,
    ): List<WidgetTodayItem> = withContext(Dispatchers.Default) {
        val showDate = LocalDate.now()
        val showDay = showDate.dayOfWeek
        //过滤出今日的课程
        val showList = list.filter { it.weekList.contains(currentWeek) && it.day == showDay }
            .sortedBy { it.startDayTime }
        if (showList.isEmpty()) {
            return@withContext emptyList()
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
        resultList.map {
            it.updateTime()
            val startTime = it.startTime.atDate(showDate).asInstant()
            WidgetTodayItem(
                it.courseName,
                listOf(
                    it.startTime.format(Formatter.TIME_NO_SECONDS),
                    it.endTime.format(Formatter.TIME_NO_SECONDS),
                ),
                it.location,
                it.backgroundColor,
                startTime.toEpochMilli(),
            )
        }.sortedBy { it.startTime }
    }

    private suspend fun loadTodayThingList(list: List<TodayThingView>): List<WidgetTodayItem> =
        withContext(Dispatchers.Default) {
            val showInstant = Instant.now()
            //过滤出今日或者明日的课程
            val showList = list
                .filter { it.showOnDay(showInstant) }
                .sortedBy { it.sort }
            if (showList.isEmpty()) {
                return@withContext emptyList()
            }
            showList.map {
                val startDateTime = it.startTime.asLocalDateTime()
                val endDateTime = it.endTime.asLocalDateTime()
                val remainDays =
                    Duration.between(LocalDate.now().atStartOfDay(), startDateTime).toDays()
                val showTime = ArrayList<String>()
                if (it.saveAsCountDown) {
                    showTime.add("还剩")
                    showTime.add("${remainDays}天")
                } else {
                    showTime.add(startDateTime.format(Formatter.TIME_NO_SECONDS))
                    showTime.add(endDateTime.format(Formatter.TIME_NO_SECONDS))
                }
                WidgetTodayItem(
                    it.title,
                    showTime,
                    it.location,
                    it.color,
                    it.startTime.toEpochMilli(),
                )
            }
        }

    suspend fun getWeekList(currentWeek: Int): List<List<WidgetWeekItem>> {
        val showCustomCourse = getConfigStore { showCustomCourseOnWeek }
        //周课表页面不显示自定义事项
        val view = AggregationRepo.fetchAggregationMainPage(
            forceLoadFromCloud = false,
            forceLoadFromLocal = true,
            showCustomCourse,
            false,
        )

        //获取自定义颜色列表
        val colorMap = CourseColorRepo.getRawCourseColorList()
        //自定义UI
        val customUi = getConfigStore { customUi }
        return loadWeekCourseList(currentWeek, view.weekViewList, colorMap, customUi)
    }

    private suspend fun loadWeekCourseList(
        currentWeek: Int,
        weekViewList: List<WeekCourseView>,
        colorMap: Map<String, Color>,
        customUi: CustomUi,
    ): List<List<WidgetWeekItem>> {
        //设置是否本周以及课程颜色
        weekViewList.forEach {
            it.thisWeek = it.weekList.contains(currentWeek)
            it.backgroundColor = colorMap[it.courseName] ?: ColorPool.hash(it.courseName)
            it.generateKey()
        }
        //组建表格的数据结构
        val expandTableCourse = Array(7) { day ->
            Array(11) { index ->
                WidgetWeekItem.empty(1, index + 1, DayOfWeek.of(day + 1))
            }
        }
        //平铺课程
        weekViewList.forEach { course ->
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
            val result = ArrayList<WidgetWeekItem>(dayArray.size)
            var last = first
            var lastKey = first.course.sortedBy { it.key }.joinToString { it.key }
            dayArray.forEachIndexed { index, widgetWeekItem ->
                if (index == 0) {
                    return@forEachIndexed
                }
                val thisKey = widgetWeekItem.course.sortedBy { it.key }.joinToString { it.key }
                if (lastKey == thisKey) {
                    last.step++
                } else {
                    result.add(last)
                    last = widgetWeekItem
                    lastKey = thisKey
                }
            }
            if (result.last() != last) {
                result.add(last)
            }
            result
        }
        //处理显示的信息
        tableCourse.forEach { array ->
            array.forEach { weekItem ->
                if (weekItem.course.isNotEmpty()) {
                    weekItem.course.sort()
                    val show = weekItem.course.first()
                    weekItem.course = ArrayList(weekItem.course.sortedBy { it.weekList.first() })
                    if (show.thisWeek) {
                        weekItem.showTitle = show.format(customUi.weekTitleTemplate)
                        weekItem.color =
                            colorMap[show.courseName] ?: ColorPool.hash(show.courseName)
                        weekItem.textColor = Color.White
                    } else {
                        weekItem.showTitle = show.format(customUi.weekNotTitleTemplate)
                        weekItem.color = XhuColor.notThisWeekBackgroundColor
                        weekItem.textColor = Color.Gray
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
        return tableCourse.toList()
    }
}