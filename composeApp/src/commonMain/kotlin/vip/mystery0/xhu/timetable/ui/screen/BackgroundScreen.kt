package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.PermMedia
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
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
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuConfirmDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
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
    val navController = LocalNavController.current
    val backgroundListState by viewModel.backgroundListState.collectAsState()

    val scope = rememberCoroutineScope()
    val imageCropper = rememberImageCropper()
    val resetConfirmDialog = rememberXhuDialogState()

    val pickerLauncher = rememberFilePickerLauncher(FileKitType.Image) { imageFile ->
        val image = imageFile ?: return@rememberFilePickerLauncher
        scope.safeLaunch(Dispatchers.Main) {
            when (val result = imageCropper.crop(image.toImageSrc())) {
                CropResult.Cancelled -> {}
                is CropError -> showToast("图片裁剪失败")
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
                title = {
                    Text(
                        text = "自定义背景图",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
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
                    IconButton(onClick = { resetConfirmDialog.show() }) {
                        Icon(
                            imageVector = Icons.Rounded.SettingsBackupRestore,
                            contentDescription = "恢复默认背景",
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { pickerLauncher.launch() },
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.PermMedia,
                        contentDescription = null,
                    )
                },
                text = { Text("相册选取") },
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            val pullToRefreshState = rememberPullToRefreshState()
            if (backgroundListState.backgroundList.isEmpty() && backgroundListState.errorMessage.isNotBlank()) {
                StateScreen(
                    title = backgroundListState.errorMessage,
                    buttonText = "重新加载",
                    imageRes = painterResource(Res.drawable.state_no_data),
                    onButtonClick = { viewModel.init() }
                )
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
                            background = background,
                            isChecked = background.backgroundId == backgroundListState.selectedBackgroundId,
                        ) {
                            viewModel.setBackground(background.backgroundId)
                        }
                    }
                }
            }
            LoadingIndicator(
                modifier = Modifier.align(Alignment.TopCenter),
                isRefreshing = backgroundListState.loading,
                state = pullToRefreshState,
            )
        }
    }

    val progressState by viewModel.progressState.collectAsState()
    ShowDownloadDialog(downloadProgressState = progressState)

    val cropState = imageCropper.cropState
    if (cropState != null) {
        ImageCropperDialog(
            state = cropState,
            style = cropperStyle(
                shapes = emptyList(),
                aspects = listOf(AspectRatio(9, 16))
            )
        )
    }

    XhuConfirmDialog(
        state = resetConfirmDialog,
        title = "恢复默认背景图",
        message = "确认清除当前自定义背景并恢复为默认背景图吗？",
        confirmText = "恢复默认",
        onConfirm = { viewModel.setBackground(0L) },
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
    val data = background.thumbnailUrl.ifBlank { background.imageResUri }
    val shape: Shape = RoundedCornerShape(16.dp)

    Box(
        modifier = Modifier
            .aspectRatio(9f / 16f)
            .clip(shape)
            .then(
                if (isChecked) {
                    Modifier.border(BorderStroke(3.dp, MaterialTheme.colorScheme.primary), shape)
                } else {
                    Modifier.border(
                        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        shape,
                    )
                }
            )
            .clickable(onClick = onSelect),
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(data)
                .build(),
            loading = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                    )
                }
            },
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        if (isChecked) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp,
                    modifier = Modifier.size(36.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "已选中",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShowDownloadDialog(downloadProgressState: DownloadProgressState) {
    if (downloadProgressState.finished) return
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        Surface(
            modifier = Modifier.size(164.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
            shadowElevation = 12.dp,
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp),
            ) {
                CircularProgressIndicator(
                    progress = { downloadProgressState.progress.progress / 100.0f },
                    modifier = Modifier.size(56.dp),
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "正在下载 ${downloadProgressState.progress.progress}%",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}
