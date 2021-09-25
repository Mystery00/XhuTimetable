package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import vip.mystery0.xhu.timetable.model.response.Poems

val todayCourseTitle: TabTitle = @Composable { viewModel ->
    val title = viewModel.todayTitle.collectAsState()
    Text(text = title.value)
}

@ExperimentalMaterialApi
val todayCourseContent: TabContent = @Composable { viewModel ->
    Box {
        DrawLine()
        val poems = viewModel.poems.collectAsState()
        poems.value?.let { value ->
            Card(onClick = {

            }) {
                DrawCard(poems = value)
            }
        }
    }
}

@Composable
private fun DrawLine() {
    Canvas(modifier = Modifier.fillMaxHeight()) {
        drawLine(
            start = Offset(8F, 0F),
            end = Offset(8F, size.height),
            color = Color.Blue
        )
    }
}

@Composable
private fun DrawCard(poems: Poems) {
    Text(text = poems.content)
}