package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import vip.mystery0.xhu.timetable.ui.component.ShowTermDialog
import vip.mystery0.xhu.timetable.ui.component.ShowUserDialog
import vip.mystery0.xhu.timetable.ui.component.ShowYearDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuConfirmDialog
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.component.isScrollingUp
import vip.mystery0.xhu.timetable.ui.component.itemsIndexed
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
    val navController = LocalNavController.current
    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    val userSelect by viewModel.userSelect.select.collectAsState()
    val yearSelect by viewModel.yearSelect.select.collectAsState()
    val termSelect by viewModel.termSelect.select.collectAsState()

    val userDialogState = rememberXhuDialogState()
    val yearDialogState = rememberXhuDialogState()
    val termDialogState = rememberXhuDialogState()

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
                title = {
                    Text(
                        text = "自定义课程",
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
                vip.mystery0.xhu.timetable.ui.component.BuildSelectFilterBar(
                    userSelect = userSelect,
                    yearSelect = yearSelect,
                    termSelect = termSelect,
                    onUserClick = { userDialogState.show() },
                    onYearClick = { yearDialogState.show() },
                    onTermClick = { termDialogState.show() },
                    onRefresh = { viewModel.loadCustomCourseList() }
                )
            },
            listContent = {
                itemsIndexed(
                    pager,
                    key = { index -> pager[index]?.courseId ?: index }
                ) { item ->
                    BuildCustomCourseCard(item) {
                        updateCustomCourse(item)
                    }
                }
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                StateScreen(
                    title = loadingErrorMessage ?: "暂无自定义课程",
                    subtitle = "点击右下角 + 按钮，手动添加旁听、选修或其他课程",
                    buttonText = "再查一次",
                    imageRes = painterResource(Res.drawable.state_no_course_data),
                    onButtonClick = { viewModel.loadCustomCourseList() }
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
                    onClick = { updateCustomCourse(CustomCourseResponse.init()) }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "添加自定义课程",
                    )
                }
            }
        }
    }

    ShowUserDialog(selectList = userSelect, state = userDialogState, onSelect = {
        viewModel.selectUser(it.studentId)
    })
    ShowYearDialog(selectList = yearSelect, state = yearDialogState, onSelect = {
        viewModel.selectYear(it.value)
    })
    ShowTermDialog(selectList = termSelect, state = termDialogState, onSelect = {
        viewModel.selectTerm(it.value)
    })
    CustomCourseBottomSheet(customCourseState.value, openBottomSheet, scope)

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CustomCourseBottomSheet(
    customCourse: CustomCourseResponse,
    openBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
) {
    val viewModel = koinViewModel<CustomCourseViewModel>()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current

    val dayDialogState = rememberXhuDialogState()
    val slotDialogState = rememberXhuDialogState()
    val deleteConfirmState = rememberXhuDialogState()

    var courseName by remember { mutableStateOf(customCourse.courseName) }
    var weekList by remember { mutableStateOf(customCourse.weekList) }
    var day by remember { mutableStateOf(customCourse.day) }
    var startDayTime by remember { mutableIntStateOf(customCourse.startDayTime) }
    var endDayTime by remember { mutableIntStateOf(customCourse.endDayTime) }
    var location by remember { mutableStateOf(customCourse.location) }
    var teacher by remember { mutableStateOf(customCourse.teacher) }

    LaunchedEffect(customCourse, openBottomSheet.value) {
        if (openBottomSheet.value) {
            courseName = customCourse.courseName
            weekList = customCourse.weekList
            day = customCourse.day
            startDayTime = customCourse.startDayTime
            endDayTime = customCourse.endDayTime
            location = customCourse.location
            teacher = customCourse.teacher
        }
    }

    val saveLoadingState by viewModel.saveLoadingState.collectAsState()

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

    // 选择星期（自研单选框）
    val dayOptions = DayOfWeek.entries.toList()
    XhuSingleSelectDialog(
        state = dayDialogState,
        title = "选择上课星期",
        options = dayOptions,
        selectedIndex = dayOptions.indexOf(day),
        itemLabel = { it.formatWeekString() },
        onSelect = { _, selected -> day = selected },
    )

    // 选择开始/结束节次（使用 remember 状态，彻底修复重组丢状态 Bug）
    if (slotDialogState.visible) {
        var tempStart by remember(slotDialogState.visible, startDayTime) { mutableIntStateOf(startDayTime.coerceIn(1, 11)) }
        var tempEnd by remember(slotDialogState.visible, endDayTime) { mutableIntStateOf(endDayTime.coerceIn(tempStart, 11)) }

        XhuDialog(
            state = slotDialogState,
            title = "选择上课节次",
            subtitle = "第 $tempStart 节 - 第 $tempEnd 节",
            dismissButtonText = "取消",
            confirmButtonText = "确定",
            onConfirmButtonClick = {
                startDayTime = tempStart
                endDayTime = tempEnd
                slotDialogState.hide()
            },
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "开始节次",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    (1..11).forEach { slot ->
                        FilterChip(
                            selected = tempStart == slot,
                            onClick = {
                                tempStart = slot
                                if (tempEnd < slot) tempEnd = slot
                            },
                            label = { Text("$slot") },
                        )
                    }
                }
                Text(
                    text = "结束节次",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    (tempStart..11).forEach { slot ->
                        FilterChip(
                            selected = tempEnd == slot,
                            onClick = { tempEnd = slot },
                            label = { Text("$slot") },
                        )
                    }
                }
            }
        }
    }

    // 删除二次确认
    XhuConfirmDialog(
        state = deleteConfirmState,
        title = "删除自定义课程",
        message = "确认删除课程「${customCourse.courseName}」吗？删除后将无法恢复。",
        icon = { Icon(Icons.Rounded.DeleteOutline, contentDescription = null) },
        confirmText = "删除",
        isDestructive = true,
        onConfirm = { viewModel.deleteCustomCourse(customCourse.courseId) },
    )

    if (!openBottomSheet.value) return

    val isEdit = customCourse.courseId != 0L

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
                    text = if (isEdit) "编辑自定义课程" else "新建自定义课程",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (isEdit && !saveLoadingState.loading) {
                    IconButton(onClick = { deleteConfirmState.show() }) {
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

            // 课程基础信息输入（规范可见轮廓）
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("课程名称 *") },
                placeholder = { Text("例如：高等数学（旁听）") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                OutlinedTextField(
                    value = teacher,
                    onValueChange = { teacher = it },
                    label = { Text("任课教师") },
                    placeholder = { Text("选填") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("上课地点") },
                    placeholder = { Text("选填") },
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            // 星期与节次独立清晰选择行
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    FilterSelectorRow(
                        icon = Icons.Rounded.Today,
                        label = "上课星期",
                        value = day.formatWeekString(),
                        highlighted = true,
                        onClick = { dayDialogState.show() },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                    FilterSelectorRow(
                        icon = Icons.Rounded.Schedule,
                        label = "上课节次",
                        value = "第 $startDayTime - $endDayTime 节",
                        highlighted = true,
                        onClick = { slotDialogState.show() },
                    )
                }
            }

            // 内联周次网格（告别弹窗套弹窗）
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
                            text = "上课周数 *",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = if (weekList.isEmpty()) "未选择" else weekList.formatWeekString(),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        QuickWeekPill("全选") { weekList = (1..20).toList() }
                        QuickWeekPill("单周") { weekList = (1..20 step 2).toList() }
                        QuickWeekPill("双周") { weekList = (2..20 step 2).toList() }
                        QuickWeekPill("清空") { weekList = emptyList() }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        (1..20).chunked(5).forEach { rowWeeks ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                            ) {
                                rowWeeks.forEach { w ->
                                    val selected = weekList.contains(w)
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) {
                                            MaterialTheme.colorScheme.primaryContainer
                                        } else {
                                            MaterialTheme.colorScheme.surfaceContainerLowest
                                        },
                                        border = BorderStroke(
                                            if (selected) 1.2.dp else 0.5.dp,
                                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .clickable {
                                                weekList = if (selected) {
                                                    weekList - w
                                                } else {
                                                    (weekList + w).sorted()
                                                }
                                            },
                                    ) {
                                        Box(
                                            modifier = Modifier.padding(vertical = 9.dp),
                                            contentAlignment = Alignment.Center,
                                        ) {
                                            Text(
                                                text = "$w",
                                                style = MaterialTheme.typography.labelMedium,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (selected) {
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                },
                                                textAlign = TextAlign.Center,
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
}

@Composable
private fun QuickWeekPill(
    label: String,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
        )
    }
}

@Composable
private fun BuildCustomCourseCard(
    item: CustomCourseResponse,
    onClick: () -> Unit,
) {
    val accentColor = ColorPool.hash(item.courseName)
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = XhuColor.cardBackground,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(start = 12.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = item.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                    ) {
                        Text(
                            text = "每${item.day.formatWeekString()} · ${item.startDayTime}-${item.endDayTime}节",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        CourseMetaRow(
                            icon = Icons.Rounded.DateRange,
                            tint = MaterialTheme.colorScheme.primary,
                            text = "第${item.weekStr}",
                        )
                        if (item.location.isNotBlank()) {
                            CourseMetaRow(
                                icon = Icons.Rounded.LocationOn,
                                tint = MaterialTheme.colorScheme.tertiary,
                                text = item.location,
                            )
                        }
                        if (item.teacher.isNotBlank()) {
                            CourseMetaRow(
                                icon = Icons.Rounded.Person,
                                tint = MaterialTheme.colorScheme.secondary,
                                text = item.teacher,
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "添加于 ${item.createTime.asLocalDateTime().format(chinaDateTime)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "编辑",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        )
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseMetaRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: androidx.compose.ui.graphics.Color,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = tint,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
