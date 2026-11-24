package vip.mystery0.xhu.timetable.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * 跨平台二次确认对话框（支持常规确认与危险操作红色主按钮）
 */
@Composable
fun XhuConfirmDialog(
    state: XhuDialogState,
    title: String,
    message: String,
    icon: (@Composable () -> Unit)? = null,
    confirmText: String = "确定",
    dismissText: String = "取消",
    isDestructive: Boolean = false,
    onConfirm: () -> Unit,
) {
    XhuDialog(
        state = state,
        title = title,
        icon = icon,
        dismissButtonText = dismissText,
        confirmButtonText = confirmText,
        isDestructive = isDestructive,
        onConfirmButtonClick = {
            state.hide()
            onConfirm()
        },
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 跨平台信息提示对话框
 */
@Composable
fun XhuInfoDialog(
    state: XhuDialogState,
    title: String,
    message: String,
    icon: (@Composable () -> Unit)? = null,
    confirmText: String = "知道了",
) {
    XhuDialog(
        state = state,
        title = title,
        icon = icon,
        dismissButtonText = null,
        confirmButtonText = confirmText,
        onConfirmButtonClick = { state.hide() },
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * 跨平台模板/文本输入对话框（支持快捷插入变量 Chip 与重置默认）
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun XhuTemplateInputDialog(
    state: XhuDialogState,
    title: String,
    initialValue: String,
    resetValue: String? = null,
    placeholder: String = "请输入内容",
    quickTokens: List<Pair<String, String>> = emptyList(),
    onConfirm: (String) -> Unit,
) {
    var textValue by remember(state.visible, initialValue) { mutableStateOf(initialValue) }

    LaunchedEffect(state.visible, initialValue) {
        if (state.visible) {
            textValue = initialValue
        }
    }

    XhuDialog(
        state = state,
        title = title,
        extraButtonText = if (resetValue != null) "重置默认" else null,
        onExtraButtonClick = resetValue?.let { def ->
            {
                textValue = def
                onConfirm(def)
                state.hide()
            }
        },
        dismissButtonText = "取消",
        confirmButtonText = "保存",
        onConfirmButtonClick = {
            onConfirm(textValue)
            state.hide()
        },
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = textValue,
                onValueChange = { textValue = it },
                placeholder = { Text(placeholder) },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )
            if (quickTokens.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "点击插入变量",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    quickTokens.forEach { (label, token) ->
                        AssistChip(
                            onClick = { textValue += token },
                            label = { Text(label) },
                        )
                    }
                }
            }
        }
    }
}
