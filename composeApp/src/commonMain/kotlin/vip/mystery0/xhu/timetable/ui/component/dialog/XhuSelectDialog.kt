package vip.mystery0.xhu.timetable.ui.component.dialog

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/**
 * 跨平台单选对话框：支持即点即选或确认按钮提交
 */
@Composable
fun <T> XhuSingleSelectDialog(
    state: XhuDialogState,
    title: String,
    options: List<T>,
    selectedIndex: Int = -1,
    itemLabel: (T) -> String = { it.toString() },
    itemSubtitle: ((T) -> String?)? = null,
    subtitle: String? = null,
    withConfirmButton: Boolean = false,
    extraButtonText: String? = null,
    onExtraButtonClick: (() -> Unit)? = null,
    onSelect: (index: Int, item: T) -> Unit,
) {
    if (!state.visible || options.isEmpty()) return

    var currentSelection by remember(state.visible, options, selectedIndex) {
        mutableIntStateOf(selectedIndex)
    }

    XhuDialog(
        state = state,
        title = title,
        subtitle = subtitle,
        extraButtonText = extraButtonText,
        onExtraButtonClick = onExtraButtonClick,
        dismissButtonText = if (withConfirmButton) "取消" else "关闭",
        confirmButtonText = if (withConfirmButton) "确定" else null,
        confirmEnabled = currentSelection in options.indices,
        onConfirmButtonClick = if (withConfirmButton) {
            {
                if (currentSelection in options.indices) {
                    onSelect(currentSelection, options[currentSelection])
                    state.hide()
                }
            }
        } else null,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 380.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            itemsIndexed(options) { index, item ->
                val isSelected = index == currentSelection
                val containerColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    },
                    label = "singleSelectBg",
                )
                val borderColor by animateColorAsState(
                    targetValue = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                    },
                    label = "singleSelectBorder",
                )

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = containerColor,
                    border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            currentSelection = index
                            if (!withConfirmButton) {
                                onSelect(index, item)
                                state.hide()
                            }
                        },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        if (withConfirmButton) {
                            RadioButton(
                                selected = isSelected,
                                onClick = null,
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = itemLabel(item),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                            val sub = itemSubtitle?.invoke(item)
                            if (!sub.isNullOrBlank()) {
                                Text(
                                    text = sub,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            }
                        }
                        if (!withConfirmButton && isSelected) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 跨平台多选对话框：支持列表模式与网格模式，可选快捷选择芯片（全选/单周/双周/清空）
 */
@Composable
fun <T> XhuMultiSelectDialog(
    state: XhuDialogState,
    title: String,
    options: List<T>,
    initialSelectedIndices: Collection<Int>,
    itemLabel: (T) -> String = { it.toString() },
    subtitle: String? = null,
    gridColumns: Int = 1,
    showQuickActions: Boolean = false,
    allowEmpty: Boolean = false,
    onConfirm: (selectedIndices: List<Int>, selectedItems: List<T>) -> Unit,
) {
    if (!state.visible || options.isEmpty()) return

    val selectedIndices = remember(state.visible, options) {
        mutableStateListOf<Int>().apply {
            addAll(initialSelectedIndices.filter { it in options.indices }.distinct())
        }
    }

    LaunchedEffect(state.visible, initialSelectedIndices) {
        if (state.visible) {
            selectedIndices.clear()
            selectedIndices.addAll(initialSelectedIndices.filter { it in options.indices }.distinct())
        }
    }

    XhuDialog(
        state = state,
        title = title,
        subtitle = subtitle ?: "已选择 ${selectedIndices.size} 项",
        dismissButtonText = "取消",
        confirmButtonText = "确定",
        confirmEnabled = allowEmpty || selectedIndices.isNotEmpty(),
        onConfirmButtonClick = {
            val sorted = selectedIndices.sorted()
            onConfirm(sorted, sorted.map { options[it] })
            state.hide()
        },
    ) {
        if (showQuickActions) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = selectedIndices.size == options.size,
                    onClick = {
                        selectedIndices.clear()
                        selectedIndices.addAll(options.indices)
                    },
                    label = { Text("全选") },
                )
                if (options.size >= 4) {
                    FilterChip(
                        selected = false,
                        onClick = {
                            selectedIndices.clear()
                            selectedIndices.addAll(options.indices.filter { it % 2 == 0 })
                        },
                        label = { Text("单数") },
                    )
                    FilterChip(
                        selected = false,
                        onClick = {
                            selectedIndices.clear()
                            selectedIndices.addAll(options.indices.filter { it % 2 == 1 })
                        },
                        label = { Text("双数") },
                    )
                }
                FilterChip(
                    selected = false,
                    onClick = { selectedIndices.clear() },
                    label = { Text("清空") },
                )
            }
        }

        if (gridColumns > 1) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridColumns),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                itemsIndexed(options) { index, item ->
                    val isSelected = selectedIndices.contains(index)
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        },
                        label = "multiGridBg",
                    )
                    val borderColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        },
                        label = "multiGridBorder",
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, borderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                if (isSelected) {
                                    selectedIndices.remove(index)
                                } else {
                                    selectedIndices.add(index)
                                }
                            },
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = itemLabel(item),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 360.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
            ) {
                itemsIndexed(options) { index, item ->
                    val isSelected = selectedIndices.contains(index)
                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                        } else {
                            MaterialTheme.colorScheme.surfaceContainerLowest
                        },
                        label = "multiListBg",
                    )

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = containerColor,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                if (isSelected) {
                                    selectedIndices.remove(index)
                                } else {
                                    selectedIndices.add(index)
                                }
                            },
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = null,
                            )
                            Text(
                                text = itemLabel(item),
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                },
                                modifier = Modifier.weight(1f),
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
