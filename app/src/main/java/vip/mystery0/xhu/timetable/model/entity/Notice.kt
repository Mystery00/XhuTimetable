package vip.mystery0.xhu.timetable.model.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tb_notice")
data class Notice(
    val serverId: String,
    val title: String,
    val content: String,
    val createTime: Long,
    var read: Boolean,
    @PrimaryKey(autoGenerate = true) val id: Int? = null,
)
