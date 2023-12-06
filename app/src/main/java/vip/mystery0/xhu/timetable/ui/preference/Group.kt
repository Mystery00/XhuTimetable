package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceTheme

@Composable
fun XhuSettingsGroup(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvidePreferenceTheme {
        PreferenceCategory(
            title = title,
        )
        Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            content()
        }
    }
}