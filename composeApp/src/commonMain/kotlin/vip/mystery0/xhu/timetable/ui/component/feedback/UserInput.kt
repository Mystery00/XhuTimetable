package vip.mystery0.xhu.timetable.ui.component.feedback

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.outlined.Mood
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserInput(
    onMessageSent: (String) -> Unit,
    modifier: Modifier = Modifier,
    resetScroll: () -> Unit = {},
) {
    var showEmojiPanel by rememberSaveable { mutableStateOf(false) }
    var textState by remember { mutableStateOf(TextFieldValue()) }
    val keyboardController = LocalSoftwareKeyboardController.current

    fun sendCurrentMessage() {
        val trimmed = textState.text.trim()
        if (trimmed.isEmpty()) return
        onMessageSent(trimmed)
        textState = TextFieldValue()
        resetScroll()
        showEmojiPanel = false
    }

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.windowInsetsPadding(
                WindowInsets.navigationBars.union(WindowInsets.ime)
            )
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                IconButton(
                    onClick = {
                        showEmojiPanel = !showEmojiPanel
                        if (showEmojiPanel) keyboardController?.hide()
                    },
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = if (showEmojiPanel) Icons.Rounded.Keyboard else Icons.Outlined.Mood,
                        contentDescription = if (showEmojiPanel) "关闭表情" else "输入表情",
                        tint = if (showEmojiPanel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLowest,
                    border = BorderStroke(
                        0.8.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 42.dp, max = 128.dp),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        contentAlignment = Alignment.CenterStart,
                    ) {
                        val contentColor = MaterialTheme.colorScheme.onSurface
                        BasicTextField(
                            value = textState,
                            onValueChange = { textState = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { state ->
                                    if (state.isFocused) {
                                        showEmojiPanel = false
                                        resetScroll()
                                    }
                                },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Send,
                            ),
                            keyboardActions = KeyboardActions(
                                onSend = { sendCurrentMessage() }
                            ),
                            maxLines = 5,
                            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            textStyle = MaterialTheme.typography.bodyMedium.copy(color = contentColor),
                        )
                        if (textState.text.isEmpty()) {
                            Text(
                                text = "输入你的问题或建议…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                        }
                    }
                }

                val canSend = textState.text.isNotBlank()
                FilledIconButton(
                    enabled = canSend,
                    onClick = { sendCurrentMessage() },
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    ),
                    modifier = Modifier.size(42.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "发送",
                        modifier = Modifier.size(19.dp),
                    )
                }
            }

            AnimatedVisibility(visible = showEmojiPanel) {
                Surface(color = MaterialTheme.colorScheme.surface) {
                    Column {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 200.dp)
                                .verticalScroll(rememberScrollState())
                                .padding(8.dp)
                        ) {
                            EmojiTable(
                                onTextAdded = { emoji ->
                                    textState = textState.addText(emoji)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun TextFieldValue.addText(newString: String): TextFieldValue {
    val start = this.selection.start.coerceIn(0, this.text.length)
    val end = this.selection.end.coerceIn(start, this.text.length)
    val newText = this.text.replaceRange(start, end, newString)
    val cursor = start + newString.length
    return this.copy(text = newText, selection = TextRange(cursor, cursor))
}

@Composable
private fun EmojiTable(
    onTextAdded: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        emojis.chunked(EMOJI_COLUMNS).forEach { rowEmojis ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                rowEmojis.forEach { emoji ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(onClick = { onTextAdded(emoji) })
                            .sizeIn(minWidth = 40.dp, minHeight = 40.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = emoji,
                            style = LocalTextStyle.current.copy(
                                fontSize = 20.sp,
                                textAlign = TextAlign.Center
                            )
                        )
                    }
                }
            }
        }
    }
}

private const val EMOJI_COLUMNS = 8

private val emojis = listOf(
    "\ud83d\ude00", "\ud83d\ude01", "\ud83d\ude02", "\ud83d\ude04",
    "\ud83d\ude05", "\ud83d\ude09", "\ud83d\ude0a", "\ud83d\ude0e",
    "\ud83d\ude0d", "\ud83d\ude18", "\ud83e\udd17", "\ud83e\udd14",
    "\ud83d\ude10", "\ud83d\ude44", "\ud83d\ude0f", "\ud83d\ude25",
    "\ud83d\ude2e", "\ud83d\ude34", "\ud83d\ude0c", "\ud83d\ude1c",
    "\ud83d\ude14", "\ud83d\ude15", "\ud83d\ude43", "\ud83d\ude37",
    "\ud83d\ude22", "\ud83d\ude2d", "\ud83d\ude31", "\ud83d\ude21",
    "\ud83d\udc4d", "\ud83d\ude4f", "\ud83c\udf89", "\u2764\ufe0f",
)
