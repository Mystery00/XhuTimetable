package vip.mystery0.xhu.timetable.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.DrawableRes
import androidx.annotation.LayoutRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.zyao89.view.zloading.ZLoadingView
import com.zyao89.view.zloading.Z_TYPE
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import kotlin.reflect.KClass

abstract class BaseComposeActivity(
    private val setSystemUiColor: Boolean = true,
    private val registerEventBus: Boolean = false,
    @LayoutRes val contentLayoutId: Int = 0
) :
    ComponentActivity(contentLayoutId), KoinComponent {
    val eventBus: EventBus by inject()

    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        if (registerEventBus) {
            eventBus.register(this)
        }
        super.onCreate(savedInstanceState)
        initIntent()
        setContent {
            BuildContentWindow()
        }
    }

    open fun initIntent() {}

    @Composable
    open fun BuildContentWindow() {
        XhuTimetableTheme {
            if (setSystemUiColor) {
                val systemUiController = rememberSystemUiController()
                val systemBarColor = MaterialTheme.colors.primary
                val isLight = MaterialTheme.colors.isLight
                SideEffect {
                    systemUiController.setSystemBarsColor(systemBarColor, darkIcons = isLight)
                    systemUiController.setNavigationBarColor(systemBarColor, darkIcons = isLight)
                }
            }
            BuildContent()
        }
    }

    @Composable
    open fun BuildContent() {
    }

    fun <T : Activity> intentTo(
        clazz: KClass<T>,
        block: (Intent) -> Unit = {},
    ) {
        startActivity(Intent(this, clazz.java).apply(block))
    }

    fun String.toast(showLong: Boolean = false) =
        newToast(
            this@BaseComposeActivity,
            this,
            if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )

    fun toastString(message: String, showLong: Boolean = false) =
        newToast(
            this@BaseComposeActivity,
            message,
            if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )

    protected fun String.notBlankToast(showLong: Boolean = false) {
        if (this.isNotBlank()) {
            newToast(
                this@BaseComposeActivity,
                this,
                if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
            )
        }
    }

    private fun newToast(context: Context, message: String?, length: Int) {
        toast?.cancel()
        toast = Toast.makeText(context, message, length)
        toast?.show()
    }

    @Composable
    protected fun ShowProgressDialog(
        show: Boolean,
        text: String,
        fontSize: TextUnit = TextUnit.Unspecified,
        type: Z_TYPE = Z_TYPE.SINGLE_CIRCLE,
    ) {
        if (!show) {
            return
        }
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            )
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(XhuColor.Common.grayBackground, shape = RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val contentColor = MaterialTheme.colors.primary
                    AndroidView(
                        factory = { context ->
                            ZLoadingView(context)
                        }, modifier = Modifier
                            .width(64.dp)
                            .height(64.dp)
                    ) {
                        it.setLoadingBuilder(type)
                        it.setColorFilter(
                            Color.valueOf(
                                contentColor.red,
                                contentColor.green,
                                contentColor.blue
                            ).toArgb()
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = text,
                        fontSize = fontSize,
                        color = contentColor
                    )
                }
            }
        }
    }

    @Composable
    fun BuildNoDataLayout() {
        BuildLayout(painter = XhuImages.noData, text = "暂无数据")
    }

    @Composable
    fun BuildNoCourseLayout() {
        BuildLayout(painter = XhuImages.noCourse, text = "暂无课程")
    }

    @Composable
    fun BuildNoPermissionLayout(
        permissionDescription: String,
        onRequestPermission: () -> Unit = {},
    ) {
        BuildLayout(
            painter = XhuImages.noCourse,
            text = permissionDescription,
            appendLayout = {
                Button(onClick = onRequestPermission) {
                    Text("授予权限")
                }
            }
        )
    }

    @Composable
    private fun BuildLayout(
        painter: Painter,
        text: String,
        appendLayout: (@Composable () -> Unit)? = null,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.surface),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.width(256.dp)
                )
                Text(
                    text = text,
                    color = MaterialTheme.colors.onSurface,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 48.dp)
                )
                appendLayout?.invoke()
            }
        }
    }

    override fun onDestroy() {
        if (registerEventBus) {
            eventBus.unregister(this)
        }
        super.onDestroy()
    }

    protected inline fun <reified T : BaseComposeActivity> pushDynamicShortcuts(
        id: String,
        label: String,
        @DrawableRes iconResId: Int,
    ) {
        val shortcut = ShortcutInfoCompat.Builder(this, id)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(IconCompat.createWithResource(this, iconResId))
            .setIntent(Intent(this, T::class.java).apply {
                action = "${packageName}.${T::class.java.simpleName}"
            })
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(this, shortcut)
    }
}