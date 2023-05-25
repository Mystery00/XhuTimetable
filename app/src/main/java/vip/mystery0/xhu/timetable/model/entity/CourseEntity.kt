package vip.mystery0.xhu.timetable.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime

@Entity(tableName = "tb_course")
data class CourseEntity(
    //课程名称
    val courseName: String,
    //上课周显示字符串
    val weekStr: String,
    //上课周列表
    val weekList: List<Int>,
    //星期序号
    val dayIndex: Int,
    //开始节次
    val startDayTime: Int,
    //结束节次
    val endDayTime: Int,
    //开始上课时间
    val startTime: String,
    //结束上课时间
    var endTime: String,
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
    //学年
    val year: Int,
    //学期
    val term: Int,
    //学号
    val studentId: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
)