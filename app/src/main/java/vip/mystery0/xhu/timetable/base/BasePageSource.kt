package vip.mystery0.xhu.timetable.base

import androidx.paging.PagingSource
import androidx.paging.PagingState
import vip.mystery0.xhu.timetable.model.transfer.PageResult

abstract class BasePageSource<T : Any> : PagingSource<Int, T>() {
    override fun getRefreshKey(state: PagingState<Int, T>): Int? =
        state.anchorPosition?.let {
            val anchorPage = state.closestPageToPosition(it)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, T> {
        val index = params.key ?: 0
        val response = loadPageData(index, params.loadSize)
        return LoadResult.Page(
            data = response.items,
            prevKey = null,
            nextKey = response.hasNext.let { if (it) index + 1 else null },
        )
    }

    abstract suspend fun loadPageData(index: Int, size: Int): PageResult<T>
}

fun <T : Any> buildPageSource(load: suspend (Int, Int) -> PageResult<T>): PagingSource<Int, T> =
    object : BasePageSource<T>() {
        override suspend fun loadPageData(index: Int, size: Int): PageResult<T> = load(index, size)
    }