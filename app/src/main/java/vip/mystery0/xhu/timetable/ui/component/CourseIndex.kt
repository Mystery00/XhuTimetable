package vip.mystery0.xhu.timetable.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

@Composable
fun CourseIndexSelector(
    index: Pair<Int, Int>,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    onIndexChange: (Pair<Int, Int>) -> Unit = {},
) {
    val min = 1
    val max = 11
    require(index.first >= min)
    require(index.second <= max)
    require(index.first <= index.second)

    var startIndex by remember { mutableIntStateOf(index.first) }
    var endIndex by remember { mutableIntStateOf(index.second) }

    Card(
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.inverseSurface),
    ) {
        Row {
            ScrollPicker(
                modifier = Modifier
                    .weight(1F),
                items = (min..max).toList(),
                initialItem = startIndex,
                textColor = MaterialTheme.colorScheme.primary,
                fontSize = fontSize,
                onItemSelected = { _, item ->
                    startIndex = item
                    onIndexChange(Pair(startIndex, endIndex))
                },
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = " - ",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = fontSize,
                )
            }
            ScrollPicker(
                modifier = Modifier
                    .weight(1F),
                items = (min..max).toList(),
                initialItem = endIndex,
                textColor = MaterialTheme.colorScheme.primary,
                fontSize = fontSize,
                onItemSelected = { _, item ->
                    endIndex = item
                    onIndexChange(Pair(startIndex, endIndex))
                },
            )
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "节",
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = fontSize,
                )
            }
        }
    }
}