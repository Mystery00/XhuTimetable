package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.config.store.Formatter
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.trackEvent
import vip.mystery0.xhu.timetable.ui.activity.loading.LoadingButton
import vip.mystery0.xhu.timetable.ui.activity.loading.LoadingValue
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.HolidayView
import vip.mystery0.xhu.timetable.viewmodel.TodayCourseSheet
import vip.mystery0.xhu.timetable.viewmodel.TodayThingSheet

val todayCourseTitleBar: TabTitle = @Composable { ext ->
    val viewModel = ext.viewModel
    val title = viewModel.todayTitle.collectAsState()
    val loading by viewModel.loading.collectAsState()
    Text(
        text = title.value,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .align(Alignment.CenterStart)
            .padding(start = 8.dp),
    )
    Row(
        modifier = Modifier
            .fillMaxHeight()
            .align(Alignment.CenterEnd)
    ) {
        if (loading) {
            LoadingButton(
                loadingValue = LoadingValue.Loading,
                modifier = Modifier
                    .fillMaxHeight(),
            ) {}
        }
        IconButton(
            onClick = {
                ext.addDialogState.show()
            },
            modifier = Modifier
                .fillMaxHeight()
        ) {
            Icon(
                painter = XhuIcons.Action.addCircle,
                contentDescription = null,
            )
        }
    }
}

@ExperimentalMaterialApi
val todayCourseContent: TabContent = @Composable { ext ->
    val activity = ext.activity
    val viewModel = ext.viewModel
    val modalBottomSheetState = ext.modalBottomSheetState

    Box {
        val poems by viewModel.poems.collectAsState()
        val holiday by viewModel.holiday.collectAsState()
        val todayThingList by viewModel.todayThing.collectAsState()
        val todayCourseList by viewModel.todayCourse.collectAsState()
        val scope = rememberCoroutineScope()

        if (poems == null && todayThingList.isEmpty() && todayCourseList.isEmpty()) {
            activity.BuildNoDataLayout()
        } else {
            DrawLine()
            val multiAccountMode by viewModel.multiAccountMode.collectAsState()
            val showStatus by viewModel.showStatus.collectAsState()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                poems?.let { value ->
                    DrawPoemsCard(modalBottomSheetState, scope, poems = value)
                }
                holiday?.let { holiday ->
                    DrawHoliday(holiday = holiday)
                }
                todayThingList.forEach {
                    DrawThingCard(thing = it, multiAccountMode = multiAccountMode)
                }
                todayCourseList.forEach {
                    DrawCourseCard(
                        course = it,
                        multiAccountMode = multiAccountMode,
                        showStatus = showStatus
                    )
                }
            }
        }
    }
}

@Composable
private fun DrawLine() {
    Box(
        modifier = Modifier
            .padding(horizontal = 10.dp)
            .fillMaxHeight()
            .width(1.dp)
            .background(color = Color.White)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@ExperimentalMaterialApi
@Composable
private fun DrawPoemsCard(dialogState: ModalBottomSheetState, scope: CoroutineScope, poems: Poems) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(9.dp),
            color = ColorPool.random
        ) {}
        Card(
            onClick = {
                scope.launch {
                    trackEvent("点击今日诗词")
                    dialogState.show()
                }
            },
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
            ) {
                Text(
                    text = poems.content,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "——${poems.origin.author}《${poems.origin.title}》",
                    fontSize = 12.sp,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun DrawHoliday(holiday: HolidayView) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(9.dp),
            color = holiday.color,
        ) {}
        Card(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
        ) {
            Box {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        top = 8.dp,
                        bottom = 8.dp,
                        end = 8.dp
                    ),
                ) {
                    Icon(
                        painter = XhuIcons.todayWaterMelon,
                        contentDescription = null,
                        tint = holiday.color,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1F)
                    ) {
                        Text(
                            text = holiday.showTitle,
                            fontWeight = FontWeight.Bold,
                            color = holiday.color,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                        )
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun DrawThingCard(
    thing: TodayThingSheet,
    multiAccountMode: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(9.dp),
            color = thing.color,
        ) {}
        Card(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
        ) {
            Box {
                if (multiAccountMode) {
                    Text(
                        text = thing.accountTitle,
                        fontSize = 8.sp,
                        color = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = RoundedCornerShape(bottomStart = 4.dp),
                            )
                            .padding(1.dp)
                            .align(Alignment.TopEnd),
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(
                        top = if (multiAccountMode) 16.dp else 8.dp,
                        bottom = 8.dp,
                        end = 8.dp
                    ),
                ) {
                    Icon(
                        painter = XhuIcons.todayWaterMelon,
                        contentDescription = null,
                        tint = thing.color,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp),
                    )
                    Column(
                        modifier = Modifier
                            .weight(1F)
                    ) {
                        Text(
                            text = thing.title,
                            fontWeight = FontWeight.Bold,
                            color = thing.color,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                        )
                        Text(
                            text = thing.timeText,
                            color = thing.color.copy(alpha = 0.8F),
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                        )
                        if (thing.location.isNotBlank()) {
                            Text(
                                text = thing.location,
                                color = thing.color.copy(alpha = 0.8F),
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                            )
                        }
                        if (thing.remark.isNotBlank()) {
                            Text(
                                text = thing.remark,
                                color = thing.color.copy(alpha = 0.6F),
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                            )
                        }
                    }
                    if (thing.saveAsCountDown) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(start = 4.dp),
                        ) {
                            if (thing.remainDays <= 0L) {
                                //今天
                                Text(
                                    text = "就在\n今天",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = thing.color,
                                )
                            } else {
                                //还没到
                                Text(
                                    text = "还剩",
                                    fontSize = 12.sp,
                                )
                                Text(
                                    text = thing.remainDays.toString(),
                                    fontSize = 24.sp,
                                    modifier = Modifier
                                        .padding(vertical = 2.dp),
                                    textAlign = TextAlign.Center,
                                    fontWeight = FontWeight.Bold,
                                    color = thing.color,
                                )
                                Text(
                                    text = "天",
                                    fontSize = 12.sp,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterialApi
@Composable
private fun DrawCourseCard(
    course: TodayCourseSheet,
    multiAccountMode: Boolean,
    showStatus: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(9.dp),
            color = course.color
        ) {}
        Card(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
        ) {
            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Icon(
                        painter = XhuIcons.todayWaterMelon,
                        contentDescription = null,
                        tint = course.color,
                        modifier = Modifier
                            .size(16.dp),
                    )
                    Text(
                        text = course.timeText.first.format(Formatter.TIME_NO_SECONDS),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(vertical = 1.dp),
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = course.timeText.second.format(Formatter.TIME_NO_SECONDS),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .padding(vertical = 1.dp),
                        textAlign = TextAlign.Center,
                    )
                }
                Surface(
                    shape = RoundedCornerShape(36.dp),
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .fillMaxHeight()
                        .width(4.dp),
                    color = course.color
                ) {}
                Box {
                    if (multiAccountMode) {
                        Text(
                            text = course.accountTitle,
                            fontSize = 8.sp,
                            color = MaterialTheme.colorScheme.onSecondary,
                            modifier = Modifier
                                .background(
                                    color = MaterialTheme.colorScheme.secondary,
                                    shape = RoundedCornerShape(bottomStart = 4.dp),
                                )
                                .padding(1.dp)
                                .align(Alignment.TopEnd),
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .padding(
                                top = if (multiAccountMode) 16.dp else 8.dp,
                                bottom = if (showStatus) 24.dp else 8.dp,
                                end = 8.dp
                            ),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 1.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = course.courseName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1F),
                                )
                                Text(
                                    text = course.timeString,
                                    fontSize = 14.sp,
                                )
                            }
                            Text(
                                text = course.teacherName,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                            )
                            Text(
                                text = course.location,
                                fontSize = 12.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 1.dp),
                            )
                        }
                    }
                    if (showStatus) {
                        Text(
                            text = course.courseStatus.title,
                            fontSize = 12.sp,
                            color = course.courseStatus.color,
                            modifier = Modifier
                                .background(
                                    color = course.courseStatus.backgroundColor,
                                    shape = RoundedCornerShape(topStart = 4.dp),
                                )
                                .padding(2.dp)
                                .align(Alignment.BottomEnd),
                        )
                    }
                }
            }
        }
    }
}