package vip.mystery0.xhu.timetable.ui.component.loading

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

@Composable
fun LoadingButton(
    modifier: Modifier,
    loadingValue: LoadingValue,
    onClick: () -> Unit,
) {
    val infiniteRotate: Float =
        if (loadingValue == LoadingValue.Loading) {
            val infiniteTransition = rememberInfiniteTransition(label = "dataRefresh")
            infiniteTransition.animateFloat(
                label = "dataRefresh",
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart,
                ),
            ).value
        } else {
            0f
        }
    IconButton(
        onClick = onClick,
        modifier = modifier
            .graphicsLayer {
                rotationZ = infiniteRotate
            },
    ) {
        Icon(
            painter = XhuIcons.Action.sync,
            contentDescription = null,
        )
    }
}