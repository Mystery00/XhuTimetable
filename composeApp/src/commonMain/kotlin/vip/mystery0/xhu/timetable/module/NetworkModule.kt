package vip.mystery0.xhu.timetable.module

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.write
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.core.remaining
import io.ktor.utils.io.exhausted
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.api.createAggregationApi
import vip.mystery0.xhu.timetable.api.createBackgroundApi
import vip.mystery0.xhu.timetable.api.createCalendarApi
import vip.mystery0.xhu.timetable.api.createClassroomApi
import vip.mystery0.xhu.timetable.api.createCommonApi
import vip.mystery0.xhu.timetable.api.createCourseApi
import vip.mystery0.xhu.timetable.api.createCustomCourseApi
import vip.mystery0.xhu.timetable.api.createCustomThingApi
import vip.mystery0.xhu.timetable.api.createExamApi
import vip.mystery0.xhu.timetable.api.createFeedbackApi
import vip.mystery0.xhu.timetable.api.createJobApi
import vip.mystery0.xhu.timetable.api.createMenuApi
import vip.mystery0.xhu.timetable.api.createNoticeApi
import vip.mystery0.xhu.timetable.api.createPoemsApi
import vip.mystery0.xhu.timetable.api.createScoreApi
import vip.mystery0.xhu.timetable.api.createUrgeApi
import vip.mystery0.xhu.timetable.api.createUserApi
import vip.mystery0.xhu.timetable.config.ktor.FileDownloadProgressState
import vip.mystery0.xhu.timetable.config.ktor.PoemsPlugin
import vip.mystery0.xhu.timetable.config.ktor.ServerApiPlugin

const val HTTP_CLIENT = "client"
const val HTTP_CLIENT_POEMS = "poemsClient"
const val HTTP_CLIENT_WS = "wsClient"
const val HTTP_CLIENT_FILE = "fileClient"

const val RETROFIT = "retrofit"
const val RETROFIT_POEMS = "poemsRetrofit"
const val RETROFIT_WS = "wsRetrofit"

private val json = Json {
    isLenient = true
    ignoreUnknownKeys = true
    encodeDefaults = true
}

val networkModule = module {
    single(named(HTTP_CLIENT)) {
        HttpClient(httpClientEngine()) {
            engine { httpClientEngineConfig(this, 20L) }
            install(ContentNegotiation) {
                json(json)
            }
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            install(ServerApiPlugin)
            install(UserAgent) {
                agent = userAgent()
            }
        }
    }
    single(named(HTTP_CLIENT_POEMS)) {
        HttpClient(httpClientEngine()) {
            engine { httpClientEngineConfig(this) }
            install(ContentNegotiation) {
                json(json)
            }
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            install(PoemsPlugin)
            install(UserAgent) {
                agent = userAgent()
            }
        }
    }
    single(named(HTTP_CLIENT_WS)) {
        HttpClient(httpClientEngine()) {
            engine { httpClientEngineConfig(this) }
            install(ContentNegotiation) {
                json(json)
            }
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            install(WebSockets) {
                pingIntervalMillis = 5_000
            }
            install(UserAgent) {
                agent = userAgent()
            }
        }
    }
    single(named(HTTP_CLIENT_FILE)) {
        HttpClient(httpClientEngine()) {
            engine { httpClientEngineConfig(this) }
            install(UserAgent) {
                agent = userAgent()
            }
        }
    }

    single(named(RETROFIT)) {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(named(HTTP_CLIENT)))
            .baseUrl("https://xgkb.api.mystery0.vip/")
            .build()
    }
    single(named(RETROFIT_POEMS)) {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(named(HTTP_CLIENT_POEMS)))
            .baseUrl("https://v2.jinrishici.com/")
            .build()
    }
    single(named(RETROFIT_WS)) {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(named(HTTP_CLIENT_WS)))
            .baseUrl("https://ws.api.mystery0.vip/")
            .build()
    }

    single { get<Ktorfit>(named(RETROFIT)).createCommonApi() }
    single { get<Ktorfit>(named(RETROFIT)).createMenuApi() }
    single { get<Ktorfit>(named(RETROFIT)).createUserApi() }
    single { get<Ktorfit>(named(RETROFIT)).createAggregationApi() }
    single { get<Ktorfit>(named(RETROFIT)).createCourseApi() }
    single { get<Ktorfit>(named(RETROFIT)).createExamApi() }
    single { get<Ktorfit>(named(RETROFIT)).createScoreApi() }
    single { get<Ktorfit>(named(RETROFIT)).createCustomCourseApi() }
    single { get<Ktorfit>(named(RETROFIT)).createCustomThingApi() }
    single { get<Ktorfit>(named(RETROFIT)).createNoticeApi() }
    single { get<Ktorfit>(named(RETROFIT)).createClassroomApi() }
    single { get<Ktorfit>(named(RETROFIT)).createCalendarApi() }
    single { get<Ktorfit>(named(RETROFIT)).createBackgroundApi() }
    single { get<Ktorfit>(named(RETROFIT)).createUrgeApi() }
    single { get<Ktorfit>(named(RETROFIT)).createJobApi() }

    single { get<Ktorfit>(named(RETROFIT_POEMS)).createPoemsApi() }
    single { get<Ktorfit>(named(RETROFIT_WS)).createFeedbackApi() }
}

expect fun httpClientEngine(): HttpClientEngineFactory<HttpClientEngineConfig>

expect fun httpClientEngineConfig(config: HttpClientEngineConfig, timeout: Long = 0)

expect fun userAgent(): String

suspend fun HttpClient.downloadFileTo(
    saveFile: PlatformFile,
    builder: HttpRequestBuilder.() -> Unit,
    progress: ((FileDownloadProgressState) -> Unit)? = null
) {
    prepareGet(builder).execute { resp ->
        val channel: ByteReadChannel = resp.body()
        var count = 0L
        while (!channel.exhausted()) {
            val chunk = channel.readRemaining()
            count += chunk.remaining
            saveFile.write(chunk.readByteArray())
            progress?.let {
                val total = resp.contentLength() ?: 1L
                val progress = count * 100 / total.toFloat()
                it(FileDownloadProgressState(count, total, progress))
            }
        }
    }
}