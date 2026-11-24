package vip.mystery0.xhu.timetable.ui.component.dialog

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import kotlin.math.roundToInt

/**
 * 跨平台颜色选择对话框：支持精选预设色板 + 自定义 RGB 精调，实时预览对比度与色值
 */
@Composable
fun XhuColorPickerDialog(
    state: XhuDialogState,
    title: String = "选择颜色",
    initialColor: Color = ColorPool.safeGet(0),
    presetColors: List<Color> = ColorPool.allColors,
    extraButtonText: String? = null,
    onExtraButtonClick: (() -> Unit)? = null,
    onSelectColor: (Color) -> Unit,
) {
    if (!state.visible) return

    var selectedColor by remember(state.visible, initialColor) {
        mutableStateOf(initialColor)
    }
    var modeIndex by remember(state.visible) { mutableIntStateOf(0) }

    val hexString = remember(selectedColor) {
        val rgb = selectedColor.toArgb() and 0x00FFFFFF
        "#" + rgb.toString(16).uppercase().padStart(6, '0')
    }
    val onPreviewColor = if (selectedColor.luminance() > 0.55f) Color(0xFF1A1C1E) else Color.White

    XhuDialog(
        state = state,
        title = title,
        icon = { Icon(Icons.Rounded.Palette, contentDescription = null) },
        extraButtonText = extraButtonText,
        onExtraButtonClick = onExtraButtonClick?.let {
            {
                it()
                state.hide()
            }
        },
        dismissButtonText = "取消",
        confirmButtonText = "应用",
        onConfirmButtonClick = {
            onSelectColor(selectedColor)
            state.hide()
        },
    ) {
        // 顶部实时颜色预览胶囊
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(16.dp),
            color = selectedColor,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "预览效果",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = onPreviewColor,
                )
                Text(
                    text = hexString,
                    style = MaterialTheme.typography.labelLarge,
                    fontFamily = FontFamily.Monospace,
                    color = onPreviewColor.copy(alpha = 0.9f),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 模式切换：预设 / 自定义
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(),
        ) {
            listOf("精选色板", "自定义").forEachIndexed { index, label ->
                SegmentedButton(
                    selected = modeIndex == index,
                    onClick = { modeIndex = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                ) {
                    Text(label)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        AnimatedContent(
            targetState = modeIndex,
            label = "colorPickerMode",
        ) { mode ->
            if (mode == 0) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(4.dp),
                ) {
                    items(presetColors) { color ->
                        val isSelected = (color.toArgb() and 0x00FFFFFF) == (selectedColor.toArgb() and 0x00FFFFFF)
                        val checkTint = if (color.luminance() > 0.55f) Color(0xFF1A1C1E) else Color.White
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 2.5.dp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            shape = CircleShape,
                                        )
                                    } else {
                                        Modifier.border(
                                            width = 1.dp,
                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                            shape = CircleShape,
                                        )
                                    }
                                )
                                .clickable { selectedColor = color },
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = checkTint,
                                    modifier = Modifier.size(18.dp),
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
                    ColorChannelSlider(
                        label = "红 (R)",
                        value = selectedColor.red,
                        trackColor = Color(0xFFE53935),
                        onValueChange = { selectedColor = selectedColor.copy(red = it) },
                    )
                    ColorChannelSlider(
                        label = "绿 (G)",
                        value = selectedColor.green,
                        trackColor = Color(0xFF43A047),
                        onValueChange = { selectedColor = selectedColor.copy(green = it) },
                    )
                    ColorChannelSlider(
                        label = "蓝 (B)",
                        value = selectedColor.blue,
                        trackColor = Color(0xFF1E88E5),
                        onValueChange = { selectedColor = selectedColor.copy(blue = it) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorChannelSlider(
    label: String,
    value: Float,
    trackColor: Color,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${(value * 255).roundToInt()}",
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..1f,
            colors = SliderDefaults.colors(
                thumbColor = trackColor,
                activeTrackColor = trackColor,
            ),
        )
    }
}
