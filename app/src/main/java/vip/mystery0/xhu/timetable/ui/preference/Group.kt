package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideTextStyle
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vip.mystery0.xhu.timetable.ui.theme.XhuColor

@Composable
fun XhuSettingsGroup(
    modifier: Modifier = Modifier,
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface {
        Column(
            modifier = modifier.fillMaxWidth(),
        ) {
            Divider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 0.33.dp,
                color = XhuColor.Common.divider,
            )
            Spacer(
                modifier = Modifier
                    .height(12.dp),
            )
            if (title != null) {
                XhuSettingsGroupTitle(title)
            }
            content()
        }
    }
}

@Composable
internal fun XhuSettingsGroupTitle(title: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(36.dp)
            .padding(horizontal = 64.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val primary = MaterialTheme.colors.primary
        val titleStyle = MaterialTheme.typography.subtitle2.copy(color = primary)
        ProvideTextStyle(value = titleStyle) { title() }
    }
}