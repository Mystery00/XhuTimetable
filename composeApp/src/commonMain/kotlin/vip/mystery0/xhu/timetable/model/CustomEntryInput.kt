package vip.mystery0.xhu.timetable.model

import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest

fun parseCourseWeeks(text: String): List<Int> {
    require(text.isNotBlank()) { "请至少选择一个上课周次" }
    val weeks = text.replace('，', ',').split(',').flatMap { part ->
        val values = part.trim().split('-')
        require(values.size in 1..2) { "周次格式应为 1,2,3 或 1-5" }
        val start = values.first().trim().toIntOrNull()
        val end = values.last().trim().toIntOrNull()
        require(start != null && end != null && start in 1..20 && end in start..20) {
            "上课周次须在 1 至 20 周之间"
        }
        (start..end).toList()
    }
    return weeks.distinct().sorted()
}

fun validateCustomCourseInput(request: CustomCourseRequest) {
    require(request.courseName.isNotBlank()) { "请输入课程名称" }
    require(request.weekList.isNotEmpty() && request.weekList.all { it in 1..20 }) { "请选择有效上课周次" }
    require(request.dayIndex in 1..7) { "请选择有效星期" }
    require(request.startDayTime in 1..11 && request.endDayTime in 1..11) { "节次须在 1 至 11 节之间" }
    require(request.startDayTime <= request.endDayTime) { "开始节次不能大于结束节次" }
    require(request.year in 1900..2200 && request.term in 1..2) { "请选择有效学年和学期" }
}

fun normalizeCustomThingInput(request: CustomThingRequest, countdown: Boolean): CustomThingRequest {
    require(request.title.isNotBlank()) { "请输入事项标题" }
    val endTime = if (countdown) {
        require(request.startTime <= Long.MAX_VALUE - 86_400_000L) { "日期超出范围" }
        request.startTime + 86_400_000L
    } else request.endTime
    require(request.startTime <= endTime) { "开始时间不能晚于结束时间" }
    require(Regex("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?").matches(request.color)) { "颜色须为十六进制色值" }
    return request.copy(endTime = endTime)
}
