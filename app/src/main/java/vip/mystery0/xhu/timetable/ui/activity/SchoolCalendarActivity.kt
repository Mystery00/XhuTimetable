package vip.mystery0.xhu.timetable.ui.activity

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ExtendedFloatingActionButton
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.listItems
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.trackEvent
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.SchoolCalendarData
import vip.mystery0.xhu.timetable.viewmodel.SchoolCalendarViewModel

class SchoolCalendarActivity : BaseComposeActivity() {
    private val viewModel: SchoolCalendarViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun BuildContent() {
        val loading by viewModel.loading.collectAsState()
        val area by viewModel.area.collectAsState()
        val schoolCalendarData by viewModel.schoolCalendarData.collectAsState()

        val selectDialogState = rememberMaterialDialogState()
        val selectedArea = remember { mutableStateOf(schoolCalendarData.area) }

        LaunchedEffect(schoolCalendarData) {
            selectedArea.value = schoolCalendarData.area
        }

        fun onBack() {
            finish()
        }
        BackHandler(
            onBack = {
                onBack()
            }
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
                    navigationIcon = {
                        IconButton(onClick = {
                            onBack()
                        }) {
                            Icon(
                                painter = XhuIcons.back,
                                contentDescription = null,
                            )
                        }
                    },
                )
            },
            floatingActionButton = {
                Column(horizontalAlignment = Alignment.End) {
                    FloatingActionButton(
                        modifier = Modifier.size(40.dp),
                        onClick = {
                            trackEvent("分享校历")
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                putExtra(
                                    Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                                        this@SchoolCalendarActivity,
                                        packageName,
                                        schoolCalendarData.cacheFile,
                                    )
                                )
                                type = "image/*"
                            }
                            startActivity(
                                Intent.createChooser(
                                    shareIntent,
                                    "分享校历到"
                                )
                            )
                        }) {
                        Icon(
                            painter = XhuIcons.Action.send,
                            contentDescription = null,
                            tint = XhuColor.Common.whiteText,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    ExtendedFloatingActionButton(
                        text = {
                            Text(text = "切换校区", color = XhuColor.Common.whiteText)
                        },
                        onClick = {
                            if (!selectDialogState.showing) {
                                selectDialogState.show()
                            }
                        },
                        icon = {
                            Icon(
                                painter = XhuIcons.Action.switch,
                                contentDescription = null,
                                tint = XhuColor.Common.whiteText,
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
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = loading.loading,
                    onRefresh = { },
                )
                Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(schoolCalendarData.imageUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .listener(onError = { _, result ->
                                result.throwable.message ?: "加载失败".toast(true)
                            })
                            .build(),
                        loading = {
                            Box(contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(modifier = Modifier.size(48.dp))
                            }
                        },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
        ShowSelectDialog(dialogState = selectDialogState, area = area, selectedArea = selectedArea)
        if (loading.errorMessage.isNotBlank()) {
            loading.errorMessage.toast(true)
        }
    }

    @Composable
    private fun ShowSelectDialog(
        dialogState: MaterialDialogState,
        area: List<SchoolCalendarData>,
        selectedArea: MutableState<String>,
    ) {
        MaterialDialog(dialogState = dialogState) {
            title("请选择需要查看校历的校区")
            listItems(list = area.map { it.area }) { index, _ ->
                selectedArea.value = area[index].area
            }
        }
    }
}