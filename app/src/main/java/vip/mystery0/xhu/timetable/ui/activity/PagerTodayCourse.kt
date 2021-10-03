package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vip.mystery0.xhu.timetable.model.Course
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

val todayCourseTitle: TabTitle = @Composable { viewModel ->
    val title = viewModel.todayTitle.collectAsState()
    Text(text = title.value, modifier = Modifier.align(Alignment.Center))
}

@ExperimentalMaterialApi
val todayCourseContent: TabContent = @Composable { viewModel ->
    val poemsDialogState = remember { mutableStateOf<Poems?>(null) }
    val poems = viewModel.poems.collectAsState()
    val todayCourseList by viewModel.todayCourse.collectAsState()
    Box {
        DrawLine()
        LazyColumn(
            contentPadding = PaddingValues(vertical = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            poems.value?.let { value ->
                item {
                    DrawPoemsCard(poemsDialogState, poems = value)
                }
            }
            items(todayCourseList.size) { index ->
                DrawCourseCard(course = todayCourseList[index])
            }
        }
        ShowPoemsDialog(dialogState = poemsDialogState)
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
private fun DrawPoemsCard(dialogState: MutableState<Poems?>, poems: Poems) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(5.dp),
            color = ColorPool.hash(poems.content)
        ) {}
        Card(
            onClick = {
                dialogState.value = poems
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
private fun DrawCourseCard(course: Course) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp, bottom = 8.dp, end = 8.dp),
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
                            text = course.time,
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
        }
    }
}

@Composable
fun ShowPoemsDialog(dialogState: MutableState<Poems?>) {
    val poems = dialogState.value?.origin ?: return
    val dismiss = {
        dialogState.value = null
    }
    AlertDialog(onDismissRequest = dismiss,
        text = {
            Column {
                Text(
                    text = "《${poems.title}》",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "[${poems.dynasty}] ${poems.author}",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = poems.content.joinToString("\n"),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (!poems.translate.isNullOrEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = poems.translate.joinToString("\n"),
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }, confirmButton = {
            TextButton(onClick = dismiss) {
                Text(text = stringResource(id = android.R.string.ok))
            }
        })
}