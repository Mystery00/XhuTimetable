package vip.mystery0.xhu.timetable.base

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import vip.mystery0.xhu.timetable.ui.theme.XhuColor

abstract class BasePageComposeActivity(
    setSystemUiColor: Boolean = true,
    registerEventBus: Boolean = false,
) : BaseComposeActivity(setSystemUiColor, registerEventBus) {

    @Composable
    protected fun <T : Any> BuildPaging(
        paddingValues: PaddingValues,
        pager: LazyPagingItems<T>,
        refreshing: Boolean,
        itemContent: @Composable LazyItemScope.(T) -> Unit,
        boxContent: @Composable BoxScope.() -> Unit = {},
    ) = BuildPaging(
        paddingValues = paddingValues,
        pager = pager,
        refreshing = refreshing,
        listContent = {
            itemsIndexed(pager) { item ->
                itemContent(item)
            }
        },
        boxContent = boxContent,
    )

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    protected fun <T : Any> BuildPaging(
        paddingValues: PaddingValues,
        pager: LazyPagingItems<T>,
        refreshing: Boolean,
        listContent: LazyListScope.() -> Unit,
        boxContent: @Composable BoxScope.() -> Unit = {},
    ) {
        Box(
            Modifier
                .padding(paddingValues)
                .fillMaxSize(),
        ) {
            val pullRefreshState = rememberPullRefreshState(
                refreshing = refreshing,
                onRefresh = { pager.refresh() },
            )
            Box(
                modifier = Modifier.pullRefresh(pullRefreshState),
            ) {
                if (pager.itemCount == 0) {
                    BuildNoDataLayout()
                }
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(XhuColor.Common.grayBackground),
                    contentPadding = PaddingValues(4.dp),
                ) {
                    listContent()
                    when (pager.loadState.append) {
                        is LoadState.Loading -> {
                            item { BuildPageFooter(text = "数据加载中，请稍后……") }
                        }

                        is LoadState.Error -> {
                            item { BuildPageFooter(text = "数据加载失败，请重试") }
                        }

                        is LoadState.NotLoading -> {
                            item { BuildPageFooter(text = "o(´^｀)o 再怎么滑也没有啦~") }
                        }
                    }
                }
                PullRefreshIndicator(
                    refreshing = refreshing,
                    state = pullRefreshState,
                    Modifier.align(Alignment.TopCenter),
                )
            }
            boxContent()
        }
    }

    protected fun <T : Any> LazyListScope.itemsIndexed(
        items: LazyPagingItems<T>,
        itemContent: @Composable LazyItemScope.(value: T) -> Unit
    ) {
        items(
            count = items.itemCount,
            key = null,
            contentType = { null }
        ) { index ->
            val item = items[index]
            if (item != null) {
                itemContent(item)
            }
        }
    }
}