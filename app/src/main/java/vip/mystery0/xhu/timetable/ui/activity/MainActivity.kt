package vip.mystery0.xhu.timetable.ui.activity

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.material.math.MathUtils.lerp
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.store.EventBus
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.response.ClientVersion
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.trackEvent
import vip.mystery0.xhu.timetable.ui.activity.loading.LoadingButton
import vip.mystery0.xhu.timetable.ui.activity.loading.LoadingValue
import vip.mystery0.xhu.timetable.ui.component.XhuDialogState
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.theme.XhuStateIcons
import vip.mystery0.xhu.timetable.ui.theme.isDarkMode
import vip.mystery0.xhu.timetable.ui.theme.stateOf
import vip.mystery0.xhu.timetable.utils.isTwiceClick
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel

class MainActivity : BaseComposeActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var addDialogState: XhuDialogState
    private lateinit var weekViewDialogState: XhuDialogState

    private val ext: MainActivityExt
        get() = MainActivityExt(this, viewModel, addDialogState, weekViewDialogState)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback {
            if (isTwiceClick())
                finish()
            else
                "再按一次退出${appName}".toast()
        }
        updateUIFromConfig()
    }

    override fun onPause() {
        addDialogState.hide()
        weekViewDialogState.hide()
        super.onPause()
    }

    @OptIn(
        ExperimentalPermissionsApi::class,
        ExperimentalFoundationApi::class,
        ExperimentalMaterial3Api::class
    )
    @Composable
    override fun BuildContent() {
        LaunchedEffect(Unit) {
            viewModel.init()
        }
        ShowCheckUpdateDialog()
        val enableCalendarView by viewModel.enableCalendarView.collectAsState()
        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState(initialPage = 0) { if (enableCalendarView) 4 else 3 }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
            if (permissionState.status != PermissionStatus.Granted) {
                LaunchedEffect("init permission") {
                    coroutineScope.launch {
                        permissionState.launchPermissionRequest()
                    }
                }
            }
        }

        addDialogState = rememberXhuDialogState()
        weekViewDialogState = rememberXhuDialogState()

        val backgroundImage by viewModel.backgroundImage.collectAsState()
        val backgroundImageBlur by viewModel.backgroundImageBlur.collectAsState()

        val isDarkMode = isDarkMode()

        LaunchedEffect("init") {
            viewModel.loadBackground(isDarkMode)
        }

        val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            topBar = {
                val tab = if (enableCalendarView) {
                    tabOfWhenEnableCalendar(pagerState.currentPage)
                } else {
                    tabOfWhenDisableCalendar(pagerState.currentPage)
                }
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        tab.titleBar?.let { it(ext) }
                    },
                    actions = {
                        val loading by viewModel.loading.collectAsState()
                        val actions = tab.actions
                        val loadingValue = if (loading) LoadingValue.Loading else LoadingValue.Stop
                        if (actions != null) {
                            if (actions(this, ext)) {
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
                NavigationBar {
                    val tabs = if (enableCalendarView) {
                        Tab.entries
                    } else {
                        Tab.entries.filter { it != Tab.CALENDAR }
                    }
                    tabs.forEachIndexed { index, tab ->
                        DrawNavigationItem(
                            checked = pagerState.currentPage == index,
                            tab = tab,
                            icon = tab.icon,
                        ) {
                            coroutineScope.launch {
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
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(backgroundImage)
                            .memoryCachePolicy(CachePolicy.DISABLED)
                            .diskCachePolicy(CachePolicy.DISABLED)
                            .build(),
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
                        if (enableCalendarView) {
                            tabOfWhenEnableCalendar(page).content(this, ext)
                        } else {
                            tabOfWhenDisableCalendar(page).content(this, ext)
                        }
                    }
                }
            }
        }

        HandleErrorMessage(flow = viewModel.errorMessage)
        val emptyUser by viewModel.emptyUser.collectAsState()
        if (emptyUser) {
            intentTo(LoginActivity::class) {
                it.putExtra(AccountSettingsActivity.INTENT_EXTRA, true)
            }
        }
    }

    @Composable
    private fun ShowCheckUpdateDialog() {
        val version by viewModel.version.collectAsState()
        val newVersion = version ?: return
        if (newVersion == ClientVersion.EMPTY) {
            return
        }
        val scope = rememberCoroutineScope()
        //需要提示版本更新
        CheckUpdate(
            version = newVersion,
            onDownload = {
                if (it) {
                    viewModel.downloadApk(newVersion)
                } else {
                    viewModel.downloadPatch(newVersion)
                }
            },
            onIgnore = {
                viewModel.ignoreVersion(newVersion)
            },
            onClose = {
                scope.launch {
                    StartRepo.version.emit(ClientVersion.EMPTY)
                }
            },
        )
    }

    @Composable
    private fun RowScope.DrawNavigationItem(
        checked: Boolean,
        tab: Tab,
        icon: Pair<Pair<Int, Int>, Pair<Int, Int>>,
        onSelect: () -> Unit = {},
    ) {
        val showTomorrowCourse by viewModel.showTomorrowCourse.collectAsState()
        val label = if (showTomorrowCourse) tab.otherLabel else tab.label

        NavigationBarItem(
            modifier = Modifier.weight(1F),
            selected = checked,
            icon = {
                Icon(
                    painter = stateOf(checked = checked, pair = icon),
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                    contentDescription = null
                )
            },
            label = {
                Text(text = label)
            },
            onClick = {
                onSelect()
            },
        )
    }

    private fun updateUIFromConfig() {
        lifecycleScope.launch {
            EventBus.subscribe(lifecycle) { eventType ->
                Log.i("TAG", "updateUIFromConfig: $eventType")
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
                    EventType.CHANGE_TERM_START_TIME,
                    EventType.CHANGE_COURSE_COLOR,
                    EventType.CHANGE_CUSTOM_UI,
                    EventType.CHANGE_CUSTOM_ACCOUNT_TITLE -> {
                        viewModel.loadLocalDataToState(changeWeekOnly = true)
                    }

                    EventType.CHANGE_MAIN_BACKGROUND -> {
                        viewModel.loadBackground()
                    }

                    EventType.UPDATE_NOTICE_CHECK -> {
                        viewModel.checkUnReadNotice()
                    }

                    EventType.UPDATE_FEEDBACK_CHECK -> {
                        viewModel.checkUnReadFeedback()
                    }
                }
            }
        }
    }
}

private enum class Tab(
    val label: String,
    val otherLabel: String = label,
    val icon: Pair<Pair<Int, Int>, Pair<Int, Int>>,
    val titleBar: TabTitle? = null,
    val actions: TabAction? = null,
    val content: TabContent,
) {
    TODAY(
        label = "今日",
        otherLabel = "明日",
        icon = XhuStateIcons.todayCourse,
        titleBar = todayCourseTitleBar,
        actions = todayCourseActions,
        content = todayCourseContent,
    ),
    WEEK(
        label = "本周",
        icon = XhuStateIcons.weekCourse,
        titleBar = weekCourseTitleBar,
        actions = weekCourseActions,
        content = weekCourseContent,
    ),
    CALENDAR(
        label = "月历",
        icon = XhuStateIcons.calendar,
        titleBar = calendarTitleBar,
        actions = calendarActions,
        content = calendarContent,
    ),
    PROFILE(
        label = "我的",
        icon = XhuStateIcons.profile,
        titleBar = profileCourseTitleBar,
        content = profileCourseContent,
    ),
}

private fun tabOfWhenEnableCalendar(index: Int): Tab = when (index) {
    0 -> Tab.TODAY
    1 -> Tab.WEEK
    2 -> Tab.CALENDAR
    3 -> Tab.PROFILE
    else -> Tab.PROFILE
}

private fun tabOfWhenDisableCalendar(index: Int): Tab = when (index) {
    0 -> Tab.TODAY
    1 -> Tab.WEEK
    2 -> Tab.PROFILE
    else -> Tab.PROFILE
}

data class MainActivityExt(
    val activity: MainActivity,
    val viewModel: MainViewModel,
    val addDialogState: XhuDialogState,
    val weekViewDialogState: XhuDialogState,
)

typealias TabTitle = @Composable (MainActivityExt) -> Unit
typealias TabAction = @Composable RowScope.(MainActivityExt) -> Boolean
typealias TabContent = @Composable ColumnScope.(MainActivityExt) -> Unit