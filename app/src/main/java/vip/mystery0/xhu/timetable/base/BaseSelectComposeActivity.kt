package vip.mystery0.xhu.timetable.base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.AlertDialog
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.OutlinedButton
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

abstract class BaseSelectComposeActivity : BaseComposeActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    protected fun BuildSelectBackLayerContent(
        scaffoldState: BackdropScaffoldState,
        selectUserState: State<List<UserSelect>>,
        selectYearState: State<List<YearSelect>>,
        selectTermState: State<List<TermSelect>>,
        showUserDialog: MutableState<Boolean>,
        showYearDialog: MutableState<Boolean>,
        showTermDialog: MutableState<Boolean>,
        onDataLoad: suspend () -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                    modifier = Modifier.weight(1F),
                    onClick = {
                        showUserDialog.value = true
                    }) {
                    val userSelect = selectUserState.value
                    val userString = userSelect.firstOrNull { it.selected }?.title ?: "查询中"
                    Text(text = "查询用户：$userString")
                }
                Button(
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                    onClick = {
                        scope.launch {
                            onDataLoad()
                            scaffoldState.conceal()
                        }
                    }) {
                    Icon(painter = XhuIcons.CustomCourse.pull, contentDescription = null)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                    modifier = Modifier.weight(1F),
                    onClick = {
                        showYearDialog.value = true
                    }) {
                    val yearSelect = selectYearState.value
                    val yearString =
                        yearSelect.firstOrNull { it.selected }?.title ?: "查询中"
                    Text(text = yearString)
                }
                OutlinedButton(
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                    modifier = Modifier.weight(1F),
                    onClick = {
                        showTermDialog.value = true
                    }) {
                    val termSelect = selectTermState.value
                    val termString =
                        termSelect.firstOrNull { it.selected }?.title ?: "查询中"
                    Text(text = termString)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    protected fun BuildSelectSheetLayout(
        bottomSheetState: ModalBottomSheetState,
        selectUserState: State<List<UserSelect>>,
        selectYearState: State<List<YearSelect>>,
        selectTermState: State<List<TermSelect>>,
        showUserDialog: MutableState<Boolean>,
        showYearDialog: MutableState<Boolean>,
        showTermDialog: MutableState<Boolean>,
        onDataLoad: suspend () -> Unit,
    ) {
        val scope = rememberCoroutineScope()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(text = "请选择需要查询的信息")
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showUserDialog.value = true
                }) {
                val userSelect = selectUserState.value
                val userString = userSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = "查询用户：$userString")
            }
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showYearDialog.value = true
                }) {
                val yearSelect = selectYearState.value
                val yearString =
                    yearSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = yearString)
            }
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    showTermDialog.value = true
                }) {
                val termSelect = selectTermState.value
                val termString =
                    termSelect.firstOrNull { it.selected }?.title ?: "查询中"
                Text(text = termString)
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        onDataLoad()
                        bottomSheetState.hide()
                    }
                }) {
                Text(text = "查询")
            }
        }
    }

    @Composable
    protected fun ShowUserDialog(
        selectState: State<List<UserSelect>>,
        show: MutableState<Boolean>,
        onSelect: (UserSelect) -> Unit,
    ) {
        ShowSelectDialog("请选择要查询的学生", selectState, show, onSelect)
    }

    @Composable
    protected fun ShowYearDialog(
        selectState: State<List<YearSelect>>,
        show: MutableState<Boolean>,
        onSelect: (YearSelect) -> Unit,
    ) {
        ShowSelectDialog("请选择要查询的学年", selectState, show, onSelect)
    }

    @Composable
    protected fun ShowTermDialog(
        selectState: State<List<TermSelect>>,
        show: MutableState<Boolean>,
        onSelect: (TermSelect) -> Unit,
    ) {
        ShowSelectDialog("请选择要查询的学期", selectState, show, onSelect)
    }

    @Composable
    private fun <T : Selectable> ShowSelectDialog(
        dialogTitle: String,
        selectState: State<List<T>>,
        show: MutableState<Boolean>,
        onSelect: (T) -> Unit,
    ) {
        val select = selectState.value
        val selectedValue = select.firstOrNull { it.selected } ?: return
        var selected by remember { mutableStateOf(selectedValue) }
        if (show.value) {
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = dialogTitle)
                },
                text = {
                    Column {
                        LazyColumn {
                            items(select.size) { index ->
                                val item = select[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                        ) {
                                            selected = item
                                        },
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(selected = selected == item, onClick = null)
                                    Text(text = item.title)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onSelect(selected)
                            show.value = false
                        },
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            show.value = false
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        }
    }
}