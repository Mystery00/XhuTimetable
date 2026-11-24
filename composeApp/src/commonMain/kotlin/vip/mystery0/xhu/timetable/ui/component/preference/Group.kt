package vip.mystery0.xhu.timetable.ui.component.preference

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import me.zhanghai.compose.preference.preferenceTheme

@Composable
fun XhuFoldSettingsGroup(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    foldState: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedVisibility(
        visible = !foldState,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
    ) {
        XhuSettingsGroup(
            title = title,
            modifier = modifier,
            content = content,
        )
    }
    if (foldState) {
        Spacer(modifier = Modifier.height(12.dp))
    }
}

/**
 * 现代 M3 分组圆角卡片设置容器
 */
@Composable
fun XhuSettingsGroup(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvidePreferenceTheme(
        theme = xhuPreferenceTheme(),
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
        ) {
            ProvideTextStyle(
                value = MaterialTheme.typography.labelLarge.copy(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                )
            ) {
                Column(
                    modifier = Modifier.padding(start = 8.dp, top = 8.dp, bottom = 6.dp)
                ) {
                    title()
                }
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(
                    width = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                ),
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    content = content,
                )
            }
        }
    }
}

@Composable
private fun xhuPreferenceTheme() = preferenceTheme(
    summaryColor = MaterialTheme.colorScheme.onSurfaceVariant,
    titleTextStyle = LocalTextStyle.current.merge(MaterialTheme.typography.bodyLarge),
    summaryTextStyle = LocalTextStyle.current.merge(MaterialTheme.typography.bodySmall),
)
