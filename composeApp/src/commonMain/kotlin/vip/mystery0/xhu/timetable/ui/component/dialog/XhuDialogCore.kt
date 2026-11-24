package vip.mystery0.xhu.timetable.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * 跨平台统一对话框显示状态控制器（纯 UI 层，Android / iOS 共用）
 */
@Stable
class XhuDialogState(initialVisible: Boolean = false) {
    var visible by mutableStateOf(initialVisible)
        private set

    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }
}

@Composable
fun rememberXhuDialogState(initialVisible: Boolean = false): XhuDialogState =
    remember { XhuDialogState(initialVisible) }

/**
 * 统一风格 M3 对话框底座：参考 sheets-compose-dialogs 的现代卡片质感，结合 M3 Expressive 容器与动效
 */
@Composable
fun XhuDialog(
    state: XhuDialogState,
    title: String,
    subtitle: String? = null,
    icon: (@Composable () -> Unit)? = null,
    dismissOnBackPress: Boolean = true,
    dismissOnClickOutside: Boolean = true,
    onDismiss: () -> Unit = { state.hide() },
    extraButtonText: String? = null,
    onExtraButtonClick: (() -> Unit)? = null,
    dismissButtonText: String? = "取消",
    onDismissButtonClick: (() -> Unit)? = null,
    confirmButtonText: String? = null,
    confirmEnabled: Boolean = true,
    isDestructive: Boolean = false,
    onConfirmButtonClick: (() -> Unit)? = null,
    showDivider: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    if (!state.visible) return

    Dialog(
        onDismissRequest = {
            if (dismissOnClickOutside) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = dismissOnBackPress,
            dismissOnClickOutside = dismissOnClickOutside,
            usePlatformDefaultWidth = false,
        ),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp)
                .widthIn(max = 420.dp)
                .heightIn(max = 640.dp),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .padding(bottom = 4.dp)
                                .size(28.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            CompositionLocalProvider(
                                LocalContentColor provides if (isDestructive) {
                                    MaterialTheme.colorScheme.error
                                } else {
                                    MaterialTheme.colorScheme.primary
                                }
                            ) {
                                icon()
                            }
                        }
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (!subtitle.isNullOrBlank()) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                if (showDivider) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }

                // Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    content = content,
                )

                // Footer Actions
                val hasFooter = extraButtonText != null ||
                    dismissButtonText != null ||
                    confirmButtonText != null

                if (hasFooter) {
                    if (showDivider) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 20.dp, top = 8.dp, bottom = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (extraButtonText != null && onExtraButtonClick != null) {
                            TextButton(onClick = onExtraButtonClick) {
                                Text(text = extraButtonText)
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        if (dismissButtonText != null) {
                            TextButton(
                                onClick = {
                                    onDismissButtonClick?.invoke() ?: onDismiss()
                                }
                            ) {
                                Text(text = dismissButtonText)
                            }
                        }
                        if (confirmButtonText != null && onConfirmButtonClick != null) {
                            Button(
                                enabled = confirmEnabled,
                                onClick = onConfirmButtonClick,
                                colors = if (isDestructive) {
                                    ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError,
                                    )
                                } else {
                                    ButtonDefaults.buttonColors()
                                },
                            ) {
                                Text(text = confirmButtonText)
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.padding(bottom = 12.dp))
                }
            }
        }
    }
}
