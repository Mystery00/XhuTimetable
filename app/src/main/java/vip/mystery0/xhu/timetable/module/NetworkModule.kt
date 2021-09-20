package vip.mystery0.xhu.timetable.module

import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.config.interceptor.LogInterceptor
import vip.mystery0.xhu.timetable.config.interceptor.ReLoginInterceptor
import java.util.concurrent.TimeUnit

const val HTTP_CLIENT = "client"
const val RETROFIT = "retrofit"

val networkModule = module {
    single(named(HTTP_CLIENT)) {
        OkHttpClient.Builder()
            .retryOnConnectionFailure(true)
            .connectTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(LogInterceptor())
            .addInterceptor(ReLoginInterceptor())
            .build()
    }

    single(named(RETROFIT)) {
        Retrofit.Builder()
            .baseUrl("https://xgkb.api.mystery0.vip")
            .client(get(named(HTTP_CLIENT)))
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    api<ServerApi>()
}

private inline fun <reified API> Module.api() {
    single<API> { get<Retrofit>(named(RETROFIT)).create(API::class.java) }
}