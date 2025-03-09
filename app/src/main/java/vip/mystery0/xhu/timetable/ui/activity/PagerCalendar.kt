package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.viewmodel.CalendarSheet
import vip.mystery0.xhu.timetable.viewmodel.CalendarSheetType
import java.time.LocalDate
import java.time.Month

val calendarTitleBar: TabTitle = @Composable { ext ->
    val viewModel = ext.viewModel
    val week = viewModel.week.collectAsState()

    val title = when {
        week.value <= 0 -> "还没有开学哦~"
        week.value > 20 -> "学期已结束啦~"
        else -> "第${week.value}周"
    }

    Text(text = title)
}

val calendarActions: TabAction = @Composable {
    true
}

@OptIn(ExperimentalFoundationApi::class)
val calendarContent: TabContent = @Composable { ext ->
    val viewModel = ext.viewModel

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3F)),
    ) {
        val calendarList by viewModel.calendarList.collectAsState()

        LazyColumn {
            calendarList.forEach { sheetWeek ->
                stickyHeader {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(0.8F)),
                    ) {
                        Text(
                            text = sheetWeek.title,
                            modifier = Modifier.padding(
                                start = paddingStart,
                                end = 4.dp,
                                top = 8.dp,
                                bottom = 8.dp,
                            ),
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp,
                        )
                    }
                }
                items(sheetWeek.items) {
                    if (it.type == CalendarSheetType.MONTH_HEADER) {
                        BuildCalendarMonthHeader(sheet = it)
                    } else {
                        BuildCalendarDay(sheet = it)
                    }
                }
            }
        }
    }
}

@Composable
fun BuildCalendarMonthHeader(sheet: CalendarSheet) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(176.dp)
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(getMonthImageResId(sheet.date))
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier.fillMaxSize(),
        )
        Text(
            text = sheet.title,
            modifier = Modifier.padding(start = paddingStart, top = 12.dp),
            color = Color.Black,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
fun BuildCalendarDay(sheet: CalendarSheet) {
    Row(modifier = Modifier.padding(vertical = 12.dp)) {
        var backgroundColor = Color.Transparent
        var color = MaterialTheme.colorScheme.onSurface
        if (sheet.today) {
            backgroundColor = MaterialTheme.colorScheme.primaryContainer
            color = MaterialTheme.colorScheme.onPrimaryContainer
        }
        Box(
            modifier = Modifier.width(paddingStart),
            contentAlignment = Alignment.TopCenter,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(backgroundColor, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = sheet.title,
                    fontSize = 12.sp,
                    color = color,
                    textAlign = TextAlign.Center,
                )
            }
        }
        Column(
            modifier = Modifier.weight(1F),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            sheet.items.forEach {
                Card(
                    modifier = Modifier
                        .padding(end = 12.dp)
                        .fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = it.color.copy(alpha = 0.8F),
                    ),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(0.dp),
                ) {
                    val contentColor = Color.White
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = it.title,
                            fontWeight = FontWeight.Bold,
                            color = contentColor,
                        )
                        if (it.subtitle.isNotBlank()) {
                            Text(
                                text = it.subtitle,
                                color = contentColor,
                            )
                        }
                        Text(
                            text = it.text,
                            color = contentColor,
                        )
                    }
                }
            }
        }
    }
}

private val paddingStart = 64.dp

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
private fun getMonthImageResId(date: LocalDate): Int =
    when (date.month) {
        Month.JANUARY -> R.mipmap.ic_month1
        Month.FEBRUARY -> R.mipmap.ic_month2
        Month.MARCH -> R.mipmap.ic_month3
        Month.APRIL -> R.mipmap.ic_month4
        Month.MAY -> R.mipmap.ic_month5
        Month.JUNE -> R.mipmap.ic_month6
        Month.JULY -> R.mipmap.ic_month7
        Month.AUGUST -> R.mipmap.ic_month8
        Month.SEPTEMBER -> R.mipmap.ic_month9
        Month.OCTOBER -> R.mipmap.ic_month10
        Month.NOVEMBER -> R.mipmap.ic_month11
        Month.DECEMBER -> R.mipmap.ic_month12
    }
