package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import coil.request.CachePolicy
import coil.size.Scale
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.SchoolCalendarViewModel

class SchoolCalendarActivity : BaseComposeActivity() {
    private val viewModel: SchoolCalendarViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun BuildContent() {
        val loading by viewModel.loading.collectAsState()
        val area by viewModel.area.collectAsState()
        val loadImageUrl by viewModel.loadImageUrl.collectAsState()

        val scope = rememberCoroutineScope()
        val scaffoldState: BackdropScaffoldState =
            rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
        var selected by remember { mutableStateOf(loadImageUrl.first) }

        LaunchedEffect(loadImageUrl) {
            selected = loadImageUrl.first
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
                            "暂未实现！".toast()
                        }) {
                            Icon(
                                painter = XhuIcons.Action.download,
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
                SwipeRefresh(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberSwipeRefreshState(loading.loading),
                    onRefresh = { },
                ) {
                    Image(
                        painter = rememberImagePainter(
                            data = loadImageUrl.second,
                            builder = {
                                scale(Scale.FIT)
                                memoryCachePolicy(CachePolicy.ENABLED)
                                diskCachePolicy(CachePolicy.ENABLED)
                                listener(
                                    onError = { _, t ->
                                        t.message ?: "加载失败".toast(true)
                                    },
                                )
                            }
                        ),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            })
        if (loading.errorMessage.isNotBlank()) {
            loading.errorMessage.toast(true)
        }
    }
}