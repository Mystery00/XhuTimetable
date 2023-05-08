package vip.mystery0.xhu.timetable.model.transfer

data class PageResult<T>(
    val current: Int,
    val total: Long,
    val items: List<T>,
    val hasNext: Boolean,
) {
    val isEmpty: Boolean
        get() = items.isEmpty()

    fun <R> emptyMap(): PageResult<R> {
        return PageResult(current, total, emptyList(), hasNext)
    }

    fun <R> map(transform: (T) -> R): PageResult<R> {
        return PageResult(current, total, items.map(transform), hasNext)
    }

    suspend fun <R> suspendMap(transform: suspend (T) -> R): PageResult<R> {
        return PageResult(current, total, items.map { transform(it) }, hasNext)
    }

    fun <R> flatMap(transform: (T) -> Iterable<R>): PageResult<R> {
        return PageResult(current, total, items.flatMap(transform), hasNext)
    }
}