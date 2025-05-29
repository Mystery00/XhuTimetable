package vip.mystery0.xhu.timetable.ui.activity

import android.Manifest
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material3.TopAppBar
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
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.list.ListDialog
import com.maxkeppeler.sheets.list.models.ListOption
import com.maxkeppeler.sheets.list.models.ListSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.base.UserSelect
import vip.mystery0.xhu.timetable.model.CalendarAccount
import vip.mystery0.xhu.timetable.ui.component.XhuDialogState
import vip.mystery0.xhu.timetable.ui.component.observerXhuDialogState
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ExportCalendarViewModel

class ExportCalendarActivity : BaseSelectComposeActivity() {
    private val viewModel: ExportCalendarViewModel by viewModels()

    @OptIn(
        ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class
    )
    @Composable
    override fun BuildContent() {
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

        fun onBack() {
            if (openBottomSheet.value) {
                openBottomSheet.value = false
                return
            }
            finish()
        }
        BackHandler(openBottomSheet.value) {
            openBottomSheet.value = false
        }
        LaunchedEffect(Unit) {
            viewModel.init()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    navigationIcon = {
                        IconButton(onClick = {
                            onBack()
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

            if (calendarPermissionState.allPermissionsGranted) {
                Box(
                    modifier = Modifier
                        .padding(paddingValues)
                        .fillMaxSize(),
                ) {
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
                            BuildNoDataLayout()
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
                }
                ExportBottomSheet(openBottomSheet, scope)
            } else {
                BuildNoPermissionLayout(
                    permissionDescription = "将课程导出到系统日历功能需要对${appName}授予“读取/写入日历”权限。\n没有这个权限，该功能无法运作",
                    onRequestPermission = {
                        calendarPermissionState.launchMultiplePermissionRequest()
                    })
            }
        }
        val exportAccountState by viewModel.actionState.collectAsState()
        val loadingDialog = observerXhuDialogState(exportAccountState.loading)
        ShowProgressDialog(showState = loadingDialog, text = "数据导出中...")
    }

    @Composable
    private fun ShowSelectExportUserDialog(
        selectList: List<UserSelect>,
        show: XhuDialogState,
        onSelect: (UserSelect) -> Unit,
    ) {
        ShowSelectDialog("请选择要导出的账号", selectList, show, onSelect)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ShowReminderDialog(
        show: XhuDialogState,
        onSelect: (Int) -> Unit = { },
    ) {
        val options = listOf(5, 10, 15, 20, 30)
        if (show.showing) {
            ListDialog(
                header = xhuHeader(title = "请选择要添加的提醒时间"),
                state = rememberUseCaseState(
                    visible = true,
                    onCloseRequest = { show.hide() }),
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
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ExportBottomSheet(
        openBottomSheet: MutableState<Boolean>,
        scope: CoroutineScope,
    ) {
        val userSelect by viewModel.userSelect.collectAsState()

        val actionState by viewModel.actionState.collectAsState()

        val userDialog = rememberXhuDialogState()
        val reminderDialog = rememberXhuDialogState()
        val sheetState = rememberModalBottomSheetState()

        var includeCustomCourse by remember { mutableStateOf(false) }
        var includeCustomThing by remember { mutableStateOf(false) }
        val reminder = remember { mutableStateListOf<Int>() }

        ShowSelectExportUserDialog(selectList = userSelect, show = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        ShowReminderDialog(show = reminderDialog) {
            if (!reminder.contains(it)) {
                reminder.add(it)
            }
        }

        fun dismissSheet() {
            scope
                .launch { sheetState.hide() }
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