package vip.mystery0.xhu.timetable.ui.activity

import android.os.Build
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.CachePolicy
import coil.request.ImageRequest
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.loadInBrowser
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import vip.mystery0.xhu.timetable.viewmodel.SplashImageViewModel

class SplashImageActivity : BaseComposeActivity() {
    private val viewModel: SplashImageViewModel by viewModels()
    private val loader: ImageLoader by lazy {
        ImageLoader(this).newBuilder()
            .components {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    @Composable
    override fun BuildContentWindow() {
        XhuTimetableTheme {
            val timer by viewModel.timerState.collectAsState()
            val showSplash = DataHolder.splashFile
            val backgroundColor = DataHolder.backgroundColor
            if (showSplash == null) {
                toMain()
                return@XhuTimetableTheme
            }
            Box(
                modifier = Modifier
                    .background(backgroundColor ?: MaterialTheme.colors.background)
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            DataHolder.splash?.locationUrl?.let {
                                loadInBrowser(it)
                            }
                        }
                    )
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(showSplash)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    imageLoader = loader,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize(),
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(36.dp)
                        .navigationBarsPadding()
                        .background(Color(0x80000000), shape = RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                viewModel.skip()
                            }
                        )) {
                    Text(
                        modifier = Modifier.padding(12.dp),
                        text = "跳 过 $timer",
                        fontSize = 12.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(36.dp)
                        .navigationBarsPadding()
                        .background(Color(0x80000000), shape = RoundedCornerShape(24.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                viewModel.hide()
                                "启动图将会隐藏7天".toast()
                            }
                        )) {
                    Text(
                        modifier = Modifier.padding(12.dp),
                        text = "隐藏",
                        fontSize = 12.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            if (timer <= 0) {
                toMain()
            }
        }
    }

    private fun toMain() {
        intentTo(MainActivity::class)
        finish()
    }
}