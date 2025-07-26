package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.Background
import vip.mystery0.xhu.timetable.viewmodel.BackgroundViewModel
import vip.mystery0.xhu.timetable.viewmodel.DownloadProgressState

@Composable
fun BackgroundScreen() {
    val viewModel = koinViewModel<BackgroundViewModel>()

    val navController = LocalNavController.current!!

    val backgroundListState by viewModel.backgroundListState.collectAsState()

    val scope = rememberCoroutineScope()
    val pickerLauncher = rememberFilePickerLauncher(FileKitType.Image) { imageFile ->
        val image = imageFile ?: return@rememberFilePickerLauncher
        //TODO crop
        viewModel.setCustomBackground(image)
    }

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "自定义背景图") },
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
                        scope.launch {
                            viewModel.setBackground(0L)
                        }
                    }) {
                        Icon(
                            painter = XhuIcons.reset,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    IconButton(onClick = {
                        pickerLauncher.launch()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val pullToRefreshState = rememberPullToRefreshState()
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .pullToRefresh(
                        state = pullToRefreshState,
                        isRefreshing = backgroundListState.loading,
                        onRefresh = {},
                        enabled = false,
                    ),
            ) {
                items(
                    items = backgroundListState.backgroundList,
                    key = { it.backgroundId },
                ) { background ->
                    PhotoItem(background) {
                        viewModel.setBackground(background.backgroundId)
                    }
                }
            }
            Indicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = backgroundListState.loading,
                state = pullToRefreshState,
            )
        }
    }

    val progressState by viewModel.progressState.collectAsState()
    ShowDownloadDialog(downloadProgressState = progressState)

    HandleErrorMessage(errorMessage = backgroundListState.errorMessage) {
        viewModel.clearErrorMessage()
    }
}

@Composable
fun PhotoItem(background: Background, onSelect: () -> Unit) {
    val data = background.thumbnailUrl.ifBlank {
        background.imageResUri
    }
    Box(
        modifier = Modifier
            .height(160.dp)
            .width(120.dp)
            .clickable {
                onSelect()
            },
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(data)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build(),
            loading = {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
            },
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(),
        )
        if (background.checked) {
            Surface(
                modifier = Modifier
                    .fillMaxSize(),
                color = Color.Transparent,
                border = BorderStroke(
                    width = 6.dp,
                    color = MaterialTheme.colorScheme.secondary
                ),
            ) {}
            Image(
                painter = XhuIcons.checked,
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.BottomEnd)
                    .size(24.dp),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun ShowDownloadDialog(downloadProgressState: DownloadProgressState) {
    if (downloadProgressState.finished) return
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        Surface(
            modifier = Modifier
                .size(144.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 24.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(
                    progress = { downloadProgressState.progress.progress / 100.0F },
                    modifier = Modifier.size(72.dp),
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "正在下载...",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                )
            }
        }
    }
}