package vip.mystery0.xhu.timetable.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.ui.component.XhuDialogState
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

abstract class BaseSelectComposeActivity : BasePageComposeActivity() {
    @Composable
    protected fun BuildUserSelectStickyHeaderContent(
        userSelect: List<UserSelect>,
        showUserDialog: XhuDialogState,
        onDataLoad: suspend () -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1F),
                onClick = {
                    showUserDialog.show()
                }) {
                val userString = userSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = "查询用户：$userString")
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                onClick = {
                    scope.launch {
                        onDataLoad()
                    }
                }) {
                Icon(painter = XhuIcons.CustomCourse.pull, contentDescription = null)
            }
        }
    }

    @Composable
    protected fun BuildYearAndTermSelectStickyHeaderContent(
        yearSelect: List<YearSelect>,
        termSelect: List<TermSelect>,
        showYearDialog: XhuDialogState,
        showTermDialog: XhuDialogState,
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1F),
                onClick = {
                    showYearDialog.show()
                }) {
                val yearString =
                    yearSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = yearString)
            }
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                modifier = Modifier.weight(1F),
                onClick = {
                    showTermDialog.show()
                }) {
                val termString =
                    termSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = termString)
            }
        }
    }

    @Composable
    protected fun BuildSelectStickyHeaderContent(
        userSelect: List<UserSelect>,
        yearSelect: List<YearSelect>,
        termSelect: List<TermSelect>,
        showUserDialog: XhuDialogState,
        showYearDialog: XhuDialogState,
        showTermDialog: XhuDialogState,
        onDataLoad: suspend () -> Unit,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BuildUserSelectStickyHeaderContent(
                userSelect = userSelect,
                showUserDialog = showUserDialog,
                onDataLoad = onDataLoad,
            )
            BuildYearAndTermSelectStickyHeaderContent(
                yearSelect = yearSelect,
                termSelect = termSelect,
                showYearDialog = showYearDialog,
                showTermDialog = showTermDialog,
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    protected fun BuildSelectSheetLayout(
        showBottomSheet: XhuDialogState,
        userSelect: List<UserSelect>,
        yearSelect: List<YearSelect>,
        termSelect: List<TermSelect>,
        showUserDialog: XhuDialogState,
        showYearDialog: XhuDialogState,
        showTermDialog: XhuDialogState,
        onDataLoad: suspend () -> Unit,
    ) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val scope = rememberCoroutineScope()

        if (showBottomSheet.showing) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet.hide()
                },
                sheetState = sheetState,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            showUserDialog.show()
                        }) {
                        val userString = userSelect.firstOrNull { it.selected }?.title ?: "查询中"
                        Text(text = "查询用户：$userString")
                    }
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                            modifier = Modifier.weight(1F),
                            onClick = {
                                showYearDialog.show()
                            }) {
                            val yearString =
                                yearSelect.firstOrNull { it.selected }?.title ?: "查询中"
                            Text(text = yearString)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                            modifier = Modifier.weight(1F),
                            onClick = {
                                showTermDialog.show()
                            }) {
                            val termString =
                                termSelect.firstOrNull { it.selected }?.title ?: "查询中"
                            Text(text = termString)
                        }
                    }
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            scope.launch {
                                onDataLoad()
                                showBottomSheet.hide()
                            }
                        }) {
                        Text(text = "查询")
                    }
                    Spacer(modifier = Modifier.heightIn(min = 16.dp, max = 24.dp))
                }
            }
        }
    }

    @Composable
    protected fun ShowUserDialog(
        selectList: List<UserSelect>,
        show: XhuDialogState,
        onSelect: (UserSelect) -> Unit,
    ) {
        ShowSelectDialog("请选择要查询的学生", selectList, show, onSelect)
    }

    @Composable
    protected fun ShowYearDialog(
        selectList: List<YearSelect>,
        show: XhuDialogState,
        onSelect: (YearSelect) -> Unit,
    ) {
        ShowSelectDialog("请选择要查询的学年", selectList, show, onSelect)
    }

    @Composable
    protected fun ShowTermDialog(
        selectList: List<TermSelect>,
        show: XhuDialogState,
        onSelect: (TermSelect) -> Unit,
    ) {
        ShowSelectDialog("请选择要查询的学期", selectList, show, onSelect)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    protected fun <T : Selectable> ShowSelectDialog(
        dialogTitle: String,
        selectList: List<T>,
        show: XhuDialogState,
        onSelect: (T) -> Unit,
        withButtonView: Boolean = true,
    ) {
        if (show.showing) {
            ListDialog(
                header = Header.Default(title = dialogTitle),
                state = rememberUseCaseState(
                    visible = true,
                    onCloseRequest = { show.hide() }),
                selection = ListSelection.Single(
                    withButtonView = withButtonView,
                    options = selectList.map {
                        ListOption(
                            titleText = it.title,
                            selected = it.selected,
                        )
                    },
                ) { index, _ ->
                    onSelect(selectList[index])
                },
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    protected fun <T> ShowSelectDialog(
        dialogTitle: String,
        options: List<T>,
        selectIndex: Int,
        itemTransform: (T) -> String = { it.toString() },
        state: XhuDialogState = rememberXhuDialogState(),
        onSelect: (Int, T) -> Unit = { _, _ -> },
        withButtonView: Boolean = true,
    ) {
        if (state.showing) {
            ListDialog(
                header = Header.Default(title = dialogTitle),
                state = rememberUseCaseState(
                    visible = true,
                    onCloseRequest = { state.hide() }),
                selection = ListSelection.Single(
                    withButtonView = withButtonView,
                    options = options.mapIndexed { index, item ->
                        ListOption(
                            titleText = itemTransform(item),
                            selected = index == selectIndex,
                        )
                    },
                ) { index, _ ->
                    onSelect(index, options[index])
                },
            )
        }
    }
}