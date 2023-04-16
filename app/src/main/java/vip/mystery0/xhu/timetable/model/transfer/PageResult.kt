package vip.mystery0.xhu.timetable.model.transfer

data class PageResult<T>(
    val current: Int,
    val total: Long,
    val items: List<T>,
    val lastId: Long,
) {
    val isEmpty: Boolean
        get() = items.isEmpty()

    val hasNext: Boolean
        get() = current * items.size < total

    fun <R> emptyMap(): PageResult<R> {
        return PageResult(current, total, emptyList(), lastId)
    }

    fun <R> map(transform: (T) -> R): PageResult<R> {
        return PageResult(current, total, items.map(transform), lastId)
    }
}