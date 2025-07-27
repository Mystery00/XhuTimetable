package vip.mystery0.xhu.timetable.repository.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
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
import kotlinx.datetime.plus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.db.dao.CourseDao
import vip.mystery0.xhu.timetable.db.dao.CustomCourseDao
import vip.mystery0.xhu.timetable.db.dao.CustomThingDao
import vip.mystery0.xhu.timetable.db.dao.ExperimentCourseDao
import vip.mystery0.xhu.timetable.db.dao.PracticalCourseDao
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.entity.CourseEntity
import vip.mystery0.xhu.timetable.model.entity.CustomCourseEntity
import vip.mystery0.xhu.timetable.model.entity.CustomThingEntity
import vip.mystery0.xhu.timetable.model.entity.ExperimentCourseEntity
import vip.mystery0.xhu.timetable.model.entity.PracticalCourseEntity
import vip.mystery0.xhu.timetable.model.response.AggregationMainPageResponse
import vip.mystery0.xhu.timetable.model.response.CalendarDayItemResponse
import vip.mystery0.xhu.timetable.model.response.CalendarDayItemType
import vip.mystery0.xhu.timetable.model.response.CalendarDayResponse
import vip.mystery0.xhu.timetable.model.response.CalendarWeekResponse
import vip.mystery0.xhu.timetable.model.response.Course
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.model.response.ExperimentCourse
import vip.mystery0.xhu.timetable.model.response.PracticalCourse
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import kotlin.random.Random
import kotlin.time.Instant

object AggregationLocalRepo : KoinComponent {
    private val courseDao: CourseDao by inject()
    private val practicalCourseDao: PracticalCourseDao by inject()
    private val experimentCourseDao: ExperimentCourseDao by inject()
    private val customCourseDao: CustomCourseDao by inject()
    private val customThingDao: CustomThingDao by inject()

    private suspend fun <T, R> queryAndMap(
        dataList: ArrayList<R>,
        query: suspend () -> List<T>,
        dataMap: (T) -> R,
    ) {
        val data = withContext(Dispatchers.IO) { query() }
        withContext(Dispatchers.Default) { dataList.addAll(data.map(dataMap)) }
    }

    suspend fun fetchAggregationMainPage(
        year: Int,
        term: Int,
        user: User,
        showCustomCourse: Boolean,
        showCustomThing: Boolean,
    ): AggregationMainPageResponse {
        //课程列表
        val courseList = ArrayList<Course>()
        //实践课程列表
        val practicalCourseList = ArrayList<PracticalCourse>()
        //实验课程列表
        val experimentCourseList = ArrayList<ExperimentCourse>()
        //自定义课程列表
        val customCourseList = ArrayList<CustomCourseResponse>()
        //自定义事项列表
        val customThingList = ArrayList<CustomThingResponse>()

        queryAndMap(
            courseList,
            query = {
                courseDao.queryList(user.studentId, year, term)
            },
            dataMap = {
                Course(
                    courseName = it.courseName,
                    weekStr = it.weekStr,
                    weekList = it.weekList,
                    day = DayOfWeek(it.dayIndex),
                    dayIndex = it.dayIndex,
                    startDayTime = it.startDayTime,
                    endDayTime = it.endDayTime,
                    startTime = LocalTime.parse(it.startTime, Formatter.TIME_NO_SECONDS),
                    endTime = LocalTime.parse(it.endTime, Formatter.TIME_NO_SECONDS),
                    location = it.location,
                    teacher = it.teacher,
                    extraData = it.extraData,
                    campus = it.campus,
                    courseType = it.courseType,
                    credit = it.credit,
                    courseCodeType = it.courseCodeType,
                    courseCodeFlag = it.courseCodeFlag,
                )
            })
        queryAndMap(
            practicalCourseList,
            query = {
                practicalCourseDao.queryList(user.studentId, year, term)
            },
            dataMap = {
                PracticalCourse(
                    courseName = it.courseName,
                    weekStr = it.weekStr,
                    weekList = it.weekList,
                    teacher = it.teacher,
                    campus = it.campus,
                    credit = it.credit,
                )
            })
        queryAndMap(
            experimentCourseList,
            query = {
                experimentCourseDao.queryList(user.studentId, year, term)
            },
            dataMap = {
                ExperimentCourse(
                    courseName = it.courseName,
                    experimentProjectName = it.experimentProjectName,
                    teacherName = it.teacherName,
                    experimentGroupName = it.experimentGroupName,
                    weekStr = it.weekStr,
                    weekList = it.weekList,
                    day = DayOfWeek(it.dayIndex),
                    dayIndex = it.dayIndex,
                    startDayTime = it.startDayTime,
                    endDayTime = it.endDayTime,
                    startTime = LocalTime.parse(it.startTime, Formatter.TIME_NO_SECONDS),
                    endTime = LocalTime.parse(it.endTime, Formatter.TIME_NO_SECONDS),
                    region = it.region,
                    location = it.location,
                )
            })
        if (showCustomCourse) {
            queryAndMap(
                customCourseList,
                query = {
                    customCourseDao.queryList(user.studentId, year, term)
                },
                dataMap = {
                    CustomCourseResponse(
                        courseId = it.courseId,
                        courseName = it.courseName,
                        weekStr = it.weekStr,
                        weekList = it.weekList,
                        day = DayOfWeek(it.dayIndex),
                        dayIndex = it.dayIndex,
                        startDayTime = it.startDayTime,
                        endDayTime = it.endDayTime,
                        startTime = LocalTime.parse(it.startTime, Formatter.TIME_NO_SECONDS),
                        endTime = LocalTime.parse(it.endTime, Formatter.TIME_NO_SECONDS),
                        location = it.location,
                        teacher = it.teacher,
                        extraData = it.extraData,
                        createTime = Instant.fromEpochMilliseconds(it.createTime),
                    )
                })
        }
        if (showCustomThing) {
            queryAndMap(
                customThingList,
                query = {
                    customThingDao.queryList(user.studentId)
                },
                dataMap = {
                    CustomThingResponse(
                        thingId = it.thingId,
                        title = it.title,
                        location = it.location,
                        allDay = it.allDay,
                        startTime = Instant.fromEpochMilliseconds(it.startTime),
                        endTime = Instant.fromEpochMilliseconds(it.endTime),
                        remark = it.remark,
                        color = it.color,
                        metadata = it.metadata,
                        createTime = Instant.fromEpochMilliseconds(it.createTime),
                    )
                })
        }
        return AggregationMainPageResponse(
            courseList = courseList,
            practicalCourseList = practicalCourseList,
            experimentCourseList = experimentCourseList,
            customCourseList = customCourseList,
            customThingList = customThingList,
        )
    }

    private suspend fun <T> queryAndMap(
        query: suspend () -> List<T>,
        dataMap: (T) -> Unit,
    ) {
        val data = withContext(Dispatchers.IO) { query() }
        withContext(Dispatchers.Default) { data.forEach(dataMap) }
    }

    suspend fun fetchAggregationCalendarPage(
        year: Int,
        term: Int,
        user: User,
        startDate: LocalDate,
    ): List<CalendarWeekResponse> {
        val map = HashMap<LocalDate, MutableList<CalendarDayItemResponse>>(180)

        //课程列表
        queryAndMap(
            query = {
                courseDao.queryList(user.studentId, year, term)
            },
            dataMap = { course ->
                course.weekList.forEach { week ->
                    val date = calculateDate(startDate, week, DayOfWeek(course.dayIndex))
                    val list = map.getOrPut(date) { ArrayList(16) }
                    list.add(
                        CalendarDayItemResponse(
                            title = course.courseName,
                            startTime = parseTime(course.startTime),
                            endTime = parseTime(course.endTime),
                            location = course.location,
                            type = CalendarDayItemType.COURSE,
                        )
                    )
                }
            }
        )
        //实验课程列表
        queryAndMap(
            query = {
                experimentCourseDao.queryList(user.studentId, year, term)
            },
            dataMap = { experimentCourse ->
                experimentCourse.weekList.forEach { week ->
                    val date =
                        calculateDate(startDate, week, DayOfWeek(experimentCourse.dayIndex))
                    val list = map.getOrPut(date) { ArrayList(16) }
                    list.add(
                        CalendarDayItemResponse(
                            title = experimentCourse.experimentProjectName,
                            startTime = parseTime(experimentCourse.startTime),
                            endTime = parseTime(experimentCourse.endTime),
                            location = experimentCourse.location,
                            type = CalendarDayItemType.EXPERIMENT_COURSE,
                            courseName = experimentCourse.courseName,
                        )
                    )
                }
            }
        )
        //自定义课程列表
        queryAndMap(
            query = {
                customCourseDao.queryList(user.studentId, year, term)
            },
            dataMap = { customCourse ->
                customCourse.weekList.forEach { week ->
                    val date = calculateDate(startDate, week, DayOfWeek(customCourse.dayIndex))
                    val list = map.getOrPut(date) { ArrayList(16) }
                    list.add(
                        CalendarDayItemResponse(
                            title = customCourse.courseName,
                            startTime = parseTime(customCourse.startTime),
                            endTime = parseTime(customCourse.endTime),
                            location = customCourse.location,
                            type = CalendarDayItemType.CUSTOM_COURSE,
                        )
                    )
                }
            })
        //自定义事项列表
        queryAndMap(
            query = {
                customThingDao.queryList(user.studentId)
            },
            dataMap = { customThing ->
                splitCustomThingDate(
                    Instant.fromEpochMilliseconds(customThing.startTime),
                    Instant.fromEpochMilliseconds(customThing.endTime),
                ).forEach { (startTime, endTime) ->
                    val list = map.getOrPut(startTime.date) { ArrayList(16) }
                    list.add(
                        CalendarDayItemResponse(
                            title = customThing.title,
                            startTime = startTime.time,
                            endTime = endTime.time,
                            location = customThing.location,
                            type = CalendarDayItemType.CUSTOM_THING,
                            customThingColor = customThing.color,
                        )
                    )
                }
            })

        //组装数据结构
        val maxDate = if (map.isEmpty()) {
            startDate.plus(20, DateTimeUnit.WEEK)
        } else {
            minOf(map.keys.max(), startDate.plus(20, DateTimeUnit.WEEK))
        }
        val result = ArrayList<CalendarWeekResponse>()
        var weekNum = 0
        var thisWeekStartDate = startDate
        while (thisWeekStartDate < maxDate) {
            val thisWeekEndDate = thisWeekStartDate.plus(1, DateTimeUnit.WEEK)
            weekNum++
            val thisWeek = ArrayList<CalendarDayResponse>()
            var day = thisWeekStartDate
            while (day < thisWeekEndDate) {
                val dayItemList = map[day]
                if (!dayItemList.isNullOrEmpty()) {
                    thisWeek.add(CalendarDayResponse(day, dayItemList.sortedWith(object :
                        Comparator<CalendarDayItemResponse> {
                        override fun compare(
                            a: CalendarDayItemResponse,
                            b: CalendarDayItemResponse
                        ): Int {
                            if (a.startTime == b.startTime) {
                                return a.title.compareTo(b.title)
                            }
                            return a.startTime.compareTo(b.startTime)
                        }
                    })))
                }
                day = day.plus(1, DateTimeUnit.DAY)
            }
            result.add(
                CalendarWeekResponse(
                    weekNum,
                    thisWeekStartDate,
                    thisWeekEndDate.minus(1, DateTimeUnit.DAY),
                    thisWeek
                )
            )
            thisWeekStartDate = thisWeekEndDate
        }
        return result
    }

    suspend fun saveResponse(
        year: Int,
        term: Int,
        user: User,
        response: AggregationMainPageResponse,
        containCustomCourse: Boolean,
        containCustomThing: Boolean,
    ) {
        //删除所有旧数据
        withContext(Dispatchers.IO) {
            courseDao.queryList(user.studentId, year, term).forEach {
                courseDao.delete(it)
            }
            practicalCourseDao.queryList(user.studentId, year, term).forEach {
                practicalCourseDao.delete(it)
            }
            experimentCourseDao.queryList(user.studentId, year, term).forEach {
                experimentCourseDao.delete(it)
            }
            if (containCustomCourse) {
                customCourseDao.queryList(user.studentId, year, term).forEach {
                    customCourseDao.delete(it)
                }
            }
            if (containCustomThing) {
                customThingDao.queryList(user.studentId).forEach {
                    customThingDao.delete(it)
                }
            }
        }
        response.courseList.forEach {
            val entity = CourseEntity(
                courseName = it.courseName,
                weekStr = it.weekStr,
                weekList = it.weekList,
                dayIndex = it.dayIndex,
                startDayTime = it.startDayTime,
                endDayTime = it.endDayTime,
                startTime = it.startTime.format(Formatter.TIME_NO_SECONDS),
                endTime = it.endTime.format(Formatter.TIME_NO_SECONDS),
                location = it.location,
                teacher = it.teacher,
                extraData = it.extraData,
                campus = it.campus,
                courseType = it.courseType,
                credit = it.credit,
                courseCodeType = it.courseCodeType,
                courseCodeFlag = it.courseCodeFlag,
                year = year,
                term = term,
                studentId = user.studentId,
            )
            withContext(Dispatchers.IO) {
                courseDao.insert(entity)
            }
        }
        response.practicalCourseList.forEach {
            val entity = PracticalCourseEntity(
                courseName = it.courseName,
                weekStr = it.weekStr,
                weekList = it.weekList,
                teacher = it.teacher,
                campus = it.campus,
                credit = it.credit,
                year = year,
                term = term,
                studentId = user.studentId,
            )
            withContext(Dispatchers.IO) {
                practicalCourseDao.insert(entity)
            }
        }
        response.experimentCourseList.forEach {
            val entity = ExperimentCourseEntity(
                courseName = it.courseName,
                experimentProjectName = it.experimentProjectName,
                teacherName = it.teacherName,
                experimentGroupName = it.experimentGroupName,
                weekStr = it.weekStr,
                weekList = it.weekList,
                dayIndex = it.dayIndex,
                startDayTime = it.startDayTime,
                endDayTime = it.endDayTime,
                startTime = it.startTime.format(Formatter.TIME_NO_SECONDS),
                endTime = it.endTime.format(Formatter.TIME_NO_SECONDS),
                region = it.region,
                location = it.location,
                year = year,
                term = term,
                studentId = user.studentId,
            )
            withContext(Dispatchers.IO) {
                experimentCourseDao.insert(entity)
            }
        }
        if (containCustomCourse) {
            response.customCourseList.forEach {
                val entity = CustomCourseEntity(
                    courseId = it.courseId,
                    courseName = it.courseName,
                    weekStr = it.weekStr,
                    weekList = it.weekList,
                    dayIndex = it.dayIndex,
                    startDayTime = it.startDayTime,
                    endDayTime = it.endDayTime,
                    startTime = it.startTime.format(Formatter.TIME_NO_SECONDS),
                    endTime = it.endTime.format(Formatter.TIME_NO_SECONDS),
                    location = it.location,
                    teacher = it.teacher,
                    extraData = it.extraData,
                    createTime = it.createTime.toEpochMilliseconds(),
                    year = year,
                    term = term,
                    studentId = user.studentId,
                )
                withContext(Dispatchers.IO) {
                    customCourseDao.insert(entity)
                }
            }
        }
        if (containCustomThing) {
            response.customThingList.forEach {
                val entity = CustomThingEntity(
                    thingId = it.thingId,
                    title = it.title,
                    location = it.location,
                    allDay = it.allDay,
                    startTime = it.startTime.toEpochMilliseconds(),
                    endTime = it.endTime.toEpochMilliseconds(),
                    remark = it.remark,
                    color = it.color,
                    metadata = it.metadata,
                    createTime = it.createTime.toEpochMilliseconds(),
                    studentId = user.studentId,
                )
                withContext(Dispatchers.IO) {
                    customThingDao.insert(entity)
                }
            }
        }
    }

    suspend fun getRandomCourseList(size: Int): List<WeekCourseView> {
        val list = ArrayList<CourseEntity>()
        withContext(Dispatchers.IO) {
            list.addAll(courseDao.queryRandomList(size * 5))
        }
        if (list.isEmpty()) {
            list.add(
                CourseEntity(
                    courseName = "西瓜课表",
                    weekStr = "",
                    weekList = emptyList(),
                    dayIndex = DayOfWeek.MONDAY.isoDayNumber,
                    startDayTime = 1,
                    endDayTime = 1,
                    startTime = "08:00",
                    endTime = "09:00",
                    location = "西华大学本部6A421",
                    teacher = "西瓜课表团队",
                    extraData = listOf("QQ群：1234567890"),
                    campus = "",
                    courseType = "",
                    credit = 0.0,
                    courseCodeType = "",
                    courseCodeFlag = "",
                    year = 2015,
                    term = 1,
                    studentId = "",
                )
            )
        }
        val resultList = withContext(Dispatchers.Default) {
            while (list.size < size) {
                //数量不够，强行复制
                list.addAll(list)
            }
            list.shuffled().take(size)
        }
        return resultList.map {
            WeekCourseView(
                courseName = it.courseName,
                weekStr = it.weekStr,
                weekList = it.weekList,
                day = DayOfWeek(it.dayIndex),
                startDayTime = it.startDayTime,
                endDayTime = it.endDayTime,
                startTime = LocalTime.parse(it.startTime, Formatter.TIME_NO_SECONDS),
                endTime = LocalTime.parse(it.endTime, Formatter.TIME_NO_SECONDS),
                courseDayTime = "",
                courseTime = "",
                location = it.location,
                teacher = it.teacher,
                extraData = it.extraData,
                accountTitle = "",
            ).apply {
                thisWeek = Random.nextBoolean()
                backgroundColor =
                    if (thisWeek) ColorPool.hash(it.courseName) else XhuColor.notThisWeekBackgroundColor
            }
        }
    }

    private fun splitCustomThingDate(
        startTimeInstant: Instant,
        endTimeInstant: Instant,
    ): List<Pair<LocalDateTime, LocalDateTime>> {
        val result = ArrayList<Pair<LocalDateTime, LocalDateTime>>()
        val startTime = startTimeInstant.asLocalDateTime()
        val endTime = endTimeInstant.asLocalDateTime()
        if (startTime.date == endTime.date) {
            //同一天开始结束
            result.add(startTime to endTime)
        } else {
            //不同天开始结束
            var start = startTime
            while (start < endTime) {
                val end = start.date.plus(1, DateTimeUnit.DAY).atTime(0, 0)
                result.add(start to end)
                val temp = start.date.plus(1, DateTimeUnit.DAY)
                start = temp.atTime(start.time)
            }
        }
        return result
    }

    /**
     * 计算上课日期
     * @param startDate 开学时间
     * @param weekNum 周数
     * @param dayOfWeek 周几
     */
    private fun calculateDate(startDate: LocalDate, weekNum: Int, dayOfWeek: DayOfWeek): LocalDate =
        startDate.plus(weekNum - 1, DateTimeUnit.WEEK)
            .plus(dayOfWeek.isoDayNumber - 1, DateTimeUnit.DAY)

    private fun parseTime(time: String): LocalTime =
        LocalTime.parse(time, Formatter.TIME_NO_SECONDS)
}