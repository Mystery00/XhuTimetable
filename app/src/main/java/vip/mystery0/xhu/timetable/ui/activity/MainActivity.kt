package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.*
import com.google.android.material.math.MathUtils.lerp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.publicDeviceId
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
    @Composable
    override fun BuildContent() {
        val coroutineScope = rememberCoroutineScope()
        val pagerState = rememberPagerState(pageCount = 3)
        Scaffold(
            topBar = {

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
                    Row {
                        Text("Page: $page")
                    }
                    Row {
                        Text(publicDeviceId)
                    }
                    Row {
                        Text(appName)
                    }
                    Row {
                        Text(appVersionName)
                    }
                    Row {
                        Text(SessionManager.mainUser.info.userName)
                    }
                }
            }
        }
    }

    @ExperimentalPagerApi
    @ExperimentalAnimationApi
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

private enum class Tab(val index: Int, val label: String) {
    TODAY(0, "今日"),
    WEEK(1, "本周"),
    PROFILE(2, "我的"),
}
