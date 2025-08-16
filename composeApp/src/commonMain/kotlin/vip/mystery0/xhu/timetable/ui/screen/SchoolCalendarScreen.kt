package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.trackEvent
import vip.mystery0.xhu.timetable.ui.component.ShowSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.SchoolCalendarViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun SchoolCalendarScreen() {
    val viewModel = koinViewModel<SchoolCalendarViewModel>()

    val navController = LocalNavController.current!!

    val loading by viewModel.loading.collectAsState()
    val area by viewModel.area.collectAsState()
    val schoolCalendarData by viewModel.schoolCalendarData.collectAsState()

    val selectDialogState = rememberUseCaseState()
    val shareLauncher = rememberShareFileLauncher()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "校历（${schoolCalendarData.area}）") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.popBackStack()
                    }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = null,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = {
                        trackEvent("分享校历")
                        schoolCalendarData.cacheFile?.let {
                            shareLauncher.launch(it)

                        }
                    }) {
                        Icon(
                            painter = XhuIcons.Action.send,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                ExtendedFloatingActionButton(
                    text = {
                        Text(text = "切换校区")
                    },
                    onClick = {
                        selectDialogState.show()
                    },
                    icon = {
                        Icon(
                            painter = XhuIcons.Action.switch,
                            contentDescription = null,
                        )
                    })
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            PullToRefreshBox(
                isRefreshing = loading.loading,
                onRefresh = { },
            ) {
                if (schoolCalendarData.imageUrl.isNotBlank()) {
                    CoilZoomAsyncImage(
                        model = schoolCalendarData.cacheFile,
                        contentDescription = "view image",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    StateScreen(
                        modifier = Modifier.fillMaxSize(),
                        title = loading.errorMessage.ifBlank { "暂无数据" },
                        buttonText = "重新加载",
                        imageRes = painterResource(Res.drawable.state_no_data),
                        verticalArrangement = Arrangement.Top,
                        onButtonClick = {
                            viewModel.init()
                        }
                    )
                }
            }
        }
    }
    ShowSingleSelectDialog(
        dialogTitle = "请选择需要查看校历的校区",
        options = area,
        selectIndex = -1,
        itemTransform = { it.area },
        useCaseState = selectDialogState,
        onSelect = { _, select ->
            viewModel.changeArea(select.area)
        },
        withButtonView = false,
    )
    HandleErrorMessage(errorMessage = loading.errorMessage) {
        viewModel.clearLoadingErrorMessage()
    }
}