package vip.mystery0.xhu.timetable.module

import android.os.Build
import com.featureprobe.mobile.FeatureProbe
import com.featureprobe.mobile.FpConfig
import com.featureprobe.mobile.FpUrlBuilder
import com.featureprobe.mobile.FpUser
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.BuildConfig
import vip.mystery0.xhu.timetable.publicDeviceId

val featureModule = module {
    single {
        val url = FpUrlBuilder("https://feature.api.mystery0.vip").build()
        val user = FpUser()
        user.stableRollout(publicDeviceId)
        user.with("deviceId", "android-$publicDeviceId")
        user.with("systemVersion", "Android ${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT}")
        user.with("factory", Build.MANUFACTURER)
        user.with("model", Build.MODEL)
        user.with("rom", Build.DISPLAY)
        user.with("packageName", BuildConfig.APPLICATION_ID)
        val config = FpConfig(url!!, "client-27a2a0787d92993d66680ac75f2fb050859c97d1", 60u, true)
        FeatureProbe(config, user)
    }
}

private val fp: FeatureProbe
    get() = KoinJavaComponent.get(FeatureProbe::class.java)

enum class Feature(val key: String, val defaultValue: Boolean) {
    JRSC("switch_jinrishici", false),
    ;

    fun isEnabled(): Boolean = fp.boolValue(key, defaultValue)
}

enum class FeatureString(val key: String, val defaultValue: String) {
    JPUSH_APP_KEY("jpush_api_key", "disable"),
    ;

    fun getValue(): String = fp.stringValue(key, defaultValue)
}