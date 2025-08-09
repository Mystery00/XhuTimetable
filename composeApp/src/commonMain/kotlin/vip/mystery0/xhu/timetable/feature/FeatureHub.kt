package vip.mystery0.xhu.timetable.feature

import co.touchlab.kermit.Logger
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * 全局委托对象 (ViewModel)，作为 SDK 的唯一入口点。
 * 采用单例模式，方便在整个应用中访问。
 */
object FeatureHub {
    private val logger = Logger.withTag("FeatureHub")
    private val repository = InMemoryFeatureRepository()
    private lateinit var client: FeatureHubClient
    private var isInitialized = false

    val httpRequests = mutableListOf<Instant>()

    fun record() {
        httpRequests.add(Clock.System.now())
        if (httpRequests.size > 5) {
            httpRequests.removeAt(0)
        }
    }

    /**
     * 初始化 SDK。必须在使用前调用。
     * @param host Feature Hub 服务的地址。
     * @param sdkKey 您的环境 SDK 密钥。
     * @param pollingIntervalMs 可选的轮询间隔。
     */
    fun initialize(host: String, sdkKey: String, pollingIntervalMs: Long = 30000L) {
        if (isInitialized) {
            logger.i("Already initialized.")
            return
        }
        client = FeatureHubClient(host, sdkKey, repository, pollingIntervalMs)
        isInitialized = true
        logger.i("Initialized successfully.")
    }

    /**
     * 启动轮询。
     * 必须在 initialize() 之后调用。
     */
    fun start(context: FeatureHubContext) {
        if (!isInitialized) throw IllegalStateException("FeatureHubSDK must be initialized before starting.")
        client.startPolling(context)
    }

    /**
     * 停止轮询。
     */
    fun stop() {
        if (isInitialized) {
            client.stopPolling()
        }
    }

    /**
     * 检查一个特征标志是否启用。
     * @param key 特征的 key。
     * @param defaultValue 如果找不到该特征，返回的默认值。
     * @return 如果特征启用则返回 true，否则返回 false。
     */
    fun isEnabled(key: String, defaultValue: Boolean = false): Boolean {
        return repository.getFeature(key)?.isEnabled() ?: defaultValue
    }

    /**
     * 获取一个特征标志的值。
     * @param key 特征的 key。
     * @param defaultValue 如果找不到该特征，返回的默认值。
     * @return 特征的值。
     */
    fun getValue(key: String, defaultValue: String = ""): String {
        return repository.getFeature(key)?.value ?: defaultValue
    }
}