package vip.mystery0.xhu.timetable.ui.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color

const val NOTIFICATION_CHANNEL_ID_DEFAULT = "XhuTimetable-Default"
private const val NOTIFICATION_CHANNEL_NAME_DEFAULT = "默认"

const val NOTIFICATION_CHANNEL_ID_DOWNLOAD = "XhuTimetable-Download"
private const val NOTIFICATION_CHANNEL_NAME_DOWNLOAD = "下载进度通知"
private const val NOTIFICATION_CHANNEL_DESCRIPTION_DOWNLOAD = "用于版本更新"

const val NOTIFICATION_CHANNEL_ID_TOMORROW = "XhuTimetable-Tomorrow"
private const val NOTIFICATION_CHANNEL_NAME_TOMORROW = "课程提醒"
private const val NOTIFICATION_CHANNEL_DESCRIPTION_TOMORROW = "每日提醒课程、考试、自定义事项等"


enum class NotificationId(val id: Int) {
    CHECK_UPDATE(1001),
    DOWNLOAD(1002),
}

fun initChannelID(applicationContext: Context) {
    val notificationManager =
        applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.createNotificationChannel(createDefaultChannel())
    notificationManager.createNotificationChannel(
        createChannel(
            NOTIFICATION_CHANNEL_ID_DOWNLOAD,
            NOTIFICATION_CHANNEL_NAME_DOWNLOAD,
            NOTIFICATION_CHANNEL_DESCRIPTION_DOWNLOAD,
            NotificationManager.IMPORTANCE_LOW
        )
    )
    notificationManager.createNotificationChannel(
        createChannel(
            NOTIFICATION_CHANNEL_ID_TOMORROW,
            NOTIFICATION_CHANNEL_NAME_TOMORROW,
            NOTIFICATION_CHANNEL_DESCRIPTION_TOMORROW,
            NotificationManager.IMPORTANCE_HIGH
        )
    )
}

private fun createDefaultChannel(): NotificationChannel {
    return createChannel(
        NOTIFICATION_CHANNEL_ID_DEFAULT,
        NOTIFICATION_CHANNEL_NAME_DEFAULT,
        null,
        NotificationManager.IMPORTANCE_LOW
    )
}

private fun createChannel(
    channelID: String,
    channelName: String,
    channelDescription: String?,
    importance: Int
): NotificationChannel {
    val channel = NotificationChannel(channelID, channelName, importance)
    channel.enableLights(true)
    channel.description = channelDescription
    channel.lightColor = Color.GREEN
    return channel
}