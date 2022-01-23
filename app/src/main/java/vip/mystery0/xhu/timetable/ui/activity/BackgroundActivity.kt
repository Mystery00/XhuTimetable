package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.Background
import vip.mystery0.xhu.timetable.viewmodel.BackgroundViewModel
import vip.mystery0.xhu.timetable.viewmodel.DownloadProgressState

class BackgroundActivity : BaseComposeActivity() {
    private val viewModel: BackgroundViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BuildContent() {
        val backgroundListState by viewModel.backgroundListState.collectAsState()
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
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
                            scope.launch {
                                "暂不支持".toast()
                            }
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
            SwipeRefresh(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
                state = rememberSwipeRefreshState(backgroundListState.loading),
                onRefresh = { },
                swipeEnabled = false,
            ) {
                LazyVerticalGrid(
                    modifier = Modifier
                        .fillMaxSize(),
                    cells = GridCells.Adaptive(minSize = 120.dp)
                ) {
                    items(backgroundListState.backgroundList) { background ->
                        PhotoItem(background)
                    }
                }
            }
        }

        val progressState by viewModel.progressState.collectAsState()
        ShowDownloadDialog(downloadProgressState = progressState)

        if (backgroundListState.errorMessage.isNotBlank()) {
            backgroundListState.errorMessage.toast(true)
        }
    }

    @Composable
    fun PhotoItem(background: Background) {
        val data = background.thumbnailUrl.ifBlank {
            background.imageResId
        }
        Box(
            modifier = Modifier
                .height(160.dp)
                .width(120.dp)
                .clickable {
                    viewModel.setBackground(background.backgroundId)
                },
        ) {
            Image(
                painter = rememberImagePainter(data = data) {
                    memoryCachePolicy(CachePolicy.ENABLED)
                    diskCachePolicy(CachePolicy.DISABLED)
                },
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
            )
            if (background.checked) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize(),
                    color = Color.Transparent,
                    border = BorderStroke(
                        width = 6.dp,
                        color = XhuColor.Common.blackText
                    ),
                ) {}
                Icon(
                    painter = XhuIcons.checked,
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.BottomEnd)
                        .width(24.dp)
                        .height(24.dp),
                    contentDescription = null,
                )
            }
        }
    }

    @Composable
    private fun ShowDownloadDialog(downloadProgressState: DownloadProgressState) {
        if (downloadProgressState.finished) return
        AlertDialog(onDismissRequest = {},
            title = {
                Text(
                    text = "正在下载...",
                    fontWeight = FontWeight.W700,
                    style = MaterialTheme.typography.h6,
                )
            }, text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = downloadProgressState.text)
                    if (!downloadProgressState.finished) {
                        LinearProgressIndicator(
                            progress = (downloadProgressState.progress.progress / 100).toFloat(),
                        )
                    }
                }
            }, confirmButton = {
            }, dismissButton = {
            })
    }
}