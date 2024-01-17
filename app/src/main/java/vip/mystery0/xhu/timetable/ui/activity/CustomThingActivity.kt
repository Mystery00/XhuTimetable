package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.maxkeppeker.sheets.core.models.base.Header
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
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.model.CustomThing
import vip.mystery0.xhu.timetable.model.request.CustomThingRequest
import vip.mystery0.xhu.timetable.model.response.CustomThingResponse
import vip.mystery0.xhu.timetable.ui.component.XhuDialogState
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.formatChinaDateTime
import vip.mystery0.xhu.timetable.utils.parseColorHexString
import vip.mystery0.xhu.timetable.utils.thingDateTimeFormatter
import vip.mystery0.xhu.timetable.viewmodel.CustomThingViewModel
import java.time.LocalDateTime
import java.time.LocalTime

class CustomThingActivity : BaseSelectComposeActivity() {
    private val viewModel: CustomThingViewModel by viewModels()

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalFoundationApi::class
    )
    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)
        val userSelect by viewModel.userSelect.collectAsState()

        val userDialog = rememberXhuDialogState()

        val scope = rememberCoroutineScope()

        val openBottomSheet = rememberSaveable { mutableStateOf(false) }

        val customThingState = remember { mutableStateOf(CustomThingResponse.init()) }

        fun updateCustomThing(data: CustomThingResponse) {
            customThingState.value = data
            openBottomSheet.value = true
        }

        fun onBack() {
            if (openBottomSheet.value) {
                openBottomSheet.value = false
                return
            }
            finish()
        }

        BackHandler {
            onBack()
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
                alwaysShowList = true,
                listContent = {
                    stickyHeader {
                        BuildUserSelectFilterChipContent(
                            userSelect = userSelect,
                            showUserDialog = userDialog,
                        ) {
                            viewModel.loadCustomThingList()
                        }
                    }
                    stickyHeader {
                        Divider()
                    }
                    itemsIndexed(
                        pager,
                        key = { index -> pager[index]?.thingId ?: index }) { item ->
                        BuildItem(item) {
                            updateCustomThing(item)
                        }
                    }
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
        ShowUserDialog(selectList = userSelect, show = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        CustomThingBottomSheet(customThingState, openBottomSheet, scope)

        HandleErrorMessage(flow = viewModel.errorMessage)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BuildDateSelector(
        dialogState: XhuDialogState,
        initData: LocalDateTime,
        onDateChange: (LocalDateTime) -> Unit,
    ) {
        val date = initData.toLocalDate()
        val time = initData.toLocalTime()

        if (dialogState.showing) {
            CalendarDialog(
                header = Header.Default(
                    title = "请选择日期",
                ),
                state = rememberUseCaseState(
                    visible = true,
                    onCloseRequest = {
                        dialogState.hide()
                    }),
                selection = CalendarSelection.Date(
                    selectedDate = date,
                ) {
                    onDateChange(LocalDateTime.of(it, time))
                },
                config = CalendarConfig(
                    yearSelection = true,
                    monthSelection = true,
                    style = CalendarStyle.MONTH,
                )
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BuildTimeSelector(
        dialogState: XhuDialogState,
        initData: LocalDateTime,
        onDateChange: (LocalDateTime) -> Unit,
    ) {
        val date = initData.toLocalDate()
        val time = initData.toLocalTime()

        if (dialogState.showing) {
            ClockDialog(
                header = Header.Default(
                    title = "请选择时间",
                ),
                state = rememberUseCaseState(
                    visible = true,
                    onCloseRequest = {
                        dialogState.hide()
                    }),
                selection = ClockSelection.HoursMinutes { hour, minutes ->
                    onDateChange(LocalDateTime.of(date, LocalTime.of(hour, minutes, 0)))
                },
                config = ClockConfig(
                    defaultTime = time,
                    is24HourFormat = true,
                )
            )
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun BuildColorSelector(
        dialogState: XhuDialogState,
        initData: Color,
        onDateChange: (Color) -> Unit,
    ) {
        if (!dialogState.showing) {
            return
        }
        ColorDialog(
            header = Header.Default(
                title = "请选择需要修改的颜色",
            ),
            state = rememberUseCaseState(
                visible = true,
                onCloseRequest = {
                    dialogState.hide()
                }),
            selection = ColorSelection(
                selectedColor = SingleColor(initData.toArgb()),
                onSelectColor = {
                    onDateChange(Color(it))
                }
            ),
            config = ColorConfig(
                templateColors = ColorPool.templateColors,
            )
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CustomThingBottomSheet(
        customThingState: MutableState<CustomThingResponse>,
        openBottomSheet: MutableState<Boolean>,
        scope: CoroutineScope,
    ) {
        val startDateDialog = rememberXhuDialogState()
        val startTimeDialog = rememberXhuDialogState()
        val endDateDialog = rememberXhuDialogState()
        val endTimeDialog = rememberXhuDialogState()
        val showColorDialog = rememberXhuDialogState()
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

        ModalBottomSheet(
            onDismissRequest = {
                focusManager.clearFocus()
                openBottomSheet.value = false
            },
            sheetState = sheetState,
        ) {
            Row(
                modifier = Modifier
                    .padding(8.dp),
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
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
                            text = startTime.format(dateFormatter),
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
                                text = startTime.format(Formatter.TIME_NO_SECONDS),
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
                                text = endTime.format(dateFormatter),
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
                                    text = endTime.format(Formatter.TIME_NO_SECONDS),
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
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
                    color = color
                ) {}
                Text(
                    modifier = Modifier
                        .weight(1F)
                        .clickable(
                            onClick = {
                                showColorDialog.show()
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
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        errorContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    )
                )
            }
            Spacer(modifier = Modifier.weight(1F))
        }
    }
}

@Composable
private fun BuildItem(
    item: CustomThingResponse,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = MutableInteractionSource(),
            ),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
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
                    text = "创建时间：${item.createTime.formatChinaDateTime()}"
                )
            }
        }
    }
}