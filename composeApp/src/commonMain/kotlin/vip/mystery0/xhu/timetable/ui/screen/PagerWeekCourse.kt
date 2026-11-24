package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.TabAction
import vip.mystery0.xhu.timetable.ui.component.TabContent
import vip.mystery0.xhu.timetable.ui.component.TabTitle
import vip.mystery0.xhu.timetable.ui.theme.CourseColorHelper
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.isDarkMode
import vip.mystery0.xhu.timetable.utils.formatChina
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.pad2
import vip.mystery0.xhu.timetable.viewmodel.CourseSheet
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerMainViewModel
import vip.mystery0.xhu.timetable.viewmodel.WeekView
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_course_data
import kotlin.math.min

private const val TOTAL_WEEKS = 20

private val CLASS_START_TIMES = listOf(
    "08:30", "09:20", "10:15", "11:05",
    "14:00", "14:50", "15:45", "16:35",
    "19:00", "19:50", "20:40",
)

val weekCourseTitleBar: TabTitle = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val pagerViewModel = koinViewModel<PagerMainViewModel>()
    val week by viewModel.week.collectAsState()
    val actualWeek by viewModel.actualCurrentWeek.collectAsState()

    val title = when {
        week <= 0 -> "尚未开学"
        week > TOTAL_WEEKS -> "学期已结束"
        else -> "第${week}周"
    }
    val isNotActualWeek = actualWeek in 1..TOTAL_WEEKS && week != actualWeek

    Surface(
        onClick = { pagerViewModel.showWeekPickerSheet.value = true },
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "切换周次",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp),
                    )
                }
                if (isNotActualWeek) {
                    Text(
                        text = "非本周 · 当前第${actualWeek}周",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

val weekCourseActions: TabAction = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val week by viewModel.week.collectAsState()
    val actualWeek by viewModel.actualCurrentWeek.collectAsState()

    val showBackToCurrent = actualWeek in 1..TOTAL_WEEKS && week != actualWeek
    AnimatedVisibility(
        visible = showBackToCurrent,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
    ) {
        IconButton(
            onClick = { viewModel.changeCurrentWeek(actualWeek) },
            modifier = Modifier.fillMaxHeight(),
        ) {
            Icon(
                imageVector = Icons.Rounded.Today,
                contentDescription = "回到本周",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
    true
}

val weekCourseContent: TabContent = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val pagerViewModel = koinViewModel<PagerMainViewModel>()

    val weekView by viewModel.weekView.collectAsState()
    val currentWeek by viewModel.week.collectAsState()
    val actualWeek by viewModel.actualCurrentWeek.collectAsState()
    val termStartDate by viewModel.termStartDate.collectAsState()
    val allWeekTables by viewModel.allWeekTables.collectAsState()
    val fallbackTableCourse by viewModel.tableCourse.collectAsState()
    val customUi by viewModel.customUi.collectAsState()
    val multiAccountMode by viewModel.multiAccountMode.collectAsState()
    val showWeekPickerSheet by pagerViewModel.showWeekPickerSheet.collectAsState()

    val courseDialogState = remember { mutableStateOf<List<WeekCourseView>>(emptyList()) }
    val scope = rememberCoroutineScope()

    val initialPage = (currentWeek - 1).coerceIn(0, TOTAL_WEEKS - 1)
    val pagerState = rememberPagerState(initialPage = initialPage) { TOTAL_WEEKS }

    // 外部点击周次选择/回本周时，平滑滚动 Pager 到目标周
    LaunchedEffect(currentWeek) {
        val targetPage = (currentWeek - 1).coerceIn(0, TOTAL_WEEKS - 1)
        if (pagerState.currentPage != targetPage && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(targetPage)
        }
    }

    // 手势左右滑动翻周停稳后，同步通知 ViewModel 更新顶栏周次与状态
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settled ->
                val targetWeek = settled + 1
                if (viewModel.week.value != targetWeek) {
                    viewModel.changeCurrentWeek(targetWeek)
                }
            }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            val pageWeek = page + 1
            val pageTable = allWeekTables[pageWeek]
                ?: if (pageWeek == currentWeek) fallbackTableCourse else emptyList()
            val pageFirstDay = termStartDate.plus(page.toLong(), DateTimeUnit.WEEK)

            WeekPageGrid(
                firstDay = pageFirstDay,
                tableCourse = pageTable,
                customUi = customUi,
                onCourseClick = { courses ->
                    courseDialogState.value = courses
                },
            )
        }

        ShowCourseDetailSheet(
            dialogState = courseDialogState,
            multiAccountMode = multiAccountMode,
        )

        if (showWeekPickerSheet) {
            WeekPickerBottomSheet(
                weekView = weekView,
                currentWeek = currentWeek,
                actualWeek = actualWeek,
                onDismiss = { pagerViewModel.showWeekPickerSheet.value = false },
                onWeekSelect = { targetWeek ->
                    pagerViewModel.showWeekPickerSheet.value = false
                    viewModel.changeCurrentWeek(targetWeek)
                    scope.launch {
                        pagerState.animateScrollToPage((targetWeek - 1).coerceIn(0, TOTAL_WEEKS - 1))
                    }
                },
            )
        }
    }
}

@Composable
private fun WeekPageGrid(
    firstDay: LocalDate,
    tableCourse: List<List<CourseSheet>>,
    customUi: CustomUi,
    onCourseClick: (List<WeekCourseView>) -> Unit,
) {
    val isDark = isDarkMode()
    val today = LocalDate.now()
    val hasAnyCourse = tableCourse.isNotEmpty() &&
        tableCourse.any { col -> col.isNotEmpty() && col.any { !it.isEmptyInstance && !it.isEmpty() } }

    Column(modifier = Modifier.fillMaxSize()) {
        // 主题感知柔和毛玻璃日期栏
        Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.72f else 0.84f),
            tonalElevation = 1.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(0.42f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(
                        text = "${firstDay.month.number.pad2()}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "月",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                for (i in 0..6) {
                    val thisDay = firstDay.plus(i, DateTimeUnit.DAY)
                    BuildDateItem(
                        week = thisDay.dayOfWeek.formatChina(),
                        date = if (thisDay.day == 1) "${thisDay.month.number.pad2()}月"
                        else thisDay.day.pad2(),
                        isToday = thisDay == today,
                    )
                }
            }
        }

        if (!hasAnyCourse) {
            StateScreen(
                title = "本周没有课程哦~",
                subtitle = "左右滑动可查看其他周次课表",
                imageRes = painterResource(Res.drawable.state_no_course_data),
                verticalArrangement = Arrangement.Center,
            )
        } else {
            val scrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(bottom = 12.dp),
            ) {
                // 左侧节次与开始时间列
                Column(
                    modifier = Modifier
                        .weight(0.42f)
                        .background(
                            MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.52f else 0.65f)
                        )
                ) {
                    for (time in 1..11) {
                        BuildTimeItem(
                            time = time,
                            startTimeLabel = CLASS_START_TIMES.getOrElse(time - 1) { "" },
                            itemHeight = customUi.weekItemHeight.dp,
                        )
                    }
                }
                for (index in 0 until 7) {
                    Column(modifier = Modifier.weight(1f)) {
                        tableCourse.getOrElse(index) { emptyList() }.forEach { sheet ->
                            if (!sheet.isEmpty()) {
                                BuildWeekItem(
                                    customUi = customUi,
                                    backgroundColor = sheet.color,
                                    itemStep = sheet.step,
                                    title = sheet.showTitle,
                                    textColor = sheet.textColor,
                                    conflictCount = (sheet.course.size - 1).coerceAtLeast(0),
                                ) {
                                    onCourseClick(sheet.course)
                                }
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(customUi.weekItemHeight.dp * sheet.step)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeekPickerBottomSheet(
    weekView: List<WeekView>,
    currentWeek: Int,
    actualWeek: Int,
    onDismiss: () -> Unit,
    onWeekSelect: (Int) -> Unit,
) {
    val activeDotColor = MaterialTheme.colorScheme.primary
    val inactiveDotColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val displayWeeks = remember(weekView, actualWeek) {
        if (weekView.isNotEmpty()) {
            weekView
        } else {
            (1..TOTAL_WEEKS).map { w ->
                WeekView(w, w == actualWeek, Array(5) { Array(5) { false } })
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column {
                    Text(
                        text = "切换教学周",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "点阵表示周一至周五各时段排课密度",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (actualWeek in 1..TOTAL_WEEKS) {
                    TextButton(onClick = { onWeekSelect(actualWeek) }) {
                        Icon(
                            imageVector = Icons.Rounded.Today,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("回到第${actualWeek}周")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 完整展示 4x5 共 20 周，避免与 Sheet 下滑关闭手势产生嵌套滚动冲突
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                displayWeeks.chunked(4).forEach { rowWeeks ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        rowWeeks.forEach { item ->
                            val isSelected = item.weekNum == currentWeek
                            val isActual = item.thisWeek || item.weekNum == actualWeek
                            Surface(
                                onClick = { onWeekSelect(item.weekNum) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = when {
                                    isSelected -> MaterialTheme.colorScheme.primaryContainer
                                    isActual -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
                                    else -> MaterialTheme.colorScheme.surfaceContainerLow
                                },
                                border = when {
                                    isSelected -> BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                                    isActual -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                    else -> BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                                },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(74.dp)
                                        .padding(vertical = 6.dp, horizontal = 4.dp),
                                ) {
                                    if (isActual) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.align(Alignment.TopEnd),
                                        ) {
                                            Text(
                                                text = "今",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontSize = 8.sp,
                                                lineHeight = 10.sp,
                                                color = MaterialTheme.colorScheme.onPrimary,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.align(Alignment.Center),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Canvas(modifier = Modifier.size(30.dp)) {
                                            val itemH = size.height / 5f
                                            val itemW = size.width / 5f
                                            val radius = min(itemH, itemW) / 2f - 1.2f
                                            for (day in 0 until 5) {
                                                for (slot in 0 until 5) {
                                                    val light = item.array.getOrElse(slot) { emptyArray() }
                                                        .getOrElse(day) { false }
                                                    drawCircle(
                                                        color = if (light) activeDotColor else inactiveDotColor,
                                                        center = Offset(
                                                            x = itemW * slot + itemW / 2f,
                                                            y = itemH * day + itemH / 2f,
                                                        ),
                                                        radius = radius.coerceAtLeast(1f),
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = "第${item.weekNum}周",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected || isActual) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onSurface
                                            },
                                            maxLines = 1,
                                        )
                                    }
                                }
                            }
                        }
                        repeat(4 - rowWeeks.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BuildWeekItem(
    customUi: CustomUi,
    backgroundColor: Color,
    itemStep: Int,
    title: String,
    textColor: Color,
    showMore: Boolean = false,
    conflictCount: Int = if (showMore) 1 else 0,
    onClick: () -> Unit,
) {
    val isNotThisWeek = backgroundColor == XhuColor.notThisWeekBackgroundColor
    val showBackgroundColor = if (isNotThisWeek) {
        MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.82f)
    } else {
        CourseColorHelper.weekCellContainer(backgroundColor, customUi.weekBackgroundAlpha)
    }
    val showTextColor = if (isNotThisWeek) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        CourseColorHelper.readableTextOn(showBackgroundColor)
    }
    val corner = customUi.weekItemCorner.coerceIn(0f, 24f).dp
    val shape = RoundedCornerShape(corner)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(customUi.weekItemHeight.dp * itemStep)
            .padding(1.5.dp)
            .clip(shape)
            .background(showBackgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 3.dp, vertical = 4.dp),
    ) {
        Text(
            text = title,
            color = showTextColor,
            textAlign = TextAlign.Center,
            fontSize = customUi.weekTitleTextSize.sp,
            lineHeight = (customUi.weekTitleTextSize * 1.25f).sp,
            fontWeight = if (isNotThisWeek) FontWeight.Normal else FontWeight.Medium,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = if (conflictCount > 0 && itemStep == 1) 12.dp else 0.dp),
        )
        if (conflictCount > 0) {
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = Color.Black.copy(alpha = 0.45f),
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                Text(
                    text = "+$conflictCount",
                    color = Color.White,
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                )
            }
        }
    }
}

@Composable
private fun RowScope.BuildDateItem(
    week: String,
    date: String,
    isToday: Boolean = false,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = week,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(
                    if (isToday) MaterialTheme.colorScheme.primary else Color.Transparent
                )
                .padding(horizontal = 6.dp, vertical = 1.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Medium,
                color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun BuildTimeItem(
    time: Int,
    startTimeLabel: String,
    itemHeight: Dp,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = time.toString(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (startTimeLabel.isNotEmpty() && itemHeight >= 48.dp) {
            Text(
                text = startTimeLabel,
                fontSize = 8.sp,
                lineHeight = 9.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 1,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ShowCourseDetailSheet(
    dialogState: MutableState<List<WeekCourseView>>,
    multiAccountMode: Boolean,
) {
    val showList = dialogState.value
    if (showList.isEmpty()) return

    val first = showList.firstOrNull { it.thisWeek }
    val initPage = if (first == null) 0 else showList.indexOf(first)
    val pagerState = rememberPagerState(initialPage = initPage) { showList.size }
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { dialogState.value = emptyList() },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 28.dp),
        ) {
            // 多门冲突课程顶部切换胶囊条
            if (showList.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    showList.forEachIndexed { idx, course ->
                        FilterChip(
                            selected = pagerState.currentPage == idx,
                            onClick = {
                                scope.launch { pagerState.animateScrollToPage(idx) }
                            },
                            label = {
                                Text(
                                    text = "${idx + 1}. ${course.courseName}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth(),
            ) { page ->
                val course = showList[page]
                val accent = CourseColorHelper.accentText(course.backgroundColor)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    // 课程头部卡带
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CourseColorHelper.softContainer(course.backgroundColor, 0.96f),
                        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (course.thisWeek) accent else MaterialTheme.colorScheme.surfaceContainerHighest,
                                ) {
                                    Text(
                                        text = if (course.thisWeek) "本周课程" else "非本周",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (course.thisWeek) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    )
                                }
                                if (multiAccountMode && course.accountTitle.isNotBlank()) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.secondaryContainer,
                                    ) {
                                        Text(
                                            text = course.accountTitle,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        )
                                    }
                                }
                            }
                            Text(
                                text = course.courseName,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }

                    // 结构化详情行
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (course.teacher.isNotBlank()) {
                            DetailInfoRow(
                                icon = Icons.Rounded.Person,
                                label = "任课教师",
                                value = course.teacher,
                            )
                        }
                        if (course.location.isNotBlank()) {
                            DetailInfoRow(
                                icon = Icons.Rounded.LocationOn,
                                label = "上课地点",
                                value = course.location,
                                copyable = true,
                            )
                        }
                        DetailInfoRow(
                            icon = Icons.Rounded.DateRange,
                            label = "上课周次",
                            value = course.weekStr,
                        )
                        DetailInfoRow(
                            icon = Icons.Rounded.AccessTime,
                            label = "上课时间",
                            value = "${course.courseDayTime}  (${course.courseTime})",
                        )
                        if (course.extraData.isNotEmpty()) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                            course.extraData.forEach { extra ->
                                DetailInfoRow(
                                    icon = Icons.Rounded.Info,
                                    label = "附加信息",
                                    value = extra,
                                    copyable = true,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
