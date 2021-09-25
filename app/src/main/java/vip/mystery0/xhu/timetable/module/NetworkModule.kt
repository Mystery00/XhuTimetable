package vip.mystery0.xhu.timetable.module

import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import vip.mystery0.xhu.timetable.api.FileApi
import vip.mystery0.xhu.timetable.api.PoemsApi
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.config.interceptor.LogInterceptor
import vip.mystery0.xhu.timetable.config.interceptor.PoemsInterceptor

const val HTTP_CLIENT = "client"
const val HTTP_CLIENT_POEMS = "poemsClient"
const val RETROFIT = "retrofit"
const val RETROFIT_POEMS = "poemsRetrofit"

val networkModule = module {
    single(named(HTTP_CLIENT)) {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .addInterceptor(LogInterceptor())
            .build()
    }
    single(named(HTTP_CLIENT_POEMS)) {
        OkHttpClient.Builder()
            .addInterceptor(PoemsInterceptor())
            .build()
    }

    single(named(RETROFIT)) {
        Retrofit.Builder()
            .baseUrl("https://xgkb.api.mystery0.vip")
            .client(get(named(HTTP_CLIENT)))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }
    single(named(RETROFIT_POEMS)) {
        Retrofit.Builder()
            .baseUrl("https://v2.jinrishici.com")
            .client(get(named(HTTP_CLIENT_POEMS)))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    serverApi<ServerApi>()
    serverApi<FileApi>()

    single<PoemsApi> { get<Retrofit>(named(RETROFIT_POEMS)).create(PoemsApi::class.java) }
}

private inline fun <reified API> Module.serverApi() {
    single<API> { get<Retrofit>(named(RETROFIT)).create(API::class.java) }
}