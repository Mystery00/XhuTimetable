package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.coroutines.flow.Flow
import vip.mystery0.xhu.timetable.ui.theme.XhuColor

@Composable
fun <T : Any> Flow<PagingData<T>>.collectAndHandleState(
    handleLoadStates: (LoadStates) -> Unit,
): LazyPagingItems<T> {
    val lazyPagingItem = collectAsLazyPagingItems()

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
    val layoutDirection = LocalLayoutDirection.current
    val bottomPadding = paddingValues.calculateBottomPadding()
    Box(
        Modifier
            .padding(
                top = paddingValues.calculateTopPadding(),
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection),
            )
            .fillMaxSize(),
    ) {
        val pullToRefreshState = rememberPullToRefreshState()
        LaunchedEffect(pager.loadState.refresh) {
            if (pager.loadState.refresh is LoadState.NotLoading) {
                pullToRefreshState.animateToHidden()
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
                contentPadding = PaddingValues(bottom = 24.dp + bottomPadding),
            ) {
                listHeader?.let {
                    stickyHeader { it() }
                }
                if (!alwaysShowList && !refreshing && pager.loadState.append !is LoadState.Loading && pager.itemCount == 0) {
                    item {
                        emptyState()
                    }
                } else {
                    listContent()
                    if (pager.itemCount > 0) {
                        when (pager.loadState.append) {
                            is LoadState.Loading -> {
                                item { BuildPageFooter(text = "正在加载更多…") }
                            }

                            is LoadState.Error -> {
                                item { BuildPageFooter(text = "加载失败，下拉刷新重试") }
                            }

                            is LoadState.NotLoading -> {
                                item { BuildPageFooter(text = "已经到底啦") }
                            }
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = bottomPadding),
        ) {
            boxContent()
        }
    }
}

@Composable
fun BuildPageFooter(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
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
    onClick: (() -> Unit)? = null,
    header: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit)? = null,
    footer: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(18.dp)
    val baseModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 6.dp)
        .clip(shape)
        .then(cardModifier)

    val cardContent: @Composable () -> Unit = {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            header?.let {
                ProvideTextStyle(
                    MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                ) {
                    header.invoke()
                }
                if (content != null) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
                }
            }
            content?.let {
                ProvideTextStyle(
                    MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                ) {
                    content.invoke()
                }
            }
            if (header != null && footer != null && content == null) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                )
            }
            footer?.let {
                if (content != null) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    )
                }
                ProvideTextStyle(
                    MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                ) {
                    footer.invoke()
                }
            }
        }
    }

    val cardColors = CardDefaults.cardColors(
        containerColor = XhuColor.cardBackground,
    )
    val border = BorderStroke(
        width = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
    )

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = baseModifier,
            shape = shape,
            colors = cardColors,
            border = border,
        ) {
            cardContent()
        }
    } else {
        Card(
            modifier = baseModifier,
            shape = shape,
            colors = cardColors,
            border = border,
        ) {
            cardContent()
        }
    }
}

@Composable
fun TextWithIcon(
    imageVector: ImageVector,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Icon(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(16.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = color.copy(alpha = 0.85f),
        )
        Text(
            text = text,
            color = color,
            style = LocalTextStyle.current,
        )
    }
}
