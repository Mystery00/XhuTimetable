package vip.mystery0.xhu.timetable.model.response

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable

@Serializable
data class PoemsToken(
    var data: String
)

@Serializable
data class PoemsSentence(
    val data: Poems,
)

@Immutable
@Serializable
data class Poems(
    val content: String,
    val origin: PoemsDetail,
)

@Serializable
data class PoemsDetail(
    val title: String,
    val dynasty: String,
    val author: String,
    val content: List<String>,
    var translate: List<String>? = null
)