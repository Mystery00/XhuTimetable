package vip.mystery0.xhu.timetable.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.core.animation.doOnEnd
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.registerAppCenter
import vip.mystery0.xhu.timetable.viewmodel.StarterViewModel

class StartActivity : BaseComposeActivity(setSystemUiColor = false) {
    private val viewModel: StarterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerAppCenter(application)
    }

    @Composable
    override fun BuildContentWindow() {
        val readyState = viewModel.readyState.collectAsState()
        readyState.value.errorMessage.notBlankToast(true)
        if (!readyState.value.loading) {
            goToMainScreen()
        }
    }

    @RequiresApi(31)
    override fun initIntent() {
        super.initIntent()
        keepSplashScreenLonger()
        customizeSplashScreenExit()
    }

    private fun goToMainScreen() {
        if (SessionManager.isLogin()) {
            if (DataHolder.splashFile == null) {
                intentTo(MainActivity::class)
            } else {
                intentTo(SplashImageActivity::class)
            }
        } else {
            intentTo(LoginActivity::class)
        }
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

    @RequiresApi(31)
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