package vip.mystery0.xhu.timetable.module

import de.jensklingenberg.ktorfit.Ktorfit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.sink
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readAvailable
import kotlinx.io.buffered
import kotlinx.serialization.json.Json
import org.koin.core.qualifier.named
import org.koin.dsl.module
import vip.mystery0.xhu.timetable.config.ktor.FileDownloadProgressState
import vip.mystery0.xhu.timetable.config.ktor.PoemsPlugin
import vip.mystery0.xhu.timetable.config.ktor.ServerApiPlugin

const val PRIVACY_URL = "https://xgkb.mystery0.vip/privacy/index.html"
const val HELP_URL = "https://blog.mystery0.vip/docs/xgkb/group"

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
            install(HttpTimeout) {
                requestTimeoutMillis = 20000L
            }
            install(ContentNegotiation) {
                json(json)
            }
            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
            install(ServerApiPlugin)
            install(UserAgent) {
                val userAgent = userAgent()
                if (userAgent.isNotBlank()) {
                    agent = userAgent
                }
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
                val userAgent = userAgent()
                if (userAgent.isNotBlank()) {
                    agent = userAgent
                }
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
                val userAgent = userAgent()
                if (userAgent.isNotBlank()) {
                    agent = userAgent
                }
            }
        }
    }
    single(named(HTTP_CLIENT_FILE)) {
        HttpClient(httpClientEngine()) {
            engine { httpClientEngineConfig(this) }
            install(UserAgent) {
                val userAgent = userAgent()
                if (userAgent.isNotBlank()) {
                    agent = userAgent
                }
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

    registerApiBindings()
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
        if (!resp.status.isSuccess()) {
            throw RuntimeException("下载失败 (HTTP ${resp.status.value})")
        }
        val channel: ByteReadChannel = resp.body()
        val total = resp.contentLength() ?: 0L
        var count = 0L
        val buffer = ByteArray(16 * 1024)
        saveFile.sink(append = false).buffered().use { sink ->
            while (true) {
                val bytesRead = channel.readAvailable(buffer)
                if (bytesRead == -1) break
                if (bytesRead > 0) {
                    sink.write(buffer, 0, bytesRead)
                    count += bytesRead
                    progress?.let {
                        val percent = if (total > 0L) {
                            (count * 100F / total.toFloat()).coerceIn(0F, 100F)
                        } else {
                            0F
                        }
                        it(FileDownloadProgressState(count, total, percent))
                    }
                }
            }
            sink.flush()
        }
        progress?.let {
            val finalTotal = if (total > 0L) total else count
            it(FileDownloadProgressState(count, finalTotal, 100F))
        }
    }
}