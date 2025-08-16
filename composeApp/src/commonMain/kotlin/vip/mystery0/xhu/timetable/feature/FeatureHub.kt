package vip.mystery0.xhu.timetable.feature

import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import vip.mystery0.xhu.timetable.config.store.setCacheStore
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import kotlin.time.Clock

/**
 * 全局委托对象 (ViewModel)，作为 SDK 的唯一入口点。
 * 采用单例模式，方便在整个应用中访问。
 */
object FeatureHub {
    private val logger = Logger.withTag("FeatureHub")
    private val repository = InMemoryFeatureRepository()
    private lateinit var client: FeatureHubClient
    private lateinit var context: FeatureHubContext
    private var isInitialized = false

    suspend fun record() {
        Logger.d("record feature pull")
        setCacheStore {
            featurePullLastExecuteTime =
                featurePullLastExecuteTime + Clock.System.now().asLocalDateTime()
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

    fun setContext(context: FeatureHubContext) {
        if (!isInitialized) throw IllegalStateException("FeatureHubSDK must be initialized before starting.")
        this.context = context
    }

    /**
     * 启动轮询。
     * 必须在 initialize() 之后调用。
     */
    fun start(scope: CoroutineScope) {
        if (!isInitialized) throw IllegalStateException("FeatureHubSDK must be initialized before starting.")
        if (!::context.isInitialized) throw IllegalStateException("FeatureHubContext must be set before starting.")
        client.startPolling(scope, context)
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