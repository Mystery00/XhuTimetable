package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowDropUp
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import vip.mystery0.xhu.timetable.model.Course
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalAnimationApi::class)
val weekCourseTitle: TabTitle = @Composable { viewModel ->
    val week = viewModel.week.collectAsState()
    val showWeekView by viewModel.showWeekView.collectAsState()
    val rotationAngle by animateFloatAsState(
        targetValue = if (showWeekView) 180F else 0F,
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
        Text(text = "第${week.value}周")
        Icon(
            imageVector = Icons.TwoTone.ArrowDropUp,
            contentDescription = null,
            modifier = Modifier.rotate(rotationAngle),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
val weekCourseContent: TabContent = @Composable { viewModel ->
    val courseDialogState = remember { mutableStateOf<List<Course>>(emptyList()) }
    val tableCourse by viewModel.tableCourse.collectAsState()
    Box {
        Column(modifier = Modifier.fillMaxSize()) {
            //顶部日期栏
            Row(
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .background(Color.Black.copy(0.1F))
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
                Column(
                    modifier = Modifier
                        .weight(0.3F)
                        .background(dateBackgroundColor)
                ) {
                    for (time in 1..11) {
                        BuildTimeItem(time = time)
                    }
                }
                if (tableCourse.isNotEmpty()) {
                    for (index in 0 until 7) {
                        Column(modifier = Modifier.weight(1F)) {
                            tableCourse[index].forEach { sheet ->
                                if (sheet != null) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(itemHeight * sheet.step)
                                            .padding(1.dp)
                                            .background(
                                                sheet.color.copy(0.8F),
                                                MaterialTheme.shapes.small
                                            )
                                            .clickable {
                                                courseDialogState.value = sheet.course
                                            },
                                    ) {
                                        Text(
                                            text = sheet.showTitle,
                                            color = sheet.textColor,
                                            textAlign = TextAlign.Center,
                                            fontSize = 10.sp,
                                            modifier = Modifier.fillMaxSize(),
                                        )
                                        if (sheet.course.size > 1) {
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
                                } else {
                                    Spacer(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(itemHeight)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        ShowCourseDialog(dialogState = courseDialogState)
    }
}

private val twoFormat = DecimalFormat("00")
private val dateBackgroundColor = Color(0x0F000000)
private val highlightDateBackgroundColor = Color(0x80000000)
private val itemHeight = 72.dp

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
private fun BuildTimeItem(time: Int) {
    Text(
        text = time.toString(),
        color = Color.White,
        textAlign = TextAlign.Center,
        fontSize = 10.sp,
        modifier = Modifier
            .fillMaxWidth()
            .height(itemHeight),
    )
}

@ExperimentalComposeUiApi
@OptIn(ExperimentalPagerApi::class)
@Composable
private fun BoxScope.ShowCourseDialog(dialogState: MutableState<List<Course>>) {
    val showList = dialogState.value
    if (showList.isNullOrEmpty()) return
    val first = showList.firstOrNull { it.thisWeek }
    val initPage = if (first == null) 0 else showList.indexOf(first)
    val pagerState = rememberPagerState(
        pageCount = showList.size,
        initialPage = initPage,
    )
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
                modifier = Modifier
                    .height(240.dp),
            ) { page ->
                val course = showList[page]
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 36.dp, vertical = 8.dp)
                        .background(course.color.copy(0.8F), RoundedCornerShape(12.dp)),
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
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = course.teacherName,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = course.location,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = course.weekString,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Text(
                        text = course.time,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                activeColor = Color.LightGray,
                indicatorWidth = 8.dp,
                indicatorHeight = 8.dp,
            )
        }
    }
}
