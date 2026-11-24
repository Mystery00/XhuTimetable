package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.trackEvent
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.ui.component.ShowUpdateDialog
import vip.mystery0.xhu.timetable.ui.component.Tab
import vip.mystery0.xhu.timetable.ui.component.loadCoilModelWithoutCache
import vip.mystery0.xhu.timetable.ui.component.loading.LoadingButton
import vip.mystery0.xhu.timetable.ui.component.loading.LoadingValue
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteLogin
import vip.mystery0.xhu.timetable.ui.navigation.RouteMain
import vip.mystery0.xhu.timetable.ui.navigation.replaceTo
import vip.mystery0.xhu.timetable.ui.theme.isDarkMode
import vip.mystery0.xhu.timetable.ui.theme.stateOf
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerProfileViewModel

@Composable
fun MainScreen() {
    val viewModel = koinViewModel<MainViewModel>()
    val navController = LocalNavController.current
    val isDarkMode = isDarkMode()

    val enableCalendarView by viewModel.enableCalendarView.collectAsState()
    val backgroundImage by viewModel.backgroundImage.collectAsState()
    val backgroundImageBlur by viewModel.backgroundImageBlur.collectAsState()
    val showTomorrowCourse by viewModel.showTomorrowCourse.collectAsState()

    val tabs = if (enableCalendarView) {
        Tab.entries
    } else {
        Tab.entries.filter { it != Tab.CALENDAR }
    }

    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val safeIndex = selectedTabIndex.coerceIn(0, (tabs.size - 1).coerceAtLeast(0))
    val currentTab = tabs[safeIndex]

    LaunchedEffect(isDarkMode) {
        viewModel.loadBackground(isDarkMode)
    }

    HandleEventBus()
    ShowUpdateDialog()

    val hasBg = backgroundImage != Unit && currentTab.useBackgroundImage
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Box(modifier = Modifier.fillMaxSize()) {
        // 全局背景图：仅在课表/月历类 Tab 展示，与半透明顶底栏自然融合
        if (hasBg) {
            AsyncImage(
                model = loadCoilModelWithoutCache(backgroundImage),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(backgroundImageBlur.dp)
            )
            // 柔和遮罩层：确保浅色/深色主题下文字与组件始终保持良好层次感
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        if (isDarkMode) {
                            Color.Black.copy(alpha = 0.28f)
                        } else {
                            Color.White.copy(alpha = 0.08f)
                        }
                    )
            )
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            containerColor = if (hasBg) Color.Transparent else MaterialTheme.colorScheme.background,
            topBar = {
                val topBarContainer = if (hasBg) {
                    MaterialTheme.colorScheme.surface.copy(alpha = if (isDarkMode) 0.78f else 0.86f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
                CenterAlignedTopAppBar(
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = topBarContainer,
                        scrolledContainerColor = topBarContainer,
                    ),
                    title = {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn(tween(180, easing = FastOutSlowInEasing)) togetherWith
                                    fadeOut(tween(140, easing = FastOutSlowInEasing))
                            },
                            label = "mainTopBarTitle",
                        ) { tab ->
                            tab.titleBar?.invoke()
                        }
                    },
                    actions = {
                        val loading by viewModel.loading.collectAsState()
                        val loadingValue = if (loading) LoadingValue.Loading else LoadingValue.Stop
                        val actions = currentTab.actions
                        if (actions != null) {
                            if (actions(this)) {
                                LoadingButton(
                                    loadingValue = loadingValue,
                                    modifier = Modifier.fillMaxHeight()
                                ) {
                                    trackEvent("手动刷新课表")
                                    viewModel.refreshCloudDataToState()
                                }
                            }
                        }
                    }
                )
            },
            bottomBar = {
                val navContainer = if (hasBg) {
                    MaterialTheme.colorScheme.surfaceContainer.copy(alpha = if (isDarkMode) 0.88f else 0.92f)
                } else {
                    MaterialTheme.colorScheme.surfaceContainer
                }
                NavigationBar(
                    containerColor = navContainer,
                    tonalElevation = 0.dp,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        DrawNavigationItem(
                            checked = safeIndex == index,
                            showTomorrowCourse = showTomorrowCourse,
                            tab = tab,
                            icon = tab.icon,
                        ) {
                            selectedTabIndex = index
                        }
                    }
                }
            }
        ) { paddingValues ->
            // 取消一级 Tab 水平滑动，释放水平手势给本周课表切周；使用轻量 Crossfade 平滑切换
            Crossfade(
                targetState = currentTab,
                animationSpec = tween(200, easing = FastOutSlowInEasing),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                label = "mainTabCrossfade",
            ) { tab ->
                Column(modifier = Modifier.fillMaxSize()) {
                    tab.content(this)
                }
            }
        }
    }

    HandleErrorMessage(flow = viewModel.errorMessage)
    val emptyUser by viewModel.emptyUser.collectAsState()
    LaunchedEffect(emptyUser) {
        if (emptyUser) {
            navController.replaceTo<RouteMain>(RouteLogin(false))
        }
    }
}

@Composable
private fun RowScope.DrawNavigationItem(
    checked: Boolean,
    showTomorrowCourse: Boolean,
    tab: Tab,
    icon: Pair<Pair<DrawableResource, DrawableResource>, Pair<DrawableResource, DrawableResource>>,
    onSelect: () -> Unit = {},
) {
    val label = if (showTomorrowCourse) tab.otherLabel else tab.label

    NavigationBarItem(
        selected = checked,
        icon = {
            Icon(
                painter = stateOf(checked = checked, pair = icon),
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
                contentDescription = label,
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (checked) FontWeight.SemiBold else FontWeight.Normal,
            )
        },
        onClick = onSelect,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    )
}

@Composable
private fun HandleEventBus() {
    val viewModel = koinViewModel<MainViewModel>()
    val profileViewModel = koinViewModel<PagerProfileViewModel>()

    LaunchedEffect(Unit) {
        EventBus.flow.collect { event ->
            event.getContentIfNotHandled()?.let { eventType ->
                Logger.i("eventbus: $eventType")
                viewModel.loadConfig()
                when (eventType) {
                    EventType.MULTI_MODE_CHANGED,
                    EventType.CHANGE_ENABLE_CALENDAR_VIEW,
                    EventType.CHANGE_MAIN_USER -> {
                        viewModel.checkMainUser()
                        viewModel.refreshCloudDataToState()
                    }

                    EventType.CHANGE_CURRENT_YEAR_AND_TERM,
                    EventType.CHANGE_SHOW_CUSTOM_COURSE,
                    EventType.CHANGE_SHOW_CUSTOM_THING,
                    EventType.CHANGE_CAMPUS -> {
                        viewModel.refreshCloudDataToState()
                    }

                    EventType.MAIN_USER_LOGOUT -> {
                        viewModel.checkMainUser()
                    }

                    EventType.CHANGE_TERM_START_TIME,
                    EventType.CHANGE_AUTO_SHOW_TOMORROW_COURSE -> {
                        viewModel.loadLocalDataToState(changeWeekOnly = true)
                        viewModel.calculateTodayTitle()
                        viewModel.loadTodayHoliday()
                    }

                    EventType.CHANGE_SHOW_HOLIDAY -> {
                        viewModel.loadTodayHoliday()
                    }

                    EventType.CHANGE_SHOW_STATUS,
                    EventType.CHANGE_SHOW_NOT_THIS_WEEK,
                    EventType.CHANGE_COURSE_COLOR,
                    EventType.CHANGE_CUSTOM_UI,
                    EventType.CHANGE_CUSTOM_ACCOUNT_TITLE -> {
                        viewModel.loadLocalDataToState(changeWeekOnly = true)
                    }

                    EventType.CHANGE_MAIN_BACKGROUND -> {
                        viewModel.loadBackground()
                    }

                    EventType.UPDATE_NOTICE_CHECK -> {
                        profileViewModel.checkUnReadNotice()
                    }

                    EventType.UPDATE_FEEDBACK_CHECK -> {
                        profileViewModel.checkUnReadFeedback()
                    }

                    else -> {}
                }
            }
        }
    }
}
