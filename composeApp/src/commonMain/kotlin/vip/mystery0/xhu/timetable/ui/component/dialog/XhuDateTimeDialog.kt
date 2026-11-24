package vip.mystery0.xhu.timetable.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

private const val MILLIS_PER_DAY = 86_400_000L

/**
 * 跨平台日期选择对话框（纯 M3 原生 DatePicker + 统一中文操作栏与可选 extraButton，Android/iOS 共用）
 */
@OptIn(ExperimentalMaterial3Api::class)
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

    val initialMillis = remember(initialDate) {
        initialDate.toEpochDays() * MILLIS_PER_DAY
    }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialMillis,
        yearRange = yearRange,
    )

    DatePickerDialog(
        onDismissRequest = { state.hide() },
        confirmButton = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (extraButtonText != null && onExtraButtonClick != null) {
                    TextButton(
                        onClick = {
                            onExtraButtonClick()
                            state.hide()
                        }
                    ) {
                        Text(extraButtonText)
                    }
                }
                TextButton(onClick = { state.hide() }) {
                    Text("取消")
                }
                Button(
                    enabled = datePickerState.selectedDateMillis != null,
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val epochDays = (millis / MILLIS_PER_DAY).toInt()
                            onSelectDate(LocalDate.fromEpochDays(epochDays))
                        }
                        state.hide()
                    }
                ) {
                    Text("确定")
                }
            }
        },
        dismissButton = {},
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                )
            },
        )
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
