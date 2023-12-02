package vip.mystery0.xhu.timetable.base

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
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
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

abstract class BaseSelectComposeActivity : BasePageComposeActivity() {
    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    protected fun BuildUserSelectBackLayerContent(
        scaffoldState: BackdropScaffoldState,
        selectUserState: State<List<UserSelect>>,
        showUserDialog: MutableState<Boolean>,
        onDataLoad: suspend () -> Unit,
    ) {
        val scope = rememberCoroutineScope()
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                onClick = {
                    scope.launch {
                        onDataLoad()
                        scaffoldState.conceal()
                    }
                }) {
                Icon(painter = XhuIcons.CustomCourse.pull, contentDescription = null)
            }
        }
    }

    @Composable
    protected fun BuildYearAndTermSelectBackLayerContent(
        selectYearState: State<List<YearSelect>>,
        selectTermState: State<List<TermSelect>>,
        showYearDialog: MutableState<Boolean>,
        showTermDialog: MutableState<Boolean>,
    ) {
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
        content: @Composable ColumnScope.() -> Unit = {
            BuildUserSelectBackLayerContent(
                scaffoldState = scaffoldState,
                selectUserState = selectUserState,
                showUserDialog = showUserDialog,
                onDataLoad = onDataLoad,
            )
            BuildYearAndTermSelectBackLayerContent(
                selectYearState = selectYearState,
                selectTermState = selectTermState,
                showYearDialog = showYearDialog,
                showTermDialog = showTermDialog,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
        )
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

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    protected fun <T : Selectable> ShowSelectDialog(
        dialogTitle: String,
        selectState: State<List<T>>,
        show: MutableState<Boolean>,
        onSelect: (T) -> Unit,
    ) {
        val select = selectState.value
        if (show.value) {
            ListDialog(
                header = Header.Default(title = dialogTitle),
                state = rememberUseCaseState(
                    visible = true,
                    onCloseRequest = { show.value = false }),
                selection = ListSelection.Single(
                    options = select.map {
                        ListOption(
                            titleText = it.title,
                            selected = it.selected,
                        )
                    },
                ) { index, _ ->
                    onSelect(select[index])
                },
            )
        }
    }
}