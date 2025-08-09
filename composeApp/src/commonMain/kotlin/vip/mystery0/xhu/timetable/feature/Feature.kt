package vip.mystery0.xhu.timetable.feature

import kotlinx.serialization.Serializable

@Serializable
data class FeatureList(
    val features: List<Feature>,
)

/**
 * 代表从 Feature Hub API 返回的单个特征标志的数据结构。
 * 使用 @Serializable 注解以便 Ktor 进行自动 JSON 解析。
 */
@Serializable
data class Feature(
    val key: String,
    val version: Int,
    val type: String,
    // value 可以是布尔值、数字或JSON字符串，这里统一用可空String接收
    val value: String? = null
) {
    /**
     * 便捷方法，判断此特征是否为启用状态。
     * 注意：这里做了简化处理，仅判断 value 是否为 "true"。
     * 在实际生产中，您可能需要根据 `type` 字段进行更复杂的类型转换。
     */
    fun isEnabled(): Boolean {
        return value.toBoolean()
    }
}