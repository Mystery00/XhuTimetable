package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.Selectable
import vip.mystery0.xhu.timetable.base.TermSelect
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.base.YearSelect
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

@Composable
fun BuildUserSelectFilterChipContent(
    userSelect: List<UserSelect>,
    showUserDialog: UseCaseState,
    onDataLoad: suspend () -> Unit,
) {
    BuildSelectBaseContent(onDataLoad = onDataLoad) {
        ElevatedFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            selected = true,
            onClick = {
                showUserDialog.show()
            },
            label = {
                val userString = userSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = userString)
            },
        )
    }
}

@Composable
fun BuildSelectFilterChipContentOnlyUser(
    userSelect: List<UserSelect>,
    showUserDialog: UseCaseState,
    onDataLoad: suspend () -> Unit,
) {
    BuildSelectBaseContent(onDataLoad = onDataLoad) {
        ElevatedFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            selected = true,
            onClick = {
                showUserDialog.show()
            },
            label = {
                val userString = userSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = userString)
            },
        )
    }
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
    BuildSelectBaseContent(onDataLoad = onDataLoad) {
        ElevatedFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            selected = true,
            onClick = {
                showUserDialog.show()
            },
            label = {
                val userString = userSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = userString)
            },
        )
        ElevatedFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            selected = true,
            onClick = {
                showYearDialog.show()
            },
            label = {
                val yearString = yearSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = yearString)
            },
        )
        ElevatedFilterChip(
            modifier = Modifier.align(Alignment.CenterVertically),
            selected = true,
            onClick = {
                showTermDialog.show()
            },
            label = {
                val termString = termSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = termString)
            },
        )
    }
}

@Composable
private fun BuildSelectBaseContent(
    modifier: Modifier = Modifier,
    onDataLoad: suspend () -> Unit,
    chips: @Composable FlowRowScope.() -> Unit,
) {
    val scope = rememberCoroutineScope()
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp),
    ) {
        chips()
        OutlinedButton(
            modifier = Modifier
                .height(32.dp)
                .align(Alignment.CenterVertically),
            onClick = {
                scope.launch {
                    onDataLoad()
                }
            },
            contentPadding = PaddingValues(0.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Icon(
                painter = XhuIcons.Action.search,
                contentDescription = null,
            )
        }
    }
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
        SelectItem(
            title = itemTransform(t),
            selected = index == selectIndex,
        )
    }
    val onValueSelect = { data: SelectItem ->
        val index = selectList.indexOf(data)
        onSelect(index, options[index])
    }
    ShowSingleSelectDialog(dialogTitle, selectList, useCaseState, onValueSelect, withButtonView)
}

@Composable
fun <T> ShowMultiSelectDialog(
    dialogTitle: String,
    options: List<T>,
    selectIndex: List<Int>,
    itemTransform: (T) -> String = { it.toString() },
    useCaseState: UseCaseState,
    onSelect: (List<Int>, List<T>) -> Unit = { _, _ -> },
    withButtonView: Boolean = true,
) {
    val selectList = options.mapIndexed { index, t ->
        SelectItem(
            title = itemTransform(t),
            selected = selectIndex.contains(index),
        )
    }
    val onValueSelect = { data: List<SelectItem> ->
        val indexList = data.map { selectList.indexOf(it) }
        onSelect(indexList, indexList.map { options[it] })
    }
    ShowMultiSelectDialog(dialogTitle, selectList, useCaseState, onValueSelect, withButtonView)
}

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
                ListOption(
                    titleText = it.title,
                    selected = it.selected,
                )
            },
            onSelectOption = { index, _ ->
                onSelect(selectList[index])
            },
        ),
    )
}

@Composable
fun <T : Selectable> ShowMultiSelectDialog(
    dialogTitle: String,
    selectList: List<T>,
    useCaseState: UseCaseState,
    onSelect: (List<T>) -> Unit,
    withButtonView: Boolean = true,
) {
    ListDialog(
        header = xhuHeader(title = dialogTitle),
        state = useCaseState,
        selection = ListSelection.Multiple(
            showCheckBoxes = true,
            withButtonView = withButtonView,
            options = selectList.map {
                ListOption(
                    titleText = it.title,
                    selected = it.selected,
                )
            },
            onSelectOptions = { indexList, _ ->
                onSelect(indexList.map { selectList[it] })
            },
        ),
    )
}