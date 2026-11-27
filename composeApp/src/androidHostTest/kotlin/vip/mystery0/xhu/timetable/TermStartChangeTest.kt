package vip.mystery0.xhu.timetable

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import vip.mystery0.xhu.timetable.model.TermStartChangeAlert
import vip.mystery0.xhu.timetable.model.TermStartChangeDetector
import vip.mystery0.xhu.timetable.utils.atStartWeek
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class TermStartChangeTest {

    private val oldDate = LocalDate(2026, 8, 31)
    private val newDate = LocalDate(2026, 9, 7)
    private val customDate = LocalDate(2026, 9, 14)

    @Test
    fun detectSkipsWhenUnchangedMissingOldOrNoCustomDate() {
        assertNull(
            TermStartChangeDetector.detect(oldDate, oldDate, customDate),
            "云端日期未变更时不应提示",
        )
        assertNull(
            TermStartChangeDetector.detect(null, newDate, customDate),
            "缺少旧云端持久化值时不应误判为变更",
        )
        assertNull(
            TermStartChangeDetector.detect(oldDate, newDate, null),
            "未设置自定义日期时不应弹窗提示",
        )
    }

    @Test
    fun detectTriggersPreservesUpdatesAndClearsOnRollback() {
        // 云端变更且存在自定义设置（哪怕恰好与新云端日期相同）都应提示
        val sameAsNew = TermStartChangeDetector.detect(oldDate, newDate, newDate)
        assertNotNull(sameAsNew)
        assertEquals(oldDate, sameAsNew.oldServerDate)
        assertEquals(newDate, sameAsNew.newServerDate)

        val alert = TermStartChangeDetector.detect(oldDate, newDate, customDate)
        assertNotNull(alert)

        // 重试时 oldServerDate 已被更新为 newDate，但尚未处理的 pendingAlert 必须保留原始 oldDate
        val retried = TermStartChangeDetector.detect(
            oldServerDate = newDate,
            newServerDate = newDate,
            customStartDate = customDate,
            pendingAlert = alert,
        )
        assertNotNull(retried)
        assertEquals(oldDate, retried.oldServerDate)

        // 服务端再次更新日期时刷新 newServerDate 并保留原始 oldDate
        val furtherDate = LocalDate(2026, 9, 21)
        val updated = TermStartChangeDetector.detect(
            oldServerDate = newDate,
            newServerDate = furtherDate,
            customStartDate = customDate,
            pendingAlert = retried,
        )
        assertNotNull(updated)
        assertEquals(oldDate, updated.oldServerDate)
        assertEquals(furtherDate, updated.newServerDate)

        // 服务端撤回回到原始旧日期时，变更消除
        assertNull(
            TermStartChangeDetector.detect(
                oldServerDate = furtherDate,
                newServerDate = oldDate,
                customStartDate = customDate,
                pendingAlert = updated,
            ),
            "服务端撤回恢复旧日期时应消除待办提醒",
        )
    }

    @Test
    fun formatDaysDiffCoversWeeksRemainderAndZero() {
        val postpone7 = TermStartChangeAlert(LocalDate(2026, 8, 31), LocalDate(2026, 9, 7), LocalDate(2026, 9, 14))
        assertEquals("推迟 7天（1周）", postpone7.serverChangeDescription)
        assertEquals("比新云端推迟 7天（1周）", postpone7.customDiffDescription)

        val advance3 = TermStartChangeAlert(LocalDate(2026, 9, 7), LocalDate(2026, 9, 4), LocalDate(2026, 9, 4))
        assertEquals("提前 3天", advance3.serverChangeDescription)
        assertEquals("与新云端日期一致", advance3.customDiffDescription)

        val postpone10 = TermStartChangeAlert(LocalDate(2026, 8, 31), LocalDate(2026, 9, 10), LocalDate(2026, 9, 7))
        assertEquals("推迟 10天（1周3天）", postpone10.serverChangeDescription)
        assertEquals("比新云端提前 3天", postpone10.customDiffDescription)
    }

    @Test
    fun termStartDateAlwaysNormalizesToMonday() {
        val expectedMonday = LocalDate(2026, 9, 7)
        for (day in 7..13) {
            val normalized = LocalDate(2026, 9, day).atStartWeek()
            assertEquals(expectedMonday, normalized)
            assertEquals(DayOfWeek.MONDAY, normalized.dayOfWeek)
        }
        assertEquals(LocalDate(2026, 8, 31), LocalDate(2026, 9, 2).atStartWeek())
        assertEquals(LocalDate(2025, 12, 29), LocalDate(2026, 1, 1).atStartWeek())
    }
}
