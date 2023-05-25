package vip.mystery0.xhu.timetable.model.response

import java.time.DayOfWeek
import java.time.LocalTime


data class CourseResponse(
    //课程列表
    val courseList: List<Course>,
    //实践课程列表
    val practicalCourseList: List<PracticalCourse>,
    //实验课程列表
    val experimentCourseList: List<ExperimentCourse>,
)

data class Course(
    //课程名称
    val courseName: String,
    //上课周显示字符串
    val weekStr: String,
    //上课周列表
    val weekList: List<Int>,
    //星期
    val day: DayOfWeek,
    //星期序号
    val dayIndex: Int,
    //开始节次
    val startDayTime: Int,
    //结束节次
    val endDayTime: Int,
    //开始上课时间
    val startTime: LocalTime,
    //结束上课时间
    val endTime: LocalTime,
    //上课地点
    val location: String,
    //教师姓名
    val teacher: String,
    //备注
    val extraData: List<String>,
    //校区名称
    val campus: String,
    //课程性质
    val courseType: String,
    //学分
    val credit: Double,
    //学习代码-课程性质
    val courseCodeType: String,
    //课程代码标记
    val courseCodeFlag: String,
)

//实践课程
data class PracticalCourse(
    //课程名称
    val courseName: String,
    //上课周显示字符串
    val weekStr: String,
    //上课周列表
    val weekList: List<Int>,
    //教师姓名
    val teacher: String,
    //校区名称
    val campus: String,
    //学分
    val credit: Double,
)

//实验课程
data class ExperimentCourse(
    //课程名称
    val courseName: String,
    //实验项目名称
    val experimentProjectName: String,
    //实验人员-老师名称
    val teacherName: String,
    //实验分组名称
    val experimentGroupName: String,
    //上课周显示字符串
    val weekStr: String,
    //上课周列表
    val weekList: List<Int>,
    //星期
    val day: DayOfWeek,
    //星期序号
    val dayIndex: Int,
    //开始节次
    val startDayTime: Int,
    //结束节次
    val endDayTime: Int,
    //开始上课时间
    val startTime: LocalTime,
    //结束上课时间
    val endTime: LocalTime,
    //校区
    val region: String,
    //？上课地点
    val location: String,
)