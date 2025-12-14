package vip.mystery0.xhu.timetable.ui.component.preference

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import me.zhanghai.compose.preference.preferenceTheme

@Composable
fun XhuFoldSettingsGroup(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    foldState: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    AnimatedContent(
        targetState = !foldState,
    ) { show ->
        if (show) {
            Column {
                XhuSettingsGroup(
                    title = title,
                    modifier = modifier,
                    content = content,
                )
            }
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun XhuSettingsGroup(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvidePreferenceTheme(
        theme = xhuPreferenceTheme(),
    ) {
        PreferenceCategory(
            title = title,
        )
        Column(
            modifier = modifier.fillMaxWidth(),
            content = content,
        )
    }
}

@Composable
private fun xhuPreferenceTheme() = preferenceTheme(
    summaryColor = MaterialTheme.colorScheme.outline,
)