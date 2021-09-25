package vip.mystery0.xhu.timetable.model.response

data class PoemsToken(
    var data: String
)

data class PoemsSentence(
    val data: Poems,
)

data class Poems(
    val content: String,
    val origin: PoemsDetail,
)

data class PoemsDetail(
    val title: String,
    val dynasty: String,
    val author: String,
    val content: List<String>,
    val translate: List<String>? = null
)