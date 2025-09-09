package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.automirrored.twotone.ArrowForwardIos
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.input.InputDialog
import com.maxkeppeler.sheets.input.models.InputConfig
import com.maxkeppeler.sheets.input.models.InputHeader
import com.maxkeppeler.sheets.input.models.InputRadioButtonGroup
import com.maxkeppeler.sheets.input.models.InputSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.BuildSelectFilterChipContent
import vip.mystery0.xhu.timetable.ui.component.PageItemLayout
import vip.mystery0.xhu.timetable.ui.component.ShowMultiSelectDialog
import vip.mystery0.xhu.timetable.ui.component.ShowSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.ShowTermDialog
import vip.mystery0.xhu.timetable.ui.component.ShowUserDialog
import vip.mystery0.xhu.timetable.ui.component.ShowYearDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.TextWithIcon
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.component.isScrollingUp
import vip.mystery0.xhu.timetable.ui.component.itemsIndexed
import vip.mystery0.xhu.timetable.ui.component.xhuHeader
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.asLocalDateTime
import vip.mystery0.xhu.timetable.utils.chinaDateTime
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.CustomCourseViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_course_data

@Composable
fun CustomCourseScreen() {
    val viewModel = koinViewModel<CustomCourseViewModel>()

    val navController = LocalNavController.current!!

    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    val userSelect by viewModel.userSelect.select.collectAsState()
    val yearSelect by viewModel.yearSelect.select.collectAsState()
    val termSelect by viewModel.termSelect.select.collectAsState()

    val userDialog by viewModel.userSelect.selectDialog.collectAsState()
    val yearDialog by viewModel.yearSelect.selectDialog.collectAsState()
    val termDialog by viewModel.termSelect.selectDialog.collectAsState()

    val scope = rememberCoroutineScope()

    val openBottomSheet = rememberSaveable { mutableStateOf(false) }

    val customCourseState = remember { mutableStateOf(CustomCourseResponse.init()) }

    fun updateCustomCourse(data: CustomCourseResponse) {
        customCourseState.value = data
        openBottomSheet.value = true
    }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "自定义课程") },
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
                BuildSelectFilterChipContent(
                    userSelect = userSelect,
                    yearSelect = yearSelect,
                    termSelect = termSelect,
                    showUserDialog = userDialog,
                    showYearDialog = yearDialog,
                    showTermDialog = termDialog,
                    onDataLoad = {
                        viewModel.loadCustomCourseList()
                    }
                )
            },
            listContent = {
                itemsIndexed(
                    pager,
                    key = { index -> pager[index]?.courseId ?: index }) { item ->
                    BuildItem(item) {
                        updateCustomCourse(item)
                    }
                }
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                StateScreen(
                    title = loadingErrorMessage ?: "暂无自定义课程数据",
                    buttonText = "再查一次",
                    imageRes = painterResource(Res.drawable.state_no_course_data),
                    verticalArrangement = Arrangement.Top,
                    onButtonClick = {
                        viewModel.loadCustomCourseList()
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
                        updateCustomCourse(CustomCourseResponse.init())
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
    ShowYearDialog(selectList = yearSelect, useCaseState = yearDialog, onSelect = {
        viewModel.selectYear(it.value)
    })
    ShowTermDialog(selectList = termSelect, useCaseState = termDialog, onSelect = {
        viewModel.selectTerm(it.value)
    })
    CustomCourseBottomSheet(customCourseState.value, openBottomSheet, scope)

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
private fun ShowWeekDialog(
    useCaseState: UseCaseState,
    initSelected: List<Int>,
    onSelect: (List<Int>) -> Unit,
) {
    val options = (1..20).toList()

    ShowMultiSelectDialog(
        dialogTitle = "请选择上课周数",
        options = options,
        selectIndex = initSelected.map { options.indexOf(it) },
        itemTransform = { "第${it}周" },
        useCaseState = useCaseState,
        onSelect = { _, options ->
            onSelect(options)
        },
    )
}

@Composable
private fun ShowCourseIndexDialog(
    useCaseState: UseCaseState,
    initCourseIndex: Pair<Int, Int>,
    onSelect: (Pair<Int, Int>) -> Unit,
) {
    var startResult = initCourseIndex.first
    var endResult = initCourseIndex.second
    val inputOptions = listOf(
        InputRadioButtonGroup(
            header = InputHeader(title = "开始节次"),
            items = (1..11).toList().map { "第 $it 节" },
            selectedIndex = initCourseIndex.first - 1,
            required = true,
            columns = 1,
            key = "Start",
            resultListener = {
                if (it != null) {
                    startResult = it + 1
                    if (startResult > endResult) {
                        endResult = startResult
                    }
                }
            }
        ),
        InputRadioButtonGroup(
            header = InputHeader(title = "结束节次"),
            items = (1..11).toList().map { "第 $it 节" },
            selectedIndex = initCourseIndex.second - 1,
            required = true,
            columns = 1,
            key = "End",
            resultListener = {
                if (it != null) {
                    endResult = it + 1
                    if (endResult < startResult) {
                        startResult = endResult
                    }
                }
            }
        ),
    )

    InputDialog(
        header = xhuHeader(title = "请选择上课时间"),
        state = useCaseState,
        config = InputConfig(columns = 2),
        selection = InputSelection(
            input = inputOptions,
            onPositiveClick = {
                onSelect(startResult to endResult)
            },
        )
    )
}

@Composable
private fun ShowWeekIndexDialog(
    useCaseState: UseCaseState,
    initDayOfWeek: DayOfWeek?,
    onSelect: (DayOfWeek) -> Unit,
) {
    val options = DayOfWeek.entries.toList()
    ShowSingleSelectDialog(
        dialogTitle = "请选择上课星期",
        options = options,
        selectIndex = options.indexOf(initDayOfWeek),
        itemTransform = { it.formatWeekString() },
        useCaseState = useCaseState,
        onSelect = { _, option ->
            onSelect(option)
        },
    )
}

@Composable
private fun CustomCourseBottomSheet(
    customCourse: CustomCourseResponse,
    openBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
) {
    val viewModel = koinViewModel<CustomCourseViewModel>()

    val weekDialog = rememberUseCaseState()
    val courseIndexDialog = rememberUseCaseState()
    val weekIndexDialog = rememberUseCaseState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var courseName by remember { mutableStateOf(customCourse.courseName) }
    var weekList by remember { mutableStateOf(customCourse.weekList) }
    var day by remember { mutableStateOf(customCourse.day) }
    var startDayTime by remember { mutableIntStateOf(customCourse.startDayTime) }
    var endDayTime by remember { mutableIntStateOf(customCourse.endDayTime) }
    var location by remember { mutableStateOf(customCourse.location) }
    var teacher by remember { mutableStateOf(customCourse.teacher) }

    LaunchedEffect(customCourse) {
        courseName = customCourse.courseName
        weekList = customCourse.weekList
        day = customCourse.day
        startDayTime = customCourse.startDayTime
        endDayTime = customCourse.endDayTime
        location = customCourse.location
        teacher = customCourse.teacher
    }

    val saveLoadingState by viewModel.saveLoadingState.collectAsState()

    val focusManager = LocalFocusManager.current

    ShowWeekDialog(
        useCaseState = weekDialog,
        initSelected = weekList,
    ) {
        weekList = it
    }
    ShowCourseIndexDialog(
        useCaseState = courseIndexDialog,
        initCourseIndex = startDayTime to endDayTime,
    ) { (start, end) ->
        startDayTime = start
        endDayTime = end
    }
    ShowWeekIndexDialog(
        useCaseState = weekIndexDialog,
        initDayOfWeek = day,
    ) {
        day = it
    }

    fun dismissSheet() {
        focusManager.clearFocus()
        scope
            .safeLaunch { sheetState.hide() }
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
    val defaultColors = OutlinedTextFieldDefaults.colors()
    val textColors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color.Transparent,
        disabledBorderColor = Color.Transparent,
        disabledLabelColor = defaultColors.unfocusedLabelColor,
        disabledTextColor = defaultColors.unfocusedTextColor,
        disabledTrailingIconColor = defaultColors.unfocusedTrailingIconColor,
        disabledLeadingIconColor = defaultColors.unfocusedLeadingIconColor,
    )

    ModalBottomSheet(
        onDismissRequest = {
            focusManager.clearFocus()
            openBottomSheet.value = false
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(horizontalPadding)
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
                if (customCourse.courseId != 0L && !saveLoadingState.loading) {
                    TextButton(
                        onClick = {
                            viewModel.deleteCustomCourse(customCourse.courseId)
                        }) {
                        Text(text = "删除", color = Color.Red)
                    }
                }
                if (!saveLoadingState.loading) {
                    TextButton(
                        onClick = {
                            viewModel.saveCustomCourse(
                                customCourse.courseId,
                                CustomCourseRequest.buildOf(
                                    courseName,
                                    weekList,
                                    day,
                                    startDayTime,
                                    endDayTime,
                                    location,
                                    teacher,
                                )
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
            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        value = courseName,
                        placeholder = {
                            Text(text = "（必填）")
                        },
                        label = {
                            Text(text = "课程名称")
                        },
                        onValueChange = { courseName = it },
                        colors = textColors
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        value = teacher,
                        placeholder = {
                            Text(text = "（选填）")
                        },
                        label = {
                            Text(text = "任课教师")
                        },
                        onValueChange = { teacher = it },
                        colors = textColors
                    )
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = horizontalPadding),
                        value = location,
                        placeholder = {
                            Text(text = "（选填）")
                        },
                        label = {
                            Text(text = "上课地点")
                        },
                        onValueChange = { location = it },
                        colors = textColors
                    )
                }
            }
            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    OutlinedTextField(
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = null,
                                onClick = {
                                    weekDialog.show()
                                }
                            ),
                        value = if (weekList.isEmpty()) "（不能为空）" else weekList.formatWeekString(),
                        label = {
                            Text(text = "上课周数")
                        },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.TwoTone.ArrowForwardIos,
                                contentDescription = null,
                                modifier = Modifier.padding(horizontal = 12.dp)
                                    .size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        onValueChange = { },
                        colors = textColors
                    )
                    OutlinedTextField(
                        enabled = false,
                        modifier = Modifier.fillMaxWidth()
                            .padding(horizontal = horizontalPadding)
                            .clickable(
                                interactionSource = MutableInteractionSource(),
                                indication = null,
                                onClick = {
                                    courseIndexDialog.show()
                                }
                            ),
                        value = "第 $startDayTime - $endDayTime 节",
                        label = {
                            Text(text = "上课时间")
                        },
                        trailingIcon = {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    modifier = Modifier
                                        .clickable(
                                            onClick = {
                                                weekIndexDialog.show()
                                            },
                                            indication = null,
                                            interactionSource = MutableInteractionSource(),
                                        ),
                                    text = day.formatWeekString(),
                                )
                                Icon(
                                    imageVector = Icons.AutoMirrored.TwoTone.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        },
                        onValueChange = { },
                        colors = textColors
                    )
                }
            }
        }
    }
}

@Composable
private fun BuildItem(
    item: CustomCourseResponse,
    onClick: () -> Unit,
) {
    PageItemLayout(
        cardModifier = Modifier.clickable(
            onClick = onClick,
            indication = null,
            interactionSource = MutableInteractionSource(),
        ),
        header = {
            Text(item.courseName)
        },
        content = {
            if (item.teacher.isNotBlank()) {
                TextWithIcon(
                    imageVector = Icons.Filled.Person,
                    text = "教师名称：${item.teacher}",
                )
            }
            TextWithIcon(
                imageVector = Icons.Filled.DateRange,
                text = buildString {
                    append("上课星期：")
                    append("第")
                    append(item.weekStr)
                },
            )
            TextWithIcon(
                imageVector = Icons.AutoMirrored.Filled.EventNote,
                text = "上课时间：每周${item.day.formatWeekString()} 第 ${item.startDayTime}-${item.endDayTime} 节",
            )
            if (item.location.isNotBlank()) {
                TextWithIcon(
                    imageVector = Icons.Filled.LocationOn,
                    text = "上课地点：${item.location}",
                )
            }
        },
        footer = {
            Text("添加时间：${item.createTime.asLocalDateTime().format(chinaDateTime)}")
        }
    )
}