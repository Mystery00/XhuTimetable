package vip.mystery0.xhu.timetable.config

import android.util.Log
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.featurehub.client.EdgeService
import io.featurehub.client.FeatureHubConfig
import io.featurehub.client.FeatureStore
import io.featurehub.client.Readyness
import io.featurehub.sse.model.FeatureEnvironmentCollection
import io.featurehub.sse.model.FeatureState
import io.featurehub.sse.model.SSEResultState
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class MyFeatureHubClient(
    host: String,
    sdkKey: String,
    private val repository: FeatureStore,
    private val client: Call.Factory,
    private val config: FeatureHubConfig,
    private var pollingInterval: Long,
) : EdgeService {
    private var makeRequests = false
    private val url: String = "$host/features?apiKey=$sdkKey"
    private val mapper = ObjectMapper()
    private var xFeaturehubHeader: String? = null

    // used for breaking the cache
    private var xContextSha = "0"
    var isStopped: Boolean = false
        private set
    private var etag: String? = null

    var whenPollingCacheExpires: Long
        private set
    private val clientSideEvaluation = FeatureHubConfig.sdkKeyIsClientSideEvaluated(sdkKey)
    private val executorService: ExecutorService = Executors.newWorkStealingPool()

    private var busy = false
    private var triggeredAtLeastOnce = false
    private var headerChanged = false
    private var waitingClients: MutableList<CompletableFuture<Readyness>> =
        kotlin.collections.ArrayList()

    companion object {
        private const val TAG = "MyFeatureHubClient"
        private val cacheControlRegex = Regex("max-age=(\\d+)")
    }

    init {
        // 确保第一次调用时必定请求服务端
        whenPollingCacheExpires = now() - 100
        this.makeRequests = true
        if (clientSideEvaluation) {
            checkForUpdates()
        }
    }

    private fun now(): Long = Instant.now().toEpochMilli()

    fun checkForUpdates(): Boolean {
        val breakCache = now() > whenPollingCacheExpires || headerChanged
        val ask = canMakeRequest && !busy && breakCache

        headerChanged = false

        if (ask) {
            busy = true
            triggeredAtLeastOnce = true

            Log.d(TAG, "checkForUpdates: start request feature hub")
            var reqBuilder = Request.Builder().url("${this.url}&contextSha=$xContextSha")
            xFeaturehubHeader?.let {
                reqBuilder = reqBuilder.addHeader("x-featurehub", it)
            }
            etag?.let {
                reqBuilder = reqBuilder.addHeader("if-none-match", it)
            }
            /**
             * @see io.featurehub.client.utils.SdkVersion.sdkVersionHeader("Java-OKHTTP")
             */
            reqBuilder.addHeader("X-SDK", "Java-OKHTTP,1.0,1.1.2")
            client.newCall(reqBuilder.build())
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        processFailure(e)
                    }

                    @Throws(IOException::class)
                    override fun onResponse(call: Call, response: Response) {
                        processResponse(response)
                    }
                })
        } else {
            Log.d(TAG, "checkForUpdates: cache not expired")
        }

        return ask
    }

    fun processCacheControlHeader(cacheControlHeader: String) {
        val matchResult = cacheControlRegex.find(cacheControlHeader) ?: return
        val interval = matchResult.groupValues[1]
        interval.toLongOrNull()?.let { newInterval ->
            if (newInterval > 0) {
                this.pollingInterval = newInterval
            }
        }
    }

    private fun processFailure(e: IOException) {
        Log.e(TAG, "processFailure", e)
        repository.notify(SSEResultState.FAILURE, null)
        busy = false
        completeReadiness()
    }

    @Throws(IOException::class)
    private fun processResponse(response: Response) {
        busy = false

        // check the cache-control for the max-age
        response.header("cache-control")?.let {
            processCacheControlHeader(it)
        }
        // preserve the etag header if it exists
        response.header("etag")?.let {
            this.etag = it
        }

        response.body.use { body ->
            when {
                response.isSuccessful && body != null -> {
                    val environments = mapper.readValue(
                        body.bytes(),
                        object : TypeReference<List<FeatureEnvironmentCollection>>() {})!!
                    val states = kotlin.collections.ArrayList<FeatureState>()
                    environments.forEach { env ->
                        env.features?.let { fs ->
                            fs.forEach { f -> f.environmentId = env.id }
                        }
                        states.addAll(env.features)
                    }
                    repository.notify(states)
                    completeReadiness()
                    if (response.code == 236) {
                        // prevent any further requests
                        this.isStopped = true
                    }
                    // reset the polling interval to prevent unnecessary polling
                    if (pollingInterval > 0) {
                        whenPollingCacheExpires = now() + (pollingInterval * 1000)
                    }
                }

                response.code == 304 -> {
                    //数据没有变更
                    completeReadiness()
                    // reset the polling interval to prevent unnecessary polling
                    if (pollingInterval > 0) {
                        whenPollingCacheExpires = now() + (pollingInterval * 1000)
                    }
                }

                response.code == 400 || response.code == 404 -> {
                    makeRequests = false
                    Log.e(TAG, "processResponse: server indicated an error with 400 or 404")
                    repository.notify(SSEResultState.FAILURE, null)
                    completeReadiness()
                }

                else -> {
                    //其他响应码，不符合正常逻辑
                    val bodyString = body?.string() ?: "[empty body]"
                    Log.e(
                        TAG,
                        "processResponse: server response code: ${response.code}, body: $bodyString"
                    )
                    repository.notify(SSEResultState.ERROR, bodyString)
                    completeReadiness()
                }
            }
        }
    }

    val canMakeRequest: Boolean
        get() = makeRequests && !this.isStopped

    private fun completeReadiness() {
        val current = waitingClients
        waitingClients = kotlin.collections.ArrayList()
        current.forEach { c ->
            runCatching {
                c.complete(repository.readyness)
            }.onFailure { e ->
                Log.w(TAG, "completeReadiness failed", e)
            }
        }
    }

    override fun contextChange(newHeader: String?, contextSha: String): Future<Readyness?> {
        val change = CompletableFuture<Readyness>()

        headerChanged = (newHeader != null && newHeader != xFeaturehubHeader)

        xFeaturehubHeader = newHeader
        xContextSha = contextSha

        if (checkForUpdates() || busy) {
            waitingClients.add(change)
        } else {
            change.complete(repository.readyness)
        }

        return change
    }

    override fun isClientEvaluation(): Boolean {
        return clientSideEvaluation
    }

    override fun close() {
        Log.i(TAG, "feature hub client closed")
        makeRequests = false
        if (client is OkHttpClient) {
            client.dispatcher.executorService.shutdownNow()
        }
        executorService.shutdownNow()
    }

    override fun getConfig(): FeatureHubConfig = config

    override fun isRequiresReplacementOnHeaderChange(): Boolean = false

    override fun poll() {
        checkForUpdates()
    }
}