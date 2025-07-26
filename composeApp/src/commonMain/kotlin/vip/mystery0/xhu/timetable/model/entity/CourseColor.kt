package vip.mystery0.xhu.timetable.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_course_color")
data class CourseColor(
    //课程名称
    val courseName: String,
    //课程颜色
    var color: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
)