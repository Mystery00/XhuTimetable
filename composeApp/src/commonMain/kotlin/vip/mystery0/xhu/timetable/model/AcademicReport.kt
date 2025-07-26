package vip.mystery0.xhu.timetable.model

import kotlinx.serialization.Serializable
import vip.mystery0.xhu.timetable.config.datetime.XhuLocalDateTime

@Serializable
data class AcademicReport(
    var title: String,
    var reportTime: XhuLocalDateTime,
    var location: String,
    var speaker: String,
    var organizer: String,
    var detailUrl: String,
    var articleContentHtml: String,
)
