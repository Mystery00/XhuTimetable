package vip.mystery0.xhu.timetable.ui.activity

import android.Manifest
import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.res.ColorStateList
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.accompanist.pager.*
import com.google.accompanist.permissions.*
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.google.android.material.math.MathUtils.lerp
import com.vanpra.composematerialdialogs.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.core.component.inject
import vip.mystery0.xhu.timetable.*
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.api.ServerApi
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.GlobalConfig
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuStateIcons
import vip.mystery0.xhu.timetable.ui.theme.isDarkMode
import vip.mystery0.xhu.timetable.ui.theme.stateOf
import vip.mystery0.xhu.timetable.utils.isTwiceClick
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import kotlin.math.absoluteValue
import kotlin.math.min

@OptIn(ExperimentalMaterialApi::class)
class MainActivity : BaseComposeActivity(setSystemUiColor = false, registerEventBus = true) {
    private val serverApi: ServerApi by inject()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var modalBottomSheetState: ModalBottomSheetState
    private lateinit var addDialogState: MaterialDialogState
    private lateinit var detectDialogState: MaterialDialogState
    private var detectContent: String = ""

    private val ext: MainActivityExt
        get() = MainActivityExt(this, viewModel, modalBottomSheetState, addDialogState)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        onBackPressedDispatcher.addCallback {
            if (isTwiceClick())
                finish()
            else
                "再按一次退出${appName}".toast()
        }
    }

    @OptIn(ExperimentalPagerApi::class, ExperimentalPermissionsApi::class)
    @Composable
    override fun BuildContent() {
        val systemUiController = rememberSystemUiController()
        val useDarkIcons = MaterialTheme.colors.isLight
        val barColor = XhuColor.mainBarColorBackground
        SideEffect {
            systemUiController.setSystemBarsColor(barColor, darkIcons = useDarkIcons)
            systemUiController.setNavigationBarColor(barColor, darkIcons = useDarkIcons)
        }
        ShowCheckUpdateDialog()
        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState(initialPage = 0)
        val loading by viewModel.loading.collectAsState()
        val poems by viewModel.poems.collectAsState()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionState = rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
            if (permissionState.status != PermissionStatus.Granted) {
                coroutineScope.launch {
                    permissionState.launchPermissionRequest()
                }
            }
        }

        modalBottomSheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        addDialogState = rememberMaterialDialogState()
        detectDialogState = rememberMaterialDialogState()

        val isDarkMode = isDarkMode()

        LaunchedEffect(key1 = "init", block = {
            viewModel.loadBackground(isDarkMode)
        })
        LaunchedEffect(pagerState) {
            snapshotFlow { pagerState.currentPage }.collect {
                viewModel.dismissWeekView()
            }
        }

        ModalBottomSheetLayout(
            sheetState = modalBottomSheetState,
            sheetShape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp),
            scrimColor = Color.Black.copy(alpha = 0.32f),
            sheetContent = {
                Box(
                    modifier = Modifier
                        .defaultMinSize(minHeight = 1.dp)
                ) {
                    Column {
                        TextButton(
                            modifier = Modifier.align(Alignment.End),
                            onClick = {
                                coroutineScope.launch {
                                    modalBottomSheetState.hide()
                                }
                            }) {
                            Text(text = "收起")
                        }
                        poems?.origin?.let { poemsDetail ->
                            SelectionContainer(
                                modifier = Modifier.padding(
                                    top = 8.dp,
                                    start = 32.dp,
                                    end = 32.dp,
                                    bottom = 32.dp,
                                ),
                            ) {
                                Column {
                                    Text(
                                        text = "《${poemsDetail.title}》",
                                        fontSize = 14.sp,
                                        textAlign = TextAlign.Center,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "[${poemsDetail.dynasty}] ${poemsDetail.author}",
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = poemsDetail.content.joinToString("\n"),
                                        fontSize = 12.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    if (!poemsDetail.translate.isNullOrEmpty()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = "诗词大意：${
                                                poemsDetail.translate!!.joinToString(
                                                    ""
                                                )
                                            }",
                                            fontSize = 11.sp,
                                            modifier = Modifier.fillMaxWidth(),
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }) {
            Scaffold(
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(barColor)
                            .height(45.dp),
                    ) {
                        val tab = tabOf(pagerState.currentPage)
                        if (tab.titleBar != null) {
                            tab.titleBar.invoke(this, ext)
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .padding(horizontal = 8.dp)
                                    .align(Alignment.CenterEnd)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        trackEvent("手动刷新课表")
                                        viewModel.loadCourseList()
                                    },
                            ) {
                                val isDark = isDarkMode()
                                AndroidView(
                                    factory = { context ->
                                        ImageView(context)
                                    },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .align(Alignment.Center),
                                ) {
                                    it.imageTintList =
                                        ColorStateList.valueOf(if (isDark) android.graphics.Color.WHITE else android.graphics.Color.BLACK)
                                    it.setImageResource(R.drawable.ic_sync)
                                    val animation =
                                        ObjectAnimator.ofFloat(it, "rotation", 0F, 360F).apply {
                                            duration = 1000L
                                            repeatCount = ValueAnimator.INFINITE
                                            addListener(object : Animator.AnimatorListener {
                                                override fun onAnimationStart(animation: Animator) {
                                                }

                                                override fun onAnimationEnd(animation: Animator) {
                                                }

                                                override fun onAnimationCancel(animation: Animator) {
                                                }

                                                override fun onAnimationRepeat(animation: Animator) {
                                                    if (!loading) {
                                                        animation.cancel()
                                                    }
                                                }
                                            })
                                        }
                                    if (loading) {
                                        animation.start()
                                    }
                                }
                            }
                            tab.title(this, ext)
                        }
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            thickness = 0.33.dp,
                            color = XhuColor.Common.divider,
                        )
                    }
                },
                bottomBar = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth(),
                            thickness = 0.33.dp,
                            color = XhuColor.Common.divider,
                        )
                        BottomNavigation(
                            backgroundColor = barColor,
                            elevation = 0.dp,
                        ) {
                            DrawNavigationItem(
                                state = pagerState,
                                tab = Tab.TODAY,
                                icon = XhuStateIcons.todayCourse,
                                coroutineScope = coroutineScope,
                            )
                            DrawNavigationItem(
                                state = pagerState,
                                tab = Tab.WEEK,
                                icon = XhuStateIcons.weekCourse,
                                coroutineScope = coroutineScope,
                            )
                            DrawNavigationItem(
                                state = pagerState,
                                tab = Tab.PROFILE,
                                icon = XhuStateIcons.profile,
                                coroutineScope = coroutineScope,
                            )
                        }
                    }
                }
            ) { paddingValues ->
                Box {
                    val backgroundImage by viewModel.backgroundImage.collectAsState()
                    val backgroundImageBlur by viewModel.backgroundImageBlur.collectAsState()
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
                        count = 3,
                        state = pagerState,
                        modifier = Modifier
                            .padding(paddingValues),
                    ) { page ->
                        Column(
                            modifier = Modifier
                                .graphicsLayer {
                                    val pageOffset =
                                        calculateCurrentOffsetForPage(page).absoluteValue
                                    lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                        .also { scale ->
                                            scaleX = scale
                                            scaleY = scale
                                        }
                                    alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                                }
                                .fillMaxSize()
                        ) {
                            when (page) {
                                Tab.TODAY.index -> Tab.TODAY.content(this, ext)
                                Tab.WEEK.index -> Tab.WEEK.content(this, ext)
                                Tab.PROFILE.index -> Tab.PROFILE.content(this, ext)
                            }
                        }
                    }
                    val showWeekView by viewModel.showWeekView.collectAsState()
                    val weekView by viewModel.weekView.collectAsState()
                    AnimatedVisibility(
                        visible = showWeekView,
                        enter = expandVertically(expandFrom = Alignment.Top) + fadeIn(),
                        exit = shrinkVertically(shrinkTowards = Alignment.Top) + fadeOut(),
                    ) {
                        val currentWeek by viewModel.week.collectAsState()
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier
                                .background(weekViewBackgroundColor)
                                .fillMaxWidth(),
                        ) {
                            items(weekView.size) { index ->
                                val week = weekView[index]
                                val thisWeek = week.thisWeek
                                val color = when {
                                    thisWeek -> weekViewThisWeekColor
                                    week.weekNum == currentWeek -> weekViewCurrentWeekColor
                                    else -> Color.Transparent
                                }
                                Column(
                                    modifier = Modifier
                                        .background(
                                            color = color,
                                            shape = MaterialTheme.shapes.medium,
                                        )
                                        .padding(
                                            horizontal = 4.dp,
                                            vertical = 2.dp,
                                        )
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                        ) {
                                            viewModel.changeCurrentWeek(week.weekNum)
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    Text(
                                        text = "第${week.weekNum}周",
                                        fontSize = 10.sp,
                                        color = Color.Black,
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Canvas(
                                        modifier = Modifier
                                            .height(32.dp)
                                            .width(28.dp)
                                    ) {
                                        val canvasHeight = size.height
                                        val canvasWidth = size.width
                                        //每一项大小
                                        val itemHeight = canvasHeight / 5F
                                        val itemWidth = canvasWidth / 5F
                                        //圆心位置
                                        val itemCenterHeight = itemHeight / 2F
                                        val itemCenterWidth = itemWidth / 2F
                                        //半径
                                        val radius = min(itemCenterHeight, itemCenterWidth) - 1F
                                        for (day in 0 until 5) {
                                            for (time in 0 until 5) {
                                                val light = week.array[time][day]
                                                drawCircle(
                                                    color = if (light) weekViewLightColor else weekViewGrayColor,
                                                    center = Offset(
                                                        x = itemWidth * time + itemCenterWidth,
                                                        y = itemHeight * day + itemCenterHeight,
                                                    ),
                                                    radius = radius,
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = if (thisWeek) "本周" else "",
                                        fontSize = 8.sp,
                                        color = Color.Black,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
        }
        val emptyUser by viewModel.emptyUser.collectAsState()
        if (emptyUser) {
            intentTo(LoginActivity::class) {
                it.putExtra(AccountSettingsActivity.INTENT_EXTRA, true)
            }
        }

        ShowAddDialog(addDialogState)
        ShowDetectDialog(detectDialogState)
    }

    @Composable
    private fun ShowCheckUpdateDialog() {
        val version by viewModel.version.collectAsState()
        val newVersion = version ?: return
        var show = newVersion.versionCode > appVersionCodeNumber
        if (GlobalConfig.debugMode && GlobalConfig.alwaysShowNewVersion) {
            show = true
        }
        if (!show) return
        //需要提示版本更新
        CheckUpdate(
            version = newVersion,
            onDownload = {
                if (it) {
                    viewModel.downloadApk()
                } else {
                    viewModel.downloadPatch()
                }
            },
            onIgnore = {
                if (!newVersion.forceUpdate) {
                    viewModel.ignoreVersion(newVersion)
                }
            }
        )
    }

    @Composable
    private fun ShowAddDialog(
        dialogState: MaterialDialogState,
    ) {
        MaterialDialog(dialogState = dialogState) {
            title("请选择需要添加的数据类型")
            listItems(list = listOf("自定义课程", "自定义事项")) { index, _ ->
                when (index) {
                    0 -> {
                        intentTo(CustomCourseActivity::class)
                    }

                    1 -> {
                        intentTo(CustomThingActivity::class)
                    }
                }
            }
        }
    }

    @Composable
    private fun ShowDetectDialog(
        dialogState: MaterialDialogState,
    ) {
        MaterialDialog(dialogState = dialogState,
            buttons = {
                positiveButton("确定")
            }) {
            title("服务器状态")
            message(detectContent)
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun RowScope.DrawNavigationItem(
        state: PagerState,
        tab: Tab,
        icon: Pair<Pair<Int, Int>, Pair<Int, Int>>,
        coroutineScope: CoroutineScope,
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f)
                .clickable(
                    onClick = {
                        coroutineScope.launch {
                            state.animateScrollToPage(tab.index)
                        }
                    },
                    indication = null,
                    interactionSource = MutableInteractionSource()
                ),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val checked = state.currentPage == tab.index
            Icon(
                painter = stateOf(checked = checked, pair = icon),
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
                contentDescription = null
            )
            Spacer(Modifier.padding(top = 2.dp))
            val showTomorrowCourse by viewModel.showTomorrowCourse.collectAsState()
            val label = if (showTomorrowCourse) tab.otherLabel else tab.label
            Text(text = label, fontSize = 12.sp, color = colorOf(checked = checked))
            Spacer(Modifier.padding(top = 2.dp))
            AnimatedVisibility(visible = checked) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(5.dp),
                    color = XhuColor.iconChecked
                ) {}
            }
            Spacer(Modifier.padding(bottom = 2.dp))
        }
    }

    suspend fun serverDetect() {
        kotlin.runCatching {
            val response = serverApi.serverDetect()
            if (response.isSuccessful) {
                detectContent = response.body()!!.joinToString("\n") {
                    "${it.title}: ${if (it.health) "正常" else "异常"}"
                }
                detectDialogState.show()
            } else {
                toastString("服务器不可用，请联系开发者", true)
            }
        }.onFailure {
            toastString("服务器不可用，请联系开发者", true)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkUnRead {
            viewModel.checkUnReadNotice()
            viewModel.checkUnReadFeedback()
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    fun updateUIFromConfig(uiEvent: UIEvent) {
        viewModel.loadConfig()
        when (uiEvent.eventType) {
            EventType.MULTI_MODE_CHANGED,
            EventType.CHANGE_MAIN_USER -> {
                viewModel.checkMainUser()
                viewModel.loadCourseList(true)
            }

            EventType.CHANGE_CURRENT_YEAR_AND_TERM,
            EventType.CHANGE_SHOW_CUSTOM_COURSE -> {
                viewModel.loadCourseList(true)
            }

            EventType.MAIN_USER_LOGOUT -> {
                viewModel.checkMainUser()
            }

            EventType.CHANGE_AUTO_SHOW_TOMORROW_COURSE -> {
                viewModel.loadCourseList(false)
                viewModel.calculateTodayTitle()
            }

            EventType.CHANGE_SHOW_NOT_THIS_WEEK,
            EventType.CHANGE_TERM_START_TIME,
            EventType.CHANGE_COURSE_COLOR,
            EventType.CHANGE_CUSTOM_UI -> {
                viewModel.loadCourseList(false)
            }

            EventType.CHANGE_SHOW_CUSTOM_THING -> {
                viewModel.loadThingList(true)
            }

            EventType.CHANGE_MAIN_BACKGROUND -> {
                viewModel.loadBackground()
            }

            EventType.READ_NOTICE -> {
                viewModel.clearLastCheckUnreadTime()
            }

            else -> {
            }
        }
    }
}

private val weekViewBackgroundColor = Color(0xFFE2F7F6)
private val weekViewThisWeekColor = Color(0xFFB7F5F2)
private val weekViewCurrentWeekColor = Color(0xFFFFFFFF)
private val weekViewLightColor = Color(0xFF3FCAB8)
private val weekViewGrayColor = Color(0xFFCFDBDB)

@Composable
private fun colorOf(checked: Boolean): Color =
    if (checked) XhuColor.iconChecked else MaterialTheme.colors.onSurface

@ExperimentalMaterialApi
private enum class Tab(
    val index: Int,
    val label: String,
    val otherLabel: String = label,
    val titleBar: TabTitle? = null,
    val title: TabTitle = {},
    val content: TabContent,
) {
    TODAY(
        index = 0,
        label = "今日",
        otherLabel = "明日",
        titleBar = todayCourseTitleBar,
        content = todayCourseContent,
    ),
    WEEK(
        index = 1,
        label = "本周",
        title = weekCourseTitle,
        content = weekCourseContent,
    ),
    PROFILE(
        index = 2,
        label = "我的",
        title = profileCourseTitle,
        content = profileCourseContent,
    ),
}

@ExperimentalMaterialApi
private fun tabOf(index: Int): Tab = when (index) {
    0 -> Tab.TODAY
    1 -> Tab.WEEK
    2 -> Tab.PROFILE
    else -> throw NoSuchElementException()
}

@OptIn(ExperimentalMaterialApi::class)
data class MainActivityExt(
    val activity: MainActivity,
    val viewModel: MainViewModel,
    val modalBottomSheetState: ModalBottomSheetState,
    val addDialogState: MaterialDialogState,
)

typealias TabTitle = @Composable BoxScope.(MainActivityExt) -> Unit
typealias TabContent = @Composable ColumnScope.(MainActivityExt) -> Unit