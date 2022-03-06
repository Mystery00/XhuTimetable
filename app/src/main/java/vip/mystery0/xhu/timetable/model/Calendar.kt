package vip.mystery0.xhu.timetable.model

import android.util.Log
import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.config.GlobalConfig
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
        val EMPTY = CalendarAccount("", "", "", 0L, 0, Color.White)

        fun byAccount(studentId: String, studentName: String): CalendarAccount =
            CalendarAccount("", studentId, studentName, -1L, 0, ColorPool.random)
    }

    val displayName: String
        get() = "${studentName}(${GlobalConfig.currentYear}-${GlobalConfig.currentTerm})@$appName"

    fun generateAccountName(): String {
        val name = "${studentId}(${GlobalConfig.currentYear}-${GlobalConfig.currentTerm})@$appName"
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
    var name: String,
)

data class CalendarEvent(
    var title: String,
    var startTime: Instant,
    var endTime: Instant,
    var location: String,
    var description: String,
    var allDay: Boolean,
) {
    var attenderList = ArrayList<CalendarAttender>()
    var reminder = ArrayList<Int>()
}