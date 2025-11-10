package vip.mystery0.xhu.timetable.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.animation.doOnEnd
import androidx.core.content.pm.ShortcutManagerCompat
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.ui.activity.NavActivity.Companion.jumpToNav
import vip.mystery0.xhu.timetable.viewmodel.StarterViewModel

class StartActivity : ComponentActivity(), KoinComponent {
    private val viewModel by viewModel<StarterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        keepSplashScreenLonger()
        customizeSplashScreenExit()
        ShortcutManagerCompat.removeAllDynamicShortcuts(this)
        jumpToNav(NavActivity.InitRoute.PRIVACY)
        finish()
    }

    private fun keepSplashScreenLonger() {
        // 监听Content View的描画时机
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // 准备好了描画放行，反之挂起
                    return if (!viewModel.readyState.value.loading) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )
    }

    private fun customizeSplashScreenExit() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return
        }

        splashScreen.setOnExitAnimationListener { view ->
            val iconView = view.iconView ?: return@setOnExitAnimationListener
            AnimatorSet().apply {
                playSequentially(
                    ObjectAnimator.ofFloat(iconView, View.TRANSLATION_Y, 0f, 50f),
                    ObjectAnimator.ofFloat(
                        iconView,
                        View.TRANSLATION_Y,
                        50f,
                        -view.height.toFloat()
                    ),
                )
                doOnEnd { view.remove() }
                start()
            }
        }
    }
}