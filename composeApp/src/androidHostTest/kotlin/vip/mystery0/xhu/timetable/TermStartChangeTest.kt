package vip.mystery0.xhu.timetable

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.serialization.json.Json
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.model.TermStartChangeAlert
import vip.mystery0.xhu.timetable.model.TermStartChangeDetector
import vip.mystery0.xhu.timetable.utils.MIN
import vip.mystery0.xhu.timetable.utils.atStartWeek
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.ReadyState
import vip.mystery0.xhu.timetable.widget.WidgetRefreshSignal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TermStartChangeTest {

    private val oldDate = LocalDate(2026, 8, 31)
    private val newDate = LocalDate(2026, 9, 7)
    private val customDate = LocalDate(2026, 9, 14)

    @Test
    fun testUnchangedCloudDate() {
        // 1. 未变化：云端值未变，不做额外逻辑，不触发提醒
        val alert = TermStartChangeDetector.detect(
            oldServerDate = oldDate,
            newServerDate = oldDate,
            customStartDate = customDate,
        )
        assertNull(alert, "云端日期未发生变更时不应提示")
    }

    @Test
    fun testChangedWithoutCustomDate() {
        // 2. 有变化无自定义：云端值变更但无自定义设置，直接更新，不弹窗提示
        val alert = TermStartChangeDetector.detect(
            oldServerDate = oldDate,
            newServerDate = newDate,
            customStartDate = null,
        )
        assertNull(alert, "不存在自定义日期时不应触发确认弹窗")
    }

    @Test
    fun testChangedWithCustomDate() {
        // 3. 有变化有自定义：云端值变更且存在自定义开学日期，触发提醒
        val alert = TermStartChangeDetector.detect(
            oldServerDate = oldDate,
            newServerDate = newDate,
            customStartDate = customDate,
        )
        assertNotNull(alert, "存在自定义日期且云端变更时必须提示")
        assertEquals(oldDate, alert.oldServerDate)
        assertEquals(newDate, alert.newServerDate)
        assertEquals(customDate, alert.customDate)
    }

    @Test
    fun testMissingOldServerDate() {
        // 4. 缺少旧云端值：没有旧的持久化云端值（如首次启动）时不判为变更
        val alert = TermStartChangeDetector.detect(
            oldServerDate = null,
            newServerDate = newDate,
            customStartDate = customDate,
        )
        assertNull(alert, "缺少旧云端持久化值时不应误判为变更")
    }

    @Test
    fun testCustomEqualsNewDateStillAlerts() {
        // 5. 自定义等于新值仍提示：自定义日期恰好等于新的云端日期也遵循触发条件，不擅自豁免
        val alert = TermStartChangeDetector.detect(
            oldServerDate = oldDate,
            newServerDate = newDate,
            customStartDate = newDate,
        )
        assertNotNull(alert, "自定义日期与新云端日期相同时仍应提示变更")
        assertEquals(oldDate, alert.oldServerDate)
        assertEquals(newDate, alert.newServerDate)
        assertEquals(newDate, alert.customDate)
    }

    @Test
    fun testClearCustomContractForSync() {
        // 6. 同步生产契约：使用 clearCustom(LocalDate.MIN) 生成清除标记
        val cleared = Customisable.clearCustom(LocalDate.MIN)
        assertFalse(cleared.custom, "clearCustom 必须将 custom 标记为 false")
        assertEquals(LocalDate.MIN, cleared.data, "clearCustom 数据标识应为 LocalDate.MIN")
        assertEquals("termStartDate", cleared.mapKey("termStartDate"), "clearCustom 映射键必须回退为非 custom 键")

        val customVal = Customisable.custom(customDate)
        assertTrue(customVal.custom, "custom 必须将 custom 标记为 true")
        assertEquals("termStartDate-custom", customVal.mapKey("termStartDate"), "自定义值映射键应为 custom 专属键")
    }

    @Test
    fun testSubsequentRunNoDuplicatePromptAndFutureChangePrompts() {
        // 7. 下一次同值无提示 / 再次变化有提示：
        val savedServerDate = newDate
        val currentCustom = customDate

        val nextRunAlert = TermStartChangeDetector.detect(
            oldServerDate = savedServerDate,
            newServerDate = newDate,
            customStartDate = currentCustom,
            pendingAlert = null,
        )
        assertNull(nextRunAlert, "下一次启动若服务端日期未再次改变，不应重复提示")

        val futureDate = LocalDate(2026, 9, 21)
        val futureAlert = TermStartChangeDetector.detect(
            oldServerDate = savedServerDate,
            newServerDate = futureDate,
            customStartDate = currentCustom,
            pendingAlert = null,
        )
        assertNotNull(futureAlert, "服务端日期再次发生变更时应重新提示")
        assertEquals(savedServerDate, futureAlert.oldServerDate)
        assertEquals(futureDate, futureAlert.newServerDate)
        assertEquals(currentCustom, futureAlert.customDate)
    }

    @Test
    fun testReadyStateNavigationGating() {
        // 8. 生产 ReadyState 导航状态门禁与登录态统一快照回归
        val alert = TermStartChangeAlert(oldDate, newDate, customDate)

        // 加载中无论登录状态如何均不可导航
        val loadingWithAlert = ReadyState(loading = true, isLogin = true, termStartChangeAlert = alert)
        assertFalse(loadingWithAlert.canNavigate, "加载中状态不可导航")
        assertTrue(loadingWithAlert.isLogin, "加载中可承载已知登录态")

        // 完成加载但存在待确认提醒：必须阻断自动导航，即便已登录
        val finishedWithAlert = ReadyState(loading = false, isLogin = true, termStartChangeAlert = alert)
        assertFalse(finishedWithAlert.canNavigate, "存在未确认提醒时必须阻断自动导航")
        assertTrue(finishedWithAlert.isLogin, "待确认提醒状态下保留已登录态")

        // 完成加载且无提醒：已登录场景方可放行
        val readyLoggedIn = ReadyState(loading = false, isLogin = true, termStartChangeAlert = null)
        assertTrue(readyLoggedIn.canNavigate, "无待选提醒且完成时方可导航")
        assertTrue(readyLoggedIn.isLogin, "导航快照准确反映已登录态")

        // 未登录场景：无提醒时放行供 InitScreen 导航至 RouteLogin
        val readyNotLoggedIn = ReadyState(loading = false, isLogin = false, termStartChangeAlert = null)
        assertTrue(readyNotLoggedIn.canNavigate, "未登录且无待办提醒时亦放行导航")
        assertFalse(readyNotLoggedIn.isLogin, "导航快照准确反映未登录态")

        // 验证用户选择后通过原子 copy/update 消除提醒的状态转移，且必须保留已登录态
        val resolved = finishedWithAlert.copy(termStartChangeAlert = null)
        assertTrue(resolved.canNavigate, "消除提醒后原子快照立即允许导航")
        assertTrue(resolved.isLogin, "解除提醒后必须保持已登录态，避免意外回退至登录页")

        // 验证异常出口携带已知登录态
        val failedWithLogin = loadingWithAlert.copy(loading = false, errorMessage = "网络错误")
        assertFalse(failedWithLogin.canNavigate, "存在待选提醒的异常出口不可导航")
        assertTrue(failedWithLogin.isLogin, "异常出口必须保留已知登录态")
    }

    @Test
    fun testPreserveAlertOnSubsequentInitFailureAndRetry() {
        // 9. 初始化后续失败重试保留提醒：
        var pendingAlert = TermStartChangeDetector.detect(
            oldServerDate = oldDate,
            newServerDate = newDate,
            customStartDate = customDate,
            pendingAlert = null,
        )
        assertNotNull(pendingAlert)
        val retryOldServerDate = newDate
        val retryNewServerDate = newDate

        val retriedAlert = TermStartChangeDetector.detect(
            oldServerDate = retryOldServerDate,
            newServerDate = retryNewServerDate,
            customStartDate = customDate,
            pendingAlert = pendingAlert,
        )
        assertNotNull(retriedAlert, "重试时不应因提前保存新值而无声丢弃尚未呈现的选择")
        assertEquals(oldDate, retriedAlert.oldServerDate, "原始旧云端日期必须被保留")
        assertEquals(newDate, retriedAlert.newServerDate)
        assertEquals(customDate, retriedAlert.customDate)

        val updatedNewDate = LocalDate(2026, 9, 21)
        val updatedRetriedAlert = TermStartChangeDetector.detect(
            oldServerDate = retryOldServerDate,
            newServerDate = updatedNewDate,
            customStartDate = customDate,
            pendingAlert = retriedAlert,
        )
        assertNotNull(updatedRetriedAlert)
        assertEquals(oldDate, updatedRetriedAlert.oldServerDate)
        assertEquals(updatedNewDate, updatedRetriedAlert.newServerDate, "配置更新使用最新数据")
    }

    @Test
    fun testDateFormattingWithWeek() {
        fun formatTermDate(date: LocalDate): String =
            "${date.format(dateFormatter)}（${date.dayOfWeek.formatWeekString()}）"

        val formatted = formatTermDate(LocalDate(2026, 9, 7))
        assertEquals("2026年09月07日（星期一）", formatted)
    }

    @Test
    fun testAlertDateDifferenceAndDescription() {
        // 10. 测试新旧云端与自定义开学时间的差值及自然语言描述
        val alertPostpone7Days = TermStartChangeAlert(
            oldServerDate = LocalDate(2026, 8, 31),
            newServerDate = LocalDate(2026, 9, 7),
            customDate = LocalDate(2026, 9, 14),
        )
        assertEquals(7L, alertPostpone7Days.serverDiffDays)
        assertEquals("推迟 7天（1周）", alertPostpone7Days.serverChangeDescription)
        assertEquals(7L, alertPostpone7Days.customDiffDays)
        assertEquals("比新云端推迟 7天（1周）", alertPostpone7Days.customDiffDescription)

        val alertAdvance3Days = TermStartChangeAlert(
            oldServerDate = LocalDate(2026, 9, 7),
            newServerDate = LocalDate(2026, 9, 4),
            customDate = LocalDate(2026, 9, 4),
        )
        assertEquals(-3L, alertAdvance3Days.serverDiffDays)
        assertEquals("提前 3天", alertAdvance3Days.serverChangeDescription)
        assertEquals(0L, alertAdvance3Days.customDiffDays)
        assertEquals("与新云端日期一致", alertAdvance3Days.customDiffDescription)

        val alertPostpone10Days = TermStartChangeAlert(
            oldServerDate = LocalDate(2026, 8, 31),
            newServerDate = LocalDate(2026, 9, 10),
            customDate = LocalDate(2026, 9, 7),
        )
        assertEquals(10L, alertPostpone10Days.serverDiffDays)
        assertEquals("推迟 10天（1周3天）", alertPostpone10Days.serverChangeDescription)
        assertEquals(-3L, alertPostpone10Days.customDiffDays)
        assertEquals("比新云端提前 3天", alertPostpone10Days.customDiffDescription)
    }

    @Test
    fun testCloudRollbackEliminatesPendingAlert() {
        // 11. 测试云端变更撤回时自动清除 pendingAlert
        val pending = TermStartChangeAlert(
            oldServerDate = oldDate,
            newServerDate = newDate,
            customDate = customDate,
        )
        val rollbackAlert = TermStartChangeDetector.detect(
            oldServerDate = newDate,
            newServerDate = oldDate, // 服务端恢复为旧日期
            customStartDate = customDate,
            pendingAlert = pending,
        )
        assertNull(rollbackAlert, "服务端日期恢复到原始旧云端日期时，变更消除，不应再弹窗提醒")
    }

    @Test
    fun testAlertSerializationAndDeserialization() {
        // 12. 测试 TermStartChangeAlert 的 JSON 序列化和反序列化
        val alert = TermStartChangeAlert(
            oldServerDate = LocalDate(2026, 8, 31),
            newServerDate = LocalDate(2026, 9, 7),
            customDate = LocalDate(2026, 9, 14),
        )
        val json = Json { ignoreUnknownKeys = true }
        val encoded = json.encodeToString(alert)
        val decoded = json.decodeFromString<TermStartChangeAlert>(encoded)
        assertEquals(alert, decoded)
        assertEquals(alert.serverDiffDays, decoded.serverDiffDays)
        assertEquals(alert.serverChangeDescription, decoded.serverChangeDescription)
        assertEquals(alert.customDiffDescription, decoded.customDiffDescription)
    }

    @Test
    fun testWidgetRefreshSignalContract() {
        // 13. 测试开学时间同步触发的 WidgetRefreshSignal 行为
        val before = WidgetRefreshSignal.changes.value.revision
        WidgetRefreshSignal.request(invalidate = true)
        val after = WidgetRefreshSignal.changes.value
        assertEquals(before + 1, after.revision)
        assertEquals(after.revision, after.invalidatedAt, "带 invalidate 的请求必须更新 invalidatedAt 为当前 revision")
    }

    @Test
    fun testTermStartDateAlwaysNormalizesToMonday() {
        // 14. 同一周周一至周日都应归一化到该周周一（2026-09-07 为周一）
        val expectedMonday = LocalDate(2026, 9, 7)
        for (day in 7..13) {
            val normalized = LocalDate(2026, 9, day).atStartWeek()
            assertEquals(expectedMonday, normalized, "2026-09-$day 应归一化到 $expectedMonday")
            assertEquals(DayOfWeek.MONDAY, normalized.dayOfWeek)
        }

        // 跨月边界：2026-09-02（周三）所在周的周一为 2026-08-31
        assertEquals(LocalDate(2026, 8, 31), LocalDate(2026, 9, 2).atStartWeek())

        // 跨年边界：2026-01-01（周四）所在周的周一为 2025-12-29
        assertEquals(LocalDate(2025, 12, 29), LocalDate(2026, 1, 1).atStartWeek())
    }
}
