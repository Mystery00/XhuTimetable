package vip.mystery0.xhu.timetable.repository

import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.config.chinaZone
import vip.mystery0.xhu.timetable.config.getConfig
import vip.mystery0.xhu.timetable.config.runOnCpu
import vip.mystery0.xhu.timetable.model.Course
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.module.getLocalRepo
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.widget.state.CourseGlance
import vip.mystery0.xhu.timetable.viewmodel.TodayCourseSheet
import vip.mystery0.xhu.timetable.viewmodel.formatTime
import vip.mystery0.xhu.timetable.viewmodel.formatTimeString
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

suspend fun getCurrentWeek(): Int = runOnCpu {
    //计算当前周
    val startDate =
        LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone).toLocalDate()
    val days =
        Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay())
            .toDays()
    ((days / 7) + 1).toInt()
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
        courseList: List<CourseResponse>,
        colorMap: Map<String, Color>,
        currentWeek: Int,
        today: LocalDate,
    ) = courseList.map {
        val thisWeek = it.week.contains(currentWeek)
        val timeString = it.time.formatTimeString()
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
            it.time.formatTime(),
            it.day,
            thisWeek,
            isToday,
            false,
            colorMap[it.name] ?: ColorPool.hash(it.name),
            it.user.studentId,
            it.user.info.userName,
        )
    }

    //获取当前学期信息
    val currentYear = getConfig { currentYear }
    val currentTerm = getConfig { currentTerm }

    //获取所有课程列表
    val courseList: List<CourseResponse> =
        if (getConfig { multiAccountMode }) {
            val list = ArrayList<CourseResponse>()
            SessionManager.loggedUserList().forEach { user ->
                list.addAll(
                    courseRepo.getCourseList(user, currentYear, currentTerm)
                )
            }
            list
        } else {
            val user = SessionManager.mainUser()
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