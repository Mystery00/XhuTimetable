package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
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
        val primary = MaterialTheme.colorScheme.primary
        val titleStyle = MaterialTheme.typography.titleSmall.copy(color = primary)
        ProvideTextStyle(value = titleStyle) { title() }
    }
}