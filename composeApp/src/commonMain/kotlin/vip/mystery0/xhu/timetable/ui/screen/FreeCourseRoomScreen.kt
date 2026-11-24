package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.PageItemLayout
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.TextWithIcon
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuDialogState
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuMultiSelectDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.CourseColorHelper
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatChina
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.CourseRoomViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_classroom

private val QUICK_PERIOD_PRESETS = listOf(
    "1-2节" to listOf(1, 2),
    "3-4节" to listOf(3, 4),
    "5-6节" to listOf(5, 6),
    "7-8节" to listOf(7, 8),
    "9-10节" to listOf(9, 10),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreeCourseRoomScreen() {
    val viewModel = koinViewModel<CourseRoomViewModel>()
    val navController = LocalNavController.current
    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)
    val scope = rememberCoroutineScope()

    val openFilterSheet = rememberSaveable { mutableStateOf(false) }
    val weekDialogState = rememberXhuDialogState()
    val dayDialogState = rememberXhuDialogState()
    val timeDialogState = rememberXhuDialogState()

    val areaSelect by viewModel.areaSelect.select.collectAsState()
    val weekSelect by viewModel.weekSelect.select.collectAsState()
    val daySelect by viewModel.daySelect.select.collectAsState()
    val timeSelect by viewModel.timeSelect.select.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "空闲教室查询",
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
                actions = {
                    IconButton(onClick = { openFilterSheet.value = true }) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "筛选条件",
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        BuildPaging(
            paddingValues = paddingValues,
            pager = pager,
            refreshing = !viewModel.init && pager.loadState.refresh is LoadState.Loading,
            key = { index -> pager[index]?.roomNo ?: index },
            listHeader = {
                val selectedArea = areaSelect.firstOrNull { it.selected }
                val selectedWeeks = weekSelect.filter { it.selected }.map { it.value }
                val selectedDays = daySelect.filter { it.selected }.map { DayOfWeek(it.value).formatChina() }
                val selectedTimes = timeSelect.filter { it.selected }.map { it.value }

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = selectedArea != null && selectedArea.value.isNotEmpty(),
                            onClick = { openFilterSheet.value = true },
                            label = { Text(selectedArea?.title ?: "全部区域") },
                        )
                        FilterChip(
                            selected = selectedWeeks.isNotEmpty(),
                            onClick = { weekDialogState.show() },
                            label = {
                                Text(
                                    if (selectedWeeks.isEmpty()) "周次*"
                                    else selectedWeeks.formatWeekString()
                                )
                            },
                        )
                        FilterChip(
                            selected = selectedDays.isNotEmpty(),
                            onClick = { dayDialogState.show() },
                            label = {
                                Text(
                                    if (selectedDays.isEmpty()) "星期*"
                                    else selectedDays.joinToString(",")
                                )
                            },
                        )
                        FilterChip(
                            selected = selectedTimes.isNotEmpty(),
                            onClick = { timeDialogState.show() },
                            label = {
                                Text(
                                    if (selectedTimes.isEmpty()) "节次*"
                                    else "第${selectedTimes.joinToString(",")}节"
                                )
                            },
                        )
                    }
                }
            },
            itemContent = { item ->
                BuildClassroomCard(item)
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                if (viewModel.init) {
                    StateScreen(
                        title = "设置条件查询空闲教室",
                        subtitle = "已为你预选当前周次、今天星期与当前时段节次，可直接查询或调整条件",
                        buttonText = "开始查询",
                        imageRes = painterResource(Res.drawable.state_no_classroom),
                        onButtonClick = {
                            if (!viewModel.search()) {
                                openFilterSheet.value = true
                            }
                        }
                    )
                } else {
                    StateScreen(
                        title = loadingErrorMessage ?: "该时段暂无空闲教室",
                        subtitle = "可以尝试调整教学楼区域、周次或节次条件",
                        buttonText = "修改查询条件",
                        imageRes = painterResource(Res.drawable.state_no_classroom),
                        onButtonClick = { openFilterSheet.value = true }
                    )
                }
            },
        )
    }

    CourseRoomFilterSheet(
        openBottomSheet = openFilterSheet,
        scope = scope,
        weekDialogState = weekDialogState,
        dayDialogState = dayDialogState,
        timeDialogState = timeDialogState,
    )

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun CourseRoomFilterSheet(
    openBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
    weekDialogState: XhuDialogState,
    dayDialogState: XhuDialogState,
    timeDialogState: XhuDialogState,
) {
    val viewModel = koinViewModel<CourseRoomViewModel>()

    val areaSelect by viewModel.areaSelect.select.collectAsState()
    val weekSelect by viewModel.weekSelect.select.collectAsState()
    val daySelect by viewModel.daySelect.select.collectAsState()
    val timeSelect by viewModel.timeSelect.select.collectAsState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun dismissSheet() {
        scope.safeLaunch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                openBottomSheet.value = false
            }
        }
    }

    XhuMultiSelectDialog(
        state = weekDialogState,
        title = "选择查询周次",
        options = weekSelect,
        initialSelectedIndices = weekSelect.mapIndexedNotNull { idx, item -> if (item.selected) idx else null },
        itemLabel = { it.title },
        gridColumns = 4,
        showQuickActions = true,
        allowEmpty = false,
        onConfirm = { _, items ->
            viewModel.changeWeek(items.map { it.value })
            if (!openBottomSheet.value && !viewModel.init) {
                viewModel.search()
            }
        },
    )
    XhuMultiSelectDialog(
        state = dayDialogState,
        title = "选择查询星期",
        options = daySelect,
        initialSelectedIndices = daySelect.mapIndexedNotNull { idx, item -> if (item.selected) idx else null },
        itemLabel = { it.title },
        gridColumns = 4,
        allowEmpty = false,
        onConfirm = { _, items ->
            viewModel.changeDay(items.map { it.value })
            if (!openBottomSheet.value && !viewModel.init) {
                viewModel.search()
            }
        },
    )
    XhuMultiSelectDialog(
        state = timeDialogState,
        title = "选择查询节次",
        options = timeSelect,
        initialSelectedIndices = timeSelect.mapIndexedNotNull { idx, item -> if (item.selected) idx else null },
        itemLabel = { it.title },
        gridColumns = 4,
        showQuickActions = true,
        allowEmpty = false,
        onConfirm = { _, items ->
            viewModel.changeTime(items.map { it.value })
            if (!openBottomSheet.value && !viewModel.init) {
                viewModel.search()
            }
        },
    )

    if (!openBottomSheet.value) return

    ModalBottomSheet(
        onDismissRequest = { openBottomSheet.value = false },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "空闲教室筛选",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                TextButton(
                    onClick = { viewModel.selectTodayDefault() }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Today,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("设为当前时段")
                }
            }

            // 教学区域（可选，默认全部区域）
            if (areaSelect.isNotEmpty()) {
                Text(
                    text = "教学区域（可选）",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    areaSelect.forEach { area ->
                        FilterChip(
                            selected = area.selected,
                            onClick = { viewModel.changeArea(area.value) },
                            label = { Text(area.title) },
                        )
                    }
                }
            }

            // 快捷节次一键选择
            val currentTimes = timeSelect.filter { it.selected }.map { it.value }
            Text(
                text = "常用节次快选",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                QUICK_PERIOD_PRESETS.forEach { (label, periods) ->
                    FilterChip(
                        selected = currentTimes == periods,
                        onClick = { viewModel.changeTime(periods) },
                        label = { Text(label) },
                    )
                }
            }

            // 周次 / 星期 / 节次 必选卡片组
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val weekList = weekSelect.filter { it.selected }.map { it.value }
                    FilterSelectorRow(
                        icon = Icons.Rounded.CalendarMonth,
                        label = "查询周次 *",
                        value = if (weekList.isEmpty()) "请选择（必选）" else weekList.formatWeekString(),
                        highlighted = weekList.isNotEmpty(),
                        onClick = { weekDialogState.show() },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                    val dayList = daySelect.filter { it.selected }.map { DayOfWeek(it.value).formatChina() }
                    FilterSelectorRow(
                        icon = Icons.Rounded.Today,
                        label = "查询星期 *",
                        value = if (dayList.isEmpty()) "请选择（必选）" else dayList.joinToString("、"),
                        highlighted = dayList.isNotEmpty(),
                        onClick = { dayDialogState.show() },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                    FilterSelectorRow(
                        icon = Icons.Rounded.Schedule,
                        label = "查询节次 *",
                        value = if (currentTimes.isEmpty()) "请选择（必选）" else "第 ${currentTimes.joinToString(",")} 节",
                        highlighted = currentTimes.isNotEmpty(),
                        onClick = { timeDialogState.show() },
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                onClick = {
                    if (viewModel.search()) {
                        dismissSheet()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "开始查询",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
internal fun FilterSelectorRow(
    icon: ImageVector,
    label: String,
    value: String,
    highlighted: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (highlighted) FontWeight.Medium else FontWeight.Normal,
            color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.widthIn(max = 180.dp),
            textAlign = TextAlign.End,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun BuildClassroomCard(item: ClassroomResponse) {
    val rawColor = ColorPool.hash(item.roomName)
    val accent = CourseColorHelper.accentText(rawColor)

    PageItemLayout(
        header = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = item.roomName.ifBlank { item.roomNo },
                    modifier = Modifier.weight(1f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accent.copy(alpha = 0.14f),
                ) {
                    Text(
                        text = item.roomNo,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
        },
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (item.campus.isNotBlank()) {
                    TextWithIcon(
                        imageVector = Icons.Filled.LocationOn,
                        text = "校区：${item.campus}",
                    )
                }
                if (item.roomType.isNotBlank()) {
                    TextWithIcon(
                        imageVector = Icons.Filled.MeetingRoom,
                        text = "场地类型：${item.roomType}",
                    )
                }
                if (item.roomRemark.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = "备注：${item.roomRemark}",
                            modifier = Modifier.padding(10.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                }
            }
        }
    )
}
