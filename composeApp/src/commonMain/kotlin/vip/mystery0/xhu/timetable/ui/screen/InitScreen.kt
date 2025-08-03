package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.absolutePath
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteInit
import vip.mystery0.xhu.timetable.ui.navigation.RouteLogin
import vip.mystery0.xhu.timetable.ui.navigation.RouteMain
import vip.mystery0.xhu.timetable.ui.navigation.RouteSplashImage
import vip.mystery0.xhu.timetable.ui.navigation.replaceTo
import vip.mystery0.xhu.timetable.viewmodel.StarterViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.ic_app_icon

@Composable
fun InitScreen() {
    val viewModel = koinViewModel<StarterViewModel>()
    val navController = LocalNavController.current!!

    val readyState by viewModel.readyState.collectAsState()
    val isLoginState by viewModel.isLoginState.collectAsState()

    Box(
        modifier = Modifier
            .background(Color(0xFF3DDC84))
            .fillMaxSize()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.align(Alignment.Center),
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_app_icon),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(64.dp))
            CircularProgressIndicator()
        }
    }
    LaunchedEffect(Unit) {
        viewModel.init()
    }
    HandleErrorMessage(errorMessage = readyState.errorMessage) {}

    if (!readyState.loading) {
        if (!isLoginState) {
            navController.replaceTo<RouteInit>(RouteLogin(false))
            return
        }
        if (readyState.splashFile == null || readyState.splashId == null) {
            navController.replaceTo<RouteInit>(RouteMain)
            return
        }

        val splashFilePath = readyState.splashFile!!.absolutePath()
        navController.replaceTo<RouteInit>(RouteSplashImage(splashFilePath, readyState.splashId!!))
    }
}