package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Today
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.ui.component.TabAction
import vip.mystery0.xhu.timetable.ui.component.TabContent
import vip.mystery0.xhu.timetable.ui.component.TabTitle
import vip.mystery0.xhu.timetable.ui.theme.CourseColorHelper
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.isDarkMode
import vip.mystery0.xhu.timetable.viewmodel.CalendarSheet
import vip.mystery0.xhu.timetable.viewmodel.CalendarSheetType
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerMainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PracticalCourseShowView
import xhutimetable.composeapp.generated.resources.Res

val calendarTitleBar: TabTitle = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val pagerViewModel = koinViewModel<PagerMainViewModel>()
    val week by viewModel.week.collectAsState()
    val dynamicTitle by pagerViewModel.calendarDisplayTitle.collectAsState()

    val fallbackTitle = when {
        week <= 0 -> "月历日程"
        week > 20 -> "学期已结束"
        else -> "第${week}周"
    }

    Text(
        text = dynamicTitle ?: fallbackTitle,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

val calendarActions: TabAction = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val pagerViewModel = koinViewModel<PagerMainViewModel>()
    val practicalList by viewModel.practicalCourseList.collectAsState()

    IconButton(
        onClick = { pagerViewModel.showPracticalCourseSheet.value = true },
        modifier = Modifier.fillMaxHeight(),
    ) {
        BadgedBox(
            badge = {
                if (practicalList.isNotEmpty()) {
                    Badge(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    ) {
                        Text(practicalList.size.toString())
                    }
                }
            }
        ) {
            Icon(
                painter = XhuIcons.Action.practicalCourse,
                contentDescription = "查看实践课程",
            )
        }
    }
    true
}

val calendarContent: TabContent = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val pagerViewModel = koinViewModel<PagerMainViewModel>()

    val calendarList by viewModel.calendarList.collectAsState()
    val practicalCourseList by viewModel.practicalCourseList.collectAsState()
    val openPracticalSheet by pagerViewModel.showPracticalCourseSheet.collectAsState()
    val isDark = isDarkMode()

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // 计算扁平化索引：今天的位置以及每个索引对应的周次标题，方便自动滚到今天和联动顶栏
    val (todayFlatIndex, indexToWeekLabel) = remember(calendarList) {
        var flatIdx = 0
        var foundToday = -1
        var firstCursorWeek = -1
        val labels = HashMap<Int, String>()
        calendarList.forEach { sheetWeek ->
            val label = "第${sheetWeek.weekNum}周"
            if (sheetWeek.cursor && firstCursorWeek == -1) {
                firstCursorWeek = flatIdx
            }
            labels[flatIdx] = label
            flatIdx++ // stickyHeader
            sheetWeek.items.forEach { item ->
                if (item.today && foundToday == -1) {
                    foundToday = flatIdx
                }
                labels[flatIdx] = label
                flatIdx++
            }
        }
        val target = when {
            foundToday >= 0 -> foundToday
            firstCursorWeek >= 0 -> firstCursorWeek
            else -> 0
        }
        target to labels
    }

    var hasAutoScrolled by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(calendarList, todayFlatIndex) {
        if (calendarList.isNotEmpty() && !hasAutoScrolled && todayFlatIndex > 0) {
            listState.scrollToItem(todayFlatIndex)
            hasAutoScrolled = true
        }
    }

    // 滚动联动顶部周次标题
    val visibleWeekLabel by remember(indexToWeekLabel) {
        derivedStateOf { indexToWeekLabel[listState.firstVisibleItemIndex] }
    }
    LaunchedEffect(visibleWeekLabel) {
        pagerViewModel.calendarDisplayTitle.value = visibleWeekLabel
    }

    val showBackToToday by remember(todayFlatIndex) {
        derivedStateOf {
            todayFlatIndex > 0 && kotlin.math.abs(listState.firstVisibleItemIndex - todayFlatIndex) > 3
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = if (isDark) 0.48f else 0.55f)),
    ) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 28.dp),
        ) {
            calendarList.forEach { sheetWeek ->
                item(key = "week_header_${sheetWeek.weekNum}") {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 2.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.88f),
                    ) {
                        Text(
                            text = sheetWeek.title,
                            modifier = Modifier.padding(
                                start = paddingStart,
                                end = 16.dp,
                                top = 8.dp,
                                bottom = 8.dp,
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                items(sheetWeek.items) { sheet ->
                    if (sheet.type == CalendarSheetType.MONTH_HEADER) {
                        BuildCalendarMonthHeader(sheet = sheet)
                    } else {
                        BuildCalendarDay(sheet = sheet)
                    }
                }
            }
        }

        // 远离今天时在右下角轻巧浮现“回今天”按钮，不遮挡卡片主体
        androidx.compose.animation.AnimatedVisibility(
            visible = showBackToToday,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
        ) {
            SmallFloatingActionButton(
                onClick = {
                    scope.launch {
                        listState.animateScrollToItem(todayFlatIndex)
                    }
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Icon(
                    imageVector = Icons.Rounded.Today,
                    contentDescription = "回到今天",
                )
            }
        }

        PracticalCourseSheet(
            openBottomSheet = openPracticalSheet,
            practicalCourseList = practicalCourseList,
        ) {
            pagerViewModel.showPracticalCourseSheet.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PracticalCourseSheet(
    openBottomSheet: Boolean,
    practicalCourseList: List<PracticalCourseShowView>,
    onDismissRequest: () -> Unit,
) {
    if (!openBottomSheet) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = "本学期实践课程",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
            if (practicalCourseList.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "本学期暂无实践课程安排",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    items(practicalCourseList) { item ->
                        val accent = CourseColorHelper.accentText(item.color)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = CourseColorHelper.softContainer(item.color),
                            ),
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(0.8.dp, accent.copy(alpha = 0.35f)),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(IntrinsicSize.Min)
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(4.dp)
                                        .fillMaxHeight()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(accent)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                    Text(
                                        text = item.courseName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Text(
                                        text = "${item.teacherName} · ${item.showWeek}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
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

@Composable
fun BuildCalendarMonthHeader(sheet: CalendarSheet) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
    ) {
        AsyncImage(
            model = getMonthImageResId(sheet.date),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.38f),
                            Color.Transparent,
                        )
                    )
                )
        )
        Text(
            text = sheet.title,
            modifier = Modifier.padding(start = 18.dp, top = 14.dp),
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun BuildCalendarDay(sheet: CalendarSheet) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
    ) {
        val bgColor = if (sheet.today) MaterialTheme.colorScheme.primary else Color.Transparent
        val textColor = if (sheet.today) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
        Box(
            modifier = Modifier.width(paddingStart),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(bgColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = sheet.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = textColor,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sheet.items.forEach { item ->
                val accent = CourseColorHelper.accentText(item.color)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = CourseColorHelper.softContainer(item.color, 0.94f),
                    ),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.6.dp, accent.copy(alpha = 0.3f)),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Min)
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.5.dp)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(4.dp))
                                .background(accent)
                        )
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            if (item.subtitle.isNotBlank()) {
                                Text(
                                    text = item.subtitle,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = accent,
                                )
                            }
                            Text(
                                text = item.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

private val paddingStart = 64.dp

private fun getMonthImageResId(date: LocalDate): String =
    when (date.month) {
        Month.JANUARY -> Res.getUri("drawable/ic_month1.jpg")
        Month.FEBRUARY -> Res.getUri("drawable/ic_month2.jpg")
        Month.MARCH -> Res.getUri("drawable/ic_month3.jpg")
        Month.APRIL -> Res.getUri("drawable/ic_month4.jpg")
        Month.MAY -> Res.getUri("drawable/ic_month5.jpg")
        Month.JUNE -> Res.getUri("drawable/ic_month6.jpg")
        Month.JULY -> Res.getUri("drawable/ic_month7.jpg")
        Month.AUGUST -> Res.getUri("drawable/ic_month8.jpg")
        Month.SEPTEMBER -> Res.getUri("drawable/ic_month9.jpg")
        Month.OCTOBER -> Res.getUri("drawable/ic_month10.jpg")
        Month.NOVEMBER -> Res.getUri("drawable/ic_month11.jpg")
        Month.DECEMBER -> Res.getUri("drawable/ic_month12.jpg")
    }
