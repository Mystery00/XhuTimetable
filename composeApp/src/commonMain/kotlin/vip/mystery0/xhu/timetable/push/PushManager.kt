package vip.mystery0.xhu.timetable.push

enum class PushPermissionState {
    GRANTED,
    DENIED,
    NOT_DETERMINED,
}

interface PushManager {
    suspend fun initialize()
    suspend fun requestPermissionIfNeeded(): PushPermissionState
    suspend fun registrationId(): String?
    suspend fun refreshRegistrationId(): String?
}

expect val pushManager: PushManager

expect fun initPush()
