package vip.mystery0.xhu.timetable.module

import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import vip.mystery0.xhu.timetable.api.AggregationApi
import vip.mystery0.xhu.timetable.api.CommonApi
import vip.mystery0.xhu.timetable.api.CourseApi
import vip.mystery0.xhu.timetable.api.ExamApi
import vip.mystery0.xhu.timetable.api.FeedbackApi
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.api.JwcApi
import vip.mystery0.xhu.timetable.api.MenuApi
import vip.mystery0.xhu.timetable.api.PoemsApi
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.api.ThingApi
import vip.mystery0.xhu.timetable.api.UserApi
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.config.interceptor.DownloadProgressInterceptor
import vip.mystery0.xhu.timetable.config.interceptor.PoemsInterceptor
import vip.mystery0.xhu.timetable.config.interceptor.ServerApiInterceptor
import vip.mystery0.xhu.timetable.config.interceptor.UserAgentInterceptor
import java.util.concurrent.TimeUnit

const val HTTP_CLIENT = "client"
const val HTTP_CLIENT_POEMS = "poemsClient"
const val RETROFIT = "retrofit"
const val RETROFIT_POEMS = "poemsRetrofit"
const val RETROFIT_FILE = "fileRetrofit"
const val RETROFIT_WS = "wsRetrofit"

val networkModule = module {
    single(named(HTTP_CLIENT)) {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor(ServerApiInterceptor())
            .addInterceptor(UserAgentInterceptor())
            .build()
    }
    single(named(HTTP_CLIENT_POEMS)) {
        OkHttpClient.Builder()
            .addInterceptor(PoemsInterceptor())
            .addInterceptor(UserAgentInterceptor())
            .build()
    }

    single(named(RETROFIT)) {
        Retrofit.Builder()
            .baseUrl(GlobalConfig.serverUrl)
            .client(get(named(HTTP_CLIENT)))
            .addConverterFactory(
                MoshiConverterFactory.create(Moshi.Builder().registerAdapter().build())
            )
            .build()
    }
    single(named(RETROFIT_POEMS)) {
        Retrofit.Builder()
            .baseUrl("https://v2.jinrishici.com")
            .client(get(named(HTTP_CLIENT_POEMS)))
            .addConverterFactory(
                MoshiConverterFactory.create(Moshi.Builder().registerAdapter().build())
            )
            .build()
    }
    single(named(RETROFIT_WS)) {
        Retrofit.Builder()
            .baseUrl("https://ws.api.mystery0.vip")
            .client(get(named(HTTP_CLIENT)))
            .addConverterFactory(
                MoshiConverterFactory.create(Moshi.Builder().registerAdapter().build())
            )
            .build()
    }
    single(named(RETROFIT_FILE)) {
        val client = OkHttpClient.Builder()
            .addInterceptor(DownloadProgressInterceptor())
            .retryOnConnectionFailure(true)
            .connectTimeout(15, TimeUnit.SECONDS)
            .build()
        Retrofit.Builder()
            .baseUrl("https://example.com/")
            .client(client)
            .build()
    }

    serverApi<ServerApi>()
    serverApi<JwcApi>()

    serverApi<CommonApi>()
    serverApi<MenuApi>()
    serverApi<UserApi>()
    serverApi<AggregationApi>()
    serverApi<CourseApi>()
    serverApi<ThingApi>()
    serverApi<ExamApi>()

    single { get<Retrofit>(named(RETROFIT_POEMS)).create(PoemsApi::class.java) }
    single { get<Retrofit>(named(RETROFIT_FILE)).create(FileApi::class.java) }
    single { get<Retrofit>(named(RETROFIT_WS)).create(FeedbackApi::class.java) }
}

private inline fun <reified API> Module.serverApi() {
    single<API> { get<Retrofit>(named(RETROFIT)).create(API::class.java) }
}

const val HINT_NETWORK = "网络无法使用，请检查网络连接！"

class NetworkNotConnectException : RuntimeException(HINT_NETWORK)