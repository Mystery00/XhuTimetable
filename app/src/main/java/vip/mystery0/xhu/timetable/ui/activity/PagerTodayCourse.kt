package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vip.mystery0.xhu.timetable.model.response.Poems
import vip.mystery0.xhu.timetable.ui.theme.ColorPool

val todayCourseTitle: TabTitle = @Composable { viewModel ->
    val title = viewModel.todayTitle.collectAsState()
    Text(text = title.value)
}

@ExperimentalMaterialApi
val todayCourseContent: TabContent = @Composable { viewModel ->
    Box {
        DrawLine()
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            val poems = viewModel.poems.collectAsState()
            poems.value?.let { value ->
                DrawCard(poems = value)
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
private fun DrawCard(poems: Poems) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            shape = CircleShape,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(5.dp),
            color = ColorPool.hash(poems.content)
        ) {}
        Card(
            onClick = {

            },
            modifier = Modifier
                .padding(end = 8.dp)
                .fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
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