package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.pager.HorizontalPagerIndicator
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.WeekCourseView
import vip.mystery0.xhu.timetable.ui.theme.MaterialIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

val weekCourseTitle: TabTitle = @Composable { ext ->
    val viewModel = ext.viewModel
    val week = viewModel.week.collectAsState()
    val showWeekView by viewModel.showWeekView.collectAsState()
    val rotationAngle by animateFloatAsState(
        targetValue = if (showWeekView) 180F else 0F, label = "weekViewRotationAngle"
    )

    Row(
        modifier = Modifier
            .align(Alignment.Center)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
            ) {
                viewModel.animeWeekView()
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        val title = when {
            week.value <= 0 -> "还没有开学哦~"
            week.value > 20 -> "学期已结束啦~"
            else -> "第${week.value}周"
        }
        Text(text = title)
        Icon(
            imageVector = MaterialIcons.TwoTone.ArrowDropUp,
            contentDescription = null,
            modifier = Modifier.rotate(rotationAngle),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
val weekCourseContent: TabContent = @Composable { ext ->
    val viewModel = ext.viewModel
    val courseDialogState = remember { mutableStateOf<List<WeekCourseView>>(emptyList()) }
    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            //顶部日期栏
            Row(
                modifier = Modifier
                    .background(Color.Black.copy(0.2F))
            ) {
                val firstDay by viewModel.dateStart.collectAsState()
                Text(
                    text = "${twoFormat.format(firstDay.monthValue)}\n月",
                    color = Color.White,
                    fontSize = 10.sp,
                    modifier = Modifier.weight(0.3F),
                    textAlign = TextAlign.Center,
                )
                for (i in 0..6) {
                    val thisDay = firstDay.plusDays(i.toLong())
                    BuildDateItem(
                        week = thisDay.dayOfWeek.getDisplayName(
                            TextStyle.SHORT,
                            Locale.CHINESE
                        ),
                        date = if (thisDay.dayOfMonth == 1) "${twoFormat.format(thisDay.monthValue)}月" else "${
                            twoFormat.format(
                                thisDay.dayOfMonth
                            )
                        }日",
                        isToday = LocalDate.now().dayOfWeek.value == i + 1
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
                val tableCourse by viewModel.tableCourse.collectAsState()
                if (tableCourse.isNotEmpty()) {
                    for (index in 0 until 7) {
                        Column(modifier = Modifier.weight(1F)) {
                            tableCourse[index].forEach { sheet ->
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
        val multiAccountMode by viewModel.multiAccountMode.collectAsState()
        ShowCourseDialog(dialogState = courseDialogState, multiAccountMode = multiAccountMode)
    }
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
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(customUi.weekItemHeight.dp * itemStep)
            .padding(1.dp)
            .background(
                backgroundColor.copy(customUi.weekBackgroundAlpha),
                RoundedCornerShape(customUi.weekItemCorner),
            )
            .clickable(onClick = onClick),
    ) {
        Text(
            text = title,
            color = textColor,
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

private val twoFormat = DecimalFormat("00")
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

@OptIn(ExperimentalFoundationApi::class)
@ExperimentalComposeUiApi
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
                                    color = MaterialTheme.colors.onSecondary,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colors.secondary,
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
