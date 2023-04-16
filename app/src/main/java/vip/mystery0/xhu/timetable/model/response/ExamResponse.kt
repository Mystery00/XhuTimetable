package vip.mystery0.xhu.timetable.model.response

import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime

data class ExamResponse(
    //课程编号
    val courseNo: String,
    //课程名称
    val courseName: String,
    //考试地点
    val location: String,
    //座位号
    val seatNo: String,
    //考试类型名称
    val examName: String,
    //考试日期
    val examDay: LocalDate,
    //考试开始时间
    val examStartTime: LocalTime,
    //考试结束时间
    val examEndTime: LocalTime,
    //考试开始时间-时间戳
    val examStartTimeMills: Instant,
    //考试结束时间-时间戳
    val examEndTimeMills: Instant,
    //考试地区
    val examRegion: String,
)
