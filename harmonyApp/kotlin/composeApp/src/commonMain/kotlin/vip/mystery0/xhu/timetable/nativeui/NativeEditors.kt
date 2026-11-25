package vip.mystery0.xhu.timetable.nativeui

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.atTime
import kotlinx.serialization.json.*
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.model.normalizeCustomThingInput
import vip.mystery0.xhu.timetable.model.parseCourseWeeks
import vip.mystery0.xhu.timetable.model.validateCustomCourseInput
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.repository.CourseColorRepo
import vip.mystery0.xhu.timetable.repository.CustomCourseRepo
import vip.mystery0.xhu.timetable.repository.CustomThingRepo
import vip.mystery0.xhu.timetable.utils.asInstant
import vip.mystery0.xhu.timetable.utils.asLocalDateTime

internal object NativeEditors {
    suspend fun editor(params: JsonObject): JsonElement {
        val user = editorUser(params)
        val id = entryId(params)
        val kind = params.text("kind")
        return when (kind) {
            "customCourses" -> {
                val (year, term) = semester(params)
                val course = if (id == null) CustomCourseResponse.init() else findCourse(user, year, term, id)
                buildJsonObject {
                    put("title", if (id == null) "新建自定义课程" else "编辑自定义课程")
                    put("fields", JsonArray(listOf(
                        field("courseName", "课程名称", course.courseName),
                        field("teacher", "任课教师", course.teacher),
                        field("location", "上课地点", course.location),
                        field("weeks", "上课周次", course.weekList.joinToString(","), "weeks",
                            (1..20).map { option(it.toString(), "第${it}周") }),
                        field("day", "星期", course.dayIndex.toString(), "select",
                            (1..7).map { option(it.toString(), listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")[it - 1]) }),
                        field("startPeriod", "开始节次", course.startDayTime.toString(), "select",
                            (1..11).map { option(it.toString(), "第${it}节") }),
                        field("endPeriod", "结束节次", course.endDayTime.toString(), "select",
                            (1..11).map { option(it.toString(), "第${it}节") }),
                    )))
                }
            }
            "customThings" -> {
                val thing = if (id == null) CustomThingResponse.init() else findThing(user, id)
                val start = thing.startTime.asLocalDateTime()
                val end = thing.endTime.asLocalDateTime()
                buildJsonObject {
                    put("title", if (id == null) "新建自定义事项" else "编辑自定义事项")
                    put("fields", JsonArray(listOf(
                        field("title", "事项标题", thing.title),
                        field("location", "地点", thing.location),
                        field("allDay", "全天事项", (thing.allDay || thing.saveAsCountDown).toString(), "toggle"),
                        field("countdown", "保存为倒计时", thing.saveAsCountDown.toString(), "toggle"),
                        field("startDate", "开始日期", start.date.toString(), "date"),
                        field("startTime", "开始时间", timeText(start.time), "time"),
                        field("endDate", "结束日期", end.date.toString(), "date"),
                        field("endTime", "结束时间", timeText(end.time), "time"),
                        field("color", "颜色", thing.color),
                        field("remark", "备注", thing.remark, "multiline"),
                    )))
                }
            }
            else -> error("该项目不能编辑")
        }
    }

    suspend fun save(params: JsonObject): JsonElement {
        val kind = params.text("kind")
        val values = params["values"] as? JsonObject ?: error("编辑内容无效")
        if (kind == "courseColors") {
            val name = params.text("id")
            require(name.isNotBlank()) { "请选择课程" }
            val color = values.text("color")
            require(Regex("#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?").matches(color)) { "颜色须为 #RRGGBB 或 #AARRGGBB" }
            CourseColorRepo.updateCourseColor(name, color)
            EventBus.post(EventType.CHANGE_COURSE_COLOR)
            return JsonNull
        }
        val user = editorUser(params)
        val id = entryId(params)
        when (kind) {
            "customCourses" -> {
                val (year, term) = semester(params)
                val original = id?.let { findCourse(user, year, term, it) }
                val request = CustomCourseRequest(
                    courseName = values.text("courseName").trim(),
                    weekList = parseCourseWeeks(values.text("weeks")),
                    dayIndex = requiredInt(values, "day", "星期"),
                    startDayTime = requiredInt(values, "startPeriod", "开始节次"),
                    endDayTime = requiredInt(values, "endPeriod", "结束节次"),
                    location = values.text("location").trim(), teacher = values.text("teacher").trim(),
                    extraData = original?.extraData.orEmpty(),
                    year = year, term = term,
                )
                validateCustomCourseInput(request)
                if (id == null) CustomCourseRepo.createCustomCourse(user, request)
                else CustomCourseRepo.updateCustomCourse(user, id, request)
                EventBus.post(EventType.CHANGE_SHOW_CUSTOM_COURSE)
            }
            "customThings" -> {
                val original = if (id != null) findThing(user, id) else null
                val countdown = requiredBoolean(values, "countdown")
                val start = dateTime(values, "startDate", "startTime")
                val end = if (countdown) start else dateTime(values, "endDate", "endTime")
                val metadata = original?.metadata?.takeIf { it.isNotBlank() }?.let {
                    nativeJson.parseToJsonElement(it).jsonObject.toMutableMap()
                } ?: mutableMapOf()
                metadata[CustomThing.Key.SAVE_AS_COUNT_DOWN] = JsonPrimitive(countdown.toString())
                val request = normalizeCustomThingInput(CustomThingRequest(
                    title = values.text("title").trim(), location = values.text("location").trim(),
                    allDay = countdown || requiredBoolean(values, "allDay"),
                    startTime = start.asInstant().toEpochMilliseconds(), endTime = end.asInstant().toEpochMilliseconds(),
                    remark = values.text("remark"), color = values.text("color"),
                    metadata = JsonObject(metadata).toString(),
                ), countdown)
                if (id == null) CustomThingRepo.createCustomThing(user, request)
                else CustomThingRepo.updateCustomThing(user, id, request)
                EventBus.post(EventType.CHANGE_SHOW_CUSTOM_THING)
            }
            else -> error("该项目不能保存")
        }
        return JsonNull
    }

    suspend fun delete(params: JsonObject): JsonElement {
        if (params.text("kind") == "courseColors") {
            val name = params.text("id")
            require(name.isNotBlank()) { "请选择课程" }
            CourseColorRepo.updateCourseColor(name, null)
            EventBus.post(EventType.CHANGE_COURSE_COLOR)
            return JsonNull
        }
        val user = editorUser(params)
        val id = entryId(params) ?: error("请选择要删除的项目")
        when (params.text("kind")) {
            "customCourses" -> {
                val (year, term) = semester(params)
                findCourse(user, year, term, id)
                CustomCourseRepo.deleteCustomCourse(user, id)
                EventBus.post(EventType.CHANGE_SHOW_CUSTOM_COURSE)
            }
            "customThings" -> {
                findThing(user, id)
                CustomThingRepo.deleteCustomThing(user, id)
                EventBus.post(EventType.CHANGE_SHOW_CUSTOM_THING)
            }
            else -> error("该项目不能删除")
        }
        return JsonNull
    }

    private suspend fun findCourse(user: User, year: Int, term: Int, id: Long): CustomCourseResponse {
        var index = 0
        do {
            val page = CustomCourseRepo.getCustomCoursePage(user, year, term, index++, 50)
            page.items.firstOrNull { it.courseId == id }?.let { return it }
            if (!page.hasNext || page.items.isEmpty()) break
        } while (true)
        error("该课程已删除或不属于所选账号和学期，请刷新列表")
    }

    private suspend fun findThing(user: User, id: Long): CustomThingResponse {
        var index = 0
        do {
            val page = CustomThingRepo.getCustomThingPage(user, index++, 50)
            page.items.firstOrNull { it.thingId == id }?.let { return it }
            if (!page.hasNext || page.items.isEmpty()) break
        } while (true)
        error("该事项已删除或不属于所选账号，请刷新列表")
    }

    private suspend fun editorUser(params: JsonObject): User {
        require(params.text("user").isNotBlank()) { "请先选择操作账号" }
        return selectedUser(params)
    }

    private fun semester(params: JsonObject): Pair<Int, Int> {
        val year = params.number("year")
        val term = params.number("term")
        require(year in 1900..2200 && term in 1..2) { "请选择有效学年和学期" }
        return year to term
    }

    private fun entryId(params: JsonObject): Long? {
        val text = params.text("id")
        if (text.isBlank() || text == "0") return null
        val id = text.toLongOrNull()
        require(id != null && id > 0) { "项目编号无效，请刷新列表" }
        return id
    }

    private fun requiredInt(values: JsonObject, key: String, title: String): Int =
        values.text(key).toIntOrNull() ?: error("请选择$title")

    private fun requiredBoolean(values: JsonObject, key: String): Boolean =
        values.text(key).toBooleanStrictOrNull() ?: error("开关值无效")

    private fun dateTime(values: JsonObject, dateKey: String, timeKey: String): LocalDateTime {
        val date = runCatching { LocalDate.parse(values.text(dateKey)) }.getOrElse { error("日期格式无效，请重新选择") }
        val time = runCatching { LocalTime.parse(values.text(timeKey)) }.getOrElse { error("时间格式无效，请重新选择") }
        return date.atTime(time)
    }

    private fun timeText(time: LocalTime): String =
        time.hour.toString().padStart(2, '0') + ":" + time.minute.toString().padStart(2, '0')

    private fun field(key: String, title: String, value: String, type: String = "text", options: List<JsonObject> = emptyList()): JsonObject =
        buildJsonObject {
            put("key", key); put("title", title); put("value", value); put("type", type)
            put("options", JsonArray(options))
        }
}
