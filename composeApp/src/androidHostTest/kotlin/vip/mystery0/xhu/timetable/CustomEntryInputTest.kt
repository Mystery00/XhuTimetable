package vip.mystery0.xhu.timetable

import kotlinx.datetime.LocalTime
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.parseCourseWeeks
import vip.mystery0.xhu.timetable.model.validateCustomCourseInput
import vip.mystery0.xhu.timetable.model.normalizeCustomThingInput
import vip.mystery0.xhu.timetable.viewmodel.suggestClassroomPeriods
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CustomEntryInputTest {
    @Test
    fun courseWeeksExpandRangesAndDeduplicate() {
        assertEquals(listOf(1, 2, 3, 5, 20), parseCourseWeeks("1-3，3, 5,20"))
    }

    @Test
    fun courseWeeksRejectEmptyOutOfRangeAndReversedRanges() {
        listOf("", "0", "21", "3-1", "1,a", "1-1000000000").forEach { value ->
            assertFailsWith<IllegalArgumentException> { parseCourseWeeks(value) }
        }
    }

    @Test
    fun courseRejectsReversedPeriodsBeforeSaving() {
        val request = CustomCourseRequest("课程", listOf(1), 1, 4, 2, "", "", emptyList(), 2026, 1)
        assertFailsWith<IllegalArgumentException> { validateCustomCourseInput(request) }
    }

    @Test
    fun ordinaryThingRejectsReversedTimesButCountdownEndsOneDayLater() {
        val request = CustomThingRequest("事项", "", false, 100_000L, 50_000L, "", "#FFFFFF", "")
        assertFailsWith<IllegalArgumentException> { normalizeCustomThingInput(request, false) }
        assertEquals(86_500_000L, normalizeCustomThingInput(request, true).endTime)
        assertEquals(50_000L, request.endTime)
    }

    @Test
    fun classroomDefaultsMoveToNextPeriodsAtBoundaries() {
        assertEquals(listOf(1, 2), suggestClassroomPeriods(LocalTime(10, 4)))
        assertEquals(listOf(3, 4), suggestClassroomPeriods(LocalTime(10, 5)))
        assertEquals(listOf(5, 6), suggestClassroomPeriods(LocalTime(12, 0)))
        assertEquals(listOf(7, 8), suggestClassroomPeriods(LocalTime(15, 35)))
        assertEquals(listOf(9, 10), suggestClassroomPeriods(LocalTime(17, 30)))
    }
}
