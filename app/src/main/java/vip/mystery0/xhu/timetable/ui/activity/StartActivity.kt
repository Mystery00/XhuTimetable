package vip.mystery0.xhu.timetable.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.core.animation.doOnEnd
import androidx.core.os.BuildCompat
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.viewmodel.StarterViewModel

class StartActivity : BaseComposeActivity() {
    private val handler = Handler(Looper.getMainLooper())
    private val viewModel: StarterViewModel by viewModels()
    private val jumpRunnable = { goToMainScreen() }

    @RequiresApi(31)
    override fun initIntent() {
        super.initIntent()
        keepSplashScreenLonger()
        customizeSplashScreenExit()
    }

    // Ensure main screen not shown when tap home key during message queueing.
    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
    }

    // Ensure main screen jump logic can do again.
    override fun onResume() {
        super.onResume()
        goToMainScreenDelayed()
    }

    private fun goToMainScreenDelayed() {
        handler.postDelayed(jumpRunnable, 1500)
    }

    private fun goToMainScreen() {
        intentTo(MainActivity::class)
        finish()
    }

    /*
     Keep splash screen showing till data initialized.
     */
    private fun keepSplashScreenLonger() {
        // 监听Content View的描画时机
        val content: View = findViewById(android.R.id.content)
        content.viewTreeObserver.addOnPreDrawListener(
            object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    // 准备好了描画放行，反之挂起
                    return if (viewModel.readyState.value.loading) {
                        content.viewTreeObserver.removeOnPreDrawListener(this)
                        true
                    } else {
                        false
                    }
                }
            }
        )
    }

    /*
    Customize splash screen exit animator.
     */
    @RequiresApi(31)
    private fun customizeSplashScreenExit() {
        // Ensure working on S device or above .
        if (!BuildCompat.isAtLeastS()) {
            return
        }

        splashScreen.setOnExitAnimationListener { splashScreenView ->
            val iconView = splashScreenView.iconView ?: return@setOnExitAnimationListener
            AnimatorSet().apply {
                playSequentially(
                    ObjectAnimator.ofFloat(iconView, View.TRANSLATION_Y, 0f, 50f),
                    ObjectAnimator.ofFloat(
                        iconView,
                        View.TRANSLATION_Y,
                        50f,
                        -splashScreenView.height.toFloat()
                    ),
                )
                doOnEnd { splashScreenView.remove() }
                start()
            }
        }
    }
}