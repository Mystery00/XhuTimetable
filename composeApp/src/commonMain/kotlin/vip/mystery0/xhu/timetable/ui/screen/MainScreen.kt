package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FlexibleBottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import co.touchlab.kermit.Logger
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.DrawableResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.config.trackEvent
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.ui.component.Tab
import vip.mystery0.xhu.timetable.ui.component.loadCoilModelWithoutCache
import vip.mystery0.xhu.timetable.ui.component.loading.LoadingButton
import vip.mystery0.xhu.timetable.ui.component.loading.LoadingValue
import vip.mystery0.xhu.timetable.ui.component.tabOfWhenDisableCalendar
import vip.mystery0.xhu.timetable.ui.component.tabOfWhenEnableCalendar
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

    val navController = LocalNavController.current!!

    val isDarkMode = isDarkMode()

    val enableCalendarView by viewModel.enableCalendarView.collectAsState()
    val backgroundImage by viewModel.backgroundImage.collectAsState()
    val backgroundImageBlur by viewModel.backgroundImageBlur.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(initialPage = 0) { if (enableCalendarView) 4 else 3 }

    LaunchedEffect(Unit) {
        viewModel.loadBackground(isDarkMode)
    }
    HandleEventBus()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            val tab = if (enableCalendarView) {
                tabOfWhenEnableCalendar(pagerState.currentPage)
            } else {
                tabOfWhenDisableCalendar(pagerState.currentPage)
            }
            CenterAlignedTopAppBar(
                scrollBehavior = scrollBehavior,
                title = {
                    tab.titleBar?.let { it() }
                },
                actions = {
                    val loading by viewModel.loading.collectAsState()
                    val actions = tab.actions
                    val loadingValue = if (loading) LoadingValue.Loading else LoadingValue.Stop
                    if (actions != null) {
                        if (actions(this)) {
                            LoadingButton(
                                loadingValue = loadingValue,
                                modifier = Modifier
                                    .fillMaxHeight()
                            ) {
                                trackEvent("手动刷新课表")
                                viewModel.refreshCloudDataToState()
                            }
                        }
                    } else {
                        LoadingButton(
                            loadingValue = loadingValue,
                            modifier = Modifier
                                .fillMaxHeight()
                        ) {
                            trackEvent("手动刷新课表")
                            viewModel.refreshCloudDataToState()
                        }
                    }
                }
            )
        },
        bottomBar = {
            val showTomorrowCourse by viewModel.showTomorrowCourse.collectAsState()
            FlexibleBottomAppBar {
                val tabs = if (enableCalendarView) {
                    Tab.entries
                } else {
                    Tab.entries.filter { it != Tab.CALENDAR }
                }
                tabs.forEachIndexed { index, tab ->
                    DrawNavigationItem(
                        checked = pagerState.currentPage == index,
                        showTomorrowCourse = showTomorrowCourse,
                        tab = tab,
                        icon = tab.icon,
                    ) {
                        coroutineScope.safeLaunch(Dispatchers.Main) {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box {
            if (backgroundImage != Unit) {
                AsyncImage(
                    model = loadCoilModelWithoutCache(backgroundImage),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(backgroundImageBlur.dp)
                )
            }
            HorizontalPager(
                beyondViewportPageCount = 3,
                state = pagerState,
                modifier = Modifier.padding(paddingValues),
            ) { page ->
                Column(
                    modifier = Modifier
                        .graphicsLayer {
                            val pageOffset =
                                (pagerState.currentPage - page + pagerState.currentPageOffsetFraction)
                            lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                .also { scale ->
                                    scaleX = scale
                                    scaleY = scale
                                }
                            alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                        }
                        .fillMaxSize()
                ) {
                    val tab = if (enableCalendarView) {
                        tabOfWhenEnableCalendar(page)
                    } else {
                        tabOfWhenDisableCalendar(page)
                    }
                    tab.content(this)
                }
            }
        }
    }

    HandleErrorMessage(flow = viewModel.errorMessage)
    val emptyUser by viewModel.emptyUser.collectAsState()
    if (emptyUser) {
        navController.replaceTo<RouteMain>(RouteLogin(false))
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
        modifier = Modifier.weight(1F),
        selected = checked,
        icon = {
            Icon(
                painter = stateOf(checked = checked, pair = icon),
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
                contentDescription = null,
            )
        },
        label = {
            Text(text = label)
        },
        onClick = {
            onSelect()
        },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
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