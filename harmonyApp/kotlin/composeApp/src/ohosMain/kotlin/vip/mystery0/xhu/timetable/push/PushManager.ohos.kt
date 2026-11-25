package vip.mystery0.xhu.timetable.push

actual val pushManager: PushManager = OhosPushManager

actual fun initPush() {
}

object OhosPushManager : PushManager {
    override suspend fun initialize() {
    }

    override suspend fun requestPermissionIfNeeded(): PushPermissionState = PushPermissionState.DENIED

    override suspend fun registrationId(): String? = null

    override suspend fun refreshRegistrationId(): String? = null
}
