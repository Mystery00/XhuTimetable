package vip.mystery0.xhu.timetable.model

import android.util.Log
import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import java.time.Instant

private val nameRegex = Regex("^(.+?)\\(\\d{4}-\\d{4}-\\d\\)@$appName$")

data class CalendarAccount(
    var accountName: String,
    var studentId: String,
    var studentName: String,
    var accountId: Long,
    var eventNum: Int,
    var color: Color,
) {
    companion object {
        fun byAccount(studentId: String, studentName: String): CalendarAccount =
            CalendarAccount("", studentId, studentName, -1L, 0, ColorPool.random)
    }

    val displayName: String
        get() {
            val nowYear = GlobalConfigStore.nowYear
            val nowTerm = GlobalConfigStore.nowTerm
            return "${studentName}(${nowYear}-${nowYear + 1}-${nowTerm})@$appName"
        }

    fun generateAccountName(): String {
        val nowYear = GlobalConfigStore.nowYear
        val nowTerm = GlobalConfigStore.nowTerm
        val name = "${studentId}(${nowYear}-${nowYear + 1}-${nowTerm})@$appName"
        accountName = name
        return name
    }

    fun parseStudent(displayName: String, accountName: String): CalendarAccount {
        kotlin.runCatching {
            studentName = nameRegex.find(displayName)!!.groupValues[1]
        }.exceptionOrNull()?.let {
            Log.w("TAG", "parse displayName failed: $displayName")
        }
        kotlin.runCatching {
            studentId = nameRegex.find(accountName)!!.groupValues[1]
        }.exceptionOrNull()?.let {
            Log.w("TAG", "parse accountName failed: $accountName")
        }
        return this
    }
}

data class CalendarAttender(
    val name: String,
)

data class CalendarEvent(
    val title: String,
    val startTime: Instant,
    val endTime: Instant,
    val location: String,
    val description: String,
    val allDay: Boolean,
) {
    val attenderList = ArrayList<CalendarAttender>()
    val reminder = ArrayList<Int>()
}