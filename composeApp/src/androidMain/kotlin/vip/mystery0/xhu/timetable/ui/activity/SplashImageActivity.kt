package vip.mystery0.xhu.timetable.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import multiplatform.network.cmptoast.showToast
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import vip.mystery0.xhu.timetable.viewmodel.SplashImageViewModel

class SplashImageActivity : ComponentActivity(), KoinComponent {
    private val viewModel by viewModel<SplashImageViewModel>()
    private val loader: ImageLoader by lazy {
        ImageLoader(this)
            .newBuilder()
            .build()
    }

    companion object {
        private const val INTENT_SPLASH_FILE_PATH = "splashFilePath"
        private const val INTENT_SPLASH_ID = "splashId"

        fun setParams(
            intent: Intent,
            splashFilePath: String,
            splashId: Long,
        ) {
            intent.putExtra(INTENT_SPLASH_FILE_PATH, splashFilePath)
            intent.putExtra(INTENT_SPLASH_ID, splashId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        super.onNewIntent(intent)
        val splashFilePath = intent.getStringExtra(INTENT_SPLASH_FILE_PATH)
        val splashId = intent.getLongExtra(INTENT_SPLASH_ID, -1)
        if (splashFilePath.isNullOrBlank() || splashId == -1L) {
            toMain()
            return
        }
        viewModel.startInit(splashFilePath, splashId)
        setContent {
            val uriHandler = LocalUriHandler.current

            XhuTimetableTheme {
                val timer by viewModel.timerState.collectAsState()
                val showSplashBackgroundColor by viewModel.showSplashBackgroundColor.collectAsState()
                val showSplashLocationUrl by viewModel.showSplashLocationUrl.collectAsState()
                val showSplashFile by viewModel.showSplashFile.collectAsState()
                Box(
                    modifier = Modifier
                        .background(
                            showSplashBackgroundColor ?: MaterialTheme.colorScheme.background
                        )
                        .fillMaxSize()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                showSplashLocationUrl?.let {
                                    uriHandler.openUri(it)
                                }
                            }
                        )
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(showSplashFile)
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
                                    showToast("启动图将会隐藏7天")
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
    }

    private fun toMain() {
        startActivity(Intent(this, NavActivity::class.java))
        finish()
    }
}