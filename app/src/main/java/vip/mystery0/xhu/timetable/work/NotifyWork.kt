package vip.mystery0.xhu.timetable.work

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import androidx.compose.ui.graphics.Color
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.config.store.getConfigStore
import vip.mystery0.xhu.timetable.model.response.ExamItem
import vip.mystery0.xhu.timetable.packageName
import vip.mystery0.xhu.timetable.setTrigger
import vip.mystery0.xhu.timetable.ui.activity.ExamActivity
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_TOMORROW
import vip.mystery0.xhu.timetable.ui.notification.NotificationId
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NotifyWork(private val appContext: Context, workerParams: WorkerParameters) :
    CoroutineWorker(appContext, workerParams), KoinComponent {
    companion object {
        private const val TAG = "NotifyWork"
        private const val NOTIFICATION_TAG = "NotifyWork"
    }

    private val notificationManager: NotificationManager by inject()
    private val workManager: WorkManager by inject()
    private val colorAccent = android.graphics.Color.parseColor("#2196F3")
    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    override suspend fun doWork(): Result {
//        setConfig { notifyWorkLastExecuteTime = Instant.now() }
//        try {
//            val currentYear = getConfig { currentYear }
//            val currentTerm = getConfig { currentTerm }
//            val mainUser = UserStore.getMainUser() ?: return complete()
//            val tomorrow = LocalDate.now().plusDays(1)
//            val currentWeek = runOnCpu {
//                //计算当前周
//                val startDate =
//                    LocalDateTime.ofInstant(getConfig { termStartTime }, chinaZone).toLocalDate()
//                val days =
//                    Duration.between(startDate.atStartOfDay(), LocalDate.now().atStartOfDay())
//                        .toDays()
//                var week = ((days / 7) + 1).toInt()
//                if (days < 0 && week > 0) {
//                    week = 0
//                }
//                week
//            }
//            val tomorrowWeek =
//                if (tomorrow.dayOfWeek == DayOfWeek.SUNDAY) currentWeek + 1 else currentWeek
//            val examResponse = getExamList(mainUser)
//            val examList = runOnCpu {
//                examResponse.list.filter {
//                    tomorrow == LocalDateTime.ofInstant(
//                        Instant.ofEpochMilli(it.startTime),
//                        chinaZone
//                    )
//                        .toLocalDate()
//                }
//            }
//            notifyTest(examList.sortedBy { it.startTime })
//            if (currentWeek > 0) {
//                //获取自定义颜色列表
//                val colorMap = getRawCourseColorList()
//                val list = courseLocalRepo.getCourseList(mainUser, currentYear, currentTerm)
//                    .filter {
//                        it.week.contains(tomorrowWeek) && it.day == tomorrow.dayOfWeek.value
//                    }
//                val tomorrowCourseList = list.map {
//                    Course(
//                        it.name,
//                        it.location,
//                        "it.time.formatTime()",
//                        it.time.first(),
//                        color = colorMap[it.name] ?: ColorPool.hash(it.name),
//                    )
//                }
//                notifyCourse(tomorrowCourseList.sortedBy { it.firstTime })
//            }
//        } catch (e: Exception) {
//            Log.w(TAG, "doWork failed", e)
//        }
        return complete()
    }

    private suspend fun complete(): Result {
        getConfigStore { notifyTime }?.let {
            setTrigger(workManager, it.atDate(LocalDate.now().plusDays(1)))
        }
        return Result.success()
    }

    private fun notifyTest(examList: List<ExamItem>) {
        if (examList.isEmpty()) {
            return
        }
        val title = "您明天有${examList.size}门考试，记得带上学生证和文具哦~"
        val builder = notificationBuilder
            .setContentTitle(title)
            .setContentIntent(
                PendingIntent.getActivity(
                    appContext,
                    0,
                    Intent(appContext, ExamActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
        examList.forEachIndexed { index, it ->
            val color = ColorPool.safeGet(index)
            val courseItem = SpannableStringBuilder()
            courseItem.append(it.courseName)
            courseItem.setSpan(
                ForegroundColorSpan(toColor(color).toArgb()),
                0,
                courseItem.length,
                0
            )
            val startTime = Instant.ofEpochMilli(it.startTime).asLocalDateTime()
            val endTime = Instant.ofEpochMilli(it.endTime).asLocalDateTime()
            val time =
                "${timeFormatter.format(startTime)} - ${timeFormatter.format(endTime)}"
            courseItem.append(" 时间：$time 地点：${it.location}")
            style.addLine(courseItem)
        }
        style.addLine("具体详情请点击查看")
        builder.setStyle(style)
        builder.build().notify(NotificationId.NOTIFY_TOMORROW_TEST)
    }

    private fun notifyCourse(courseList: List<Course>) {
        if (courseList.isEmpty()) {
            return
        }
        val title = "您明天有${courseList.size}节课要上哦~"
        val builder = notificationBuilder
            .setContentTitle(title)
            .setContentIntent(
                PendingIntent.getActivity(
                    appContext,
                    0,
                    appContext.packageManager.getLaunchIntentForPackage(packageName),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
        courseList.forEach {
            val courseItem = SpannableStringBuilder()
            courseItem.append(it.courseName)
            courseItem.setSpan(
                ForegroundColorSpan(toColor(it.color).toArgb()),
                0,
                courseItem.length,
                0
            )
            courseItem.append("  ${it.time} at ${it.location}")
            style.addLine(courseItem)
        }
        style.addLine("具体详情请点击查看")
        builder.setStyle(style)
        builder.build().notify(NotificationId.NOTIFY_TOMORROW_COURSE)
    }

    private val notificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_TOMORROW)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.mipmap.ic_stat_init)
            .setColor(colorAccent)
            .setAutoCancel(true)

    private fun Notification.notify(id: NotificationId) {
        notificationManager.notify(NOTIFICATION_TAG, id.id, this)
    }

    private fun toColor(color: Color): android.graphics.Color =
        android.graphics.Color.valueOf(color.red, color.green, color.blue)

    private data class Course(
        val courseName: String,
        val location: String,
        val time: String,
        val firstTime: Int,
        val color: Color,
    )
}
