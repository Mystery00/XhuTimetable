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
import androidx.compose.material.*
import androidx.compose.runtime.*
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import java.util.*
import kotlin.reflect.KClass

abstract class BaseComposeActivity(
    @LayoutRes val contentLayoutId: Int = 0
) :
    ComponentActivity(contentLayoutId), KoinComponent {
    private var toast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
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
            BuildContent()
        }
    }

    @Composable
    open fun BuildContent() {
    }

    protected fun <T : Activity> intentTo(clazz: KClass<T>) {
        startActivity(Intent(this, clazz.java))
    }

    protected fun String.toast(showLong: Boolean = false) =
        newToast(
            this@BaseComposeActivity,
            this,
            if (showLong) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        )

    private fun newToast(context: Context, message: String?, length: Int) {
        toast?.cancel()
        toast = Toast.makeText(context, message, length)
        toast?.show()
    }

    protected fun @receiver:StringRes Int.asString(): String = getString(this)
}