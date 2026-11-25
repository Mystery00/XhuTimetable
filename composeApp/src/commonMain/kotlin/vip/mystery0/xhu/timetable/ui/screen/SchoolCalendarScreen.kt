package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.trackEvent
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.SchoolCalendarViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun SchoolCalendarScreen() {
    val viewModel = koinViewModel<SchoolCalendarViewModel>()
    val navController = LocalNavController.current

    val loading by viewModel.loading.collectAsState()
    val area by viewModel.area.collectAsState()
    val schoolCalendarData by viewModel.schoolCalendarData.collectAsState()

    val areaDialogState = rememberXhuDialogState()
    val shareLauncher = rememberShareFileLauncher()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            val currentArea = schoolCalendarData.area
            CenterAlignedTopAppBar(
                title = {
                    if (area.size > 1) {
                        Surface(
                            onClick = { areaDialogState.show() },
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                            ) {
                                Text(
                                    text = if (currentArea.isBlank()) "校历" else "校历 · $currentArea",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = "切换校区",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (currentArea.isBlank()) "校历" else "校历（$currentArea）",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    IconButton(
                        enabled = schoolCalendarData.cacheFile != null,
                        onClick = {
                            trackEvent("分享校历")
                            schoolCalendarData.cacheFile?.let { shareLauncher.launch(it) }
                        }
                    ) {
                        Icon(
                            painter = XhuIcons.Action.send,
                            contentDescription = "分享校历",
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        val bottomPadding = paddingValues.calculateBottomPadding()
        // 移除与双指缩放/拖拽冲突的空 PullToRefreshBox，释放完整画布视野
        Box(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    start = paddingValues.calculateStartPadding(layoutDirection),
                    end = paddingValues.calculateEndPadding(layoutDirection),
                )
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            if (schoolCalendarData.cacheFile != null) {
                CoilZoomAsyncImage(
                    model = schoolCalendarData.cacheFile,
                    contentDescription = "校历图片",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomPadding),
                )
            } else if (!loading.loading) {
                StateScreen(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = bottomPadding),
                    title = loading.errorMessage.ifBlank { "暂无校历数据" },
                    buttonText = "重新加载",
                    imageRes = painterResource(Res.drawable.state_no_data),
                    onButtonClick = { viewModel.init() }
                )
            }
            if (loading.loading) {
                CircularProgressIndicator()
            }
        }
    }

    XhuSingleSelectDialog(
        state = areaDialogState,
        title = "选择查看校历的校区",
        options = area,
        selectedIndex = area.indexOfFirst { it.area == schoolCalendarData.area },
        itemLabel = { it.area },
        onSelect = { _, select -> viewModel.changeArea(select.area) },
    )

    HandleErrorMessage(errorMessage = loading.errorMessage) {
        viewModel.clearLoadingErrorMessage()
    }
}
