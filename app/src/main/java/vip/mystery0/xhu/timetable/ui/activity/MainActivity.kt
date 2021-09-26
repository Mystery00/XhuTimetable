package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.size.Scale
import com.google.accompanist.pager.*
import com.google.android.material.math.MathUtils.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.ui.theme.XhuStateIcons
import vip.mystery0.xhu.timetable.ui.theme.stateOf
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import kotlin.math.absoluteValue

class MainActivity : BaseComposeActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val viewModel: MainViewModel by viewModels()

    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @Composable
    override fun BuildContent() {
        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState(pageCount = 3)
        Scaffold(
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    tabOf(pagerState.currentPage).title(this, viewModel)
                }
            },
            bottomBar = {
                BottomNavigation(
                    backgroundColor = Color.White,
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
        ) { paddingValues ->
            Box {
                Image(
                    painter = rememberImagePainter(data = R.mipmap.main_bg) {
                        scale(Scale.FIT)
                        diskCachePolicy(CachePolicy.DISABLED)
                    },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                )
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .padding(paddingValues),
                ) { page ->
                    Column(
                        modifier = Modifier
                            .graphicsLayer {
                                val pageOffset = calculateCurrentOffsetForPage(page).absoluteValue
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
                            Tab.TODAY.index -> Tab.TODAY.content(this, viewModel)
                            Tab.WEEK.index -> Tab.WEEK.content(this, viewModel)
                            Tab.PROFILE.index -> Tab.PROFILE.content(this, viewModel)
                        }
                    }
                }
            }
        }
    }

    @ExperimentalPagerApi
    @ExperimentalAnimationApi
    @ExperimentalMaterialApi
    @Composable
    private fun RowScope.DrawNavigationItem(
        state: PagerState,
        tab: Tab,
        icon: Pair<Int, Int>,
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
                painter = stateOf(checked = checked, icon = icon),
                tint = Color.Unspecified,
                modifier = Modifier.size(24.dp),
                contentDescription = null
            )
            Spacer(Modifier.padding(top = 2.dp))
            Text(text = tab.label, fontSize = 12.sp, color = colorOf(checked = checked))
            Spacer(Modifier.padding(top = 2.dp))
            AnimatedVisibility(visible = checked) {
                Surface(
                    shape = CircleShape,
                    modifier = Modifier.size(5.dp),
                    color = MaterialTheme.colors.primary
                ) {}
            }
            Spacer(Modifier.padding(bottom = 2.dp))
        }
    }
}

@Composable
private fun colorOf(checked: Boolean): Color =
    if (checked) MaterialTheme.colors.primary else Color.Black

@ExperimentalMaterialApi
private enum class Tab(
    val index: Int,
    val label: String,
    val title: TabTitle,
    val content: TabContent,
) {
    TODAY(0, "今日", todayCourseTitle, todayCourseContent),
    WEEK(1, "本周", weekCourseTitle, weekCourseContent),
    PROFILE(2, "我的", profileCourseTitle, profileCourseContent),
}

@ExperimentalMaterialApi
private fun tabOf(index: Int): Tab = when (index) {
    0 -> Tab.TODAY
    1 -> Tab.WEEK
    2 -> Tab.PROFILE
    else -> throw NoSuchElementException()
}

typealias TabTitle = @Composable BoxScope.(MainViewModel) -> Unit
typealias TabContent = @Composable ColumnScope.(MainViewModel) -> Unit