package vip.mystery0.xhu.timetable.repository

import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.TodayCourseView
import vip.mystery0.xhu.timetable.model.TodayThingView
import vip.mystery0.xhu.timetable.model.WidgetTodayItem
import vip.mystery0.xhu.timetable.module.betweenDays
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.enTimeFormatter
import vip.mystery0.xhu.timetable.utils.timeFormatter
import vip.mystery0.xhu.timetable.viewmodel.CourseSheet
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
            AggregationRepo.fetchAggregationMainPage(false, showCustomCourse, showCustomThing)

        //获取自定义颜色列表
        val colorMap = getRawCourseColorList()
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
            val timeText = it.courseTime
            val startTime =
                LocalTime.parse(timeText.first, timeFormatter).atDate(showDate).asInstant()
            WidgetTodayItem(
                it.courseName,
                listOf(
                    timeText.first,
                    timeText.second,
                ),
                it.location,
                it.backgroundColor,
                startTime.toEpochMilli(),
            )
        }
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
                showTime.add(startDateTime.format(enTimeFormatter))
                showTime.add(endDateTime.format(enTimeFormatter))
                if (it.saveAsCountDown){
                    showTime.add("+${remainDays}D")
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

    private suspend fun loadWeekCourseList(){

    }
}

suspend fun getWeekCourse(currentWeek: Int): List<List<CourseSheet>> {
    TODO()
//    val courseRepo: CourseRepo111 = getLocalRepo()
//
//    fun convertCourseList(
//        courseList: List<OldCourseResponse>,
//        colorMap: Map<String, Color>,
//        currentWeek: Int,
//        today: LocalDate,
//    ) = courseList.map {
//        val thisWeek = it.week.contains(currentWeek)
//        val timeString = "it.time.formatTimeString()"
//        val isToday = thisWeek && it.day == today.dayOfWeek.value
//        Course(
//            it.name,
//            it.teacher,
//            it.location,
//            it.week,
//            it.weekString,
//            it.type,
//            it.time,
//            timeString,
////            it.time.formatTime(),
//            "",
//            it.day,
//            it.extraData,
//            thisWeek,
//            isToday,
//            false,
//            colorMap[it.name] ?: ColorPool.hash(it.name),
//            it.user.studentId,
//            it.user.info.name,
//        )
//    }
//
//    //获取当前学期信息
//    val currentYear = getConfig { currentYear }
//    val currentTerm = getConfig { currentTerm }
//
//    //获取所有课程列表
//    val courseList: List<OldCourseResponse> =
//        if (getConfig { multiAccountMode }) {
//            val list = ArrayList<OldCourseResponse>()
//            UserStore.loggedUserList().forEach { user ->
//                list.addAll(
//                    courseRepo.getCourseList(user, currentYear, currentTerm)
//                )
//            }
//            list
//        } else {
//            val user = UserStore.mainUser()
//            courseRepo.getCourseList(user, currentYear, currentTerm)
//        }
//
//    //获取自定义颜色列表
//    val colorMap = getRawCourseColorList()
//
//    return runOnCpu {
//        //转换对象
//        val allCourseList = convertCourseList(courseList, colorMap, currentWeek, LocalDate.now())
//
//        //组建表格的数据结构
//        val expandTableCourse = Array(7) { day ->
//            Array(11) { index ->
//                CourseSheet.empty(1, index + 1, day + 1)
//            }
//        }
//        //展开的列表，key为 当前课程的唯一标识，用于合并，value为课程信息，用于后续生成格子信息
//        val expandItemMap = HashMap<String, Course>(allCourseList.size)
//        //生成key
//        allCourseList.forEach {
//            it.generateKey()
//            expandItemMap[it.key] = it
//        }
//        //平铺课程
//        allCourseList.forEach { course ->
//            course.timeSet.forEach { time ->
//                //填充表格
////                expandTableCourse[course.day - 1][time - 1].course.add(course)
//            }
//        }
//        //合并相同的格子
//        val tableCourse = Array<ArrayList<CourseSheet>>(7) { ArrayList(11) }
//        expandTableCourse.forEachIndexed { index, dayArray ->
//            val first = dayArray.first()
//            var lastKey = first.course.joinToString { it.key }
//            var lastSheet = first
//            for (i in 1 until dayArray.size) {
//                val thisSheet = dayArray[i]
//                val thisKey = thisSheet.course.joinToString { it.key }
//                if (lastKey != thisKey) {
//                    //键不相等，那么添加这个sheet
//                    tableCourse[index].add(lastSheet)
//                    lastKey = thisKey
//                    lastSheet = thisSheet
//                } else {
//                    //键相等，合并
//                    lastSheet.step++
//                }
//            }
//            if (tableCourse[index].lastOrNull() != lastSheet) {
//                tableCourse[index].add(lastSheet)
//            }
//        }
//        //填充显示的信息
//        val tableCourseList = tableCourse.map { array ->
//            array.map { courseSheet ->
////                if (courseSheet.course.isNotEmpty()) {
////                    val list = courseSheet.course.sortedWith { o1, o2 ->
////                        if (o1.thisWeek == o2.thisWeek) {
////                            if (o1.type != o2.type) {
////                                o2.type.type.compareTo(o1.type.type)
////                            } else {
////                                o1.weekSet.first().compareTo(o2.weekSet.first())
////                            }
////                        } else {
////                            o2.thisWeek.compareTo(o1.thisWeek)
////                        }
////                    }
////                    val show = list.first()
////                    courseSheet.showTitle =
////                        if (show.thisWeek) show.format("{courseName}@{location}")
////                        else show.format("[非本周]\n{courseName}@{location}")
////                    courseSheet.course =
////                        ArrayList(courseSheet.course.distinct().sortedBy { it.weekSet.first() })
////                    courseSheet.color =
////                        if (show.thisWeek) colorMap[show.courseName]
////                            ?: ColorPool.hash(show.courseName) else XhuColor.notThisWeekBackgroundColor
////                    courseSheet.textColor = if (show.thisWeek) Color.White else Color.Gray
////                }
//                courseSheet
//            }
//        }
//        val showNotThisWeek = getConfig { showNotThisWeek }
//        if (!showNotThisWeek) {
//            tableCourseList.forEach { array ->
//                array.forEach { sheet ->
//                    sheet.course.removeIf { !it.thisWeek }
//                }
//            }
//        }
//        tableCourseList
//}
}