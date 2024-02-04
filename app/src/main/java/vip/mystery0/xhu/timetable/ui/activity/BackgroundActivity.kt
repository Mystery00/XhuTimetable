package vip.mystery0.xhu.timetable.ui.activity

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.viewModels
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
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.customImageDir
import vip.mystery0.xhu.timetable.screenHeight
import vip.mystery0.xhu.timetable.screenWidth
import vip.mystery0.xhu.timetable.ui.activity.contract.BackgroundResultContract
import vip.mystery0.xhu.timetable.ui.activity.contract.UCropResultContract
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.Background
import vip.mystery0.xhu.timetable.viewmodel.BackgroundViewModel
import vip.mystery0.xhu.timetable.viewmodel.DownloadProgressState
import java.io.File
import java.time.Instant

class BackgroundActivity : BaseComposeActivity() {
    companion object {
        private const val FILE_NAME_CUSTOM_BACKGROUND = "custom-background"
    }

    private val viewModel: BackgroundViewModel by viewModels()
    private val cropLauncher = registerForActivityResult(UCropResultContract()) {
        if (it == null) {
            "操作已取消".toast()
            return@registerForActivityResult
        }
        viewModel.setCustomBackground(it)
    }
    private val imageSelectLauncher =
        registerForActivityResult(BackgroundResultContract()) { intent ->
            if (intent == null) {
                "操作已取消".toast()
                return@registerForActivityResult
            }
            intent.data?.let {
                val saveFile = File(
                    customImageDir,
                    "${FILE_NAME_CUSTOM_BACKGROUND}-${Instant.now().toEpochMilli()}.png"
                )
                val destinationUri = Uri.fromFile(saveFile)
                cropLauncher.launch(
                    UCrop.of(it, destinationUri)
                        .withAspectRatio(screenWidth.toFloat(), screenHeight.toFloat())
                        .withMaxResultSize(screenWidth * 10, screenHeight * 10)
                        .withOptions(UCrop.Options().apply {
                            setCompressionFormat(Bitmap.CompressFormat.PNG)
                        })
                )
            }
        }

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun BuildContent() {
        val backgroundListState by viewModel.backgroundListState.collectAsState()
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
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
                            imageSelectLauncher.launch("image/*")
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
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = backgroundListState.loading,
                    onRefresh = { },
                )
                Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 120.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(
                            items = backgroundListState.backgroundList,
                            key = { it.backgroundId },
                        ) { background ->
                            PhotoItem(background)
                        }
                    }
                }
            }
        }

        val progressState by viewModel.progressState.collectAsState()
        ShowDownloadDialog(downloadProgressState = progressState)

        HandleErrorMessage(errorMessage = backgroundListState.errorMessage) {
            viewModel.clearErrorMessage()
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
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
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
}