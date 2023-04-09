package vip.mystery0.xhu.timetable.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

//@Entity(tableName = "tb_jwc_course")
data class JwcCourse(
    //课程名称
    val courseName: String,
    //上课周显示字符串
    val weekStr: String,
    //上课周列表，存储数据为数组
    val weekList: String,
    //星期序号
    val dayIndex: Int,
    //开始节次
    val startDayTime: Int,
    //结束节次
    val endDayTime: Int,
    //上课地点
    val location: String,
    //教师姓名
    val teacher: String,
    //备注，存储数据为数组
    val extraData: String,
    @PrimaryKey(autoGenerate = true) val id: Long? = null,
)
