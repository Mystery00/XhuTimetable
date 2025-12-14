package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.ui.component.TabAction
import vip.mystery0.xhu.timetable.ui.component.TabContent
import vip.mystery0.xhu.timetable.ui.component.TabTitle
import vip.mystery0.xhu.timetable.ui.component.loadCoilModelWithoutCache
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.CalendarSheet
import vip.mystery0.xhu.timetable.viewmodel.CalendarSheetType
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PracticalCourseShowView
import xhutimetable.composeapp.generated.resources.Res

val calendarTitleBar: TabTitle = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val week = viewModel.week.collectAsState()

    val title = when {
        week.value <= 0 -> "还没有开学哦~"
        week.value > 20 -> "学期已结束啦~"
        else -> "第${week.value}周"
    }

    Text(text = title)
}

val calendarActions: TabAction = @Composable {
    true
}

val calendarContent: TabContent = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val openBottomSheet = rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3F)),
    ) {
        val calendarList by viewModel.calendarList.collectAsState()
        val practicalCourseList by viewModel.practicalCourseList.collectAsState()

        val listState = rememberLazyListState()
        var isFabVisible by remember { mutableStateOf(true) }

        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    // available.y < 0 表示手指向上划动（页面内容向下滚动） -> 隐藏
                    // available.y > 0 表示手指向下划动（页面内容向上滚动） -> 显示
                    if (available.y < -5f) { // 设置一个小阈值防止抖动
                        isFabVisible = false
                    } else if (available.y > 5f) {
                        isFabVisible = true
                    }
                    return Offset.Zero // 不消耗滚动事件，让 LazyColumn 继续处理
                }
            }
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.nestedScroll(nestedScrollConnection)
        ) {
            calendarList.forEach { sheetWeek ->
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(0.8F)),
                    ) {
                        Text(
                            text = sheetWeek.title,
                            modifier = Modifier.padding(
                                start = paddingStart,
                                end = 4.dp,
                                top = 8.dp,
                                bottom = 8.dp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
                items(sheetWeek.items) {
                    if (it.type == CalendarSheetType.MONTH_HEADER) {
                        BuildCalendarMonthHeader(sheet = it)
                    } else {
                        BuildCalendarDay(sheet = it)
                    }
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        ) {
            AnimatedVisibility(
                visible = isFabVisible,
                // 进入动画：从下方滑入 + 放大 + 淡入
                enter = slideInVertically(initialOffsetY = { it * 2 }) +
                        scaleIn(animationSpec = spring(stiffness = Spring.StiffnessMediumLow)) +
                        fadeIn(),
                // 退出动画：向下方滑出 + 缩小 + 淡出
                exit = slideOutVertically(targetOffsetY = { it * 2 }) +
                        scaleOut() +
                        fadeOut(),
            ) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(text = "查看实践课程")
                    },
                    onClick = {
                        openBottomSheet.value = true
                    },
                    icon = {
                        Icon(
                            painter = XhuIcons.Action.practicalCourse,
                            contentDescription = null,
                        )
                    }
                )
            }
        }

        PracticalCourseSheet(
            openBottomSheet = openBottomSheet.value,
            practicalCourseList = practicalCourseList,
        ) {
            openBottomSheet.value = false
        }
    }
}

@Composable
private fun PracticalCourseSheet(
    openBottomSheet: Boolean,
    practicalCourseList: List<PracticalCourseShowView>,
    onDismissRequest: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    if (openBottomSheet && practicalCourseList.isNotEmpty()) {
        ModalBottomSheet(
            onDismissRequest = onDismissRequest,
            sheetState = sheetState,
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    Text(
                        text = "实践课程列表",
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                items(practicalCourseList) {
                    Card(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = it.color,
                        ),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.elevatedCardElevation(0.dp),
                    ) {
                        val contentColor = Color.White
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = it.courseName,
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                            )
                            Text(
                                text = it.teacherName,
                                color = contentColor,
                            )
                            Text(
                                text = it.showWeek,
                                color = contentColor,
                            )
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
            .height(176.dp)
    ) {
        AsyncImage(
            model = loadCoilModelWithoutCache(getMonthImageResId(sheet.date)),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = sheet.title,
            modifier = Modifier.padding(start = paddingStart, top = 12.dp),
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun BuildCalendarDay(sheet: CalendarSheet) {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        var backgroundColor = Color.Transparent
        var color = MaterialTheme.colorScheme.onSurface
        if (sheet.today) {
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
            color = MaterialTheme.colorScheme.onPrimaryContainer
        }
        Box(
            modifier = Modifier.width(paddingStart),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = sheet.title,
                    fontSize = 12.sp,
                    color = color,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            sheet.items.forEach {
                Card(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = it.color.copy(alpha = 0.8F),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(0.dp),
                ) {
                    val contentColor = Color.White
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = it.title,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        )
                        if (it.subtitle.isNotBlank()) {
                            Text(
                                text = it.subtitle,
                                color = contentColor,
                            )
                        }
                        Text(
                            text = it.text,
                            color = contentColor,
                        )
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
