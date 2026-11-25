package vip.mystery0.xhu.timetable.nativeui

import io.github.vinceglb.filekit.absolutePath
import kotlinx.coroutines.CancellationException
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.daysUntil
import kotlinx.datetime.format
import kotlinx.datetime.isoDayNumber
import kotlinx.serialization.json.*
import vip.mystery0.xhu.timetable.base.YearSelectDataLoader
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.request.ClassroomRequest
import vip.mystery0.xhu.timetable.model.request.SchoolTimetableRequest
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.model.response.SchoolTimetableResponse
import vip.mystery0.xhu.timetable.model.ws.MessageType
import vip.mystery0.xhu.timetable.model.ws.Status
import vip.mystery0.xhu.timetable.repository.*
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.chinaDateTime
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.utils.formatWithDecimal
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.sha256
import vip.mystery0.xhu.timetable.utils.thingDateFormatter
import vip.mystery0.xhu.timetable.utils.thingDateTimeFormatter
import vip.mystery0.xhu.timetable.utils.thingTimeFormatter
import vip.mystery0.xhu.timetable.viewmodel.AreaSelectDataLoader
import vip.mystery0.xhu.timetable.viewmodel.suggestClassroomPeriods
import kotlin.uuid.Uuid

object NativeQueries {
    private const val PAGE_SIZE = 20
    private class TimetableCache(val owner: String) {
        val items = mutableMapOf<String, SchoolTimetableResponse>()
    }
    private var timetableCache: TimetableCache? = null

    private suspend fun timetableMainUser(): User = UserStore.getMainUser() ?: error("请先登录")

    suspend fun options(params: JsonObject): JsonElement {
        val kind = params.text("kind")
        val user = if (kind == "timetable") timetableMainUser() else selectedUser(params)
        val yearLoader = YearSelectDataLoader()
        val yearItems = yearLoader.initSelect()
        val campuses = if (kind == "timetable") CourseRepo.loadCampusList() else emptyMap()
        val colleges = if (kind == "timetable") CourseRepo.loadCollegeList() else emptyMap()
        val college = params.text("college")
        val majors = if (kind == "timetable" && college.isNotBlank()) CourseRepo.loadMajorList(college) else emptyMap()
        return buildJsonObject {
            put("year", params.number("year", getConfigStore { nowYear }))
            put("term", params.number("term", getConfigStore { nowTerm }))
            put("years", JsonArray(yearItems.map { option(it.value.toString(), it.title) }))
            put("users", userSummaries())
            put("selectedUser", user.studentId)
            put("campus", params.text("campus"))
            put("campuses", JsonArray(campuses.map { (label, value) -> option(value, label) }))
            put("colleges", JsonArray(colleges.map { (label, value) -> option(value, label) }))
            put("majors", JsonArray(majors.map { (label, value) -> option(value, label) }))
            put("buildings", JsonArray(AreaSelectDataLoader().initSelect().map { option(it.value, it.title) }))
            put("week", WidgetRepo.calculateWeek().coerceIn(1, 20))
            put("day", LocalDate.now().dayOfWeek.isoDayNumber)
            put("periods", JsonArray(suggestClassroomPeriods(LocalTime.now()).map(::JsonPrimitive)))
        }
    }

    suspend fun query(params: JsonObject): JsonElement {
        val page = params.number("page", 1)
        require(page > 0) { "页码必须从 1 开始" }
        val index = page - 1
        val kind = params.text("kind")
        if (kind == "licenses") return licenses(page)
        val user = if (kind == "timetable") timetableMainUser() else selectedUser(params)
        val year = params.number("year", getConfigStore { nowYear })
        val term = params.number("term", getConfigStore { nowTerm })
        return when (kind) {
            "scores" -> {
                val result = ScoreRepo.getScorePage(user, year, term, index, PAGE_SIZE)
                val gpa = if (page == 1) {
                    try {
                        ScoreRepo.getGpa(user, year, term)
                    } catch (cancelled: CancellationException) {
                        throw cancelled
                    } catch (_: Exception) {
                        null
                    }
                } else null
                val metrics = gpa?.let {
                    listOf(
                        detailField("gpa", "学期平均绩点 (GPA)", it.gpa.formatWithDecimal(2)),
                        detailField("averageScore", "平均成绩", it.averageScore.formatWithDecimal(2)),
                        detailField("totalCredit", "总学分", it.totalCredit.formatWithDecimal(2)),
                        detailField("totalScore", "总成绩", it.totalScore.formatWithDecimal(2)),
                    )
                } ?: emptyList()
                val summary = when {
                    gpa != null -> "平均成绩 ${gpa.averageScore.formatWithDecimal(2)} · 总学分 ${gpa.totalCredit.formatWithDecimal(2)} · 平均绩点 ${gpa.gpa.formatWithDecimal(2)}"
                    page == 1 -> "绩点暂未获取，可下拉重试"
                    else -> ""
                }
                listResult("成绩查询", result.items.map {
                    displayItem(
                        id = "${it.courseNo}:${it.teachingClassName}:${it.scoreType}", title = it.courseName,
                        subtitle = "${it.courseType} · ${it.scoreType}", value = it.score.toString(),
                        badge = it.scoreType, color = scoreColor(it.score), detail = it.scoreDescription,
                        fields = listOf(
                            detailField("credit", "学分", it.credit.toString()),
                            detailField("gpa", "绩点", it.gpa.toString()),
                            detailField("creditGpa", "学分绩点", it.creditGpa.toString()),
                            detailField("courseNo", "课程编号", it.courseNo),
                            detailField("teachingClassName", "教学班", it.teachingClassName),
                            detailField("scoreDescription", "成绩说明", it.scoreDescription),
                            detailField("courseType", "课程性质", it.courseType),
                            detailField("scoreType", "成绩性质", it.scoreType),
                        ),
                    )
                }, page, result.hasNext, summary, metrics)
            }
            "exams" -> {
                val result = ExamRepo.getExamPage(user, year, term, index, PAGE_SIZE)
                listResult("考试安排", result.items.map {
                    displayItem(
                        id = "${it.courseName}:${it.date}:${it.time}", title = it.courseName,
                        subtitle = it.examName, time = "${it.dateString} ${it.time}", location = it.location,
                        color = it.courseColor.nativeHex(), status = it.examStatus.title, badge = it.showText,
                        date = it.date.toString(),
                        detail = listOf(
                            it.courseName, "考试日期：${it.dateString}", "考试时间：${it.time}",
                            "考试地点：${it.location}", "座位号：${it.seatNo}", "考试类型：${it.examName}",
                            "考试区域：${it.examRegion}", "考试状态：${it.examStatus.title}",
                        ).joinToString("\n"),
                        fields = listOf(
                            detailField("seatNo", "座位号", it.seatNo),
                            detailField("date", "考试日期", it.date.toString()),
                            detailField("dateString", "考试日期", it.dateString),
                            detailField("time", "考试时间", it.time),
                            detailField("examName", "考试类型", it.examName),
                            detailField("examRegion", "考试区域", it.examRegion),
                            detailField("status", "考试状态", it.examStatus.name),
                            detailField("showText", "考试提醒", it.showText),
                        ),
                    )
                }, page, result.hasNext)
            }
            "expScores" -> listResult("实验成绩", ScoreRepo.fetchExpScoreList(user, year, term).map {
                displayItem(
                    id = "${it.teachingClassName}:${it.courseName}", title = it.courseName,
                    subtitle = it.teachingClassName, value = it.totalScore.toString(), color = scoreColor(it.totalScore),
                    children = it.itemList.mapIndexed { itemIndex, item ->
                        displayItem(
                            id = "${it.teachingClassName}:${it.courseName}:$itemIndex", title = item.experimentProjectName,
                            value = item.score.toString(), color = scoreColor(item.score),
                            fields = listOf(
                                detailField("credit", "学分", item.credit.toString()),
                                detailField("mustTest", "选必做", item.mustTest),
                                detailField("scoreDescription", "成绩说明", item.scoreDescription),
                            ),
                        )
                    },
                )
            })
            "rooms" -> {
                val weeks = params.numbers("weeks")
                val days = params.numbers("days")
                val periods = params.numbers("periods")
                require(weeks.isNotEmpty() && weeks.all { it in 1..20 }) { "请至少选择一个有效查询周次" }
                require(days.isNotEmpty() && days.all { it in 1..7 }) { "请至少选择一个有效查询星期" }
                require(periods.isNotEmpty() && periods.all { it in 1..11 }) { "请至少选择一个有效查询节次" }
                val result = ClassroomRepo.getClassroomPage(
                    ClassroomRequest(params.text("area"), weeks.distinct(), days.distinct(), periods.distinct()), index, PAGE_SIZE)
                listResult("空闲教室", result.items.map {
                    displayItem(
                        id = it.roomNo, title = it.roomName, subtitle = "${it.campus} · ${it.roomType}",
                        badge = "${it.seatCount} 座", location = "${it.buildingNo} · ${it.floorNo}层",
                        color = ColorPool.hash(it.roomName).nativeHex(),
                        detail = listOf("教室编号：${it.roomNo}", "考试座位：${it.examSeatCount}",
                            "借用类型：${it.roomBorrowType}", "使用部门：${it.usedDepartment}",
                            "托管部门：${it.entrustedDepartment}", it.roomRemark).joinToString("\n"),
                        fields = listOf(
                            detailField("roomNo", "教室编号", it.roomNo),
                            detailField("campus", "校区", it.campus),
                            detailField("roomType", "教室类型", it.roomType),
                            detailField("roomRemark", "备注", it.roomRemark),
                        ),
                    )
                }, page, result.hasNext)
            }
            "notices" -> {
                val result = NoticeRepo.getNoticePage(index, PAGE_SIZE)
                listResult("通知公告", result.items.map {
                    displayItem(
                        id = it.noticeId.toString(), title = it.title, detail = it.content,
                        time = it.createTime.asLocalDateTime().format(chinaDateTime),
                        actions = JsonArray(it.actions.filter { action -> action.actionType in setOf("COPY", "OPEN_URI") }.map { action ->
                            buildJsonObject { put("type", action.actionType); put("label", action.text); put("value", action.metadata) }
                        }),
                    )
                }, page, result.hasNext)
            }
            "customCourses" -> {
                val result = CustomCourseRepo.getCustomCoursePage(user, year, term, index, PAGE_SIZE)
                listResult("自定义课程", result.items.map { courseItem(it) }, page, result.hasNext)
            }
            "customThings" -> {
                val result = CustomThingRepo.getCustomThingPage(user, index, PAGE_SIZE)
                listResult("自定义事项", result.items.map { thingItem(it) }, page, result.hasNext)
            }
            "courseColors" -> listResult("自定义课程颜色", CourseColorRepo.getCourseColorList(params.text("keywords")).map { (name, color) ->
                displayItem(id = name, title = name, color = color.data.nativeHex(), selected = color.custom)
            })
            "schoolCalendar" -> listResult("校历", SchoolCalendarRepo.getList().map {
                displayItem(id = it.area, title = it.area, image = it.imageUrl, url = it.imageUrl)
            })
            "backgrounds" -> {
                val backgrounds = BackgroundRepo.getList()
                val selectedPath = getConfigStore { backgroundImage?.absolutePath() }
                val selectedFileName = selectedPath?.substringAfterLast('/')
                val selectedBackground = backgrounds.firstOrNull {
                    selectedFileName?.matches(Regex("${it.imageUrl.sha256()}-\\d+\\.(jpg|jpeg|png|webp)")) == true
                }
                val items = mutableListOf(displayItem(
                    id = "default", title = "默认背景", image = "main_bg.png", url = "default",
                    selected = selectedPath == null,
                ))
                if (selectedPath != null && selectedBackground == null) {
                    items.add(displayItem(
                        id = "local", title = "本地图片", image = selectedPath, selected = true,
                    ))
                }
                items.addAll(backgrounds.map {
                    displayItem(
                        id = it.backgroundId.toString(), title = "课表背景", image = it.thumbnailUrl, url = it.imageUrl,
                        selected = it.backgroundId == selectedBackground?.backgroundId,
                    )
                })
                listResult("背景设置", items)
            }
            "timetable" -> {
                val cache = if (page == 1) {
                    TimetableCache(user.studentId).also { timetableCache = it }
                } else {
                    timetableCache?.takeIf { it.owner == user.studentId } ?: error("账号已切换，请重新查询课程")
                }
                val request = SchoolTimetableRequest(
                    params.text("campus").ifBlank { null }, params.text("college").ifBlank { null },
                    params.text("major").ifBlank { null }, params.text("courseName").ifBlank { null },
                    params.text("teacherName").ifBlank { null },
                )
                val result = CourseRepo.getAllCoursePage(request, index, PAGE_SIZE, user)
                check(timetableMainUser().studentId == cache.owner) { "账号已切换，请重新查询课程" }
                check(timetableCache === cache) { "查询结果已更新，请重新查询课程" }
                listResult("全校课表", result.items.map {
                    val id = Uuid.random().toString()
                    cache.items[id] = it
                    val semesters = it.customCourseList.map { course ->
                        "${course.year}-${course.year + 1}学年 第${course.term}学期"
                    }.distinct().joinToString("、").ifEmpty { "未提供" }
                    displayItem(id = id, title = it.courseName, subtitle = it.teacher, time = it.showTimeString,
                        location = it.location, detail = "${it.showTimeString}\n${it.location}\n任课教师：${it.teacher}\n学年学期：$semesters",
                        color = ColorPool.hash(it.courseName).nativeHex(),
                        fields = listOf(
                            detailField("teacher", "任课教师", it.teacher),
                            detailField("time", "周次时段", it.showTimeString),
                            detailField("location", "上课地点", it.location),
                            detailField("yearTerm", "学年学期", semesters),
                            detailField("slotCount", "排课数量", it.customCourseList.size.toString()),
                        ))
                }, page, result.hasNext)
            }
            "feedback" -> feedback(page, params.text("beforeId"))
            "urge" -> {
                val result = UrgeRepo.getUrgePage(index, PAGE_SIZE)
                listResult("功能催更", result.items.map {
                    displayItem(id = it.urgeId.toString(), title = it.title, detail = it.description,
                        value = it.count.toString(), subtitle = "完成进度 ${it.rate}%",
                        status = when { it.complete -> "已完成"; it.urged -> "已催更"; else -> "可催更" })
                }, page, result.hasNext, "今日还可催更 ${UrgeRepo.remainCountFlow.value} 次")
            }
            else -> error("未知查询页面")
        }
    }

    suspend fun editor(params: JsonObject): JsonElement = NativeEditors.editor(params)

    suspend fun save(params: JsonObject): JsonElement {
        if (params.text("kind") == "timetable") {
            val user = timetableMainUser()
            val cache = timetableCache ?: error("请重新查询后选择课程")
            require(cache.owner == user.studentId) { "账号已切换，请重新查询课程" }
            val course = cache.items[params.text("id")] ?: error("请重新查询后选择课程")
            require(course.customCourseList.isNotEmpty()) { "该课程没有可导入的排课数据" }
            course.customCourseList.forEach { CustomCourseRepo.createCustomCourse(user, it) }
            EventBus.post(EventType.CHANGE_SHOW_CUSTOM_COURSE)
            return JsonNull
        }
        return NativeEditors.save(params)
    }

    suspend fun delete(params: JsonObject): JsonElement = NativeEditors.delete(params)

    suspend fun feedbackSend(params: JsonObject): JsonElement {
        val content = params.text("content").trim()
        require(content.isNotEmpty()) { "请输入反馈内容" }
        sendNativeFeedback(content)
        return JsonNull
    }

    suspend fun urge(params: JsonObject): JsonElement {
        val id = params.text("id").toLongOrNull()
        require(id != null && id > 0) { "请选择要催更的功能" }
        UrgeRepo.doUrge(id)
        return JsonNull
    }

    fun dispose() {
        timetableCache = null
    }

    private suspend fun feedback(page: Int, beforeId: String): JsonObject {
        val cursor = if (page == 1) Long.MAX_VALUE else beforeId.toLongOrNull()?.takeIf { it > 0 }
            ?: error("请从已显示的最早消息继续加载")
        val messages = FeedbackRepo.loadLastMessage(cursor, PAGE_SIZE)
        val next = messages.minOfOrNull { it.id }
        if (page == 1 && messages.isNotEmpty()) {
            setCacheStore { firstFeedbackMessageId = messages.maxOf { it.id } }
            EventBus.post(EventType.UPDATE_FEEDBACK_CHECK)
        }
        return listResult("意见反馈", messages.sortedByDescending { it.id }.map {
            val visible = it.status == Status.NORMAL
            displayItem(
                id = it.id.toString(), title = if (it.isMe) "我" else "开发者",
                detail = when { !visible -> "消息已撤回"; it.messageType == MessageType.TEXT -> it.content; else -> "附件" },
                time = it.sendTime.asLocalDateTime().toString().replace('T', ' '),
                status = if (it.isMe) "sent" else "received",
                image = if (visible && it.messageType == MessageType.IMAGE) it.content else "",
                url = if (visible && it.messageType != MessageType.TEXT) it.content else "",
            )
        }, page, messages.size == PAGE_SIZE && next != null && next < cursor)
    }

    private suspend fun licenses(page: Int): JsonObject {
        val root = nativeJson.parseToJsonElement(readNativeLicenses()).jsonObject
        val licenses = root["licenses"] as? JsonObject ?: JsonObject(emptyMap())
        val libraries = root["libraries"] as? JsonArray ?: JsonArray(emptyList())
        val offset = (page - 1).toLong() * PAGE_SIZE
        val items = libraries.drop(offset.coerceAtMost(libraries.size.toLong()).toInt()).take(PAGE_SIZE).map { element ->
            val library = element.jsonObject
            val licenseIds = library["licenses"] as? JsonArray ?: JsonArray(emptyList())
            val entries = licenseIds.mapNotNull { licenses[it.jsonPrimitive.content] as? JsonObject }
            displayItem(
                id = library.text("uniqueId"), title = library.text("name", library.text("uniqueId")),
                subtitle = library.text("artifactVersion"), badge = entries.joinToString(" · ") { it.text("name") },
                detail = listOf(library.text("description"), entries.joinToString("\n\n") {
                    "${it.text("name")}\n${it.text("content")}"
                }).filter { it.isNotBlank() }.joinToString("\n\n"),
                url = library.text("website").ifBlank { entries.firstOrNull()?.text("url") ?: "" },
            )
        }
        return listResult("开源许可", items, page, offset + PAGE_SIZE < libraries.size, "共 ${libraries.size} 个开源依赖")
    }
}

internal suspend fun selectedUser(params: JsonObject): User {
    val id = params.text("user")
    return if (id.isBlank()) UserStore.getMainUser() ?: error("请先登录")
    else UserStore.getUserByStudentId(id) ?: error("所选账号已退出，请重新选择")
}

private fun scoreColor(score: Double): String = when {
    score < 60 -> "#C94444"
    score >= 90 -> "#27834C"
    score >= 75 -> "#267D78"
    else -> "#B06B20"
}

internal fun courseItem(course: CustomCourseResponse): JsonObject = displayItem(
    id = course.courseId.toString(), title = course.courseName, subtitle = course.teacher,
    time = "${course.weekStr} · ${course.day.formatWeekString()} · ${course.startDayTime}-${course.endDayTime}节",
    location = course.location, color = ColorPool.hash(course.courseName).nativeHex(),
    detail = course.extraData.joinToString("\n"),
    startTime = course.startTime.format(thingTimeFormatter), endTime = course.endTime.format(thingTimeFormatter),
    fields = listOf(
        detailField("weeks", "上课周次", course.weekStr),
        detailField("day", "星期", course.day.formatWeekString()),
        detailField("periods", "上课节次", "${course.startDayTime}-${course.endDayTime}节"),
        detailField("teacher", "任课教师", course.teacher),
        detailField("location", "上课地点", course.location),
        detailField("createTime", "添加时间", course.createTime.asLocalDateTime().format(chinaDateTime)),
    ),
)

internal fun thingItem(thing: CustomThingResponse): JsonObject {
    val start = thing.startTime.asLocalDateTime()
    val end = thing.endTime.asLocalDateTime()
    val countdown = thing.saveAsCountDown
    val time = buildString {
        append(if (thing.allDay) start.date.format(thingDateFormatter) else start.format(thingDateTimeFormatter))
        if (!countdown) {
            when {
                thing.allDay && start.date != end.date -> append(" — ${end.date.format(thingDateFormatter)}")
                !thing.allDay && start.date == end.date -> append(" — ${end.time.format(thingTimeFormatter)}")
                !thing.allDay -> append(" — ${end.format(thingDateTimeFormatter)}")
            }
        }
    }
    val badge = when {
        countdown -> {
            val days = LocalDate.now().daysUntil(start.date)
            when {
                days > 0 -> "还剩 $days 天"
                days == 0 -> "就在今天"
                else -> "已过 ${-days} 天"
            }
        }
        thing.allDay -> "全天事项"
        else -> "日程提醒"
    }
    return displayItem(
        id = thing.thingId.toString(), title = thing.title, detail = thing.remark,
        time = time, location = thing.location, color = thing.color, badge = badge,
        date = start.date.toString(), startTime = start.time.format(thingTimeFormatter), endTime = end.time.format(thingTimeFormatter),
        fields = listOf(
            detailField("allDay", "全天事项", thing.allDay.toString()),
            detailField("countdown", "保存为倒计时", countdown.toString()),
            detailField("startDate", "开始日期", start.date.toString()),
            detailField("endDate", "结束日期", end.date.toString()),
            detailField("startTime", "开始时间", start.time.format(thingTimeFormatter)),
            detailField("endTime", "结束时间", end.time.format(thingTimeFormatter)),
            detailField("remark", "备注", thing.remark),
            detailField("createTime", "添加时间", thing.createTime.asLocalDateTime().format(chinaDateTime)),
        ),
    )
}

expect suspend fun sendNativeFeedback(content: String)

expect suspend fun readNativeLicenses(): String
