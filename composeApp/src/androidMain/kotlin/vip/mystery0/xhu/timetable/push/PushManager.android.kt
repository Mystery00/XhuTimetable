package vip.mystery0.xhu.timetable.push

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import cn.jpush.android.api.JPushInterface
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import vip.mystery0.xhu.timetable.context

private const val PUSH_CHANNEL_ID = "XhuTimetable-Push"

actual val pushManager: PushManager = AndroidPushManager

actual fun initPush() {
    CoroutineScope(Dispatchers.Default).launch {
        pushManager.initialize()
    }
}

object AndroidPushManager : PushManager {
    private var initialized = false

    override suspend fun initialize() {
        if (initialized) return
        JPushInterface.setDebugMode(false)
        JPushInterface.init(context)
        createNotificationChannel()
        initialized = true
        refreshRegistrationId()
    }

    override suspend fun requestPermissionIfNeeded(): PushPermissionState {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return PushPermissionState.GRANTED
        }
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (granted) return PushPermissionState.GRANTED
        JPushInterface.requestPermission(context)
        return PushPermissionState.NOT_DETERMINED
    }

    override suspend fun registrationId(): String? {
        val cached = GlobalCacheStore.pushRegistrationId
        if (cached.isNotBlank()) return cached
        return refreshRegistrationId()
    }

    override suspend fun refreshRegistrationId(): String? {
        initialize()
        val registrationId = JPushInterface.getRegistrationID(context)
        if (registrationId.isNotBlank()) {
            GlobalCacheStore.pushRegistrationId = registrationId
            return registrationId
        }
        return null
    }

    private fun createNotificationChannel() {
        val notificationManager =
            context.getSystemService(NotificationManager::class.java) ?: return
        val channel = NotificationChannel(
            PUSH_CHANNEL_ID,
            "成绩变更提醒",
            NotificationManager.IMPORTANCE_DEFAULT,
        )
        notificationManager.createNotificationChannel(channel)
    }
}
