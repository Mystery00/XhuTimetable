package vip.mystery0.xhu.timetable.ui.activity

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropScaffoldState
import androidx.compose.material.BackdropValue
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.trackEvent
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.SchoolCalendarViewModel

class SchoolCalendarActivity : BaseComposeActivity() {
    private val viewModel: SchoolCalendarViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun BuildContent() {
        val loading by viewModel.loading.collectAsState()
        val area by viewModel.area.collectAsState()
        val schoolCalendarData by viewModel.schoolCalendarData.collectAsState()

        val scope = rememberCoroutineScope()
        val scaffoldState: BackdropScaffoldState =
            rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
        var selected by remember { mutableStateOf(schoolCalendarData.area) }

        LaunchedEffect(schoolCalendarData) {
            selected = schoolCalendarData.area
        }

        fun onBack() {
            if (scaffoldState.isConcealed) {
                finish()
            } else {
                scope.launch {
                    scaffoldState.conceal()
                }
            }
        }
        BackHandler(
            onBack = {
                onBack()
            }
        )

        BackdropScaffold(
            modifier = Modifier,
            scaffoldState = scaffoldState,
            appBar = {
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
                    actions = {
                        if (scaffoldState.isRevealed) {
                            IconButton(onClick = {
                                viewModel.changeArea(selected)
                            }) {
                                Icon(
                                    painter = XhuIcons.Action.done,
                                    contentDescription = null,
                                )
                            }
                        }
                        IconButton(onClick = {
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
                            )
                        }
                    }
                )
            }, backLayerContent = {
                Column(modifier = Modifier.padding(16.dp)) {
                    area.forEach {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                ) {
                                    selected = it.area
                                },
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(selected = selected == it.area, onClick = null)
                            Text(text = it.area)
                        }
                    }
                }
            }, frontLayerContent = {
                Box(modifier = Modifier.fillMaxSize()) {
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
            })
        if (loading.errorMessage.isNotBlank()) {
            loading.errorMessage.toast(true)
        }
    }
}