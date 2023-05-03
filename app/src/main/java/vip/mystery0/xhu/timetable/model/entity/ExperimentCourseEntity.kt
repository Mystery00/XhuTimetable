package vip.mystery0.xhu.timetable.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_experiment_course")
data class ExperimentCourseEntity(
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
    //星期序号
    val dayIndex: Int,
    //开始节次
    val startDayTime: Int,
    //结束节次
    val endDayTime: Int,
    //校区
    val region: String,
    //？上课地点
    val location: String,
    //学年
    val year: Int,
    //学期
    val term: Int,
    //学号
    val studentId: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
)
