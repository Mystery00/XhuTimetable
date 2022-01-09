package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
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
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.dateFormatter
import vip.mystery0.xhu.timetable.utils.thingDateTimeFormatter
import vip.mystery0.xhu.timetable.viewmodel.CustomThingSheet
import vip.mystery0.xhu.timetable.viewmodel.TodayCourseSheet

val todayCourseTitle: TabTitle = @Composable { ext ->
    val viewModel = ext.viewModel
    val title = viewModel.todayTitle.collectAsState()
    Text(text = title.value, modifier = Modifier.align(Alignment.Center))
}

@ExperimentalMaterialApi
val todayCourseContent: TabContent = @Composable { ext ->
    val activity = ext.activity
    val viewModel = ext.viewModel
    val modalBottomSheetState = ext.modalBottomSheetState

    Box {
        val poems by viewModel.poems.collectAsState()
        val todayThingList by viewModel.todayThing.collectAsState()
        val todayCourseList by viewModel.todayCourse.collectAsState()
        val scope = rememberCoroutineScope()

        if (poems == null && todayCourseList.isEmpty()) {
            activity.BuildNoCourseLayout()
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
                todayThingList.forEach {
                    DrawThingCard(customThingSheet = it, multiAccountMode = multiAccountMode)
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
            .padding(horizontal = 8.dp)
            .fillMaxHeight()
            .width(1.dp)
            .background(color = Color.White)
    )
}

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
                .size(5.dp),
            color = ColorPool.random
        ) {}
        Card(
            onClick = {
                scope.launch {
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
private fun DrawThingCard(
    customThingSheet: CustomThingSheet,
    multiAccountMode: Boolean,
) {
    val thing = customThingSheet.thing
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(5.dp),
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
                        text = "${customThingSheet.studentId}(${customThingSheet.userName})",
                        fontSize = 8.sp,
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colors.secondary,
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
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = thing.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                        )
                        val startText =
                            thing.startTime.format(if (thing.allDay) dateFormatter else thingDateTimeFormatter)
                        val endText =
                            thing.endTime.format(if (thing.allDay) dateFormatter else thingDateTimeFormatter)
                        Text(
                            text = "$startText - $endText",
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                        )
                        Text(
                            text = thing.location,
                            fontSize = 12.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp),
                        )
                        Text(
                            text = thing.remark,
                            fontSize = 12.sp,
                            fontStyle = FontStyle.Italic,
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
                .size(5.dp),
            color = course.color
        ) {}
        Card(
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
        ) {
            Box {
                if (multiAccountMode) {
                    Text(
                        text = "${course.studentId}(${course.userName})",
                        fontSize = 8.sp,
                        color = MaterialTheme.colors.onSecondary,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colors.secondary,
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
                        bottom = if (showStatus) 24.dp else 8.dp,
                        end = 8.dp
                    ),
                ) {
                    Icon(
                        painter = XhuIcons.todayWaterMelon,
                        contentDescription = null,
                        tint = course.color,
                        modifier = Modifier
                            .padding(6.dp)
                            .size(16.dp),
                    )
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
                            text = course.time,
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