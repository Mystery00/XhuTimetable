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
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.core.animation.doOnEnd
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.model.response.Splash
import vip.mystery0.xhu.timetable.registerAppCenter
import vip.mystery0.xhu.timetable.viewmodel.StarterViewModel
import java.io.File

class StartActivity : BaseComposeActivity(setSystemUiColor = false) {
    private val viewModel: StarterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerAppCenter(application)
    }

    @Composable
    override fun BuildContentWindow() {
        val readyState by viewModel.readyState.collectAsState()
        val isLoginState by viewModel.isLoginState.collectAsState()
        readyState.errorMessage.notBlankToast(true)
        if (!readyState.loading) {
            goToMainScreen(
                isLoginState,
                readyState.splashFile,
                readyState.splashId,
            )
        }
    }

    @RequiresApi(31)
    override fun initIntent() {
        super.initIntent()
        keepSplashScreenLonger()
        customizeSplashScreenExit()
    }

    private fun goToMainScreen(
        isLogin: Boolean,
        splashFile: File?,
        splashId: Long?,
    ) {
        if (!isLogin) {
            intentTo(LoginActivity::class)
            finish()
            return
        }
        if (splashFile == null || splashId == null) {
            intentTo(MainActivity::class)
            finish()
            return
        }
        val splashFilePath = splashFile.absolutePath
        intentTo(SplashImageActivity::class) {
            SplashImageActivity.setParams(it, splashFilePath, splashId)
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