package vip.mystery0.xhu.timetable.push

import android.app.Notification
import android.util.Log
import cn.jiguang.api.utils.JCollectionAuth
import cn.jpush.android.api.BasicPushNotificationBuilder
import cn.jpush.android.api.JPushInterface
import cn.jpush.android.data.JPushConfig
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.module.FeatureString
import vip.mystery0.xhu.timetable.ui.notification.NOTIFICATION_CHANNEL_ID_PUSH

object PushSetter : KoinComponent {
    private const val TAG = "PushSetter"

    fun init() {
        val apiKey = FeatureString.JPUSH_APP_KEY.getValue()
        if (apiKey == "disable") {
            Log.i(TAG, "disable jpush with feature switch")
            return
        }
        JPushInterface.setDebugMode(false)
        @Suppress("DEPRECATION")
        JCollectionAuth.setAuth(context, true)
        val config = JPushConfig()
        config.setjAppKey(apiKey)
        JPushInterface.init(context, config)
        registerNotification()
    }

    private fun registerNotification() {
        val build = BasicPushNotificationBuilder(context)
        build.statusBarDrawable = R.drawable.ic_stat_course_notify
        build.notificationFlags = Notification.FLAG_AUTO_CANCEL
        build.notificationDefaults =
            Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE or Notification.DEFAULT_LIGHTS
        JPushInterface.setPushNotificationBuilder(1, build)
        JPushInterface.setChannel(context, NOTIFICATION_CHANNEL_ID_PUSH)
    }
}