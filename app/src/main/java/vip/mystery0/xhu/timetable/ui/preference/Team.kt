package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceTheme
import me.zhanghai.compose.preference.preferenceTheme

@Composable
fun TeamContent(
    title: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    ProvidePreferenceTheme(
        theme = teamPreferenceTheme(),
    ) {
        PreferenceCategory(
            title = {
                Text(text = title)
            },
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            content = content,
        )
    }
}

@Composable
fun TeamItem(
    title: String,
    subTitle: String,
    iconUrl: String,
) {
    Preference(
        icon = {
            Icon(
                modifier = Modifier.size(48.dp),
                painter = rememberAsyncImagePainter(
                    ImageRequest.Builder(LocalContext.current)
                        .data(data = iconUrl)
                        .apply {
                            size(width = 192, height = 192)
                        }
                        .build()
                ),
                contentDescription = title,
                tint = Color.Unspecified,
            )
        },
        title = { Text(text = title) },
        summary = {
            Text(text = subTitle)
        },
    )
}

@Composable
private fun teamPreferenceTheme() = preferenceTheme(
    summaryColor = MaterialTheme.colorScheme.outline,
    padding = PaddingValues(
        horizontal = 4.dp,
        vertical = 16.dp,
    ),
)