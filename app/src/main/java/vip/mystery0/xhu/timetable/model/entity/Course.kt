package vip.mystery0.xhu.timetable.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

@Entity(tableName = "tb_course_item")
data class CourseItem(
    //课程名称
    val courseName: String,
    //教师名称
    val teacherName: String,
    //上课地点
    val location: String,
    //上课周显示的字符串
    val weekString: String,
    //上课周
    val weekNum: Int,
    //节次[列表]
    val time: String,
    //周几
    val weekIndex: Int,
    //课程类型，0-全课，1-单周，2-双周
    val type: CourseType,
    //课程来源
    val source: CourseSource,
    //学年
    val year: String,
    //学期
    val term: Int,
    //学号
    val studentId: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
)

enum class CourseType(val type: Int) {
    //全部
    ALL(0),

    //单周
    SINGLE(1),

    //双周
    DOUBLE(2),

    //自定义课程
    CUSTOM_COURSE(3),
}

enum class CourseSource(val source: Int) {
    //教务系统
    JWC(0),

    //自定义
    CUSTOM(1),
}

class CourseConverts {
    @TypeConverter
    fun toCourseType(value: Int) = enumValues<CourseType>()[value]

    @TypeConverter
    fun fromCourseType(value: CourseType) = value.type

    @TypeConverter
    fun toCourseSource(value: Int) = enumValues<CourseSource>()[value]

    @TypeConverter
    fun fromCourseSource(value: CourseSource) = value.source
}