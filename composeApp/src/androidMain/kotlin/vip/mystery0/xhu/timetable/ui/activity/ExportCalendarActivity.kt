package vip.mystery0.xhu.timetable.ui.activity

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.androidx.viewmodel.ext.android.viewModel
import vip.mystery0.xhu.timetable.base.appName
import vip.mystery0.xhu.timetable.model.CalendarAccount
import vip.mystery0.xhu.timetable.ui.component.ShowProgressDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuConfirmDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.screen.FilterSelectorRow
import vip.mystery0.xhu.timetable.ui.theme.SetSystemAppearance
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import vip.mystery0.xhu.timetable.ui.theme.applyInitialImmersiveEdgeToEdge
import vip.mystery0.xhu.timetable.ui.theme.isDarkMode
import vip.mystery0.xhu.timetable.viewmodel.ExportCalendarViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data
import xhutimetable.composeapp.generated.resources.state_no_permission

class ExportCalendarActivity : ComponentActivity() {
    private val viewModel: ExportCalendarViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyInitialImmersiveEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        setContent {
            XhuTimetableTheme {
                SetSystemAppearance(isDark = isDarkMode())
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

        // 修复原版在 Composable 主体直接调用导致每次重组重复刷新的 Bug
        LaunchedEffect(calendarPermissionState.allPermissionsGranted) {
            if (calendarPermissionState.allPermissionsGranted) {
                viewModel.loadCalendarAccountList()
            }
        }

        val openBottomSheet = rememberSaveable { mutableStateOf(false) }
        val scope = rememberCoroutineScope()
        val exportLoadingDialog = rememberUseCaseState()
        val deleteConfirmState = rememberXhuDialogState()
        var pendingDeleteAccount by remember { mutableStateOf<CalendarAccount?>(null) }

        LaunchedEffect(Unit) {
            viewModel.init()
        }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = "日历导出管理",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { finish() }) {
                            Icon(
                                painter = XhuIcons.back,
                                contentDescription = "返回",
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    },
                )
            },
        ) { paddingValues ->
            val calendarAccountListState by viewModel.calendarAccountListState.collectAsState()
            val pullToRefreshState = rememberPullToRefreshState()
            val layoutDirection = LocalLayoutDirection.current
            val bottomPadding = paddingValues.calculateBottomPadding()

            Box(
                modifier = Modifier
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                    )
                    .fillMaxSize(),
            ) {
                if (calendarPermissionState.allPermissionsGranted) {
                    PullToRefreshBox(
                        state = pullToRefreshState,
                        isRefreshing = calendarAccountListState.loading,
                        onRefresh = { viewModel.loadCalendarAccountList() },
                        indicator = {
                            PullToRefreshDefaults.LoadingIndicator(
                                state = pullToRefreshState,
                                isRefreshing = calendarAccountListState.loading,
                                modifier = Modifier.align(Alignment.TopCenter)
                            )
                        }
                    ) {
                        val list = calendarAccountListState.list
                        if (list.isEmpty() && !calendarAccountListState.loading) {
                            StateScreen(
                                modifier = Modifier.padding(bottom = bottomPadding),
                                title = "暂无已导出的日历账户",
                                subtitle = "点击右下角 + 按钮将本学期课程导出至系统日历",
                                imageRes = painterResource(Res.drawable.state_no_data),
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 10.dp,
                                    bottom = 88.dp + bottomPadding,
                                ),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                items(list, key = { it.accountId }) { item ->
                                    BuildCalendarAccountCard(
                                        item = item,
                                        onDeleteClick = {
                                            pendingDeleteAccount = item
                                            deleteConfirmState.show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                    FloatingActionButton(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 20.dp, bottom = 20.dp + bottomPadding),
                        onClick = {
                            if (!calendarAccountListState.loading) {
                                openBottomSheet.value = true
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "导出新日历",
                        )
                    }
                    ExportBottomSheet(openBottomSheet, scope)
                } else {
                    StateScreen(
                        modifier = Modifier.padding(bottom = bottomPadding),
                        title = "需要日历读写权限",
                        subtitle = "将课程导出到系统日历功能需要向${appName()}授予“读取/写入日历”权限。",
                        buttonText = "前往授权",
                        imageRes = painterResource(Res.drawable.state_no_permission),
                        onButtonClick = {
                            calendarPermissionState.launchMultiplePermissionRequest()
                        }
                    )
                }
            }
        }

        pendingDeleteAccount?.let { account ->
            XhuConfirmDialog(
                state = deleteConfirmState,
                title = "删除导出日历",
                message = "确认删除「${account.studentName} (${account.studentId})」在系统日历中的 ${account.eventNum} 个课程日程吗？",
                icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null) },
                confirmText = "确认删除",
                isDestructive = true,
                onConfirm = {
                    viewModel.deleteCalendarAccount(account.accountId)
                    pendingDeleteAccount = null
                },
            )
        }

        val exportAccountState by viewModel.actionState.collectAsState()
        ShowProgressDialog(
            useCaseState = exportLoadingDialog,
            text = "正在导出日程…",
            successText = if (exportAccountState.actionSuccess) "导出完成" else "",
            errorText = exportAccountState.errorMessage,
        )
        LaunchedEffect(exportAccountState) {
            if (exportAccountState.loading) {
                exportLoadingDialog.show()
            } else {
                delay(1200L)
                exportLoadingDialog.hide()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun ExportBottomSheet(
        openBottomSheet: MutableState<Boolean>,
        scope: CoroutineScope,
    ) {
        val userSelect by viewModel.userSelect.select.collectAsState()
        val actionState by viewModel.actionState.collectAsState()

        val userDialogState = rememberXhuDialogState()
        val reminderDialogState = rememberXhuDialogState()
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        var includeCustomCourse by remember { mutableStateOf(false) }
        var includeCustomThing by remember { mutableStateOf(false) }
        val reminder = remember { mutableStateListOf<Int>() }

        XhuSingleSelectDialog(
            state = userDialogState,
            title = "选择要导出的账号",
            options = userSelect,
            selectedIndex = userSelect.indexOfFirst { it.selected },
            itemLabel = { "${it.userName} (${it.studentId})" },
            onSelect = { _, item -> viewModel.selectUser(item.studentId) },
        )

        val reminderOptions = listOf(5, 10, 15, 20, 30)
        XhuSingleSelectDialog(
            state = reminderDialogState,
            title = "添加课前提醒时间",
            options = reminderOptions,
            selectedIndex = -1,
            itemLabel = { "开始前 $it 分钟提醒" },
            onSelect = { _, minutes ->
                if (!reminder.contains(minutes)) {
                    reminder.add(minutes)
                    reminder.sort()
                }
            },
        )

        fun dismissSheet() {
            scope.launch { sheetState.hide() }.invokeOnCompletion {
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

        if (!openBottomSheet.value) return

        ModalBottomSheet(
            onDismissRequest = { openBottomSheet.value = false },
            sheetState = sheetState,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { dismissSheet() }) {
                        Icon(Icons.Rounded.Close, contentDescription = "关闭")
                    }
                    Text(
                        text = "导出到系统日历",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                    )
                    Button(
                        shape = RoundedCornerShape(12.dp),
                        onClick = {
                            viewModel.exportCalendar(includeCustomCourse, includeCustomThing, reminder)
                        }
                    ) {
                        Text("开始导出")
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        val selected = userSelect.firstOrNull { it.selected }
                        FilterSelectorRow(
                            icon = Icons.Rounded.Person,
                            label = "导出账号",
                            value = selected?.let { "${it.userName} (${it.studentId})" } ?: "请选择账号",
                            highlighted = selected != null,
                            onClick = { userDialogState.show() },
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "包含自定义课程",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = includeCustomCourse,
                                onCheckedChange = { includeCustomCourse = it },
                            )
                        }
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "包含自定义事项",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            Switch(
                                checked = includeCustomThing,
                                onCheckedChange = { includeCustomThing = it },
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsActive,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "课前日程提醒",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.weight(1f),
                            )
                            TextButton(onClick = { reminderDialogState.show() }) {
                                Text("添加提醒")
                            }
                        }
                        if (reminder.isEmpty()) {
                            Text(
                                text = "未设置提醒（点击右上角添加）",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        } else {
                            reminder.forEach { minutes ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Text(
                                        text = "开始前 $minutes 分钟提醒",
                                        style = MaterialTheme.typography.bodyMedium,
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(
                                        onClick = { reminder.remove(minutes) },
                                        modifier = Modifier.size(36.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "移除提醒",
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
    }
}

@Composable
private fun BuildCalendarAccountCard(
    item: CalendarAccount,
    onDeleteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = XhuColor.cardBackground),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(36.dp),
                color = item.color,
            ) {}
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${item.studentName} (${item.studentId})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "共导出 ${item.eventNum} 个日程事项",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            FilledTonalIconButton(
                onClick = onDeleteClick,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                ),
                modifier = Modifier.size(40.dp),
            ) {
                Icon(
                    imageVector = Icons.Rounded.DeleteOutline,
                    contentDescription = "删除日历",
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
