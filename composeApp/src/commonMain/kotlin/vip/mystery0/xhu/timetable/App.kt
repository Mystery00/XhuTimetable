package vip.mystery0.xhu.timetable

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import coil3.ImageLoader
import coil3.compose.setSingletonImageLoaderFactory
import coil3.request.CachePolicy
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import vip.mystery0.xhu.timetable.feature.FeatureHub
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.Nav
import vip.mystery0.xhu.timetable.ui.navigation.Navs
import vip.mystery0.xhu.timetable.ui.theme.SetSystemAppearance
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import vip.mystery0.xhu.timetable.ui.theme.isDarkMode

@Composable
fun App(startRoute: Nav) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        FeatureHub.start(scope)
    }
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                addPlatformFileSupport()
            }
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .build()
    }
    val navController = rememberNavController()
    XhuTimetableTheme {
        SetSystemAppearance(isDark = isDarkMode())
        CompositionLocalProvider(LocalNavController provides navController) {
            NavHost(
                navController = navController,
                startDestination = startRoute,
                enterTransition = {
                    fadeIn(animationSpec = tween(300)) + slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(300),
                        initialOffset = { it / 5 },
                    )
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(300)) + slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(300),
                        targetOffset = { it / 5 },
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(300),
                        initialOffset = { it / 5 },
                    ) + fadeIn(animationSpec = tween(300))
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(300),
                        targetOffset = { it / 5 },
                    ) + fadeOut(animationSpec = tween(300))
                },
                builder = Navs,
            )
        }
    }
}

expect fun killCurrentProcess()