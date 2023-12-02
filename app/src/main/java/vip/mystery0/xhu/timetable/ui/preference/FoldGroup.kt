package vip.mystery0.xhu.timetable.ui.preference

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import vip.mystery0.xhu.timetable.ui.theme.XhuColor

@Composable
fun XhuFoldSettingsGroup(
    modifier: Modifier = Modifier,
    foldState: MutableState<Boolean> = remember { mutableStateOf(false) },
    title: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = MutableInteractionSource(),
        ) {
            foldState.value = !foldState.value
        }
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .animateContentSize(),
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
            if (foldState.value) {
                Spacer(
                    modifier = Modifier
                        .height(12.dp),
                )
            } else {
                content()
            }
        }
    }
}