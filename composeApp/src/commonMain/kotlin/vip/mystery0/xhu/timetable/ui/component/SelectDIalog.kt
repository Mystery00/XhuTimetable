package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import vip.mystery0.xhu.timetable.base.Selectable
import vip.mystery0.xhu.timetable.base.TermSelect
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.base.YearSelect
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuDialogState
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuMultiSelectDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuSingleSelectDialog

/**
 * 单行紧凑筛选条：用户筛选
 */
@Composable
fun BuildUserSelectFilterBar(
    userSelect: List<UserSelect>,
    onUserClick: () -> Unit,
    onRefresh: (suspend () -> Unit)? = null,
) {
    FilterChipBarContainer(onRefresh = onRefresh) {
        CompactFilterChip(
            label = userSelect.firstOrNull { it.selected }?.title ?: "加载中",
            onClick = onUserClick,
        )
    }
}

/**
 * 单行紧凑筛选条：用户 + 学年 + 学期
 */
@Composable
fun BuildSelectFilterBar(
    userSelect: List<UserSelect>,
    yearSelect: List<YearSelect>,
    termSelect: List<TermSelect>,
    onUserClick: () -> Unit,
    onYearClick: () -> Unit,
    onTermClick: () -> Unit,
    onRefresh: (suspend () -> Unit)? = null,
) {
    FilterChipBarContainer(onRefresh = onRefresh) {
        CompactFilterChip(
            label = userSelect.firstOrNull { it.selected }?.title ?: "加载中",
            onClick = onUserClick,
        )
        CompactFilterChip(
            label = yearSelect.firstOrNull { it.selected }?.title ?: "学年",
            onClick = onYearClick,
        )
        CompactFilterChip(
            label = termSelect.firstOrNull { it.selected }?.title ?: "学期",
            onClick = onTermClick,
        )
    }
}

@Composable
private fun CompactFilterChip(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 6.dp, top = 7.dp, bottom = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 112.dp),
            )
            Icon(
                imageVector = Icons.Rounded.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
private fun FilterChipBarContainer(
    modifier: Modifier = Modifier,
    onRefresh: (suspend () -> Unit)? = null,
    chips: @Composable RowScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                chips()
            }
            if (onRefresh != null) {
                IconButton(
                    onClick = {
                        scope.safeLaunch {
                            onRefresh()
                        }
                    },
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "刷新",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}

// =========================================================================
// 新一代纯 UI 层自研跨平台 XhuDialogState 选择对话框
// =========================================================================

@Composable
fun ShowUserDialog(
    selectList: List<UserSelect>,
    state: XhuDialogState,
    onSelect: (UserSelect) -> Unit,
) {
    ShowSingleSelectDialog("选择学生账号", selectList, state, onSelect)
}

@Composable
fun ShowYearDialog(
    selectList: List<YearSelect>,
    state: XhuDialogState,
    onSelect: (YearSelect) -> Unit,
) {
    ShowSingleSelectDialog("选择学年", selectList, state, onSelect)
}

@Composable
fun ShowTermDialog(
    selectList: List<TermSelect>,
    state: XhuDialogState,
    onSelect: (TermSelect) -> Unit,
) {
    ShowSingleSelectDialog("选择学期", selectList, state, onSelect)
}

@Composable
fun <T : Selectable> ShowSingleSelectDialog(
    dialogTitle: String,
    selectList: List<T>,
    state: XhuDialogState,
    onSelect: (T) -> Unit,
    withButtonView: Boolean = false,
) {
    XhuSingleSelectDialog(
        state = state,
        title = dialogTitle,
        options = selectList,
        selectedIndex = selectList.indexOfFirst { it.selected },
        itemLabel = { it.title },
        withConfirmButton = withButtonView,
        onSelect = { _, item -> onSelect(item) },
    )
}

@Composable
fun <T> ShowSingleSelectDialog(
    dialogTitle: String,
    options: List<T>,
    selectIndex: Int,
    itemTransform: (T) -> String = { it.toString() },
    state: XhuDialogState,
    onSelect: (Int, T) -> Unit = { _, _ -> },
    withButtonView: Boolean = false,
) {
    XhuSingleSelectDialog(
        state = state,
        title = dialogTitle,
        options = options,
        selectedIndex = selectIndex,
        itemLabel = itemTransform,
        withConfirmButton = withButtonView,
        onSelect = onSelect,
    )
}

@Composable
fun <T> ShowMultiSelectDialog(
    dialogTitle: String,
    options: List<T>,
    selectIndex: List<Int>,
    itemTransform: (T) -> String = { it.toString() },
    state: XhuDialogState,
    gridColumns: Int = 1,
    showQuickActions: Boolean = false,
    onSelect: (List<Int>, List<T>) -> Unit = { _, _ -> },
) {
    XhuMultiSelectDialog(
        state = state,
        title = dialogTitle,
        options = options,
        initialSelectedIndices = selectIndex,
        itemLabel = itemTransform,
        gridColumns = gridColumns,
        showQuickActions = showQuickActions,
        onConfirm = onSelect,
    )
}

// =========================================================================
// 旧 UseCaseState 兼容重载（保持现有外部引用零破坏）
// =========================================================================

@Composable
fun BuildUserSelectFilterChipContent(
    userSelect: List<UserSelect>,
    showUserDialog: UseCaseState,
    onDataLoad: suspend () -> Unit,
) {
    BuildUserSelectFilterBar(
        userSelect = userSelect,
        onUserClick = { showUserDialog.show() },
        onRefresh = onDataLoad,
    )
}

@Composable
fun BuildSelectFilterChipContent(
    userSelect: List<UserSelect>,
    yearSelect: List<YearSelect>,
    termSelect: List<TermSelect>,
    showUserDialog: UseCaseState,
    showYearDialog: UseCaseState,
    showTermDialog: UseCaseState,
    onDataLoad: suspend () -> Unit,
) {
    BuildSelectFilterBar(
        userSelect = userSelect,
        yearSelect = yearSelect,
        termSelect = termSelect,
        onUserClick = { showUserDialog.show() },
        onYearClick = { showYearDialog.show() },
        onTermClick = { showTermDialog.show() },
        onRefresh = onDataLoad,
    )
}

@Composable
fun ShowUserDialog(
    selectList: List<UserSelect>,
    useCaseState: UseCaseState,
    onSelect: (UserSelect) -> Unit,
) {
    ShowSingleSelectDialog("请选择要查询的学生", selectList, useCaseState, onSelect)
}

@Composable
fun ShowYearDialog(
    selectList: List<YearSelect>,
    useCaseState: UseCaseState,
    onSelect: (YearSelect) -> Unit,
) {
    ShowSingleSelectDialog("请选择要查询的学年", selectList, useCaseState, onSelect)
}

@Composable
fun ShowTermDialog(
    selectList: List<TermSelect>,
    useCaseState: UseCaseState,
    onSelect: (TermSelect) -> Unit,
) {
    ShowSingleSelectDialog("请选择要查询的学期", selectList, useCaseState, onSelect)
}

@Composable
fun <T> ShowSingleSelectDialog(
    dialogTitle: String,
    options: List<T>,
    selectIndex: Int,
    itemTransform: (T) -> String = { it.toString() },
    useCaseState: UseCaseState,
    onSelect: (Int, T) -> Unit = { _, _ -> },
    withButtonView: Boolean = true,
) {
    val selectList = options.mapIndexed { index, t ->
        SelectItem(title = itemTransform(t), selected = index == selectIndex)
    }
    val onValueSelect = { data: SelectItem ->
        val index = selectList.indexOf(data)
        onSelect(index, options[index])
    }
    ShowSingleSelectDialog(dialogTitle, selectList, useCaseState, onValueSelect, withButtonView)
}

@Immutable
private data class SelectItem(
    override val title: String,
    override val selected: Boolean,
) : Selectable

@Composable
fun <T : Selectable> ShowSingleSelectDialog(
    dialogTitle: String,
    selectList: List<T>,
    useCaseState: UseCaseState,
    onSelect: (T) -> Unit,
    withButtonView: Boolean = true,
) {
    ListDialog(
        header = xhuHeader(title = dialogTitle),
        state = useCaseState,
        selection = ListSelection.Single(
            withButtonView = withButtonView,
            options = selectList.map {
                ListOption(titleText = it.title, selected = it.selected)
            },
            onSelectOption = { index, _ -> onSelect(selectList[index]) },
        ),
    )
}
