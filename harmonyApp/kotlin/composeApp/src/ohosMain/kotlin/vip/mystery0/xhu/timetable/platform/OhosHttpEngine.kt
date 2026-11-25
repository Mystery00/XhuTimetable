@file:OptIn(io.ktor.utils.io.InternalAPI::class, kotlin.ExperimentalStdlibApi::class)
@file:Suppress("DEPRECATION")

package vip.mystery0.xhu.timetable.platform

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineBase
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.engine.callContext
import io.ktor.client.engine.engines
import io.ktor.client.engine.mergeHeaders
import io.ktor.client.plugins.HttpTimeoutCapability
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpProtocolVersion
import io.ktor.http.HttpStatusCode
import io.ktor.http.HeadersBuilder
import io.ktor.http.content.OutgoingContent
import io.ktor.util.date.GMTDate
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.readByteArray
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.io.encoding.Base64
import kotlin.native.EagerInitialization
import kotlin.uuid.Uuid

@EagerInitialization
private val registerEngine = engines.append(OhosHttp)

class OhosHttpConfig : HttpClientEngineConfig() {
    var timeoutMillis: Long = 20_000
}

object OhosHttp : HttpClientEngineFactory<OhosHttpConfig> {
    override fun create(block: OhosHttpConfig.() -> Unit): HttpClientEngine =
        OhosHttpEngine(OhosHttpConfig().apply(block))
}

private class OhosHttpEngine(override val config: OhosHttpConfig) : HttpClientEngineBase("HarmonyOS") {
    override val supportedCapabilities = setOf(HttpTimeoutCapability)

    override suspend fun execute(data: HttpRequestData): HttpResponseData {
        val started = GMTDate()
        val requestId = Uuid.random().toString()
        val headers = mutableListOf<HttpHeader>()
        mergeHeaders(data.headers, data.body) { name, value -> headers += HttpHeader(name, value) }
        // 避免系统解压后的字节长度与 Ktor 的响应长度校验不一致。
        if (headers.none { it.name.equals("Accept-Encoding", ignoreCase = true) }) {
            headers += HttpHeader("Accept-Encoding", "identity")
        }
        val timeout = data.getCapabilityOrNull(HttpTimeoutCapability)
        val request = NativeHttpRequest(
            requestId = requestId,
            url = data.url.toString(),
            method = data.method.value,
            headers = headers,
            bodyBase64 = Base64.encode(data.body.toRequestBytes()),
            connectTimeoutMs = (timeout?.connectTimeoutMillis ?: config.timeoutMillis).coerceAtMost(Int.MAX_VALUE.toLong()),
            readTimeoutMs = (timeout?.socketTimeoutMillis ?: config.timeoutMillis).coerceAtMost(Int.MAX_VALUE.toLong()),
        )
        val response = try {
            Json.decodeFromString<NativeHttpResponse>(
                ArkTsBridge.call("requestHttp", Json.encodeToString(request))
            )
        } catch (cancelled: CancellationException) {
            withContext(NonCancellable) {
                runCatching { ArkTsBridge.call("cancelHttp", requestId) }
            }
            throw cancelled
        }
        return HttpResponseData(
            statusCode = HttpStatusCode.fromValue(response.status),
            requestTime = started,
            headers = HeadersBuilder().apply {
                response.headers.forEach { append(it.name, it.value) }
            }.build(),
            version = HttpProtocolVersion.HTTP_1_1,
            body = ByteReadChannel(Base64.decode(response.bodyBase64)),
            callContext = callContext(),
        )
    }
}

private suspend fun OutgoingContent.toRequestBytes(): ByteArray = when (this) {
    is OutgoingContent.NoContent -> ByteArray(0)
    is OutgoingContent.ByteArrayContent -> bytes()
    is OutgoingContent.ReadChannelContent -> readFrom().readRemaining().readByteArray()
    is OutgoingContent.WriteChannelContent -> coroutineScope {
        val channel = ByteChannel(autoFlush = true)
        val writer = launch {
            try {
                writeTo(channel)
                channel.flushAndClose()
            } catch (error: Throwable) {
                channel.cancel(error)
                throw error
            }
        }
        channel.readRemaining().readByteArray().also { writer.join() }
    }
    is OutgoingContent.ContentWrapper -> delegate().toRequestBytes()
    else -> throw UnsupportedOperationException("鸿蒙预览版暂不支持此请求类型")
}

@Serializable
private data class HttpHeader(val name: String, val value: String)

@Serializable
private data class NativeHttpRequest(
    val requestId: String,
    val url: String,
    val method: String,
    val headers: List<HttpHeader>,
    val bodyBase64: String,
    val connectTimeoutMs: Long,
    val readTimeoutMs: Long,
)

@Serializable
private data class NativeHttpResponse(
    val status: Int,
    val headers: List<HttpHeader>,
    val bodyBase64: String,
)
