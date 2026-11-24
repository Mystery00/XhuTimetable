package vip.mystery0.xhu.timetable.ui.component.dialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import vip.mystery0.xhu.timetable.utils.formatChina
import vip.mystery0.xhu.timetable.utils.now

private val WEEKDAY_LABELS = listOf("一", "二", "三", "四", "五", "六", "日")

private fun daysInMonth(year: Int, month: Int): Int = when (month) {
    1, 3, 5, 7, 8, 10, 12 -> 31
    4, 6, 9, 11 -> 30
    2 -> if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) 29 else 28
    else -> 30
}

/**
 * 跨平台自绘日期选择对话框（Android / iOS 完全一致的紧凑卡片日历，支持年份网格快选与一键回到今天）
 */
@Composable
fun XhuDatePickerDialog(
    state: XhuDialogState,
    title: String = "选择日期",
    initialDate: LocalDate,
    yearRange: IntRange = 2015..2040,
    extraButtonText: String? = null,
    onExtraButtonClick: (() -> Unit)? = null,
    onSelectDate: (LocalDate) -> Unit,
) {
    if (!state.visible) return

    val today = remember { LocalDate.now() }
    var selectedDate by remember(state.visible, initialDate) { mutableStateOf(initialDate) }
    var displayYear by remember(state.visible, initialDate) { mutableIntStateOf(initialDate.year) }
    var displayMonth by remember(state.visible, initialDate) { mutableIntStateOf(initialDate.month.number) }
    var pickingYear by remember(state.visible) { mutableStateOf(false) }

    fun shiftMonth(delta: Int) {
        var m = displayMonth + delta
        var y = displayYear
        while (m < 1) {
            m += 12
            y -= 1
        }
        while (m > 12) {
            m -= 12
            y += 1
        }
        if (y in yearRange) {
            displayYear = y
            displayMonth = m
        }
    }

    XhuDialog(
        state = state,
        title = title,
        icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = null) },
        extraButtonText = extraButtonText,
        onExtraButtonClick = onExtraButtonClick?.let {
            {
                it()
                state.hide()
            }
        },
        dismissButtonText = "取消",
        confirmButtonText = "确定",
        onConfirmButtonClick = {
            onSelectDate(selectedDate)
            state.hide()
        },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            // 已选日期预览卡带 + 一键今天
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "${selectedDate.year}年",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.78f),
                        )
                        Text(
                            text = "${selectedDate.month.number}月${selectedDate.day}日  ${selectedDate.dayOfWeek.formatChina()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                    Surface(
                        onClick = {
                            if (today.year in yearRange) {
                                selectedDate = today
                                displayYear = today.year
                                displayMonth = today.month.number
                                pickingYear = false
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                    ) {
                        Text(
                            text = "今天",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        )
                    }
                }
            }

            // 年月导航条
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Surface(
                    onClick = { pickingYear = !pickingYear },
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.7f),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            text = "${displayYear}年 ${displayMonth}月",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Icon(
                            imageVector = if (pickingYear) {
                                Icons.Rounded.KeyboardArrowUp
                            } else {
                                Icons.Rounded.KeyboardArrowDown
                            },
                            contentDescription = "选择年份",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                if (!pickingYear) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { shiftMonth(-1) },
                            enabled = displayYear > yearRange.first || displayMonth > 1,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "上个月",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            onClick = { shiftMonth(1) },
                            enabled = displayYear < yearRange.last || displayMonth < 12,
                            modifier = Modifier.size(34.dp),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = "下个月",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }

            // 日历主体 / 年份快选网格切换
            AnimatedContent(
                targetState = pickingYear,
                transitionSpec = {
                    fadeIn(tween(180, easing = FastOutSlowInEasing)) togetherWith
                        fadeOut(tween(140, easing = FastOutSlowInEasing))
                },
                label = "datePickerMode",
            ) { isYearMode ->
                if (isYearMode) {
                    val years = remember(yearRange) { yearRange.toList() }
                    val gridState = rememberLazyGridState()
                    LaunchedEffect(Unit) {
                        val idx = years.indexOf(displayYear).coerceAtLeast(0)
                        gridState.scrollToItem((idx - 3).coerceAtLeast(0))
                    }
                    LazyVerticalGrid(
                        state = gridState,
                        columns = GridCells.Fixed(3),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(258.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(years, key = { it }) { year ->
                            val isCurrent = year == displayYear
                            Surface(
                                onClick = {
                                    displayYear = year
                                    val maxD = daysInMonth(year, displayMonth)
                                    selectedDate = LocalDate(
                                        year,
                                        displayMonth,
                                        selectedDate.day.coerceAtMost(maxD),
                                    )
                                    pickingYear = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isCurrent) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f)
                                },
                                border = if (!isCurrent && year == today.year) {
                                    BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                                } else {
                                    null
                                },
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(
                                        text = "${year}年",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isCurrent) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurface
                                        },
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        // 星期栏（周一至周日）
                        Row(modifier = Modifier.fillMaxWidth()) {
                            WEEKDAY_LABELS.forEachIndexed { idx, label ->
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (idx >= 5) {
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 4.dp),
                                )
                            }
                        }

                        val firstDayOfMonth = LocalDate(displayYear, displayMonth, 1)
                        val leadingBlank = firstDayOfMonth.dayOfWeek.isoDayNumber - 1 // 周一=0..周日=6
                        val totalDays = daysInMonth(displayYear, displayMonth)

                        // 固定 6 行保证切月时弹窗高度完全稳定不跳动
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            for (row in 0 until 6) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    for (col in 0 until 7) {
                                        val cellIndex = row * 7 + col
                                        val dayNum = cellIndex - leadingBlank + 1
                                        if (dayNum in 1..totalDays) {
                                            val cellDate = LocalDate(displayYear, displayMonth, dayNum)
                                            val isSelected = cellDate == selectedDate
                                            val isToday = cellDate == today
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .then(
                                                            when {
                                                                isSelected -> Modifier.background(
                                                                    MaterialTheme.colorScheme.primary
                                                                )
                                                                isToday -> Modifier.border(
                                                                    width = 1.5.dp,
                                                                    color = MaterialTheme.colorScheme.primary,
                                                                    shape = CircleShape,
                                                                )
                                                                else -> Modifier
                                                            }
                                                        )
                                                        .clickable { selectedDate = cellDate },
                                                    contentAlignment = Alignment.Center,
                                                ) {
                                                    Text(
                                                        text = dayNum.toString(),
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = when {
                                                            isSelected || isToday -> FontWeight.Bold
                                                            else -> FontWeight.Normal
                                                        },
                                                        color = when {
                                                            isSelected -> MaterialTheme.colorScheme.onPrimary
                                                            isToday -> MaterialTheme.colorScheme.primary
                                                            else -> MaterialTheme.colorScheme.onSurface
                                                        },
                                                    )
                                                }
                                            }
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .height(40.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * 跨平台时间选择对话框（M3 TimePicker + 拨盘/键盘输入自由切换，24小时制）
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun XhuTimePickerDialog(
    state: XhuDialogState,
    title: String = "选择时间",
    initialTime: LocalTime,
    extraButtonText: String? = null,
    onExtraButtonClick: (() -> Unit)? = null,
    onSelectTime: (LocalTime) -> Unit,
) {
    if (!state.visible) return

    val timePickerState = rememberTimePickerState(
        initialHour = initialTime.hour,
        initialMinute = initialTime.minute,
        is24Hour = true,
    )
    var useKeyboardInput by remember(state.visible) { mutableStateOf(false) }

    XhuDialog(
        state = state,
        title = title,
        extraButtonText = extraButtonText,
        onExtraButtonClick = onExtraButtonClick?.let {
            {
                it()
                state.hide()
            }
        },
        dismissButtonText = "取消",
        confirmButtonText = "确定",
        onConfirmButtonClick = {
            onSelectTime(LocalTime(timePickerState.hour, timePickerState.minute))
            state.hide()
        },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (useKeyboardInput) {
                TimeInput(state = timePickerState)
            } else {
                TimePicker(state = timePickerState)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = { useKeyboardInput = !useKeyboardInput }) {
                Icon(
                    imageVector = if (useKeyboardInput) Icons.Rounded.Schedule else Icons.Rounded.Keyboard,
                    contentDescription = if (useKeyboardInput) "切换到时钟拨盘" else "切换到键盘输入",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = if (useKeyboardInput) "切换为拨盘选择" else "切换为键盘输入",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}
