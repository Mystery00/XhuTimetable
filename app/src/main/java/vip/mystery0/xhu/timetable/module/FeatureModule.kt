package vip.mystery0.xhu.timetable.module

import android.os.Build
import android.util.Log
import io.featurehub.android.FeatureHubClient
import io.featurehub.client.ClientContext
import io.featurehub.client.EdgeFeatureHubConfig
import io.featurehub.sse.model.StrategyAttributeDeviceName
import io.featurehub.sse.model.StrategyAttributePlatformName
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.java.KoinJavaComponent
import vip.mystery0.xhu.timetable.BuildConfig
import vip.mystery0.xhu.timetable.appVersionCode
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.publicDeviceId

val featureModule = module {
    single<ClientContext> {
        val edgeUrl = "https://fh.api.mystery0.vip"
        val apiKey = "65041db9-520c-4962-a512-34fd055abeae/41eFdAIdx5mMavrd4UYjJtpaz4UJEQWvFMTTmVhJ"
        val fhConfig = EdgeFeatureHubConfig(edgeUrl, apiKey)
        val httpClient = get<OkHttpClient>(named(HTTP_CLIENT_FEATURE_HUB))
        fhConfig.setEdgeService {
            FeatureHubClient(
                edgeUrl,
                listOf(apiKey),
                fhConfig.repository,
                httpClient,
                fhConfig,
                60
            )
        }
        fhConfig.repository.addReadynessListener {
            Log.i("FeatureHub", "ready: $it")
        }
        fhConfig.newContext()
            .userKey(publicDeviceId)
            .attr("deviceId", "android-$publicDeviceId")
            .attr("systemVersion", "Android ${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT}")
            .attr("factory", Build.MANUFACTURER)
            .attr("model", Build.MODEL)
            .attr("rom", Build.DISPLAY)
            .attr("packageName", BuildConfig.APPLICATION_ID)
            .device(StrategyAttributeDeviceName.MOBILE)
            .platform(StrategyAttributePlatformName.ANDROID)
            .version("${appVersionName}-${appVersionCode}")
    }
}

private val fc: ClientContext
    get() = KoinJavaComponent.get<ClientContext>(ClientContext::class.java).build().get()

enum class Feature(val key: String, private val defaultValue: Boolean) {
    JRSC("switch_jinrishici", false),
    ;

    fun isEnabled(): Boolean = fc.feature(key).boolean ?: defaultValue
}

enum class FeatureString(val key: String, private val defaultValue: String) {
    JPUSH_APP_KEY("jpush_api_key", "disable"),
    LOGIN_LABEL("data_login_label", "1"),
    ;

    fun getValue(): String = fc.feature(key).string ?: defaultValue
}