package vip.mystery0.xhu.timetable.model.response

import kotlinx.serialization.Serializable

@Serializable
data class PoemsToken(
    var data: String
)

@Serializable
data class PoemsSentence(
    val data: Poems,
)

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