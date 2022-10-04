package vip.mystery0.xhu.timetable.model.response

data class AcademicReportResponse(
    var title: String,
    var reportTime: Long,
    var location: String,
    var speaker: String,
    var organizer: String,
    var detailUrl: String,
    var articleContentHtml: String,
)
