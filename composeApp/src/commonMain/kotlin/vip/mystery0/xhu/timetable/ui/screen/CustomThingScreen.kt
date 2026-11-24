package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.BuildUserSelectFilterChipContent
import vip.mystery0.xhu.timetable.ui.component.PageItemLayout
import vip.mystery0.xhu.timetable.ui.component.ShowUserDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.TextWithIcon
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuColorPickerDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuConfirmDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuDatePickerDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuTimePickerDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.component.isScrollingUp
import vip.mystery0.xhu.timetable.ui.component.itemsIndexed
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.chinaDateTime
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.parseColorHexString
import vip.mystery0.xhu.timetable.utils.thingDateFormatter
import vip.mystery0.xhu.timetable.utils.thingDateTimeFormatter
import vip.mystery0.xhu.timetable.utils.thingTimeFormatter
import vip.mystery0.xhu.timetable.viewmodel.CustomThingViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_thing

@Composable
fun CustomThingScreen() {
    val viewModel = koinViewModel<CustomThingViewModel>()
    val navController = LocalNavController.current
    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    val userSelect by viewModel.userSelect.select.collectAsState()
    val userDialogState = rememberXhuDialogState()

    val scope = rememberCoroutineScope()
    val openBottomSheet = rememberSaveable { mutableStateOf(false) }
    val customThingState = remember { mutableStateOf(CustomThingResponse.init()) }

    fun updateCustomThing(data: CustomThingResponse) {
        customThingState.value = data
        openBottomSheet.value = true
    }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "自定义事项",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
                        )
                    }
                },
            )
        }
    ) { paddingValues ->
        val lazyListState = rememberLazyListState()
        val refreshing by viewModel.refreshing.collectAsState()
        val isScrollingUp = lazyListState.isScrollingUp()
        BuildPaging(
            state = lazyListState,
            paddingValues = paddingValues,
            pager = pager,
            refreshing = refreshing,
            listHeader = {
                vip.mystery0.xhu.timetable.ui.component.BuildUserSelectFilterBar(
                    userSelect = userSelect,
                    onUserClick = { userDialogState.show() },
                    onRefresh = { viewModel.loadCustomThingList() }
                )
            },
            listContent = {
                itemsIndexed(
                    pager,
                    key = { index -> pager[index]?.thingId ?: index }
                ) { item ->
                    BuildCustomThingCard(item) {
                        updateCustomThing(item)
                    }
                }
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                StateScreen(
                    title = loadingErrorMessage ?: "暂无自定义事项",
                    subtitle = "点击右下角 + 按钮，添加作业提醒、日程或倒计时",
                    buttonText = "再查一次",
                    imageRes = painterResource(Res.drawable.state_no_thing),
                    onButtonClick = { viewModel.loadCustomThingList() }
                )
            },
        ) {
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp),
                visible = isScrollingUp,
                enter = fadeIn() + scaleIn(),
                exit = fadeOut() + scaleOut(),
            ) {
                FloatingActionButton(
                    onClick = { updateCustomThing(CustomThingResponse.init()) }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "添加自定义事项",
                    )
                }
            }
        }
    }

    ShowUserDialog(selectList = userSelect, state = userDialogState, onSelect = {
        viewModel.selectUser(it.studentId)
    })
    CustomThingBottomSheet(customThingState, openBottomSheet, scope)

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomThingBottomSheet(
    customThingState: MutableState<CustomThingResponse>,
    openBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
) {
    val viewModel = koinViewModel<CustomThingViewModel>()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    val startDateDialog = rememberXhuDialogState()
    val startTimeDialog = rememberXhuDialogState()
    val endDateDialog = rememberXhuDialogState()
    val endTimeDialog = rememberXhuDialogState()
    val colorDialog = rememberXhuDialogState()
    val deleteConfirmDialog = rememberXhuDialogState()

    val saveLoadingState by viewModel.saveLoadingState.collectAsState()
    val customThing = customThingState.value

    var thingTitle by remember { mutableStateOf(customThing.title) }
    var location by remember { mutableStateOf(customThing.location) }
    var allDay by remember { mutableStateOf(customThing.allDay) }
    var saveAsCountdown by remember { mutableStateOf(customThing.saveAsCountDown) }
    var startTime by remember { mutableStateOf(customThing.startTime.asLocalDateTime()) }
    var endTime by remember { mutableStateOf(customThing.endTime.asLocalDateTime()) }
    var remark by remember { mutableStateOf(customThing.remark) }
    var color by remember { mutableStateOf(customThing.color.parseColorHexString()) }

    LaunchedEffect(customThing, openBottomSheet.value) {
        if (openBottomSheet.value) {
            thingTitle = customThing.title
            location = customThing.location
            allDay = customThing.allDay
            saveAsCountdown = customThing.saveAsCountDown
            startTime = customThing.startTime.asLocalDateTime()
            endTime = customThing.endTime.asLocalDateTime()
            remark = customThing.remark
            color = customThing.color.parseColorHexString()
        }
    }

    fun dismissSheet() {
        focusManager.clearFocus()
        scope.safeLaunch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                openBottomSheet.value = false
            }
        }
    }

    LaunchedEffect(saveLoadingState.loading) {
        if (!saveLoadingState.loading && saveLoadingState.actionSuccess && openBottomSheet.value) {
            dismissSheet()
        }
    }

    // 自研跨平台日期/时间/颜色/删除确认弹窗
    XhuDatePickerDialog(
        state = startDateDialog,
        title = if (saveAsCountdown) "选择目标日期" else "选择开始日期",
        initialDate = startTime.date,
        onSelectDate = { newDate ->
            startTime = LocalDateTime(newDate, startTime.time)
            if (endTime.date < newDate) {
                endTime = LocalDateTime(newDate, endTime.time)
            }
        },
    )
    XhuTimePickerDialog(
        state = startTimeDialog,
        title = "选择开始时间",
        initialTime = startTime.time,
        onSelectTime = { newTime -> startTime = LocalDateTime(startTime.date, newTime) },
    )
    XhuDatePickerDialog(
        state = endDateDialog,
        title = "选择结束日期",
        initialDate = endTime.date,
        onSelectDate = { newDate -> endTime = LocalDateTime(newDate, endTime.time) },
    )
    XhuTimePickerDialog(
        state = endTimeDialog,
        title = "选择结束时间",
        initialTime = endTime.time,
        onSelectTime = { newTime -> endTime = LocalDateTime(endTime.date, newTime) },
    )
    XhuColorPickerDialog(
        state = colorDialog,
        title = "选择主题颜色",
        initialColor = color,
        onSelectColor = { color = it },
    )
    XhuConfirmDialog(
        state = deleteConfirmDialog,
        title = "删除自定义事项",
        message = "确认删除事项「${customThing.title}」吗？删除后将无法恢复。",
        icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null) },
        confirmText = "删除",
        isDestructive = true,
        onConfirm = { viewModel.deleteCustomThing(customThing.thingId) },
    )

    if (!openBottomSheet.value) return

    val isEdit = customThing.thingId != 0L

    ModalBottomSheet(
        onDismissRequest = {
            focusManager.clearFocus()
            openBottomSheet.value = false
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // 清晰顶部标题与操作栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = { dismissSheet() }) {
                    Icon(Icons.Rounded.Close, contentDescription = "关闭")
                }
                Text(
                    text = if (isEdit) "编辑自定义事项" else "新建自定义事项",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (isEdit && !saveLoadingState.loading) {
                    IconButton(onClick = { deleteConfirmDialog.show() }) {
                        Icon(
                            imageVector = Icons.Rounded.DeleteOutline,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error,
                        )
                    }
                }
                Button(
                    enabled = !saveLoadingState.loading,
                    shape = RoundedCornerShape(12.dp),
                    onClick = {
                        viewModel.saveCustomThing(
                            customThing.thingId,
                            CustomThingRequest.buildOf(
                                thingTitle,
                                location,
                                allDay,
                                startTime,
                                endTime,
                                remark,
                                color,
                                mapOf(
                                    CustomThing.Key.SAVE_AS_COUNT_DOWN to saveAsCountdown.toString()
                                )
                            ),
                            saveAsCountdown,
                        )
                    }
                ) {
                    if (saveLoadingState.loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                    Text(if (saveLoadingState.loading) "保存中" else "保存")
                }
            }

            // 标题与地点
            OutlinedTextField(
                value = thingTitle,
                onValueChange = { thingTitle = it },
                label = { Text("事项标题 *") },
                placeholder = { Text("例如：英语四级考试 / 课程论文提交") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            // 时间与倒计时配置卡片
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "存储为倒计时",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = "在今日课程页置顶显示剩余天数",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Switch(
                            checked = saveAsCountdown,
                            onCheckedChange = {
                                saveAsCountdown = it
                                if (it) allDay = true
                            },
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "全天事项",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Switch(
                            checked = allDay,
                            enabled = !saveAsCountdown,
                            onCheckedChange = { allDay = it },
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                    DateTimeFieldRow(
                        label = if (saveAsCountdown) "目标日期" else "开始时间",
                        dateText = startTime.date.format(dateFormatter),
                        timeText = if (!allDay) startTime.time.format(Formatter.TIME_NO_SECONDS) else null,
                        onDateClick = { startDateDialog.show() },
                        onTimeClick = { startTimeDialog.show() },
                    )
                    if (!saveAsCountdown) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                        )
                        DateTimeFieldRow(
                            label = "结束时间",
                            dateText = endTime.date.format(dateFormatter),
                            timeText = if (!allDay) endTime.time.format(Formatter.TIME_NO_SECONDS) else null,
                            onDateClick = { endDateDialog.show() },
                            onTimeClick = { endTimeDialog.show() },
                        )
                    }
                }
            }

            // 内联主题色快速点选 + 自定义调色盘入口
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "标签颜色",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        TextButton(onClick = { colorDialog.show() }) {
                            Icon(
                                imageVector = Icons.Rounded.Palette,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("更多颜色")
                        }
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ColorPool.allColors.take(10).forEach { preset ->
                            val selected = (preset.toArgb() and 0x00FFFFFF) == (color.toArgb() and 0x00FFFFFF)
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(preset)
                                    .then(
                                        if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        else Modifier
                                    )
                                    .clickable { color = preset },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (selected) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = if (preset.luminance() > 0.55f) Color.Black else Color.White,
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("地点（选填）") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = remark,
                onValueChange = { remark = it },
                label = { Text("备注（选填）") },
                shape = RoundedCornerShape(14.dp),
                minLines = 2,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun DateTimeFieldRow(
    label: String,
    dateText: String,
    timeText: String?,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        Surface(
            onClick = onDateClick,
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
        ) {
            Text(
                text = dateText,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            )
        }
        if (timeText != null) {
            Surface(
                onClick = onTimeClick,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
            ) {
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                )
            }
        }
    }
}

@Composable
private fun BuildCustomThingCard(
    item: CustomThingResponse,
    onClick: () -> Unit,
) {
    PageItemLayout(
        onClick = onClick,
        header = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(item.color.parseColorHexString())
                )
                Text(
                    text = item.title,
                    modifier = Modifier.weight(1f),
                )
                if (item.saveAsCountDown) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Text(
                            text = "倒计时",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                        )
                    }
                }
            }
        },
        content = {
            val startDateTime = item.startTime.asLocalDateTime()
            val endDateTime = item.endTime.asLocalDateTime()
            val timeText = buildString {
                if (item.allDay) {
                    append(startDateTime.date.format(thingDateFormatter))
                } else {
                    append(startDateTime.format(thingDateTimeFormatter))
                }
                if (!item.saveAsCountDown) {
                    when {
                        item.allDay && startDateTime.date == endDateTime.date -> {}
                        item.allDay && startDateTime.date != endDateTime.date -> {
                            append(" - ")
                            append(endDateTime.date.format(thingDateFormatter))
                        }

                        !item.allDay && startDateTime.date == endDateTime.date -> {
                            append(" - ")
                            append(endDateTime.time.format(thingTimeFormatter))
                        }

                        !item.allDay && startDateTime.date != endDateTime.date -> {
                            append(" - ")
                            append(endDateTime.format(thingDateTimeFormatter))
                        }
                    }
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                TextWithIcon(
                    imageVector = Icons.AutoMirrored.Filled.EventNote,
                    text = "时间：$timeText",
                )
                if (item.location.isNotBlank()) {
                    TextWithIcon(
                        imageVector = Icons.Filled.LocationOn,
                        text = "地点：${item.location}",
                    )
                }
            }
        },
        footer = {
            Text("添加于 ${item.createTime.asLocalDateTime().format(chinaDateTime)}")
        }
    )
}
