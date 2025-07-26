package vip.mystery0.xhu.timetable.module

import android.app.AlarmManager
import android.app.NotificationManager
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.work.WorkManager
import co.touchlab.kermit.Logger
import io.featurehub.client.ClientContext
import io.featurehub.client.EdgeFeatureHubConfig
import io.featurehub.sse.model.StrategyAttributeDeviceName
import io.featurehub.sse.model.StrategyAttributePlatformName
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.BuildConfig
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.appVersionCode
import vip.mystery0.xhu.timetable.base.appVersionName
import vip.mystery0.xhu.timetable.base.publicDeviceId
import vip.mystery0.xhu.timetable.config.MyFeatureHubClient
import vip.mystery0.xhu.timetable.context
import vip.mystery0.xhu.timetable.db.AppDatabase
import java.util.concurrent.TimeUnit

actual fun platformModule(): Module = module {
    single { WorkManager.getInstance(androidContext()) }
    single { androidContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager }
    single { androidContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
    single { androidContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    single { androidContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager }

    single<RoomDatabase.Builder<AppDatabase>> {
        getDatabaseBuilder(get())
            .setDriver(AndroidSQLiteDriver())
    }

    single<ClientContext> {
        val edgeUrl = "https://fh.api.mystery0.vip"

        val apiKey = context.getString(R.string.feature_api_key)
        val fhConfig = EdgeFeatureHubConfig(edgeUrl, apiKey)
        val httpClient = OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .build()
        fhConfig.setEdgeService {
            MyFeatureHubClient(
                edgeUrl,
                apiKey,
                fhConfig.repository,
                httpClient,
                fhConfig,
                120L,
            )
        }
        fhConfig.repository.addReadynessListener {
            Logger.withTag("FeatureHub").i("ready: $it")
        }
        fhConfig.newContext()
            .userKey(publicDeviceId())
            .attr("deviceId", publicDeviceId())
            .attr("systemVersion", "Android ${Build.VERSION.RELEASE}-${Build.VERSION.SDK_INT}")
            .attr("factory", Build.MANUFACTURER)
            .attr("model", Build.MODEL)
            .attr("rom", Build.DISPLAY)
            .attr("packageName", BuildConfig.APPLICATION_ID)
            .attr("versionName", appVersionName())
            .attr("versionCode", appVersionCode())
            .device(StrategyAttributeDeviceName.MOBILE)
            .platform(StrategyAttributePlatformName.ANDROID)
            .version("${appVersionName()}-${appVersionCode()}")
    }
}

private fun getDatabaseBuilder(ctx: Context): RoomDatabase.Builder<AppDatabase> {
    val appContext = ctx.applicationContext
    return Room.databaseBuilder(
        context = appContext,
        klass = AppDatabase::class.java,
        name = DATABASE_NAME
    )
}

actual fun Throwable.desc(): String = this.javaClass.simpleName