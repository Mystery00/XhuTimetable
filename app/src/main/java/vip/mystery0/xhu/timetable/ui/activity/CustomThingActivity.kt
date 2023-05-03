package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.color.ARGBPickerState
import com.vanpra.composematerialdialogs.color.ColorPalette
import com.vanpra.composematerialdialogs.color.colorChooser
import com.vanpra.composematerialdialogs.datetime.date.datepicker
import com.vanpra.composematerialdialogs.datetime.time.timepicker
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.chinaDateTimeFormatter
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.dateWithWeekFormatter
import vip.mystery0.xhu.timetable.utils.enTimeFormatter
import vip.mystery0.xhu.timetable.utils.parseColorHexString
import vip.mystery0.xhu.timetable.utils.thingDateTimeFormatter
import vip.mystery0.xhu.timetable.viewmodel.CustomThingViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterialApi::class)
class CustomThingActivity : BaseSelectComposeActivity() {
    private val viewModel: CustomThingViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAsLazyPagingItems()
        val userSelectStatus = viewModel.userSelect.collectAsState()

        val saveLoadingState by viewModel.saveLoadingState.collectAsState()

        val userDialog = remember { mutableStateOf(false) }

        val showSelect = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val scaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
        val scope = rememberCoroutineScope()

        val startDateDialog = rememberMaterialDialogState()
        val endDateDialog = rememberMaterialDialogState()
        val startTimeDialog = rememberMaterialDialogState()
        val endTimeDialog = rememberMaterialDialogState()
        val dialogState = rememberMaterialDialogState()

        var customThing by remember { mutableStateOf(CustomThingResponse.init()) }
        var thingTitle by remember { mutableStateOf(customThing.title) }
        var location by remember { mutableStateOf(customThing.location) }
        var allDay by remember { mutableStateOf(customThing.allDay) }
        var saveAsCountdown by remember { mutableStateOf(customThing.saveAsCountDown) }
        val startTime = remember { mutableStateOf(customThing.startTime.asLocalDateTime()) }
        val endTime = remember { mutableStateOf(customThing.endTime.asLocalDateTime()) }
        var remark by remember { mutableStateOf(customThing.remark) }
        val color = remember { mutableStateOf(customThing.color.parseColorHexString()) }

        suspend fun updateCustomThing(data: CustomThingResponse) {
            withContext(Dispatchers.Default) {
                customThing = data
                thingTitle = data.title
                location = data.location
                allDay = data.allDay
                saveAsCountdown = data.saveAsCountDown
                startTime.value = data.startTime.asLocalDateTime()
                endTime.value = data.endTime.asLocalDateTime()
                remark = data.remark
                color.value = data.color.parseColorHexString()
            }
        }

        fun onBack() {
            if (scaffoldState.isRevealed) {
                scope.launch {
                    scaffoldState.conceal()
                }
                return
            }
            if (showSelect.isVisible) {
                scope.launch {
                    showSelect.hide()
                }
                return
            }
            finish()
        }

        BackHandler {
            onBack()
        }

        val focusManager = LocalFocusManager.current
        if (!saveLoadingState.init && !saveLoadingState.loading && saveLoadingState.actionSuccess) {
            LaunchedEffect(key1 = "autoHideSheet", block = {
                scope.launch {
                    showSelect.hide()
                    focusManager.clearFocus()
                }
            })
        }

        BackdropScaffold(
            modifier = Modifier,
            scaffoldState = scaffoldState,
            appBar = {
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
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            scope.launch {
                                if (showSelect.isVisible) {
                                    showSelect.hide()
                                    return@launch
                                }
                                if (scaffoldState.isRevealed) {
                                    scaffoldState.conceal()
                                } else {
                                    scaffoldState.reveal()
                                }
                            }
                        }) {
                            Icon(
                                painter = XhuIcons.Action.view,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                )
            }, backLayerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    BuildUserSelectBackLayerContent(
                        scaffoldState = scaffoldState,
                        selectUserState = userSelectStatus,
                        showUserDialog = userDialog,
                    ) {
                        viewModel.loadCustomThingList()
                    }
                }
            }, frontLayerContent = {
                ModalBottomSheetLayout(
                    sheetState = showSelect,
                    scrimColor = Color.Black.copy(alpha = 0.32f),
                    sheetContent = {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .clickable {
                                            focusManager.clearFocus()
                                            scope.launch {
                                                showSelect.hide()
                                            }
                                        },
                                    painter = XhuIcons.CustomCourse.close,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.weight(1F))
                                if (customThing.thingId != 0L && !saveLoadingState.loading) {
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteCustomThing(customThing.thingId)
                                        }) {
                                        Text(text = "删除", color = Color.Red)
                                    }
                                }
                                if (!saveLoadingState.loading) {
                                    TextButton(
                                        onClick = {
                                            viewModel.saveCustomThing(
                                                customThing.thingId,
                                                CustomThingRequest.buildOf(
                                                    thingTitle,
                                                    location,
                                                    allDay,
                                                    startTime.value,
                                                    endTime.value,
                                                    remark,
                                                    color.value,
                                                    mapOf(
                                                        CustomThing.Key.SAVE_AS_COUNT_DOWN to saveAsCountdown.toString()
                                                    )
                                                ),
                                                saveAsCountdown,
                                            )
                                        }) {
                                        Text(text = "保存")
                                    }
                                }
                                if (saveLoadingState.loading) {
                                    TextButton(
                                        enabled = false,
                                        onClick = {
                                        }) {
                                        Text(text = "保存操作中...")
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextField(
                                    modifier = Modifier.weight(1F),
                                    value = thingTitle,
                                    placeholder = {
                                        Text(text = "（必填）")
                                    },
                                    label = {
                                        Text(text = "标题")
                                    },
                                    onValueChange = { thingTitle = it },
                                    colors = TextFieldDefaults.textFieldColors(
                                        backgroundColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .padding(8.dp),
                            ) {
                                Image(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(12.dp),
                                    painter = XhuIcons.CustomCourse.time,
                                    contentDescription = null
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1F),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .weight(1F),
                                            text = "全天",
                                        )
                                        Switch(
                                            checked = allDay,
                                            onCheckedChange = { allDay = it },
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .weight(1F),
                                            text = "存储为倒计时",
                                        )
                                        Switch(
                                            checked = saveAsCountdown,
                                            onCheckedChange = {
                                                saveAsCountdown = it
                                                if (it) {
                                                    //启用存储为倒计时，那么自动打开全天
                                                    allDay = true
                                                }
                                            },
                                        )
                                    }
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(36.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(
                                            modifier = Modifier
                                                .weight(1F)
                                                .clickable(
                                                    onClick = {
                                                        startDateDialog.show()
                                                    },
                                                    indication = null,
                                                    interactionSource = MutableInteractionSource(),
                                                ),
                                            text = startTime.value.format(dateWithWeekFormatter),
                                        )
                                        if (!allDay) {
                                            Text(
                                                modifier = Modifier
                                                    .clickable(
                                                        onClick = {
                                                            startTimeDialog.show()
                                                        },
                                                        indication = null,
                                                        interactionSource = MutableInteractionSource(),
                                                    ),
                                                text = startTime.value.format(enTimeFormatter),
                                            )
                                        }
                                    }
                                    if (!saveAsCountdown) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(36.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                modifier = Modifier
                                                    .weight(1F)
                                                    .clickable(
                                                        onClick = {
                                                            endDateDialog.show()
                                                        },
                                                        indication = null,
                                                        interactionSource = MutableInteractionSource(),
                                                    ),
                                                text = endTime.value.format(dateWithWeekFormatter),
                                            )
                                            if (!allDay) {
                                                Text(
                                                    modifier = Modifier
                                                        .clickable(
                                                            onClick = {
                                                                endTimeDialog.show()
                                                            },
                                                            indication = null,
                                                            interactionSource = MutableInteractionSource(),
                                                        ),
                                                    text = endTime.value.format(enTimeFormatter),
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextField(
                                    modifier = Modifier.weight(1F),
                                    value = location,
                                    placeholder = {
                                        Text(text = "（选填）")
                                    },
                                    label = {
                                        Text(text = "地点")
                                    },
                                    leadingIcon = {
                                        Image(
                                            painter = XhuIcons.CustomCourse.location,
                                            contentDescription = null
                                        )
                                    },
                                    onValueChange = { location = it },
                                    colors = TextFieldDefaults.textFieldColors(
                                        backgroundColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    )
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp)
                                        .size(24.dp),
                                    color = color.value
                                ) {}
                                Text(
                                    modifier = Modifier
                                        .weight(1F)
                                        .clickable(
                                            onClick = {
                                                dialogState.show()
                                            },
                                            indication = null,
                                            interactionSource = MutableInteractionSource(),
                                        ),
                                    text = "设置颜色",
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                TextField(
                                    modifier = Modifier.weight(1F),
                                    value = remark,
                                    placeholder = {
                                        Text(text = "（选填）")
                                    },
                                    label = {
                                        Text(text = "备注")
                                    },
                                    leadingIcon = {
                                        Image(
                                            painter = XhuIcons.CustomCourse.remark,
                                            contentDescription = null
                                        )
                                    },
                                    onValueChange = { remark = it },
                                    colors = TextFieldDefaults.textFieldColors(
                                        backgroundColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.weight(1F))
                        }
                    }) {
                    Box {
                        SwipeRefresh(
                            modifier = Modifier.fillMaxSize(),
                            state = rememberSwipeRefreshState(isRefreshing = false),
                            onRefresh = {
                                pager.refresh()
                            },
                            swipeEnabled = false,
                        ) {
                            if (pager.itemCount == 0) {
                                BuildNoDataLayout()
                                return@SwipeRefresh
                            }
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(XhuColor.Common.grayBackground),
                                contentPadding = PaddingValues(4.dp),
                            ) {
                                itemsIndexed(pager) { _, item ->
                                    item?.let {
                                        BuildItem(it) {
                                            scope.launch {
                                                updateCustomThing(item)
                                                showSelect.show()
                                            }
                                        }
                                    }
                                }
                                when (pager.loadState.append) {
                                    is LoadState.Loading -> {
                                        item { BuildPageFooter(text = "数据加载中，请稍后……") }
                                    }

                                    is LoadState.Error -> {
                                        item { BuildPageFooter(text = "数据加载失败，请重试") }
                                    }

                                    is LoadState.NotLoading -> {
                                        item { BuildPageFooter(text = "o(´^｀)o 再怎么滑也没有啦~") }
                                    }
                                }
                            }
                        }
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp),
                            onClick = {
                                scope.launch {
                                    updateCustomThing(CustomThingResponse.init())
                                    showSelect.show()
                                }
                            }) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }
                }
            })
        ShowUserDialog(selectState = userSelectStatus, show = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        BuildDateSelector(dialogState = startDateDialog, data = startTime)
        BuildTimeSelector(dialogState = startTimeDialog, data = startTime)
        BuildDateSelector(dialogState = endDateDialog, data = endTime)
        BuildTimeSelector(dialogState = endTimeDialog, data = endTime)
        BuildColorSelector(
            dialogState = dialogState,
            currentColor = color,
        )
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
        }
    }

    @Composable
    private fun BuildDateSelector(
        dialogState: MaterialDialogState,
        data: MutableState<LocalDateTime>,
    ) {
        val date = data.value.toLocalDate()
        val time = data.value.toLocalTime()
        var selectedDate = date
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    data.value = LocalDateTime.of(selectedDate, time)
                }
                negativeButton("取消")
            }) {
            datepicker(
                title = "请选择日期",
                initialDate = selectedDate,
                yearRange = 2020..LocalDate.now().plusYears(1).year
            ) {
                selectedDate = it
            }
        }
    }

    @Composable
    private fun BuildTimeSelector(
        dialogState: MaterialDialogState,
        data: MutableState<LocalDateTime>,
    ) {
        val date = data.value.toLocalDate()
        val time = data.value.toLocalTime()
        var selectedTime = time
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    data.value = LocalDateTime.of(date, selectedTime)
                }
                negativeButton("取消")
            }) {
            timepicker(
                title = "请选择时间",
                initialTime = selectedTime,
                is24HourClock = true,
            ) {
                selectedTime = it
            }
        }
    }

    @Composable
    private fun BuildColorSelector(
        dialogState: MaterialDialogState,
        currentColor: MutableState<Color>,
    ) {
        var selectedColor = currentColor.value
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    currentColor.value = selectedColor
                }
                negativeButton("取消")
            }) {
            title("请选择需要修改的颜色")
            val colors = ArrayList(ColorPalette.Primary).apply { add(0, currentColor.value) }
            colorChooser(colors = colors, argbPickerState = ARGBPickerState.WithoutAlphaSelector) {
                selectedColor = it
            }
        }
    }

    override fun onStop() {
        if (viewModel.changeCustomThing) {
            eventBus.post(UIEvent(EventType.CHANGE_SHOW_CUSTOM_THING))
        }
        super.onStop()
    }
}

@Composable
private fun BuildItem(
    item: CustomThingResponse,
    placeHolder: Boolean = false,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .placeholder(
                visible = placeHolder,
                highlight = PlaceholderHighlight.shimmer(),
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = MutableInteractionSource(),
            ),
        backgroundColor = XhuColor.cardBackground,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier
                        .size(24.dp),
                    color = item.color.parseColorHexString()
                ) {}
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = item.title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.time, contentDescription = null)
                val startText =
                    item.startTime.asLocalDateTime()
                        .format(if (item.allDay) dateFormatter else thingDateTimeFormatter)
                val endText =
                    item.endTime.asLocalDateTime()
                        .format(if (item.allDay) dateFormatter else thingDateTimeFormatter)
                val timeText = if (item.saveAsCountDown) startText else "$startText - $endText"
                Text(
                    text = "时间：$timeText"
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.location, contentDescription = null)
                Text(
                    text = "地点：${item.location}",
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "创建时间：${
                        item.createTime.asLocalDateTime().format(chinaDateTimeFormatter)
                    }"
                )
            }
        }
    }
}