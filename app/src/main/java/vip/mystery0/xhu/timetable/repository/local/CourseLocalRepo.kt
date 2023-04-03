package vip.mystery0.xhu.timetable.repository.local

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.config.User
import vip.mystery0.xhu.timetable.config.runOnIo
import vip.mystery0.xhu.timetable.model.entity.CourseItem
import vip.mystery0.xhu.timetable.model.entity.CourseSource
import vip.mystery0.xhu.timetable.model.entity.CourseType
import vip.mystery0.xhu.timetable.model.response.CourseResponse
import vip.mystery0.xhu.timetable.model.response.OldCourseResponse
import vip.mystery0.xhu.timetable.repository.CourseRepo
import vip.mystery0.xhu.timetable.repository.db.dao.CourseDao

class CourseLocalRepo : CourseRepo {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
        .adapter<List<String>>(
            Types.newParameterizedType(
                List::class.java,
                String::class.java
            )
        )

    private val courseDao: CourseDao by inject()

    override suspend fun fetchCourseList(user: User, year: Int, term: Int): CourseResponse {
        TODO("Not yet implemented")
    }

    override suspend fun getCourseList(
        user: User,
        year: String,
        term: Int,
    ): List<OldCourseResponse> = runOnIo {
        val courseList = courseDao.queryCourseList(user.studentId, year, term)
        val result = ArrayList<OldCourseResponse>(courseList.size)
        val map = HashMap<String, OldCourseResponse>()
        val weekMap = HashMap<String, ArrayList<Int>>()
        courseList.forEach { item ->
            val key =
                "${item.courseName}!${item.teacherName}!${item.location}!${item.weekIndex}!${item.time}!${item.type}"
            var courseItem = map[key]
            if (courseItem == null) {
                var extraData = item.extraData
                if (extraData.isBlank()) {
                    extraData = "[]"
                }
                courseItem = OldCourseResponse(
                    item.courseName,
                    item.teacherName,
                    item.location,
                    item.weekString,
                    arrayListOf(),
                    item.time.split(",").map { it.toInt() },
                    item.type,
                    item.weekIndex,
                    moshi.fromJson(extraData) ?: emptyList()
                )
                map[key] = courseItem
            }
            val list = weekMap[key] ?: arrayListOf()
            list.add(item.weekNum)
            weekMap[key] = list
        }
        map.forEach { (key, courseResponse) ->
            result.add(
                OldCourseResponse(
                    courseResponse.name,
                    courseResponse.teacher,
                    courseResponse.location,
                    courseResponse.weekString,
                    weekMap[key]!!,
                    courseResponse.time,
                    courseResponse.type,
                    courseResponse.day,
                    courseResponse.extraData,
                )
            )
        }
        result.forEach { it.user = user }
        result
    }

    override suspend fun saveCourseList(
        year: String,
        term: Int,
        studentId: String,
        list: List<OldCourseResponse>
    ) = runOnIo {
        //删除旧数据
        courseDao.queryCourseList(studentId, year, term).forEach {
            courseDao.deleteCourseItem(it)
        }
        list.forEach { course ->
            course.week.forEach { week ->
                courseDao.saveCourseItem(
                    CourseItem(
                        course.name,
                        course.teacher,
                        course.location,
                        course.weekString,
                        week,
                        course.time.joinToString(","),
                        course.day,
                        course.type,
                        CourseSource.JWC,
                        moshi.toJson(course.extraData),
                        year,
                        term,
                        studentId,
                    )
                )
            }
        }
    }

    override suspend fun getRandomCourseList(size: Int): List<OldCourseResponse> = runOnIo {
        val courseList = ArrayList(courseDao.queryDistinctCourseByUsernameAndTerm())
        if (courseList.isEmpty()) {
            //没有数据，添加一条模拟数据
            courseList.add(
                CourseItem(
                    courseName = "西瓜课表",
                    teacherName = "西瓜课表团队",
                    location = "西华大学本部6A",
                    weekString = "",
                    weekNum = 1,
                    time = "",
                    weekIndex = 1,
                    type = CourseType.ALL,
                    source = CourseSource.JWC,
                    extraData = "[\"QQ群：426878786\"]",
                    year = "",
                    term = 1,
                    studentId = ""
                )
            )
        }
        while (courseList.size < size) {
            //数量不够，强行复制
            courseList.addAll(courseList)
        }
        val itemList = courseList.shuffled().take(size)
        itemList.map { item ->
            var extraData = item.extraData
            if (extraData.isBlank()) {
                extraData = "[]";
            }
            OldCourseResponse(
                item.courseName,
                item.teacherName,
                item.location,
                "",
                emptyList(),
                emptyList(),
                CourseType.ALL,
                item.weekIndex,
                moshi.fromJson(extraData) ?: emptyList()
            )
        }
    }
}