package vip.mystery0.xhu.timetable.ui.screen

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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import multiplatform.network.cmptoast.showToast
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteMain
import vip.mystery0.xhu.timetable.ui.navigation.RouteSplashImage
import vip.mystery0.xhu.timetable.ui.navigation.replaceTo
import vip.mystery0.xhu.timetable.viewmodel.SplashImageViewModel

@Composable
fun SplashImageScreen(
    splashFilePath: String?,
    splashId: Long,
    toMain: (() -> Unit)? = null,
) {
    if (splashFilePath.isNullOrBlank() || splashId == -1L) {
        ToMain(toMain)
        return
    }
    val viewModel = koinViewModel<SplashImageViewModel>()

    val timer by viewModel.timerState.collectAsState()
    val showSplashBackgroundColor by viewModel.showSplashBackgroundColor.collectAsState()
    val showSplashLocationUrl by viewModel.showSplashLocationUrl.collectAsState()
    val showSplashFile by viewModel.showSplashFile.collectAsState()

    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) {
        viewModel.startInit(splashFilePath, splashId)
    }

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
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(showSplashFile)
                .crossfade(true)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build(),
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
        ToMain(toMain)
    }
}

@Composable
private fun ToMain(
    toMain: (() -> Unit)? = null,
) {
    if (toMain != null) {
        toMain()
        return
    }
    val navController = LocalNavController.current!!

    navController.replaceTo<RouteSplashImage>(RouteMain)
}