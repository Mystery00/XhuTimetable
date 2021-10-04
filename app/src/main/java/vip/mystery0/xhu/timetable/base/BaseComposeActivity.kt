package vip.mystery0.xhu.timetable.base

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.greenrobot.eventbus.EventBus
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import java.util.*
import kotlin.reflect.KClass

abstract class BaseComposeActivity(
    private val setSystemUiColor: Boolean = true,
    private val registerEventBus: Boolean = false,
    @LayoutRes val contentLayoutId: Int = 0
) :
    ComponentActivity(contentLayoutId), KoinComponent {
    private val eventBus: EventBus by inject()

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
                SideEffect {
                    systemUiController.setSystemBarsColor(systemBarColor, darkIcons = false)
                    systemUiController.setNavigationBarColor(systemBarColor, darkIcons = false)
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

    protected fun @receiver:StringRes Int.asString(): String = getString(this)

    @Composable
    fun BuildNoDataLayout() {
        BuildLayout(painter = XhuImages.noData, text = "暂无数据")
    }

    @Composable
    fun BuildNoCourseLayout() {
        BuildLayout(painter = XhuImages.noCourse, text = "暂无课程")
    }

    @Composable
    private fun BuildLayout(painter: Painter, text: String) {
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
                Text(text = text, color = XhuColor.Common.nullDataColor)
            }
        }
    }

    override fun onDestroy() {
        if (registerEventBus) {
            eventBus.unregister(this)
        }
        super.onDestroy()
    }
}