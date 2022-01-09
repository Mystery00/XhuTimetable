package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
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
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.dateWithWeekFormatter
import vip.mystery0.xhu.timetable.utils.enTimeFormatter
import vip.mystery0.xhu.timetable.utils.thingDateTimeFormatter
import vip.mystery0.xhu.timetable.viewmodel.CustomThingViewModel
import java.time.LocalDate
import java.time.LocalDateTime

@OptIn(ExperimentalMaterialApi::class)
class CustomThingActivity : BaseComposeActivity() {
    private val viewModel: CustomThingViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BuildContent() {
        val customThingListState by viewModel.customThingListState.collectAsState()
        val saveCustomThingState by viewModel.saveCustomThingState.collectAsState()

        val showSelect = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            confirmStateChange = {
                !customThingListState.loading && !saveCustomThingState.loading
            })
        val scaffoldState: BackdropScaffoldState =
            rememberBackdropScaffoldState(
                initialValue = BackdropValue.Revealed,
                confirmStateChange = {
                    !showSelect.isVisible && !customThingListState.loading && !saveCustomThingState.loading
                })
        val dialogState = rememberMaterialDialogState()
        val scope = rememberCoroutineScope()

        val userDialog = remember { mutableStateOf(false) }
        val yearDialog = remember { mutableStateOf(false) }
        val termDialog = remember { mutableStateOf(false) }
        val startDateDialog = rememberMaterialDialogState()
        val endDateDialog = rememberMaterialDialogState()
        val startTimeDialog = rememberMaterialDialogState()
        val endTimeDialog = rememberMaterialDialogState()

        val dateDialogState = rememberMaterialDialogState()
        val timeDialogState = rememberMaterialDialogState()

        var customThing by remember { mutableStateOf(CustomThing.EMPTY) }
        var thingTitle by remember { mutableStateOf(customThing.title) }
        var location by remember { mutableStateOf(customThing.location) }
        var allDay by remember { mutableStateOf(customThing.allDay) }
        val startTime = remember { mutableStateOf(customThing.startTime) }
        val endTime = remember { mutableStateOf(customThing.endTime) }
        var remark by remember { mutableStateOf(customThing.remark) }
        val color = remember { mutableStateOf(customThing.color) }

        fun updateCustomThing(data: CustomThing) {
            customThing = data
            thingTitle = data.title
            location = data.location
            allDay = data.allDay
            startTime.value = data.startTime
            endTime.value = data.endTime
            remark = data.remark
            color.value = data.color
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
            if (viewModel.changeCustomThing) {
                eventBus.post(UIEvent(EventType.CHANGE_SHOW_CUSTOM_THING))
            }
            finish()
        }

        BackHandler(
            onBack = {
                onBack()
            }
        )

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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                            modifier = Modifier.weight(1F),
                            onClick = {
                                userDialog.value = true
                            }) {
                            val userSelect by viewModel.userSelect.collectAsState()
                            val selected = userSelect.firstOrNull { it.selected }
                            val userString =
                                selected?.let { "${it.userName}(${it.studentId})" } ?: "查询中"
                            Text(text = "查询用户：$userString")
                        }
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                            onClick = {
                                viewModel.loadCustomThingList()
                                scope.launch {
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
                                yearDialog.value = true
                            }) {
                            val yearSelect by viewModel.yearSelect.collectAsState()
                            val yearString =
                                yearSelect.firstOrNull { it.selected }?.let { "${it.year}学年" }
                                    ?: "查询中"
                            Text(text = yearString)
                        }
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                            modifier = Modifier.weight(1F),
                            onClick = {
                                termDialog.value = true
                            }) {
                            val termSelect by viewModel.termSelect.collectAsState()
                            val termString =
                                termSelect.firstOrNull { it.selected }?.let { "第${it.term}学期" }
                                    ?: "查询中"
                            Text(text = termString)
                        }
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
                                            scope.launch {
                                                showSelect.hide()
                                            }
                                        },
                                    painter = XhuIcons.CustomCourse.close,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.weight(1F))
                                if (customThing.thingId != 0L) {
                                    TextButton(
                                        onClick = {
                                            viewModel.delete(customThing.thingId)
                                        }) {
                                        Text(text = "删除", color = Color.Red)
                                    }
                                }
                                TextButton(
                                    onClick = {
                                        if (startTime.value.isAfter(endTime.value)) {
                                            "开始时间不能晚于结束时间".toast(true)
                                            return@TextButton
                                        }
                                        viewModel.saveCustomThing(
                                            customThing.thingId,
                                            thingTitle,
                                            location,
                                            allDay,
                                            startTime.value,
                                            endTime.value,
                                            remark,
                                            color.value,
                                        )
                                    }) {
                                    Text(text = "保存")
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
                                verticalAlignment = Alignment.CenterVertically,
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
                            state = rememberSwipeRefreshState(customThingListState.loading),
                            onRefresh = {
                            },
                            swipeEnabled = false,
                        ) {
                            val list = customThingListState.customThingList
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(XhuColor.Common.grayBackground),
                                contentPadding = PaddingValues(4.dp),
                            ) {
                                if (customThingListState.loading) {
                                    scope.launch {
                                        showSelect.hide()
                                    }
                                    items(3) {
                                        BuildItem(
                                            CustomThing.PLACEHOLDER,
                                            true,
                                        ) {}
                                    }
                                } else {
                                    items(list.size) { index ->
                                        val item = list[index]
                                        BuildItem(item) {
                                            if (!customThingListState.loading) {
                                                updateCustomThing(item)
                                                scope.launch {
                                                    showSelect.animateTo(targetValue = ModalBottomSheetValue.Expanded)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            if (!customThingListState.loading && list.isEmpty()) {
                                BuildNoDataLayout()
                            }
                        }
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp),
                            onClick = {
                                if (!customThingListState.loading) {
                                    updateCustomThing(CustomThing.EMPTY)
                                    scope.launch {
                                        showSelect.animateTo(targetValue = ModalBottomSheetValue.Expanded)
                                    }
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
        ShowUserDialog(show = userDialog)
        ShowYearDialog(show = yearDialog)
        ShowTermDialog(show = termDialog)
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
        val init by viewModel.init.collectAsState()
        if (init) {
            LaunchedEffect(key1 = "init", block = {
                viewModel.loadCustomThingList()
                scope.launch {
                    scaffoldState.conceal()
                }
            })
        }
    }

    @Composable
    private fun ShowUserDialog(
        show: MutableState<Boolean>,
    ) {
        val userSelect by viewModel.userSelect.collectAsState()
        val selectedUser = userSelect.firstOrNull { it.selected } ?: return
        if (show.value) {
            var selected by remember { mutableStateOf(selectedUser) }
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = "请选择要查询的学生")
                },
                text = {
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
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.selectUser(selected.studentId)
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


    @Composable
    private fun ShowYearDialog(
        show: MutableState<Boolean>,
    ) {
        val yearSelect by viewModel.yearSelect.collectAsState()
        val selectedYear = yearSelect.firstOrNull { it.selected } ?: return
        if (show.value) {
            var selected by remember { mutableStateOf(selectedYear) }
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = "请选择要查询的学生")
                },
                text = {
                    Column {
                        LazyColumn {
                            items(yearSelect.size) { index ->
                                val item = yearSelect[index]
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
                                    Text(text = item.year)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.selectYear(selected.year)
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

    @Composable
    private fun ShowTermDialog(
        show: MutableState<Boolean>,
    ) {
        val termSelect by viewModel.termSelect.collectAsState()
        val selectedTerm = termSelect.firstOrNull { it.selected } ?: return
        if (show.value) {
            var selected by remember { mutableStateOf(selectedTerm) }
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = "请选择要查询的学期")
                },
                text = {
                    LazyColumn {
                        items(termSelect.size) { index ->
                            val item = termSelect[index]
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
                                Text(text = "${item.term}")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.selectTerm(selected.term)
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

    @Composable
    private fun BuildDateSelector(
        dialogState: MaterialDialogState,
        data: MutableState<LocalDateTime>,
    ) {
        val date = data.value.toLocalDate()
        var selectedDate = date
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    data.value = LocalDateTime.of(selectedDate, data.value.toLocalTime())
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
        val time = data.value.toLocalTime()
        var selectedTime = time
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    data.value = LocalDateTime.of(data.value.toLocalDate(), selectedTime)
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
}

@Composable
private fun BuildItem(
    item: CustomThing,
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
                    color = item.color
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
                Text(
                    text = "时间：${item.startTime.format(thingDateTimeFormatter)} - ${
                        item.endTime.format(
                            thingDateTimeFormatter
                        )
                    }"
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
        }
    }
}