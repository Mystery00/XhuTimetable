package multiplatform.network.cmptoast

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.TextUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

actual object AppContext

private val toastScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

actual fun showToast(
    message: String,
    gravity: ToastGravity,
    backgroundColor: Color,
    textColor: Color,
    duration: ToastDuration,
    padding: ToastPadding?,
    textSize: TextUnit,
    topPadding: Int,
    bottomPadding: Int,
    cornerRadius: Int?,
    icon: ImageBitmap?,
    iconSizeDp: Int,
) {
    toastScope.launch {
        ToastManager.showToast(
            ToastData(message, gravity, backgroundColor, textColor, duration, padding,
                textSize, topPadding, bottomPadding, cornerRadius, icon, iconSizeDp)
        )
    }
}
