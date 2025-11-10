package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import vip.mystery0.xhu.timetable.config.toast.showShortToast
import vip.mystery0.xhu.timetable.ui.theme.XhuColor


@Composable
fun <T : Any> Flow<PagingData<T>>.collectAndHandleState(
    handleLoadStates: (LoadStates) -> Unit,
): LazyPagingItems<T> {
    val lazyPagingItem = collectAsLazyPagingItems()

    // Handle the different load state event
    val pagingLoadStates = lazyPagingItem.loadState.mediator ?: lazyPagingItem.loadState.source
    LaunchedEffect(pagingLoadStates) {
        handleLoadStates(pagingLoadStates)
    }

    return lazyPagingItem
}

@Composable
fun <T : Any> BuildPaging(
    state: LazyListState = rememberLazyListState(),
    paddingValues: PaddingValues,
    pager: LazyPagingItems<T>,
    refreshing: Boolean,
    key: ((index: Int) -> Any)? = null,
    alwaysShowList: Boolean = false,
    listHeader: (@Composable LazyItemScope.() -> Unit)? = null,
    itemContent: @Composable LazyItemScope.(T) -> Unit,
    emptyState: @Composable LazyItemScope.() -> Unit = {},
    boxContent: @Composable BoxScope.() -> Unit = {},
) = BuildPaging(
    state = state,
    paddingValues = paddingValues,
    pager = pager,
    refreshing = refreshing,
    listHeader = listHeader,
    listContent = {
        itemsIndexed(pager, key = key) { item ->
            itemContent(item)
        }
    },
    alwaysShowList = alwaysShowList,
    emptyState = emptyState,
    boxContent = boxContent,
)

@Composable
fun <T : Any> BuildPaging(
    state: LazyListState = rememberLazyListState(),
    paddingValues: PaddingValues,
    pager: LazyPagingItems<T>,
    refreshing: Boolean,
    listHeader: (@Composable LazyItemScope.() -> Unit)? = null,
    listContent: LazyListScope.() -> Unit,
    alwaysShowList: Boolean = false,
    emptyState: @Composable LazyItemScope.() -> Unit = {},
    boxContent: @Composable BoxScope.() -> Unit = {},
) {
    Box(
        Modifier
            .padding(paddingValues)
            .fillMaxSize(),
    ) {
        val pullToRefreshState = rememberPullToRefreshState()
        LaunchedEffect(pager.loadState.refresh) {
            if (pager.loadState.refresh is LoadState.NotLoading) {
                pullToRefreshState.animateToHidden()
                showShortToast("数据加载完成！")
            }
        }
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = {
                pager.refresh()
            },
            state = pullToRefreshState,
            indicator = {
                PullToRefreshDefaults.LoadingIndicator(
                    state = pullToRefreshState,
                    isRefreshing = refreshing,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        ) {
            LazyColumn(
                state = state,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                listHeader?.let {
                    stickyHeader { it() }
                }
                if (!alwaysShowList && pager.loadState.append !is LoadState.Loading && pager.itemCount == 0) {
                    item {
                        emptyState()
                    }
                } else {
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
            }
        }
        boxContent()
    }
}

@Composable
fun BuildPageFooter(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, fontSize = 14.sp, color = MaterialTheme.colorScheme.outline)
    }
}

fun <T : Any> LazyListScope.itemsIndexed(
    items: LazyPagingItems<T>,
    key: ((index: Int) -> Any)? = null,
    itemContent: @Composable LazyItemScope.(value: T) -> Unit
) {
    items(
        count = items.itemCount,
        key = key,
        contentType = { null }
    ) { index ->
        val item = items[index]
        if (item != null) {
            itemContent(item)
        }
    }
}

@Composable
fun LazyListState.isScrollingUp(): Boolean {
    var previousIndex by remember(this) { mutableIntStateOf(firstVisibleItemIndex) }
    var previousScrollOffset by remember(this) { mutableIntStateOf(firstVisibleItemScrollOffset) }
    return remember(this) {
        derivedStateOf {
            if (previousIndex != firstVisibleItemIndex) {
                previousIndex > firstVisibleItemIndex
            } else {
                previousScrollOffset >= firstVisibleItemScrollOffset
            }.also {
                previousIndex = firstVisibleItemIndex
                previousScrollOffset = firstVisibleItemScrollOffset
            }
        }
    }.value
}

@Composable
fun PageItemLayout(
    cardModifier: Modifier = Modifier,
    header: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = cardModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            header?.let {
                ProvideTextStyle(
                    LocalTextStyle.current.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                    )
                ) {
                    header.invoke()
                }
                if (content != null) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp))
                }
            }
            content?.invoke()
            if (header != null && footer != null && content == null) {
                HorizontalDivider(modifier = Modifier.fillMaxWidth())
            }
            footer?.let {
                if (content != null) {
                    HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(top = 4.dp))
                }
                ProvideTextStyle(
                    LocalTextStyle.current.copy(
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 12.sp,
                    )
                ) {
                    footer.invoke()
                }
            }
        }
    }
}

@Composable
fun TextWithIcon(
    imageVector: ImageVector,
    text: String,
    color: Color = MaterialTheme.colorScheme.outline,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = color,
        )
        Text(
            text = text,
            color = color,
        )
    }
}