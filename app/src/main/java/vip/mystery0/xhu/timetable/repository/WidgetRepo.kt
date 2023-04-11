package vip.mystery0.xhu.timetable.repository

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.model.Course
import vip.mystery0.xhu.timetable.model.response.OldCourseResponse
import vip.mystery0.xhu.timetable.module.getLocalRepo
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.widget.state.CourseGlance
import vip.mystery0.xhu.timetable.viewmodel.CourseSheet
import vip.mystery0.xhu.timetable.viewmodel.TodayCourseSheet
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import java.util.TreeSet

suspend fun getCurrentWeek(): Int = runOnCpu {
    //计算当前周
    val startDate =
        LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone).toLocalDate()
    val days =
        Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay())
            .toDays()
    var week = ((days / 7) + 1).toInt()
    if (days < 0 && week > 0) {
        week = 0
    }
    week
}

suspend fun getTimeTitle() =
    runOnCpu {
        val startDate =
            LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone).toLocalDate()
        val nowDate = LocalDate.now()
        val weekIndex =
            nowDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINESE)
        if (nowDate.isBefore(startDate)) {
            //开学之前，那么计算剩余时间
            val remainDays =
                Duration.between(nowDate.atStartOfDay(), startDate.atStartOfDay()).toDays()
            "距离开学还有${remainDays}天 $weekIndex"
        } else {
            val days =
                Duration.between(startDate.atStartOfDay(), nowDate.atStartOfDay()).toDays()
            val week = (days / 7) + 1
            if (week > 20) {
                "本学期已结束"
            } else {
                "第${week}周 $weekIndex"
            }
        }
    }

suspend fun getTodayCourse(currentWeek: Int): List<CourseGlance> {
    val courseRepo: CourseRepo = getLocalRepo()

    fun convertCourseList(
        courseList: List<OldCourseResponse>,
        colorMap: Map<String, Color>,
        currentWeek: Int,
        today: LocalDate,
    ) = courseList.map {
        val thisWeek = it.week.contains(currentWeek)
//        val timeString = it.time.formatTimeString()
        val timeString = "it.time.formatTimeString()"
        val isToday = thisWeek && it.day == today.dayOfWeek.value
        Course(
            it.name,
            it.teacher,
            it.location,
            it.week,
            it.weekString,
            it.type,
            it.time,
            timeString,
//            it.time.formatTime(),
            "",
            it.day,
            it.extraData,
            thisWeek,
            isToday,
            false,
            colorMap[it.name] ?: ColorPool.hash(it.name),
            it.user.studentId,
            it.user.info.name,
        )
    }

    //获取当前学期信息
    val currentYear = getConfig { currentYear }
    val currentTerm = getConfig { currentTerm }

    //获取所有课程列表
    val courseList: List<OldCourseResponse> =
        if (getConfig { multiAccountMode }) {
            val list = ArrayList<OldCourseResponse>()
            UserStore.loggedUserList().forEach { user ->
                list.addAll(
                    courseRepo.getCourseList(user, currentYear, currentTerm)
                )
            }
            list
        } else {
            val user = UserStore.mainUser()
            courseRepo.getCourseList(user, currentYear, currentTerm)
        }

    //获取自定义颜色列表
    val colorMap = getRawCourseColorList()

    val todayList = runOnCpu {
        //转换对象
        val allCourseList = convertCourseList(courseList, colorMap, currentWeek, LocalDate.now())

        //过滤出今日的课程
        val todayCourse =
            allCourseList
                .filter { it.today }
                .sortedBy { it.timeSet.first() }
                .groupBy { it.studentId }
        //合并相同的课程
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
                        "" to "",
                        "",
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
        //设置数据
        val now = LocalDateTime.now()
        resultCourse.sortedBy { it.timeSet.first() }.map { it.calc(now) }
    }
    return todayList.mapIndexed { index, todayCourseSheet ->
        val time = todayCourseSheet.timeText.let {
            buildString {
                appendLine(it.first)
                append(it.second)
            }
        }
        CourseGlance(
            index.toLong(),
            todayCourseSheet.courseName,
            todayCourseSheet.location,
            time,
            todayCourseSheet.color,
        )
    }
}

suspend fun getWeekCourse(currentWeek: Int): List<List<CourseSheet>> {
    val courseRepo: CourseRepo = getLocalRepo()

    fun convertCourseList(
        courseList: List<OldCourseResponse>,
        colorMap: Map<String, Color>,
        currentWeek: Int,
        today: LocalDate,
    ) = courseList.map {
        val thisWeek = it.week.contains(currentWeek)
        val timeString = "it.time.formatTimeString()"
        val isToday = thisWeek && it.day == today.dayOfWeek.value
        Course(
            it.name,
            it.teacher,
            it.location,
            it.week,
            it.weekString,
            it.type,
            it.time,
            timeString,
//            it.time.formatTime(),
            "",
            it.day,
            it.extraData,
            thisWeek,
            isToday,
            false,
            colorMap[it.name] ?: ColorPool.hash(it.name),
            it.user.studentId,
            it.user.info.name,
        )
    }

    //获取当前学期信息
    val currentYear = getConfig { currentYear }
    val currentTerm = getConfig { currentTerm }

    //获取所有课程列表
    val courseList: List<OldCourseResponse> =
        if (getConfig { multiAccountMode }) {
            val list = ArrayList<OldCourseResponse>()
            UserStore.loggedUserList().forEach { user ->
                list.addAll(
                    courseRepo.getCourseList(user, currentYear, currentTerm)
                )
            }
            list
        } else {
            val user = UserStore.mainUser()
            courseRepo.getCourseList(user, currentYear, currentTerm)
        }

    //获取自定义颜色列表
    val colorMap = getRawCourseColorList()

    return runOnCpu {
        //转换对象
        val allCourseList = convertCourseList(courseList, colorMap, currentWeek, LocalDate.now())

        //组建表格的数据结构
        val expandTableCourse = Array(7) { day ->
            Array(11) { index ->
                CourseSheet.empty(1, index + 1, day + 1)
            }
        }
        //展开的列表，key为 当前课程的唯一标识，用于合并，value为课程信息，用于后续生成格子信息
        val expandItemMap = HashMap<String, Course>(allCourseList.size)
        //生成key
        allCourseList.forEach {
            it.generateKey()
            expandItemMap[it.key] = it
        }
        //平铺课程
        allCourseList.forEach { course ->
            course.timeSet.forEach { time ->
                //填充表格
//                expandTableCourse[course.day - 1][time - 1].course.add(course)
            }
        }
        //合并相同的格子
        val tableCourse = Array<ArrayList<CourseSheet>>(7) { ArrayList(11) }
        expandTableCourse.forEachIndexed { index, dayArray ->
            val first = dayArray.first()
            var lastKey = first.course.joinToString { it.key }
            var lastSheet = first
            for (i in 1 until dayArray.size) {
                val thisSheet = dayArray[i]
                val thisKey = thisSheet.course.joinToString { it.key }
                if (lastKey != thisKey) {
                    //键不相等，那么添加这个sheet
                    tableCourse[index].add(lastSheet)
                    lastKey = thisKey
                    lastSheet = thisSheet
                } else {
                    //键相等，合并
                    lastSheet.step++
                }
            }
            if (tableCourse[index].lastOrNull() != lastSheet) {
                tableCourse[index].add(lastSheet)
            }
        }
        //填充显示的信息
        val tableCourseList = tableCourse.map { array ->
            array.map { courseSheet ->
//                if (courseSheet.course.isNotEmpty()) {
//                    val list = courseSheet.course.sortedWith { o1, o2 ->
//                        if (o1.thisWeek == o2.thisWeek) {
//                            if (o1.type != o2.type) {
//                                o2.type.type.compareTo(o1.type.type)
//                            } else {
//                                o1.weekSet.first().compareTo(o2.weekSet.first())
//                            }
//                        } else {
//                            o2.thisWeek.compareTo(o1.thisWeek)
//                        }
//                    }
//                    val show = list.first()
//                    courseSheet.showTitle =
//                        if (show.thisWeek) show.format("{courseName}@{location}")
//                        else show.format("[非本周]\n{courseName}@{location}")
//                    courseSheet.course =
//                        ArrayList(courseSheet.course.distinct().sortedBy { it.weekSet.first() })
//                    courseSheet.color =
//                        if (show.thisWeek) colorMap[show.courseName]
//                            ?: ColorPool.hash(show.courseName) else XhuColor.notThisWeekBackgroundColor
//                    courseSheet.textColor = if (show.thisWeek) Color.White else Color.Gray
//                }
                courseSheet
            }
        }
        val showNotThisWeek = getConfig { showNotThisWeek }
        if (!showNotThisWeek) {
            tableCourseList.forEach { array ->
                array.forEach { sheet ->
                    sheet.course.removeIf { !it.thisWeek }
                }
            }
        }
        tableCourseList
    }
}