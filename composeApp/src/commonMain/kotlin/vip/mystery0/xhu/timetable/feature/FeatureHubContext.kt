package vip.mystery0.xhu.timetable.feature

import io.ktor.http.encodeURLParameter

@ConsistentCopyVisibility
data class FeatureHubContext internal constructor(
    val userKey: String? = null,
    val device: String? = null,
    val platform: String? = null,
    val version: String? = null,
    val customAttributes: Map<String, String> = emptyMap()
) {
    fun buildFeatureHeader(): String {
        val map = HashMap<String, List<String>>()
        userKey?.let { map["userkey"] = listOf(it) }
        device?.let { map["device"] = listOf(it) }
        platform?.let { map["platform"] = listOf(it) }
        version?.let { map["version"] = listOf(it) }
        customAttributes.forEach { (key, value) -> map[key] = listOf(value) }
        return map
            .map { (key, value) ->
                "$key=${
                    value.joinToString(",").encodeURLParameter(spaceToPlus = true)
                }"
            }
            .sorted()
            .joinToString(",")
    }
}

class ContextBuilder {
    private var userKey: String? = null
    private var device: String? = null
    private var platform: String? = null
    private var version: String? = null
    private val customAttributes = mutableMapOf<String, String>()

    fun userKey(key: String) = apply { this.userKey = key }
    fun device(device: String) = apply { this.device = device }
    fun platform(platform: String) = apply { this.platform = platform }
    fun version(version: String) = apply { this.version = version }
    fun attr(key: String, value: String) = apply { this.customAttributes[key] = value }

    fun build(): FeatureHubContext {
        return FeatureHubContext(userKey, device, platform, version, customAttributes)
    }
}