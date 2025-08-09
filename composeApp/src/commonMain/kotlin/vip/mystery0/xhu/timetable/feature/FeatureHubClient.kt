package vip.mystery0.xhu.timetable.feature

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLBuilder
import io.ktor.http.path
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.kotlincrypto.hash.sha2.SHA256

/**
 * Feature Hub 客户端，负责与 Feature Hub API 进行通信。
 * @param host Feature Hub 服务的地址，例如 "http://localhost:8080"。
 * @param sdkKey 您的环境 SDK 密钥。
 * @param repository 用于存储特征标志的仓库。
 * @param pollingIntervalMs 轮询间隔，单位为毫秒。
 */
internal class FeatureHubClient(
    private val host: String,
    private val sdkKey: String,
    private val repository: InMemoryFeatureRepository,
    private val pollingIntervalMs: Long = 30000L // 默认30秒轮询一次
) {
    private val logger = Logger.withTag("FeatureHubClient")

    // 创建 Ktor HttpClient 实例
    private val client = HttpClient {
        // 安装内容协商插件，用于自动处理 JSON
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true // 忽略 API 返回的未知字段
            })
        }
    }

    private var pollingJob: Job? = null
    private var etag: String? = null // 用于缓存控制，减少不必要的数据传输

    private fun calculateContextSha(featureHeader: String): String {
        val bytes = SHA256().digest(featureHeader.encodeToByteArray())
        val hexFormat = HexFormat {
            number {
                removeLeadingZeros = true
                minLength = 2
            }
        }
        return bytes.joinToString("") {
            it.toHexString(hexFormat)
        }
    }

    /**
     * 向 Feature Hub 服务器请求最新的特征标志。
     * 使用 ETag (If-None-Match) 缓存机制，如果服务器返回 304 Not Modified，则不处理响应体。
     */
    suspend fun fetchFeatures(context: FeatureHubContext) {
        FeatureHub.record()
        val featureHeader = context.buildFeatureHeader()
        val url = URLBuilder(host).apply {
            path("features")
            parameters.append("apiKey", sdkKey)
            parameters.append("contextSha", calculateContextSha(featureHeader))
        }.buildString()
        logger.d("Fetching features from $url")
        try {
            val response = client.get(url) {
                header("x-featurehub", featureHeader)
                // 如果我们有 etag，就将其添加到请求头中
                etag?.let { header(HttpHeaders.IfNoneMatch, it) }
                header("X-SDK", "Java-OKHTTP,1.0,1.1.2")
            }

            when (response.status) {
                HttpStatusCode.OK -> {
                    // 成功获取新数据
                    val newFeatures: List<FeatureList> = response.body()
                    repository.updateFeatures(newFeatures.getOrNull(0)?.features ?: emptyList())
                    // 保存新的 etag 以备下次请求使用
                    etag = response.headers[HttpHeaders.ETag]
                    logger.d("Features updated successfully. New ETag: $etag")
                }

                HttpStatusCode.NotModified -> {
                    // 数据未改变，无需任何操作
                    logger.d("Features not modified (304).")
                }

                else -> {
                    // 处理其他错误情况
                    logger.w("Error fetching features. Status: ${response.status}")
                }
            }
        } catch (e: Exception) {
            // 处理网络异常等问题
            logger.w("Exception while fetching features: ${e.message}")
        }
    }

    /**
     * 启动后台轮询任务。
     * 如果任务已在运行，则不会重复启动。
     */
    fun startPolling(context: FeatureHubContext) {
        if (pollingJob?.isActive == true) {
            logger.i("Polling is already active.")
            return
        }
        logger.d("Starting polling with interval ${pollingIntervalMs}ms...")
        // 在一个独立的协程作用域中启动轮询
        pollingJob = CoroutineScope(Dispatchers.Default).launch {
            // 首次启动时立即获取一次
            fetchFeatures(context)
            // 循环执行，直到协程被取消
            while (isActive) {
                delay(pollingIntervalMs)
                fetchFeatures(context)
            }
        }
    }

    /**
     * 停止后台轮询任务。
     */
    fun stopPolling() {
        logger.i("Stopping polling.")
        pollingJob?.cancel()
        pollingJob = null
    }
}