package vip.mystery0.xhu.timetable.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_custom_course")
data class CustomCourseEntity(
    val courseId: Long,
    val courseName: String,
    val weekStr: String,
    val weekList: List<Int>,
    val dayIndex: Int,
    val startDayTime: Int,
    val endDayTime: Int,
    val location: String,
    val teacher: String,
    val extraData: List<String>,
    val createTime: Long,
    //学年
    val year: Int,
    //学期
    val term: Int,
    //学号
    val studentId: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
)