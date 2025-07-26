package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuInstant
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDate
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalTime

@Serializable
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
    val examDay: XhuLocalDate,
    //考试开始时间
    val examStartTime: XhuLocalTime,
    //考试结束时间
    val examEndTime: XhuLocalTime,
    //考试开始时间-时间戳
    val examStartTimeMills: XhuInstant,
    //考试结束时间-时间戳
    val examEndTimeMills: XhuInstant,
    //考试地区
    val examRegion: String,
)
