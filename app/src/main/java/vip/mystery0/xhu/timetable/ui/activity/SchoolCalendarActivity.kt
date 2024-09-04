package vip.mystery0.xhu.timetable.ui.activity

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.github.panpf.zoomimage.CoilZoomAsyncImage
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.trackEvent
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.SchoolCalendarViewModel

class SchoolCalendarActivity : BaseSelectComposeActivity() {
    private val viewModel: SchoolCalendarViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @Composable
    override fun BuildContent() {
        val loading by viewModel.loading.collectAsState()
        val area by viewModel.area.collectAsState()
        val schoolCalendarData by viewModel.schoolCalendarData.collectAsState()

        val selectDialogState = rememberXhuDialogState()

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
                    title = { Text(text = "${title}（${schoolCalendarData.area}）") },
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
                        IconButton(onClick = {
                            trackEvent("分享校历")
                            val shareIntent = Intent().apply {
                                action = Intent.ACTION_SEND
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
                                Intent.createChooser(shareIntent, null)
                            )
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
                            if (!selectDialogState.showing) {
                                selectDialogState.show()
                            }
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
                val pullRefreshState = rememberPullRefreshState(
                    refreshing = loading.loading,
                    onRefresh = { },
                )
                Box(modifier = Modifier.pullRefresh(pullRefreshState)) {
                    if (schoolCalendarData.imageUrl.isNotBlank()) {
                        CoilZoomAsyncImage(
                            model = schoolCalendarData.cacheFile,
                            contentDescription = "view image",
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
        ShowSelectDialog(
            dialogTitle = "请选择需要查看校历的校区",
            options = area,
            selectIndex = -1,
            itemTransform = { it.area },
            state = selectDialogState,
            onSelect = { _, select ->
                viewModel.changeArea(select.area)
            },
            withButtonView = false,
        )
        HandleErrorMessage(errorMessage = loading.errorMessage) {
            viewModel.clearLoadingErrorMessage()
        }
    }
}