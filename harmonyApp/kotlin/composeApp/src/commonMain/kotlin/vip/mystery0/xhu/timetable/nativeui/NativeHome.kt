package vip.mystery0.xhu.timetable.nativeui

import io.github.vinceglb.filekit.absolutePath
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.plus
import kotlinx.datetime.format
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.config.store.PoemsStore
import kotlinx.serialization.json.*
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.model.response.CalendarDayItemType
import vip.mystery0.xhu.timetable.ui.theme.CourseColorHelper
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.formatChina
import vip.mystery0.xhu.timetable.viewmodel.CalendarSheetItem
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel

suspend fun homeSnapshot(model: MainViewModel): JsonObject {
    val config = getConfigStore { customUi }
    val background = getConfigStore { backgroundImage?.absolutePath() ?: "main_bg.png" }
    val showStatus = getConfigStore { showStatus }
    val hideBackgroundInDark = getConfigStore { disableBackgroundWhenNight }
    val showPoemTranslation = getConfigStore { PoemsStore.showPoemsTranslate }
    return buildJsonObject {
        put("title", model.selectedTodayHeaderText.value)
        put("customDate", model.isTodayDateCustomSelected.value)
        put("weekDays", JsonArray(model.todayWeekDays.value.map { day ->
            buildJsonObject {
                put("date", day.date.toString())
                put("weekShort", day.weekShort)
                put("dayNumber", day.dayNumber)
                put("isToday", day.isToday)
                put("isSelected", day.isSelected)
                put("hasCourse", day.hasCourse)
            }
        }))
        put("week", model.week.value)
        put("actualWeek", model.actualCurrentWeek.value)
        put("calendarEnabled", model.enableCalendarView.value)
        put("multiAccount", model.multiAccountMode.value)
        put("loading", model.loading.value)
        put("error", model.errorMessage.value)
        put("showTomorrow", model.showTomorrowCourse.value)
        put("showStatus", showStatus)
        put("poem", model.poems.value?.content.orEmpty())
        model.poems.value?.origin?.let { poem ->
            put("poemDetails", buildJsonObject {
                put("title", poem.title)
                put("author", poem.author)
                put("dynasty", poem.dynasty)
                put("lines", JsonArray(poem.content.map(::JsonPrimitive)))
                put("translation", JsonArray(if (showPoemTranslation) poem.translate.orEmpty().map(::JsonPrimitive) else emptyList()))
            })
        }
        put("holiday", model.holiday.value?.showTitle.orEmpty())
        put("holidayColor", model.holiday.value?.color?.nativeHex().orEmpty())
        put("background", background)
        put("backgroundBlur", config.backgroundImageBlur)
        put("hideBackgroundInDark", hideBackgroundInDark)
        put("cellHeight", config.weekItemHeight)
        put("cellRadius", config.weekItemCorner)
        put("textSize", config.weekTitleTextSize)
        put("cellOpacity", config.weekBackgroundAlpha)
        put("todayOpacity", config.todayBackgroundAlpha)
        put("today", JsonArray(model.todayCourse.value.mapIndexed { index, course ->
            displayItem(
                id = index.toString(), title = course.courseName, subtitle = course.teacherName,
                time = course.timeString, location = course.location, color = course.color.nativeHex(),
                status = course.courseStatus.title,
                badge = if (model.multiAccountMode.value) course.accountTitle else "",
                detail = "${course.showDate}\n${course.timeText.first} - ${course.timeText.second}\n${course.timeSet.sorted().joinToString(",")}节",
                startTime = course.timeText.first.format(Formatter.TIME_NO_SECONDS),
                endTime = course.timeText.second.format(Formatter.TIME_NO_SECONDS),
                date = course.showDate.toString(), nextCourse = course.isNextCourse,
                fields = listOf(detailField("weeks", "上课周次", course.weekStr)) +
                    course.extraData.map { detailField("extra", "附加信息", it) },
            )
        }))
        put("things", JsonArray(model.todayThing.value.mapIndexed { index, thing ->
            displayItem(
                id = index.toString(), title = thing.title, subtitle = thing.remark,
                time = thing.timeText, location = thing.location,
                color = thing.color.nativeHex(), badge = if (model.multiAccountMode.value) thing.accountTitle else "",
                value = if (thing.saveAsCountDown) thing.remainDays.toString() else "",
            )
        }))
        put("classTimeSlots", JsonArray(model.classTimeSlots.value.map { (start, end) ->
            buildJsonObject {
                put("start", start)
                put("end", end)
            }
        }))
        put("weeks", JsonArray((1..20).map { week ->
            val table = model.allWeekTables.value[week]
                ?: if (week == model.week.value) model.tableCourse.value else emptyList()
            buildJsonObject {
                put("week", week)
                put("date", model.termStartDate.value.plus(week - 1, DateTimeUnit.WEEK).toString())
                put("cells", JsonArray(table.flatten().filterNot { it.isEmpty() }.map { cell ->
                    buildJsonObject {
                        put("title", cell.showTitle)
                        put("day", cell.day)
                        put("start", cell.startIndex)
                        put("span", cell.step)
                        put("color", cell.color.nativeHex())
                        put("textColor", CourseColorHelper.readableTextOn(cell.color).nativeHex())
                        put("courses", JsonArray(cell.course.map { courseItem(it) }))
                    }
                }))
            }
        }))
        put("calendar", JsonArray(model.calendarList.value.map { week ->
            buildJsonObject {
                put("week", week.weekNum)
                put("title", week.title)
                put("cursor", week.cursor)
                put("days", JsonArray(week.items.map { day ->
                    buildJsonObject {
                        put("date", day.date.toString())
                        put("title", day.title)
                        put("today", day.today)
                        put("week", week.weekNum)
                        put("kind", day.type.name)
                        put("items", JsonArray(day.items.mapIndexed { index, item ->
                            displayItem(
                                id = index.toString(), title = item.title, subtitle = item.subtitle,
                                detail = item.text, color = item.color.nativeHex(), value = item.type.name,
                                children = listOf(item.calendarDetail(model.actualCurrentWeek.value)),
                            )
                        }))
                    }
                }))
            }
        }))
        put("weekOverview", JsonArray(model.weekView.value.map { week ->
            buildJsonObject {
                put("week", week.weekNum)
                put("current", week.thisWeek)
                put("cells", JsonArray(week.array.map { row -> JsonArray(row.map(::JsonPrimitive)) }))
            }
        }))
        put("practical", JsonArray(model.practicalCourseList.value.mapIndexed { index, item ->
            displayItem(id = index.toString(), title = item.courseName, subtitle = item.teacherName,
                time = item.showWeek, value = "${item.credit}学分", color = item.color.nativeHex(),
                badge = if (model.multiAccountMode.value) item.accountTitle else "")
        }))
    }
}

private fun CalendarSheetItem.calendarDetail(actualWeek: Int): JsonObject {
    val timeRange = "${startTime.format(Formatter.TIME_NO_SECONDS)} - ${endTime.format(Formatter.TIME_NO_SECONDS)}"
    if (type == CalendarDayItemType.CUSTOM_THING) {
        val thing = thingDetail
        val place = thing?.location?.takeIf { it.isNotBlank() } ?: location
        return displayItem(
            title = title, color = color.nativeHex(),
            status = if (thing?.saveAsCountDown == true) "倒计时日程" else "自定义事项",
            fields = buildList {
                add(detailField("time", "日程时间", thing?.timeText ?: "${date.format(dateFormatter)}  $timeRange"))
                if (place.isNotBlank()) add(detailField("location", "地点", place))
                thing?.remark?.takeIf { it.isNotBlank() }?.let { add(detailField("remark", "备注", it)) }
            },
        )
    }
    val course = courseDetail
    val extra = buildList {
        if (subtitle.isNotBlank() && course?.extraData.orEmpty().none { it.contains(subtitle) }) add("所属课程：$subtitle")
        addAll(course?.extraData.orEmpty())
    }
    val thisWeek = weekNum == actualWeek
    if (course != null) return courseItem(course, extra, thisWeek, color.nativeHex())
    return displayItem(
        title = title, time = timeRange, location = location, color = color.nativeHex(),
        status = if (thisWeek) "" else "非本周",
        fields = courseFields("第${weekNum}周", "${date.format(dateFormatter)} ${date.dayOfWeek.formatChina()}", extra),
    )
}

private fun courseFields(weeks: String, periods: String, extra: List<String>): List<JsonObject> =
    listOf(detailField("weeks", "上课周次", weeks), detailField("periods", "上课节次", periods)) +
        extra.map { detailField("extra", "附加信息", it) }

private fun courseItem(
    course: WeekCourseView,
    extra: List<String> = course.extraData,
    thisWeek: Boolean = course.thisWeek,
    color: String = course.backgroundColor.nativeHex(),
): JsonObject = displayItem(
    id = course.key, title = course.courseName, subtitle = course.teacher,
    detail = (listOf(course.weekStr, course.courseDayTime) + extra).joinToString("\n"),
    time = course.courseTime, location = course.location, color = color,
    startTime = course.startTime.format(Formatter.TIME_NO_SECONDS),
    endTime = course.endTime.format(Formatter.TIME_NO_SECONDS),
    fields = courseFields(course.weekStr, course.courseDayTime, extra),
    badge = course.accountTitle, status = if (thisWeek) "" else "非本周",
)
