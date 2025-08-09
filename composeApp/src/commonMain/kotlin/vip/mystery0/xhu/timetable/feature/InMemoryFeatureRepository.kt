package vip.mystery0.xhu.timetable.feature

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 一个简单的内存存储库，用于保存从服务器获取的特征标志。
 * 使用 StateFlow 以便 Compose UI 可以观察其变化。
 */
internal class InMemoryFeatureRepository {
    // 使用 MutableStateFlow 来存储特征标志，键为 feature key，值为 Feature 对象
    private val _features = MutableStateFlow<Map<String, Feature>>(emptyMap())
    val features: StateFlow<Map<String, Feature>> = _features.asStateFlow()

    /**
     * 更新内存中的特征标志列表。
     * @param newFeatures 从服务器获取的最新特征列表。
     */
    fun updateFeatures(newFeatures: List<Feature>) {
        _features.value = newFeatures.associateBy { it.key }
    }

    /**
     * 根据 key 获取单个特征。
     */
    fun getFeature(key: String): Feature? {
        return _features.value[key]
    }
}