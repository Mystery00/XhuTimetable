package vip.mystery0.xhu.timetable

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
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
import io.github.vinceglb.filekit.coil.addPlatformFileSupport
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.store.getCacheStore
import vip.mystery0.xhu.timetable.feature.FeatureHub
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.push.initPush
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.Nav
import vip.mystery0.xhu.timetable.ui.navigation.Navs
import vip.mystery0.xhu.timetable.ui.theme.SetSystemAppearance
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import vip.mystery0.xhu.timetable.ui.theme.isDarkMode

private const val NAV_ANIM_DURATION = 260

@Composable
fun App(startRoute: Nav) {
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        val allowPrivacy = getCacheStore { allowPrivacy }
        if (allowPrivacy) {
            FeatureHub.start(scope)
            initPush()
        } else {
            EventBus.flow.collect { event ->
                if (event.peekContent() == EventType.ALLOW_PRIVACY) {
                    if (event.getContentIfNotHandled() == EventType.ALLOW_PRIVACY) {
                        FeatureHub.start(scope)
                        initPush()
                    }
                }
            }
        }
    }
    setSingletonImageLoaderFactory { context ->
        ImageLoader.Builder(context)
            .components {
                addPlatformFileSupport()
            }
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
                    fadeIn(
                        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
                    ) + slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing),
                        initialOffset = { it / 8 },
                    )
                },
                exitTransition = {
                    fadeOut(
                        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
                    ) + slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.Start,
                        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing),
                        targetOffset = { it / 12 },
                    )
                },
                popEnterTransition = {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing),
                        initialOffset = { it / 12 },
                    ) + fadeIn(
                        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
                    )
                },
                popExitTransition = {
                    slideOutOfContainer(
                        AnimatedContentTransitionScope.SlideDirection.End,
                        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing),
                        targetOffset = { it / 8 },
                    ) + fadeOut(
                        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
                    )
                },
                builder = Navs,
            )
        }
    }
}

expect fun killCurrentProcess()
