package vip.mystery0.xhu.timetable.model.entity

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import vip.mystery0.xhu.timetable.config.chinaZone
import java.time.Instant

@Entity(tableName = "tb_custom_thing")
data class CustomThing(
    //事件id
    @PrimaryKey val thingId: Long,
    //标题
    val title: String,
    //地点
    val location: String,
    //是否全天
    val allDay: Boolean,
    //开始时间
    val startTime: Long,
    //结束时间
    val endTime: Long,
    //备注
    val remark: String,
    //颜色
    val color: String,
    //额外数据
    val extraData: String,
    //学年
    val year: String,
    //学期
    val term: Int,
    //学号
    val studentId: String,
) {
    fun toModel(): vip.mystery0.xhu.timetable.model.CustomThing {
        val start = Instant.ofEpochMilli(startTime).atZone(chinaZone).toLocalDateTime()
        val end = Instant.ofEpochMilli(endTime).atZone(chinaZone).toLocalDateTime()
        val parseColor = android.graphics.Color.parseColor(color)
        return vip.mystery0.xhu.timetable.model.CustomThing(
            thingId,
            title,
            location,
            allDay,
            start,
            end,
            remark,
            color,
            Color(parseColor),
            extraData,
        )
    }
}
