package vip.mystery0.xhu.timetable.push

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNUserNotificationCenter
import vip.mystery0.xhu.timetable.config.store.GlobalCacheStore
import kotlin.coroutines.resume

actual val pushManager: PushManager = IosPushManager

actual fun initPush() {
}

object IosPushManager : PushManager {
    override suspend fun initialize() {
    }

    override suspend fun requestPermissionIfNeeded(): PushPermissionState =
        suspendCancellableCoroutine { continuation ->
            val options = UNAuthorizationOptionAlert or
                    UNAuthorizationOptionSound or
                    UNAuthorizationOptionBadge
            UNUserNotificationCenter.currentNotificationCenter()
                .requestAuthorizationWithOptions(options) { granted, _ ->
                    continuation.resume(
                        if (granted) PushPermissionState.GRANTED else PushPermissionState.DENIED
                    )
                }
        }

    override suspend fun registrationId(): String? =
        GlobalCacheStore.pushRegistrationId.takeIf { it.isNotBlank() }

    override suspend fun refreshRegistrationId(): String? = registrationId()
}

object IosPushBridge {
    fun updateRegistrationId(registrationId: String) {
        GlobalCacheStore.pushRegistrationId = registrationId
    }
}
