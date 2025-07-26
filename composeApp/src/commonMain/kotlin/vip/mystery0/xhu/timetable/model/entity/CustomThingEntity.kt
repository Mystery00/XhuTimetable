package vip.mystery0.xhu.timetable.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_custom_thing")
data class CustomThingEntity(
    var thingId: Long,
    var title: String,
    var location: String,
    var allDay: Boolean,
    var startTime: Long,
    var endTime: Long,
    var remark: String,
    var color: String,
    var metadata: String,
    val createTime: Long,
    //学号
    val studentId: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
)