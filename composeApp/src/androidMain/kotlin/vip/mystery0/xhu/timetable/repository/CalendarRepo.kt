package vip.mystery0.xhu.timetable.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.graphics.Color
import android.net.Uri
import android.provider.CalendarContract
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.api.CalendarApi
import vip.mystery0.xhu.timetable.base.BaseDataRepo
import vip.mystery0.xhu.timetable.base.packageName
import vip.mystery0.xhu.timetable.config.store.User
import vip.mystery0.xhu.timetable.config.store.UserStore.withAutoLoginOnce
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.model.CalendarAccount
import vip.mystery0.xhu.timetable.model.CalendarAttender
import vip.mystery0.xhu.timetable.model.CalendarEvent
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import java.util.TimeZone

object CalendarRepo : BaseDataRepo {
    private val calendarApi: CalendarApi by inject()
    private const val CALENDARS_ACCOUNT_TYPE = CalendarContract.ACCOUNT_TYPE_LOCAL

    private val contentResolver: ContentResolver by lazy {
        context.contentResolver
    }

    suspend fun getEventList(
        user: User,
        year: Int,
        term: Int,
        includeCustomCourse: Boolean,
        includeCustomThing: Boolean,
    ): List<CalendarEvent> {
        checkForceLoadFromCloud(true)

        val eventList = user.withAutoLoginOnce {
            calendarApi.exportCalendarEventList(
                it,
                year,
                term,
                includeCustomCourse,
                includeCustomThing,
            )
        }
        return withContext(Dispatchers.Default) {
            eventList.map { response ->
                val event = CalendarEvent(
                    response.title,
                    response.startTime,
                    response.endTime,
                    response.location,
                    response.description,
                    response.allDay,
                )
                event.attenderList.addAll(response.attenders.map { CalendarAttender(it) })
                event
            }
        }
    }

    /**
     * 查询所有的日历账号
     */
    fun getAllCalendarAccount(): List<CalendarAccount> {
        val selection =
            "${CalendarContract.Calendars.OWNER_ACCOUNT} = ? and ${CalendarContract.Calendars.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf(packageName(), CALENDARS_ACCOUNT_TYPE)
        val userCursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )
        val result = ArrayList<CalendarAccount>()
        userCursor.use { cursor ->
            if (cursor == null) {
                return@use
            }
            while (cursor.moveToNext()) {
                val accountNameIndex =
                    cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
                val displayNameIndex =
                    cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                val colorIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)
                val accountName = cursor.getString(accountNameIndex)
                val displayName = cursor.getString(displayNameIndex)
                val accountId = cursor.getLong(idIndex)
                val color = Color.valueOf(cursor.getInt(colorIndex))
                val eventNum = getCalendarAccountEventNum(accountId)
                result.add(
                    CalendarAccount(
                        accountName,
                        "",
                        "",
                        accountId,
                        eventNum,
                        androidx.compose.ui.graphics.Color(
                            color.red(),
                            color.green(),
                            color.blue()
                        ),
                    ).parseStudent(displayName, accountName)
                )
            }
        }
        return result
    }

    /**
     * 根据账号名称查询账号id
     */
    fun getCalendarIdByAccountName(accountName: String): Long? {
        val selection =
            "${CalendarContract.Calendars.ACCOUNT_NAME} = ? and ${CalendarContract.Calendars.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf(accountName, CALENDARS_ACCOUNT_TYPE)
        val userCursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )
        return userCursor.use { cursor ->
            if (cursor == null) { //查询返回空值
                return null
            }
            val count: Int = cursor.count
            if (count > 0) { //存在现有账户，取第一个账户的id返回
                cursor.moveToFirst()
                val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
                cursor.getLong(idIndex)
            } else {
                null
            }
        }
    }

    /**
     * 根据账号id加载账号数据
     */
    private fun loadCalendarAccount(accountId: Long): CalendarAccount? {
        val selection =
            "${CalendarContract.Calendars._ID} = ? and ${CalendarContract.Calendars.ACCOUNT_TYPE} = ?"
        val selectionArgs = arrayOf(accountId.toString(), CALENDARS_ACCOUNT_TYPE)
        val userCursor = contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )
        userCursor.use { cursor ->
            if (cursor == null) {
                return null
            }
            val count: Int = cursor.count
            if (count == 0) {
                return null
            }
            cursor.moveToFirst()
            val accountNameIndex =
                cursor.getColumnIndex(CalendarContract.Calendars.ACCOUNT_NAME)
            val displayNameIndex =
                cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
            val idIndex = cursor.getColumnIndex(CalendarContract.Calendars._ID)
            val colorIndex = cursor.getColumnIndex(CalendarContract.Calendars.CALENDAR_COLOR)
            val accountName = cursor.getString(accountNameIndex)
            val displayName = cursor.getString(displayNameIndex)
            val trueAccountId = cursor.getLong(idIndex)
            val color = Color.valueOf(cursor.getInt(colorIndex))
            val eventNum = getCalendarAccountEventNum(trueAccountId)
            return CalendarAccount(
                accountName,
                "",
                "",
                trueAccountId,
                eventNum,
                androidx.compose.ui.graphics.Color(color.red(), color.green(), color.blue()),
            ).parseStudent(displayName, accountName)
        }
    }

    private fun getCalendarAccountEventNum(calendarId: Long): Int {
        val selection = "${CalendarContract.Events.CALENDAR_ID} = ?"
        val selectionArgs = arrayOf(calendarId.toString())
        val userCursor = contentResolver.query(
            CalendarContract.Events.CONTENT_URI,
            null,
            selection,
            selectionArgs,
            null
        )
        userCursor.use { cursor ->
            return cursor?.count ?: 0
        }
    }

    /**
     * 删除日历账号
     */
    fun deleteCalendarAccount(calendarId: Long) {
        val deleteUri =
            ContentUris.withAppendedId(CalendarContract.Calendars.CONTENT_URI, calendarId)
        contentResolver.delete(deleteUri, null, null)
    }

    /**
     * 添加日历账户，账户创建成功则返回账户id
     */
    private fun addCalendarAccount(account: CalendarAccount): Boolean {
        val randomColor = ColorPool.random
        val color = Color.valueOf(randomColor.red, randomColor.green, randomColor.blue)
        val accountName = account.generateAccountName()
        val displayName = account.displayName
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.NAME, displayName)
            put(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDARS_ACCOUNT_TYPE)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, displayName)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.CALENDAR_COLOR, color.toArgb())
            put(
                CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,
                CalendarContract.Calendars.CAL_ACCESS_OWNER
            )
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, packageName())
        }
        val uri = CalendarContract.Calendars.CONTENT_URI.asSyncAdapter(accountName)
        val result = contentResolver.insert(uri, values) ?: return false
        account.accountId = ContentUris.parseId(result)
        return true
    }

    /**
     * 删除所有事项
     */
    fun deleteAllEvent(calendarId: Long) {
        val deleteUri =
            ContentUris.withAppendedId(
                CalendarContract.Calendars.CONTENT_URI,
                calendarId
            )
        contentResolver.delete(deleteUri, null, null)
    }

    fun addEvent(account: CalendarAccount, event: CalendarEvent): Boolean {
        val checkAddResult = checkAndAddCalendarAccount(account)
        if (!checkAddResult) {
            return false
        }
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, account.accountId)
            put(CalendarContract.Events.TITLE, event.title)
            val startTime = event.startTime.toEpochMilliseconds()
            put(CalendarContract.Events.DTSTART, startTime)
            val endTime = event.endTime.toEpochMilliseconds()
            put(CalendarContract.Events.DTEND, endTime)
            put(CalendarContract.Events.EVENT_LOCATION, event.location)
            put(CalendarContract.Events.DESCRIPTION, event.description)
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.ALL_DAY, if (event.allDay) 1 else 0)
        }
        val result = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            ?: throw RuntimeException("课程（${event.title}）转换失败")
        val eventId = ContentUris.parseId(result)
        event.attenderList.forEach { addAttendees(eventId, it) }
        event.reminder.forEach { addReminder(eventId, it) }
        return true
    }

    private fun addAttendees(eventId: Long, attendee: CalendarAttender) {
        val values = ContentValues().apply {
            put(CalendarContract.Attendees.EVENT_ID, eventId)
            put(CalendarContract.Attendees.ATTENDEE_NAME, attendee.name)
        }
        contentResolver.insert(CalendarContract.Attendees.CONTENT_URI, values)
    }

    private fun addReminder(eventId: Long, minutes: Int) {
        val values = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, minutes)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
    }

    /**
     * 检查是否已经添加了日历账户，如果没有添加先添加一个日历账户再查询
     */
    private fun checkAndAddCalendarAccount(account: CalendarAccount): Boolean {
        val checkResult = checkCalendarAccount(account)
        if (checkResult) {
            return true
        }
        return addCalendarAccount(account)
    }

    /**
     * 检查是否存在现有账户
     */
    private fun checkCalendarAccount(account: CalendarAccount): Boolean {
        if (account.accountId != -1L) {
            //存在accountId，通过accountId检查
            val existAccount = loadCalendarAccount(account.accountId)
            if (existAccount == null) {
                //清除accountId
                account.accountId = -1L
            }
        }
        //不知道accountId，通过accountName检查
        val id = getCalendarIdByAccountName(account.generateAccountName()) ?: return false
        account.accountId = id
        return true
    }

    private fun Uri.asSyncAdapter(accountName: String): Uri {
        return buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, accountName)
            .appendQueryParameter(
                CalendarContract.Calendars.ACCOUNT_TYPE,
                CALENDARS_ACCOUNT_TYPE
            )
            .build()
    }
}