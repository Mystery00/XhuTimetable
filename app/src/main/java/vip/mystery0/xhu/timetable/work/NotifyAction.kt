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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.model.TodayCourseView
import vip.mystery0.xhu.timetable.repository.AggregationRepo
import vip.mystery0.xhu.timetable.repository.CourseColorRepo
import vip.mystery0.xhu.timetable.repository.ExamRepo
import vip.mystery0.xhu.timetable.repository.WidgetRepo
import vip.mystery0.xhu.timetable.ui.activity.ExamActivity
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_TOMORROW
import vip.mystery0.xhu.timetable.ui.notification.NotificationId
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.viewmodel.Exam
import java.time.LocalDate

class NotifyAction(
    private val appContext: Context,
) : KoinComponent {
    companion object {
        private const val NOTIFICATION_TAG = "NotifyAction"
    }

    private val notificationManager: NotificationManager by inject()

    private val colorAccent = android.graphics.Color.parseColor("#2196F3")

    suspend fun checkNotifyExam() {
        val examList = ExamRepo.getTomorrowExamList()
        if (examList.isEmpty()) {
            return
        }
        notifyExam(examList)
    }

    private fun notifyExam(examList: List<Exam>) {
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
            courseItem.append(" 时间：${it.time} 地点：${it.location}")
            style.addLine(courseItem)
        }
        style.addLine("具体详情请点击查看")
        builder.setStyle(style)
        builder.build().notify(NotificationId.NOTIFY_TOMORROW_TEST)
    }

    suspend fun checkNotifyCourse() {
        val view = AggregationRepo.fetchAggregationMainPage(
            forceLoadFromCloud = false,
            forceLoadFromLocal = false,
            showCustomCourse = true,
            showCustomThing = false,
        )
        val showDate = LocalDate.now().plusDays(1)
        val showDay = showDate.dayOfWeek
        val showCurrentWeek = WidgetRepo.calculateWeek(showDate)
        //过滤出明日的课程
        val showList = view.todayViewList
            .filter { it.weekList.contains(showCurrentWeek) && it.day == showDay }
            .sortedBy { it.startDayTime }
        if (showList.isEmpty()) {
            return
        }
        //获取自定义颜色列表
        val colorMap = CourseColorRepo.getRawCourseColorList()
        //合并相同课程
        val resultList = ArrayList<TodayCourseView>(showList.size)
        //计算key与设置颜色
        showList.forEach {
            it.backgroundColor = colorMap[it.courseName] ?: ColorPool.hash(it.courseName)
            it.generateKey()
        }
        showList.groupBy { it.user.studentId }
            .forEach { (_, list) ->
                var last = list.first()
                var lastKey = last.key
                list.forEachIndexed { index, todayCourseView ->
                    if (index == 0) {
                        resultList.add(todayCourseView)
                        return@forEachIndexed
                    }
                    val thisKey = todayCourseView.key
                    if (lastKey == thisKey) {
                        if (last.endDayTime == todayCourseView.startDayTime - 1) {
                            //合并两个课程
                            last.endDayTime = todayCourseView.endDayTime
                        } else {
                            //两个课程相同但是节次不连续，不合并
                            resultList.add(todayCourseView)
                            last = todayCourseView
                            lastKey = thisKey
                        }
                    } else {
                        resultList.add(todayCourseView)
                        last = todayCourseView
                        lastKey = thisKey
                    }
                }
                if (resultList.last() != last) {
                    resultList.add(last)
                }
            }
        //最后按照开始节次排序
        notifyCourse(resultList.sortedBy { it.startDayTime }
            .map {
                it.updateTime()
                it
            })
    }

    private fun notifyCourse(courseList: List<TodayCourseView>) {
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
                    appContext.packageManager.getLaunchIntentForPackage(vip.mystery0.xhu.timetable.packageName),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )
        val style = NotificationCompat.InboxStyle()
            .setBigContentTitle(title)
        courseList.forEach {
            val courseItem = SpannableStringBuilder()
            courseItem.append(it.courseName)
            courseItem.setSpan(
                ForegroundColorSpan(toColor(it.backgroundColor).toArgb()),
                0,
                courseItem.length,
                0
            )
            courseItem.append("  ")
            courseItem.append(it.startTime.format(Formatter.TIME_NO_SECONDS))
            courseItem.append(" - ")
            courseItem.append(it.endTime.format(Formatter.TIME_NO_SECONDS))
            courseItem.append(" at ${it.location}")
            style.addLine(courseItem)
        }
        style.addLine("具体详情请点击查看")
        builder.setStyle(style)
        builder.build().notify(NotificationId.NOTIFY_TOMORROW_COURSE)
    }

    private val notificationBuilder: NotificationCompat.Builder
        get() = NotificationCompat.Builder(appContext, NOTIFICATION_CHANNEL_ID_TOMORROW)
            .setDefaults(Notification.DEFAULT_ALL)
            .setSmallIcon(R.drawable.ic_stat_init)
            .setColor(colorAccent)
            .setAutoCancel(true)

    private fun Notification.notify(id: NotificationId) {
        notificationManager.notify(NOTIFICATION_TAG, id.id, this)
    }

    private fun toColor(color: Color): android.graphics.Color =
        android.graphics.Color.valueOf(color.red, color.green, color.blue)
}