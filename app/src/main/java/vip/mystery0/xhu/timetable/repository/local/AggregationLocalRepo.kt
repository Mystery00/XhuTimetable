package vip.mystery0.xhu.timetable.repository.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.UserInfo
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.entity.CourseEntity
import vip.mystery0.xhu.timetable.model.entity.CustomCourseEntity
import vip.mystery0.xhu.timetable.model.entity.CustomThingEntity
import vip.mystery0.xhu.timetable.model.entity.ExperimentCourseEntity
import vip.mystery0.xhu.timetable.model.entity.PracticalCourseEntity
import vip.mystery0.xhu.timetable.model.response.AggregationMainPageResponse
import vip.mystery0.xhu.timetable.model.response.Course
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.model.response.ExperimentCourse
import vip.mystery0.xhu.timetable.model.response.PracticalCourse
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao
import vip.mystery0.xhu.timetable.repository.db.dao.CustomCourseDao
import vip.mystery0.xhu.timetable.repository.db.dao.CustomThingDao
import vip.mystery0.xhu.timetable.repository.db.dao.ExperimentCourseDao
import vip.mystery0.xhu.timetable.repository.db.dao.PracticalCourseDao
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import java.time.DayOfWeek
import java.time.Instant
import kotlin.random.Random

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
        val data = withContext(Dispatchers.IO) {
            query()
        }
        withContext(Dispatchers.Default) {
            dataList.addAll(data.map(dataMap))
        }
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
                    day = DayOfWeek.of(it.dayIndex),
                    dayIndex = it.dayIndex,
                    startDayTime = it.startDayTime,
                    endDayTime = it.endDayTime,
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
                    day = DayOfWeek.of(it.dayIndex),
                    dayIndex = it.dayIndex,
                    startDayTime = it.startDayTime,
                    endDayTime = it.endDayTime,
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
                        day = DayOfWeek.of(it.dayIndex),
                        dayIndex = it.dayIndex,
                        startDayTime = it.startDayTime,
                        endDayTime = it.endDayTime,
                        location = it.location,
                        teacher = it.teacher,
                        extraData = it.extraData,
                        createTime = Instant.ofEpochMilli(it.createTime),
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
                        startTime = Instant.ofEpochMilli(it.startTime),
                        endTime = Instant.ofEpochMilli(it.endTime),
                        remark = it.remark,
                        color = it.color,
                        metadata = it.metadata,
                        createTime = Instant.ofEpochMilli(it.createTime),
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

    suspend fun saveResponse(
        year: Int,
        term: Int,
        user: User,
        response: AggregationMainPageResponse,
    ) {
        response.courseList.forEach {
            val entity = CourseEntity(
                courseName = it.courseName,
                weekStr = it.weekStr,
                weekList = it.weekList,
                dayIndex = it.dayIndex,
                startDayTime = it.startDayTime,
                endDayTime = it.endDayTime,
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
        response.customCourseList.forEach {
            val entity = CustomCourseEntity(
                courseId = it.courseId,
                courseName = it.courseName,
                weekStr = it.weekStr,
                weekList = it.weekList,
                dayIndex = it.dayIndex,
                startDayTime = it.startDayTime,
                endDayTime = it.endDayTime,
                location = it.location,
                teacher = it.teacher,
                extraData = it.extraData,
                createTime = it.createTime.toEpochMilli(),
                year = year,
                term = term,
                studentId = user.studentId,
            )
            withContext(Dispatchers.IO) {
                customCourseDao.insert(entity)
            }
        }
        response.customThingList.forEach {
            val entity = CustomThingEntity(
                thingId = it.thingId,
                title = it.title,
                location = it.location,
                allDay = it.allDay,
                startTime = it.startTime.toEpochMilli(),
                endTime = it.endTime.toEpochMilli(),
                remark = it.remark,
                color = it.color,
                metadata = it.metadata,
                createTime = it.createTime.toEpochMilli(),
                studentId = user.studentId,
            )
            withContext(Dispatchers.IO) {
                customThingDao.insert(entity)
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
                    dayIndex = DayOfWeek.MONDAY.value,
                    startDayTime = 1,
                    endDayTime = 1,
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
        val userInfo = UserInfo("3120150905411", "", Gender.MALE, 2015, "", "", "", "")
        return resultList.map {
            WeekCourseView(
                courseName = it.courseName,
                weekStr = it.weekStr,
                weekList = it.weekList,
                day = DayOfWeek.of(it.dayIndex),
                startDayTime = it.startDayTime,
                endDayTime = it.endDayTime,
                courseDayTime = "",
                courseTime = "",
                location = it.location,
                teacher = it.teacher,
                extraData = it.extraData,
                user = User("3120150905411", "", "", userInfo, null)
            ).apply {
                thisWeek = Random.nextBoolean()
                backgroundColor =
                    if (thisWeek) ColorPool.hash(it.courseName) else XhuColor.notThisWeekBackgroundColor
            }
        }
    }
}