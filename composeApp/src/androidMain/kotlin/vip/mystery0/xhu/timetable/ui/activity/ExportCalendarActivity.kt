package vip.mystery0.xhu.timetable.ui.activity

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.androidx.viewmodel.ext.android.viewModel
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.base.appName
import vip.mystery0.xhu.timetable.model.CalendarAccount
import vip.mystery0.xhu.timetable.ui.component.ShowProgressDialog
import vip.mystery0.xhu.timetable.ui.component.ShowSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.xhuHeader
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import vip.mystery0.xhu.timetable.viewmodel.ExportCalendarViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data
import xhutimetable.composeapp.generated.resources.state_no_permission

class ExportCalendarActivity : ComponentActivity() {
    private val viewModel: ExportCalendarViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        setContent {
            XhuTimetableTheme {
                BuildContent()
            }
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    private fun BuildContent() {
        val calendarPermissionState = rememberMultiplePermissionsState(
            listOf(
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR,
            )
        )

        if (calendarPermissionState.allPermissionsGranted) {
            viewModel.loadCalendarAccountList()
        }

        val openBottomSheet = rememberSaveable { mutableStateOf(false) }

        val scope = rememberCoroutineScope()
        val exportLoadingDialog = rememberUseCaseState()

        LaunchedEffect(Unit) {
            viewModel.init()
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = { Text(text = "日历导出管理") },
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
                        }) {
                            Icon(
                                painter = XhuIcons.back,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            val calendarAccountListState by viewModel.calendarAccountListState.collectAsState()

            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            ) {
                if (calendarPermissionState.allPermissionsGranted) {
                    PullToRefreshBox(
                        isRefreshing = calendarAccountListState.loading,
                        onRefresh = {
                            viewModel.loadCalendarAccountList()
                        },
                    ) {
                        val list = calendarAccountListState.list
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp),
                        ) {
                            items(list.size) { index ->
                                BuildItem(item = list[index], onClick = {
                                    viewModel.deleteCalendarAccount(it.accountId)
                                })
                            }
                        }
                        if (list.isEmpty()) {
                            StateScreen(
                                title = "暂无数据",
                                imageRes = painterResource(Res.drawable.state_no_data),
                                verticalArrangement = Arrangement.Top,
                            )
                        }
                    }
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                        onClick = {
                            if (!calendarAccountListState.loading) {
                                scope.launch {
                                    openBottomSheet.value = true
                                }
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                        )
                    }
                    ExportBottomSheet(openBottomSheet, scope)
                } else {
                    StateScreen(
                        title = "权限不足",
                        subtitle = "将课程导出到系统日历功能需要对${appName()}授予“读取/写入日历”权限。\n没有这个权限，该功能无法运作",
                        buttonText = "授权",
                        imageRes = painterResource(Res.drawable.state_no_permission),
                        verticalArrangement = Arrangement.Top,
                        onButtonClick = {
                            calendarPermissionState.launchMultiplePermissionRequest()
                        }
                    )
                }
            }
        }
        val exportAccountState by viewModel.actionState.collectAsState()
        ShowProgressDialog(
            useCaseState = exportLoadingDialog,
            text = "数据导出中...",
            successText = if (exportAccountState.actionSuccess) "导出完成" else "",
            errorText = exportAccountState.errorMessage,
        )
        LaunchedEffect(exportAccountState) {
            if (exportAccountState.loading) {
                exportLoadingDialog.show()
            } else {
                delay(1500L)
                exportLoadingDialog.hide()
            }
        }
    }

    @Composable
    private fun ShowSelectExportUserDialog(
        selectList: List<UserSelect>,
        useCaseState: UseCaseState,
        onSelect: (UserSelect) -> Unit,
    ) {
        ShowSingleSelectDialog("请选择要导出的账号", selectList, useCaseState, onSelect)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ShowReminderDialog(
        useCaseState: UseCaseState,
        onSelect: (Int) -> Unit = { },
    ) {
        val options = listOf(5, 10, 15, 20, 30)
        ListDialog(
            header = xhuHeader(title = "请选择要添加的提醒时间"),
            state = useCaseState,
            selection = ListSelection.Single(
                options = options.map {
                    ListOption(
                        titleText = "开始前 $it 分钟提醒",
                    )
                },
            ) { index, _ ->
                onSelect(options[index])
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ExportBottomSheet(
        openBottomSheet: MutableState<Boolean>,
        scope: CoroutineScope,
    ) {
        val userSelect by viewModel.userSelect.select.collectAsState()
        val actionState by viewModel.actionState.collectAsState()

        val userDialog by viewModel.userSelect.selectDialog.collectAsState()

        val reminderDialog = rememberUseCaseState()
        val sheetState = rememberModalBottomSheetState()

        var includeCustomCourse by remember { mutableStateOf(false) }
        var includeCustomThing by remember { mutableStateOf(false) }
        val reminder = remember { mutableStateListOf<Int>() }

        ShowSelectExportUserDialog(selectList = userSelect, useCaseState = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        ShowReminderDialog(useCaseState = reminderDialog) {
            if (!reminder.contains(it)) {
                reminder.add(it)
            }
        }

        fun dismissSheet() {
            scope.launch { sheetState.hide() }
                .invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        openBottomSheet.value = false
                    }
                }
        }

        LaunchedEffect(actionState.loading) {
            if (!actionState.loading && actionState.actionSuccess && openBottomSheet.value) {
                dismissSheet()
            }
        }

        if (!openBottomSheet.value) {
            return
        }

        ModalBottomSheet(
            onDismissRequest = {
                openBottomSheet.value = false
            },
            sheetState = sheetState,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable {
                                dismissSheet()
                            },
                        painter = XhuIcons.CustomCourse.close,
                        contentDescription = null,
                    )
                    Spacer(modifier = Modifier.weight(1F))
                    TextButton(onClick = {
                        viewModel.exportCalendar(includeCustomCourse, includeCustomThing, reminder)
                    }) {
                        Text(text = "导出")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp),
                        painter = XhuIcons.multiUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    val selected = userSelect.firstOrNull { it.selected }
                    val userString =
                        selected?.let { "账号：${it.userName}(${it.studentId})" }
                            ?: "选择导出的账号"
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                userDialog.show()
                            },
                        text = userString,
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp),
                        painter = XhuIcons.customCourse,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.weight(1F),
                        text = "包含自定义课程",
                    )
                    Switch(
                        checked = includeCustomCourse,
                        onCheckedChange = { includeCustomCourse = it },
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp),
                        painter = XhuIcons.customThing,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        modifier = Modifier.weight(1F),
                        text = "包含自定义事项",
                    )
                    Switch(
                        checked = includeCustomThing,
                        onCheckedChange = { includeCustomThing = it },
                    )
                }
                Row(
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp),
                ) {
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .padding(12.dp),
                        painter = XhuIcons.notifyCourse,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Column(
                        modifier = Modifier.weight(1F),
                    ) {
                        reminder.forEach {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "事件开始前 $it 分钟提醒",
                                    modifier = Modifier.weight(1F),
                                )
                                IconButton(onClick = {
                                    reminder.remove(it)
                                }) {
                                    Icon(
                                        painter = XhuIcons.close,
                                        contentDescription = null
                                    )
                                }
                            }
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        reminderDialog.show()
                                    }),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "添加提醒",
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildItem(
    item: CalendarAccount, onClick: (CalendarAccount) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(36.dp),
                color = item.color
            ) {}
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1F)
            ) {
                Text(
                    text = "${item.studentName}(${item.studentId})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                )
                Text(
                    text = "共 ${item.eventNum} 个事项",
                )
            }
            TextButton(onClick = {
                onClick(item)
            }) {
                Text(
                    text = "删除"
                )
            }
        }
    }
}