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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp

private const val TAG = "CourseIndex"

@Composable
fun CourseIndexSelector(
    startIndex: Int,
    endIndex: Int,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = TextUnit.Unspecified,
    onIndexChange: (Int, Int) -> Unit = { _, _ -> },
) {
    val min = 1
    val max = 11
    require(startIndex >= min)
    require(endIndex <= max)
    val items = (min..max).toList()

    Card(
        modifier = modifier,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.inverseSurface),
    ) {
        Row {
            ScrollPicker(
                modifier = Modifier
                    .weight(1F),
                items = items,
                initialItem = startIndex,
                textColor = MaterialTheme.colorScheme.primary,
                fontSize = fontSize,
                onItemSelected = { _, item ->
                    onIndexChange(item, endIndex)
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
                items = items,
                initialItem = endIndex,
                textColor = MaterialTheme.colorScheme.primary,
                fontSize = fontSize,
                onItemSelected = { _, item ->
                    onIndexChange(startIndex, item)
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