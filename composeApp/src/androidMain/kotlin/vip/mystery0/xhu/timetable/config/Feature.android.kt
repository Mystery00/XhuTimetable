package vip.mystery0.xhu.timetable.config

import io.featurehub.client.ClientContext
import org.koin.java.KoinJavaComponent

private val fc: ClientContext
    get() = KoinJavaComponent.get<ClientContext>(ClientContext::class.java).build().get()

actual fun featureEnabled(featureKey: String): Boolean? = fc.feature(featureKey).boolean

actual fun featureString(featureKey: String): String? = fc.feature(featureKey).string