package vip.mystery0.xhu.timetable.model

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
