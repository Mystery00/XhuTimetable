package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.maxkeppeler.sheets.option.OptionDialog
import com.maxkeppeler.sheets.option.models.DisplayMode
import com.maxkeppeler.sheets.option.models.Option
import com.maxkeppeler.sheets.option.models.OptionConfig
import com.maxkeppeler.sheets.option.models.OptionSelection
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.ui.component.HorizontalPagerIndicator
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.TabAction
import vip.mystery0.xhu.timetable.ui.component.TabContent
import vip.mystery0.xhu.timetable.ui.component.TabTitle
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatChina
import vip.mystery0.xhu.timetable.utils.now
import vip.mystery0.xhu.timetable.utils.pad2
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerMainViewModel
import vip.mystery0.xhu.timetable.viewmodel.WeekView
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_course_data
import kotlin.math.min

private val weekViewLightColor = Color(0xFF3FCAB8)
private val weekViewGrayColor = Color(0xFFCFDBDB)

val weekCourseTitleBar: TabTitle = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val week = viewModel.week.collectAsState()

    val title = when {
        week.value <= 0 -> "还没有开学哦~"
        week.value > 20 -> "学期已结束啦~"
        else -> "第${week.value}周"
    }

    Text(text = title)
}

val weekCourseActions: TabAction = @Composable {
    val pagerViewModel = koinViewModel<PagerMainViewModel>()
    val showWeekViewDialog by pagerViewModel.showWeekViewDialog.collectAsState()
    IconButton(
        onClick = {
            showWeekViewDialog.show()
        },
        modifier = Modifier
            .fillMaxHeight()
    ) {
        Icon(
            painter = XhuIcons.Action.weekView,
            contentDescription = null,
        )
    }
    true
}

val weekCourseContent: TabContent = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val weekView by viewModel.weekView.collectAsState()
    val currentWeek by viewModel.week.collectAsState()
    val tableCourse by viewModel.tableCourse.collectAsState()
    val courseDialogState = remember { mutableStateOf<List<WeekCourseView>>(emptyList()) }

    Box {
        if (tableCourse.isEmpty() || tableCourse.all { c -> c.isEmpty() || c.all { it.isEmptyInstance } }) {
            StateScreen(
                title = "本周没有课程哦~",
                subtitle = "哎呀，这周的课程表光秃秃的，一个都没有耶~\n是不是可以尽情玩耍啦！",
                imageRes = painterResource(Res.drawable.state_no_course_data),
                verticalArrangement = Arrangement.Top,
            )
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                //顶部日期栏
                Row(
                    modifier = Modifier
                        .background(Color.Black.copy(0.2F))
                ) {
                    val firstDay by viewModel.dateStart.collectAsState()
                    Text(
                        text = "${firstDay.month.number.pad2()}\n月",
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.weight(0.3F),
                        textAlign = TextAlign.Center,
                    )
                    for (i in 0..6) {
                        val thisDay = firstDay.plus(i, DateTimeUnit.DAY)
                        BuildDateItem(
                            week = thisDay.dayOfWeek.formatChina(),
                            date = if (thisDay.day == 1) "${thisDay.month.number.pad2()}月"
                            else "${thisDay.day.pad2()}日",
                            isToday = LocalDate.now().dayOfWeek.isoDayNumber == i + 1
                        )
                    }
                }
                //课程节次列表
                Row(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    val customUi by viewModel.customUi.collectAsState()
                    Column(
                        modifier = Modifier
                            .weight(0.3F)
                            .background(dateBackgroundColor)
                    ) {
                        for (time in 1..11) {
                            BuildTimeItem(time = time, customUi.weekItemHeight.dp)
                        }
                    }
                    if (tableCourse.isNotEmpty()) {
                        for (index in 0 until 7) {
                            Column(modifier = Modifier.weight(1F)) {
                                tableCourse.getOrElse(index) { emptyList() }.forEach { sheet ->
                                    if (!sheet.isEmpty()) {
                                        BuildWeekItem(
                                            customUi = customUi,
                                            backgroundColor = sheet.color,
                                            itemStep = sheet.step,
                                            title = sheet.showTitle,
                                            textColor = sheet.textColor,
                                            showMore = sheet.course.size > 1,
                                        ) {
                                            courseDialogState.value = sheet.course
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
        val multiAccountMode by viewModel.multiAccountMode.collectAsState()
        ShowCourseDialog(dialogState = courseDialogState, multiAccountMode = multiAccountMode)

        WeekViewDialog(weekView, currentWeek) {
            viewModel.changeCurrentWeek(it)
        }
    }
}

@Composable
private fun WeekViewDialog(
    weekView: List<WeekView>,
    currentWeek: Int,
    onWeekChange: (Int) -> Unit = {},
) {
    val pagerViewModel = koinViewModel<PagerMainViewModel>()
    val showWeekViewDialog by pagerViewModel.showWeekViewDialog.collectAsState()
    if (weekView.isEmpty()) {
        return
    }
    val options = weekView.map {
        Option(
            titleText = "第${it.weekNum}周",
            selected = currentWeek == it.weekNum,
            customView = { _ ->
                var modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
                if (it.thisWeek) {
                    modifier = modifier.border(
                        2.dp,
                        MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
                Column(
                    modifier = modifier.padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Canvas(
                        modifier = Modifier
                            .height(32.dp)
                            .width(32.dp)
                    ) {
                        val canvasHeight = size.height
                        val canvasWidth = size.width
                        //每一项大小
                        val itemHeight = canvasHeight / 5F
                        val itemWidth = canvasWidth / 5F
                        //圆心位置
                        val itemCenterHeight = itemHeight / 2F
                        val itemCenterWidth = itemWidth / 2F
                        //半径
                        val radius = min(itemCenterHeight, itemCenterWidth) - 1F
                        for (day in 0 until 5) {
                            for (time in 0 until 5) {
                                val light = it.array.getOrElse(time) { emptyArray() }
                                    .getOrElse(day) { false }
                                drawCircle(
                                    color = if (light) weekViewLightColor else weekViewGrayColor,
                                    center = Offset(
                                        x = itemWidth * time + itemCenterWidth,
                                        y = itemHeight * day + itemCenterHeight,
                                    ),
                                    radius = radius,
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "第${it.weekNum}周",
                        fontSize = 12.sp,
                    )
                }
            }
        )
    }
    OptionDialog(
        state = showWeekViewDialog,
        selection = OptionSelection.Single(
            options = options,
            withButtonView = false,
            onSelectOption = { index, _ ->
                onWeekChange(weekView[index].weekNum)
            }
        ),
        config = OptionConfig(
            mode = DisplayMode.GRID_VERTICAL,
            gridColumns = 4,
        )
    )
}

@Composable
fun BuildWeekItem(
    customUi: CustomUi,
    backgroundColor: Color,
    itemStep: Int,
    title: String,
    textColor: Color,
    showMore: Boolean,
    onClick: () -> Unit,
) {
    val showBackgroundColor =
        if (backgroundColor == XhuColor.notThisWeekBackgroundColor) {
            MaterialTheme.colorScheme.surfaceVariant
        } else {
            backgroundColor.copy(customUi.weekBackgroundAlpha)
        }
    val showTextColor =
        if (backgroundColor == XhuColor.notThisWeekBackgroundColor) {
            MaterialTheme.colorScheme.onSurfaceVariant
        } else {
            textColor
        }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(customUi.weekItemHeight.dp * itemStep)
            .padding(1.dp)
            .background(
                showBackgroundColor,
                RoundedCornerShape(customUi.weekItemCorner),
            )
            .clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            color = showTextColor,
            textAlign = TextAlign.Center,
            fontSize = customUi.weekTitleTextSize.sp,
            modifier = Modifier.fillMaxSize(),
        )
        if (showMore) {
            Icon(
                painter = XhuIcons.conflict,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(6.dp),
            )
        }
    }
}

private val dateBackgroundColor = Color(0x2E000000)
private val highlightDateBackgroundColor = Color(0x80000000)

@Composable
private fun RowScope.BuildDateItem(week: String, date: String, isToday: Boolean = false) {
    Text(
        text = "${week}\n${date}",
        color = Color.White,
        textAlign = TextAlign.Center,
        fontSize = 10.sp,
        modifier = Modifier
            .background(if (isToday) highlightDateBackgroundColor else Color.Transparent)
            .weight(1F),
    )
}

@Composable
private fun BuildTimeItem(time: Int, itemHeight: Dp) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = time.toString(),
            color = Color.White,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun BoxScope.ShowCourseDialog(
    dialogState: MutableState<List<WeekCourseView>>,
    multiAccountMode: Boolean,
) {
    val showList = dialogState.value
    if (showList.isEmpty()) return
    val first = showList.firstOrNull { it.thisWeek }
    val initPage = if (first == null) 0 else showList.indexOf(first)
    val pagerState = rememberPagerState(initialPage = initPage) { showList.size }
    Dialog(onDismissRequest = {
        dialogState.value = emptyList()
    }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.height(240.dp),
            ) { page ->
                val course = showList[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp, vertical = 8.dp)
                        .background(course.backgroundColor.copy(0.8F), RoundedCornerShape(12.dp)),
                ) {
                    SelectionContainer {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = course.courseName,
                                maxLines = 2,
                                fontSize = 18.sp,
                                overflow = TextOverflow.Ellipsis,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                style = LocalTextStyle.current.copy(
                                    fontWeight = FontWeight.Bold,
                                ),
                            )
                            Text(
                                text = course.teacher,
                                color = Color.White,
                            )
                            Text(
                                text = course.location,
                                color = Color.White,
                            )
                            Text(
                                text = course.weekStr,
                                color = Color.White,
                            )
                            Text(
                                text = course.courseTime,
                                color = Color.White,
                            )
                            if (course.extraData.isNotEmpty()) {
                                for (data in course.extraData) {
                                    Text(
                                        text = data,
                                        color = Color.White,
                                    )
                                }
                            }
                            if (multiAccountMode) {
                                Text(
                                    text = course.accountTitle,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(4.dp),
                                        )
                                        .padding(2.dp),
                                )
                            }
                        }
                    }
                }
            }
            HorizontalPagerIndicator(
                pageCount = showList.size,
                pagerState = pagerState,
                modifier = Modifier.align(Alignment.CenterHorizontally),
                activeColor = Color.LightGray,
                indicatorWidth = 8.dp,
                indicatorHeight = 8.dp,
            )
        }
    }
}
