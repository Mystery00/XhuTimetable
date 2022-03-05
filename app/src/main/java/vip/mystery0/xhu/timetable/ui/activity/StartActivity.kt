package vip.mystery0.xhu.timetable.ui.activity

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.animation.doOnEnd
import androidx.core.os.BuildCompat
import coil.ImageLoader
import coil.compose.rememberImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import com.google.accompanist.insets.ProvideWindowInsets
import com.google.accompanist.insets.navigationBarsPadding
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.registerAppCenter
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import vip.mystery0.xhu.timetable.viewmodel.StarterViewModel

class StartActivity : BaseComposeActivity(setSystemUiColor = false) {
    private val viewModel: StarterViewModel by viewModels()
    private val loader: ImageLoader by lazy {
        ImageLoader.invoke(this).newBuilder()
            .componentRegistry {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder(this@StartActivity))
                } else {
                    add(GifDecoder())
                }
            }.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerAppCenter(application)
    }

    @Composable
    override fun BuildContentWindow() {
        XhuTimetableTheme {
            ProvideWindowInsets {
                val readyState = viewModel.readyState.collectAsState()
                val timerState = viewModel.timerState.collectAsState()
                readyState.value.errorMessage.notBlankToast(true)

                if (readyState.value.splash != null && timerState.value > 0) {
                    val showSplash = readyState.value.splash
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Image(
                            painter = rememberImagePainter(
                                data = showSplash,
                                imageLoader = loader,
                            ) {
                                crossfade(true)
                                diskCachePolicy(CachePolicy.DISABLED)
                            },
                            contentDescription = null,
                            contentScale = ContentScale.FillWidth,
                            modifier = Modifier
                                .matchParentSize()
                        )
                        Box(modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(36.dp)
                            .navigationBarsPadding()
                            .background(Color(0x80000000), shape = RoundedCornerShape(24.dp))
                            .clickable {
                                viewModel.skip()
                            }) {
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = "跳 过 ${timerState.value}",
                                fontSize = 12.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                } else {
                    if (!readyState.value.loading) {
                        goToMainScreen()
                    }
                }
            }
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
            intentTo(MainActivity::class)
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
        if (!BuildCompat.isAtLeastS()) {
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