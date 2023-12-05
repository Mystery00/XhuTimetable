package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun <T> ScrollPicker(
    items: List<T>,
    initialItem: T,
    modifier: Modifier = Modifier,
    itemTransformer: (T) -> String = { it.toString() },
    textStyle: TextStyle = LocalTextStyle.current,
    textColor: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    onItemSelected: (index: Int, item: T) -> Unit = { _, _ -> },
) {
    val scrollState = rememberLazyListState(0)
    var lastSelectedIndex by remember { mutableIntStateOf(0) }
    var itemsState by remember { mutableStateOf(items) }

    LaunchedEffect(initialItem) {
        val targetIndex = items.indexOf(initialItem)
        itemsState = items
        lastSelectedIndex = targetIndex
        scrollState.scrollToItem(targetIndex)
    }
    LazyColumn(
        modifier = modifier,
        state = scrollState,
        flingBehavior = rememberSnapFlingBehavior(lazyListState = scrollState)
    ) {
        items(items) {
            Box(
                modifier = Modifier
                    .fillParentMaxSize()
                    .onGloballyPositioned { coordinates ->
                        val y = coordinates.positionInParent().y
                        val parentHalfHeight = (coordinates.size.height / 2f)
                        val isSelected =
                            (y > parentHalfHeight - coordinates.size.height && y < parentHalfHeight + coordinates.size.height)
                        val index = itemsState.indexOf(it)
                        if (isSelected && lastSelectedIndex != index) {
                            onItemSelected(index, it)
                            lastSelectedIndex = index
                        }
                    },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = itemTransformer(it),
                    style = textStyle,
                    color = textColor,
                    fontSize = fontSize,
                )
            }
        }
    }
}