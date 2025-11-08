package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.PermMedia
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.attafitamim.krop.core.crop.AspectRatio
import com.attafitamim.krop.core.crop.CropError
import com.attafitamim.krop.core.crop.CropResult
import com.attafitamim.krop.core.crop.crop
import com.attafitamim.krop.core.crop.cropperStyle
import com.attafitamim.krop.core.crop.rememberImageCropper
import com.attafitamim.krop.filekit.ImageFormat
import com.attafitamim.krop.filekit.encodeToByteArray
import com.attafitamim.krop.filekit.toImageSrc
import com.attafitamim.krop.ui.ImageCropperDialog
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import kotlinx.coroutines.Dispatchers
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.Background
import vip.mystery0.xhu.timetable.viewmodel.BackgroundViewModel
import vip.mystery0.xhu.timetable.viewmodel.DownloadProgressState
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun BackgroundScreen() {
    val viewModel = koinViewModel<BackgroundViewModel>()

    val navController = LocalNavController.current!!

    val backgroundListState by viewModel.backgroundListState.collectAsState()

    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()
    val pickerLauncher = rememberFilePickerLauncher(FileKitType.Image) { imageFile ->
        val image = imageFile ?: return@rememberFilePickerLauncher
        scope.safeLaunch(Dispatchers.Main) {
            when (val result = imageCropper.crop(image.toImageSrc())) {
                CropResult.Cancelled -> {}
                is CropError -> {
                    showToast("图片裁剪失败")
                }

                is CropResult.Success -> {
                    val bitmap = result.bitmap
                    val bytes = bitmap.encodeToByteArray(ImageFormat.JPEG)
                    viewModel.setCustomBackground(bytes)
                }
            }
        }
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
                        viewModel.setBackground(0L)
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.SettingsBackupRestore,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    pickerLauncher.launch()
                },
            ) {
                Icon(
                    imageVector = Icons.Rounded.PermMedia,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val pullToRefreshState = rememberPullToRefreshState()
            if (backgroundListState.backgroundList.isEmpty() && backgroundListState.errorMessage.isNotBlank()) {
                val loadingErrorMessage = backgroundListState.errorMessage
                StateScreen(
                    title = loadingErrorMessage,
                    buttonText = "重新加载",
                    imageRes = painterResource(Res.drawable.state_no_data),
                    verticalArrangement = Arrangement.Top,
                    onButtonClick = {
                        viewModel.init()
                    }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
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
                        PhotoItem(
                            background,
                            background.backgroundId == backgroundListState.selectedBackgroundId,
                        ) {
                            viewModel.setBackground(background.backgroundId)
                        }
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

    val cropState = imageCropper.cropState
    if (cropState != null) ImageCropperDialog(
        state = cropState, style = cropperStyle(
            shapes = emptyList(),
            aspects = listOf(AspectRatio(9, 16))
        )
    )

    HandleErrorMessage(errorMessage = backgroundListState.errorMessage) {
        viewModel.clearErrorMessage()
    }
}

@Composable
fun PhotoItem(
    background: Background,
    isChecked: Boolean,
    onSelect: () -> Unit,
) {
    val data = background.thumbnailUrl.ifBlank {
        background.imageResUri
    }

    fun Modifier.borderWhen(
        condition: Boolean,
        border: BorderStroke,
        shape: Shape,
    ): Modifier =
        if (condition) border(border, shape) else this

    Box(
        modifier = Modifier
            .padding(6.dp)
            .height(240.dp)
            .clip(MaterialTheme.shapes.medium)
            .borderWhen(
                isChecked,
                BorderStroke(4.dp, MaterialTheme.colorScheme.primary),
                MaterialTheme.shapes.medium,
            )
            .clickable {
                onSelect()
            },
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(data)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .diskCachePolicy(CachePolicy.DISABLED)
                .build(),
            loading = {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(48.dp))
                }
            },
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.shapes.medium),
            contentScale = ContentScale.Crop
        )
        if (isChecked) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp),
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