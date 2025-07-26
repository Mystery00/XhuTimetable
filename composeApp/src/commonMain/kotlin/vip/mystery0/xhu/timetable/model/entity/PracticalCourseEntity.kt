package vip.mystery0.xhu.timetable.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_practical_course")
data class PracticalCourseEntity(
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
    //学年
    val year: Int,
    //学期
    val term: Int,
    //学号
    val studentId: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
)
