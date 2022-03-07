package vip.mystery0.xhu.timetable.ui.activity

import android.Manifest
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.zyao89.view.zloading.Z_TYPE
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.model.CalendarAccount
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ExportCalendarViewModel

@OptIn(ExperimentalMaterialApi::class)
class ExportCalendarActivity : BaseComposeActivity() {
    private val viewModel: ExportCalendarViewModel by viewModels()

    @OptIn(ExperimentalPermissionsApi::class)
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

        val showSheet = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val scope = rememberCoroutineScope()

        fun onBack() {
            if (showSheet.isVisible) {
                scope.launch {
                    showSheet.hide()
                }
                return
            }
            finish()
        }

        val userDialog = remember { mutableStateOf(false) }
        val reminderDialog = remember { mutableStateOf(false) }

        val reminder = remember { mutableStateListOf<Int>() }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
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
            Box {
                ModalBottomSheetLayout(
                    sheetState = showSheet,
                    scrimColor = Color.Black.copy(alpha = 0.32f),
                    sheetContent = {
                        var includeCustomCourse by remember { mutableStateOf(false) }
                        var includeCustomThing by remember { mutableStateOf(false) }

                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .clickable {
                                            scope.launch {
                                                showSheet.hide()
                                            }
                                        },
                                    painter = XhuIcons.CustomCourse.close,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.weight(1F))
                                TextButton(onClick = {
                                    viewModel.exportCalendar(
                                        includeCustomCourse, includeCustomThing, reminder
                                    )
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
                                    tint = XhuColor.Common.blackText,
                                )
                                val userSelect by viewModel.userSelect.collectAsState()
                                val selected = userSelect.firstOrNull { it.selected }
                                val userString =
                                    selected?.let { "账号：${it.userName}(${it.studentId})" }
                                        ?: "选择导出的账号"
                                Text(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            userDialog.value = true
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
                                    tint = XhuColor.Common.blackText,
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
                                    tint = XhuColor.Common.blackText,
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
                                    tint = XhuColor.Common.blackText,
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
                                            .clickable(interactionSource = remember { MutableInteractionSource() },
                                                indication = null,
                                                onClick = {
                                                    reminderDialog.value = true
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
                    }) {
                    if (calendarPermissionState.allPermissionsGranted) {
                        val calendarAccountListState by viewModel.calendarAccountListState.collectAsState()
                        SwipeRefresh(
                            modifier = Modifier
                                .padding(paddingValues)
                                .fillMaxSize(),
                            state = rememberSwipeRefreshState(calendarAccountListState.loading),
                            onRefresh = { viewModel.loadCalendarAccountList() },
                        ) {
                            val list = calendarAccountListState.list
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(XhuColor.Common.grayBackground),
                                contentPadding = PaddingValues(4.dp),
                            ) {
                                if (calendarAccountListState.loading) {
                                    scope.launch {
                                        showSheet.hide()
                                    }
                                } else {
                                    items(list.size) { index ->
                                        BuildItem(item = list[index], onClick = {
                                            viewModel.deleteCalendarAccount(it.accountId)
                                        })
                                    }
                                }
                            }
                            if (list.isEmpty()) {
                                BuildNoDataLayout()
                            }
                        }
                        FloatingActionButton(modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(24.dp),
                            onClick = {
                                if (!calendarAccountListState.loading) {
                                    scope.launch {
                                        showSheet.show()
                                    }
                                }
                            }) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    } else {
                        BuildNoPermissionLayout(permissionDescription = "将课程导出到系统日历功能需要对${appName}授予“读取/写入日历”权限。没有这个权限，该功能无法运作",
                            onRequestPermission = {
                                calendarPermissionState.launchMultiplePermissionRequest()
                            })
                    }
                }
            }
        }
        ShowUserDialog(show = userDialog)
        ShowReminderDialog(show = reminderDialog, list = reminder)
        val exportAccountState by viewModel.actionState.collectAsState()
        ShowProgressDialog(
            show = exportAccountState.loading, text = "数据导出中", type = Z_TYPE.STAR_LOADING
        )
    }

    @Composable
    private fun ShowUserDialog(
        show: MutableState<Boolean>,
    ) {
        val userSelect by viewModel.userSelect.collectAsState()
        val selectedUser = userSelect.firstOrNull { it.selected } ?: return
        if (show.value) {
            var selected by remember { mutableStateOf(selectedUser) }
            AlertDialog(onDismissRequest = {
                show.value = false
            }, title = {
                Text(text = "请选择要导出的账号")
            }, text = {
                LazyColumn {
                    items(userSelect.size) { index ->
                        val item = userSelect[index]
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
                            Text(text = "${item.userName}(${item.studentId})")
                        }
                    }
                }
            }, confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.selectUser(selected.studentId)
                        show.value = false
                    },
                ) {
                    Text("确认")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    show.value = false
                }) {
                    Text("取消")
                }
            })
        }
    }

    @Composable
    private fun ShowReminderDialog(
        show: MutableState<Boolean>,
        list: MutableList<Int>,
    ) {
        if (show.value) {
            var selected by remember { mutableStateOf(5) }
            AlertDialog(onDismissRequest = {
                show.value = false
            }, title = {
                Text(text = "请选择要添加的提醒时间")
            }, text = {
                LazyColumn {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    selected = 5
                                },
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selected == 5, onClick = null)
                            Text(text = "开始前 5 分钟提醒")
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    selected = 10
                                },
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selected == 10, onClick = null)
                            Text(text = "开始前 10 分钟提醒")
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    selected = 20
                                },
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selected == 20, onClick = null)
                            Text(text = "开始前 20 分钟提醒")
                        }
                    }
                }
            }, confirmButton = {
                TextButton(
                    onClick = {
                        if (!list.contains(selected)) list.add(selected)
                        show.value = false
                    },
                ) {
                    Text("确认")
                }
            }, dismissButton = {
                TextButton(onClick = {
                    show.value = false
                }) {
                    Text("取消")
                }
            })
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
        backgroundColor = XhuColor.cardBackground,
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