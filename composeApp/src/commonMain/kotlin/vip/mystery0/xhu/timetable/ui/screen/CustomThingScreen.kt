package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.calendar.CalendarDialog
import com.maxkeppeler.sheets.calendar.models.CalendarConfig
import com.maxkeppeler.sheets.calendar.models.CalendarSelection
import com.maxkeppeler.sheets.calendar.models.CalendarStyle
import com.maxkeppeler.sheets.clock.ClockDialog
import com.maxkeppeler.sheets.clock.models.ClockConfig
import com.maxkeppeler.sheets.clock.models.ClockSelection
import com.maxkeppeler.sheets.color.ColorDialog
import com.maxkeppeler.sheets.color.models.ColorConfig
import com.maxkeppeler.sheets.color.models.ColorSelection
import com.maxkeppeler.sheets.color.models.SingleColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
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
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun CustomThingScreen() {
    val viewModel = koinViewModel<CustomThingViewModel>()

    val navController = LocalNavController.current!!

    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    val userSelect by viewModel.userSelect.select.collectAsState()
    val userDialog by viewModel.userSelect.selectDialog.collectAsState()

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
                title = { Text(text = "自定义事项") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = null,
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
                BuildUserSelectFilterChipContent(
                    userSelect = userSelect,
                    showUserDialog = userDialog,
                ) {
                    viewModel.loadCustomThingList()
                }
            },
            listContent = {
                stickyHeader {
                    HorizontalDivider()
                }
                itemsIndexed(
                    pager,
                    key = { index -> pager[index]?.thingId ?: index }) { item ->
                    BuildItem(item) {
                        updateCustomThing(item)
                    }
                }
            },
            emptyState = {
                StateScreen(
                    title = "暂无自定义事项",
                    buttonText = "再查一次",
                    imageRes = painterResource(Res.drawable.state_no_data),
                    verticalArrangement = Arrangement.Top,
                    onButtonClick = {
                        viewModel.loadCustomThingList()
                    }
                )
            },
        ) @Composable {
            AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(24.dp),
                visible = isScrollingUp,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                FloatingActionButton(
                    onClick = {
                        updateCustomThing(CustomThingResponse.init())
                    }) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                    )
                }
            }
        }
    }
    ShowUserDialog(selectList = userSelect, useCaseState = userDialog, onSelect = {
        viewModel.selectUser(it.studentId)
    })
    CustomThingBottomSheet(customThingState, openBottomSheet, scope)

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
private fun BuildDateSelector(
    useCaseState: UseCaseState,
    initData: LocalDateTime,
    onDateChange: (LocalDateTime) -> Unit,
) {
    val date = initData.date
    val time = initData.time

    CalendarDialog(
        header = Header.Default(
            title = "请选择日期",
        ),
        state = useCaseState,
        selection = CalendarSelection.Date(
            selectedDate = date,
        ) {
            onDateChange(LocalDateTime(it, time))
        },
        config = CalendarConfig(
            yearSelection = true,
            monthSelection = true,
            style = CalendarStyle.MONTH,
        )
    )
}

@Composable
private fun BuildTimeSelector(
    useCaseState: UseCaseState,
    initData: LocalDateTime,
    onDateChange: (LocalDateTime) -> Unit,
) {
    val date = initData.date
    val time = initData.time

    ClockDialog(
        header = Header.Default(
            title = "请选择时间",
        ),
        state = useCaseState,
        selection = ClockSelection.HoursMinutes(
        ) { hour, minutes ->
            onDateChange(LocalDateTime(date, LocalTime(hour, minutes)))
        },
        config = ClockConfig(
            defaultTime = time,
            is24HourFormat = true,
        )
    )
}

@Composable
private fun BuildColorSelector(
    useCaseState: UseCaseState,
    initData: Color,
    onDateChange: (Color) -> Unit,
) {
    ColorDialog(
        header = Header.Default(
            title = "请选择需要修改的颜色",
        ),
        state = useCaseState,
        selection = ColorSelection(
            selectedColor = SingleColor(initData.toArgb()),
            onSelectColor = {
                onDateChange(Color(it))
            }
        ),
        config = ColorConfig(
            templateColors = ColorPool.templateColors,
            allowCustomColorAlphaValues = false,
        )
    )
}

@Composable
private fun CustomThingBottomSheet(
    customThingState: MutableState<CustomThingResponse>,
    openBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
) {
    val viewModel = koinViewModel<CustomThingViewModel>()

    val startDateDialog = rememberUseCaseState()
    val startTimeDialog = rememberUseCaseState()
    val endDateDialog = rememberUseCaseState()
    val endTimeDialog = rememberUseCaseState()
    val showColorDialog = rememberUseCaseState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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

    LaunchedEffect(customThing) {
        thingTitle = customThing.title
        location = customThing.location
        allDay = customThing.allDay
        saveAsCountdown = customThing.saveAsCountDown
        startTime = customThing.startTime.asLocalDateTime()
        endTime = customThing.endTime.asLocalDateTime()
        remark = customThing.remark
        color = customThing.color.parseColorHexString()
    }

    val focusManager = LocalFocusManager.current

    BuildDateSelector(startDateDialog, startTime) {
        startTime = it
    }
    BuildTimeSelector(startTimeDialog, startTime) {
        startTime = it
    }
    BuildDateSelector(endDateDialog, endTime) {
        endTime = it
    }
    BuildTimeSelector(endTimeDialog, endTime) {
        endTime = it
    }
    BuildColorSelector(showColorDialog, color) {
        color = it
    }

    fun dismissSheet() {
        focusManager.clearFocus()
        scope
            .launch { sheetState.hide() }
            .invokeOnCompletion {
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

    if (!openBottomSheet.value) {
        return
    }

    val horizontalPadding = 12.dp

    ModalBottomSheet(
        onDismissRequest = {
            focusManager.clearFocus()
            openBottomSheet.value = false
        },
        sheetState = sheetState,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = horizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = {
                    dismissSheet()
                },
                modifier = Modifier.padding(12.dp)
            ) {
                Icon(Icons.Rounded.Cancel, null)
            }
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
        Column {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                value = thingTitle,
                placeholder = {
                    Text(text = "（必填）")
                },
                label = {
                    Text(text = "标题")
                },
                onValueChange = { thingTitle = it },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    disabledLabelColor = OutlinedTextFieldDefaults.colors().unfocusedTextColor,
                )
            )
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                value = "",
                enabled = false,
                label = {
                    Text(text = "全天")
                },
                leadingIcon = {
                    Icon(Icons.Rounded.Schedule, null)
                },
                trailingIcon = {
                    Switch(
                        checked = allDay,
                        onCheckedChange = { allDay = it },
                    )
                },
                onValueChange = { },
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    disabledBorderColor = Color.Transparent,
                    disabledLabelColor = OutlinedTextFieldDefaults.colors().unfocusedTextColor,
                    disabledLeadingIconColor = OutlinedTextFieldDefaults.colors().unfocusedLeadingIconColor
                )
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
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
                            interactionSource = remember { MutableInteractionSource() },
                        ),
                    text = startTime.date.format(dateFormatter),
                )
                if (!allDay) {
                    Text(
                        modifier = Modifier
                            .clickable(
                                onClick = {
                                    startTimeDialog.show()
                                },
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() },
                            ),
                        text = startTime.time.format(Formatter.TIME_NO_SECONDS),
                    )
                }
            }
            if (!saveAsCountdown) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = horizontalPadding)
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
                                interactionSource = remember { MutableInteractionSource() },
                            ),
                        text = endTime.date.format(dateFormatter),
                    )
                    if (!allDay) {
                        Text(
                            modifier = Modifier
                                .clickable(
                                    onClick = {
                                        endTimeDialog.show()
                                    },
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ),
                            text = endTime.time.format(Formatter.TIME_NO_SECONDS),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1F),
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
        }
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            value = location,
            placeholder = {
                Text(text = "（选填）")
            },
            label = {
                Text(text = "地点")
            },
            leadingIcon = {
                Icon(Icons.Rounded.LocationOn, null)
            },
            onValueChange = { location = it },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                disabledLabelColor = OutlinedTextFieldDefaults.colors().unfocusedTextColor,
            )
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = horizontalPadding)
                .clickable(
                    onClick = {
                        showColorDialog.show()
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ),
            value = "",
            enabled = false,
            label = {
                Text(text = "设置颜色")
            },
            leadingIcon = {
                Icon(Icons.Rounded.Palette, null)
            },
            trailingIcon = {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(24.dp),
                    color = color
                ) {}
            },
            onValueChange = { },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                disabledLabelColor = OutlinedTextFieldDefaults.colors().unfocusedTextColor,
                disabledLeadingIconColor = OutlinedTextFieldDefaults.colors().unfocusedLeadingIconColor
            )
        )
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = horizontalPadding),
            value = remark,
            placeholder = {
                Text(text = "（选填）")
            },
            label = {
                Text(text = "备注")
            },
            leadingIcon = {
                Icon(Icons.AutoMirrored.Rounded.Notes, null)
            },
            singleLine = false,
            maxLines = 5,
            onValueChange = { remark = it },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                disabledBorderColor = Color.Transparent,
                disabledLabelColor = OutlinedTextFieldDefaults.colors().unfocusedTextColor,
            )
        )
        Spacer(modifier = Modifier.weight(1F))
    }
}

@Composable
private fun BuildItem(
    item: CustomThingResponse,
    onClick: () -> Unit,
) {
    PageItemLayout(
        cardModifier = Modifier.clickable(
            onClick = onClick,
            indication = null,
            interactionSource = MutableInteractionSource(),
        ),
        header = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Box(
                    modifier = Modifier.size(24.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Surface(
                        shape = CircleShape,
                        modifier = Modifier
                            .size(16.dp),
                        color = item.color.parseColorHexString()
                    ) {}
                }
                Text(item.title)
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
            TextWithIcon(
                imageVector = Icons.AutoMirrored.Filled.EventNote,
                text = "时间：${timeText}",
            )
            TextWithIcon(
                imageVector = Icons.Filled.LocationOn,
                text = "地点：${item.location.ifBlank { "未指定" }}",
            )
        },
        footer = {
            Text("添加时间：${item.createTime.asLocalDateTime().format(chinaDateTime)}")
        }
    )
}