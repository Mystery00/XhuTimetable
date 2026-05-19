package vip.mystery0.xhu.timetable.push

import android.content.Context
import cn.jpush.android.api.NotificationMessage
import cn.jpush.android.service.JPushMessageReceiver
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.ui.activity.NavActivity

class XhuJPushMessageReceiver : JPushMessageReceiver() {
    override fun onRegister(context: Context?, registrationId: String?) {
        if (!registrationId.isNullOrBlank()) {
            GlobalCacheStore.pushRegistrationId = registrationId
        }
    }

    override fun onNotifyMessageOpened(context: Context?, message: NotificationMessage?) {
        context ?: return
        val intent = NavActivity.jumpIntent(context, NavActivity.InitRoute.SCORE.name)
            .addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}
