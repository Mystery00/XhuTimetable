package vip.mystery0.xhu.timetable.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

data class AcademicReport(
    var title: String,
    var reportTime: LocalDateTime,
    var location: String,
    var speaker: String,
    var organizer: String,
    var detailUrl: String,
    var articleContentHtml: String,
)
