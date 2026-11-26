package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleBackPress
import vip.mystery0.xhu.timetable.config.Customisable
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuColorPickerDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuConfirmDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.CustomCourseColorViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun CustomCourseColorScreen() {
    val viewModel = koinViewModel<CustomCourseColorViewModel>()
    val navController = LocalNavController.current
    val list by viewModel.listState.collectAsState()

    val colorDialogState = rememberXhuDialogState()
    val resetColorConfirmState = rememberXhuDialogState()

    var editingCourseName by remember { mutableStateOf("") }
    var editingCourseColor by remember { mutableStateOf(ColorPool.safeGet(0)) }
    var pendingResetCourseName by remember { mutableStateOf("") }
    var showSearchView by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    val inputField = @Composable {
        SearchBarDefaults.InputField(
            query = searchText,
            onQueryChange = {
                searchText = it
                viewModel.loadList(it)
            },
            onSearch = {},
            expanded = false,
            onExpandedChange = {},
            placeholder = {
                Text(modifier = Modifier.clearAndSetSemantics {}, text = "搜索课程名称…")
            },
            trailingIcon = {
                IconButton(onClick = {
                    if (searchText.isNotEmpty()) {
                        searchText = ""
                        viewModel.loadList("")
                    } else {
                        showSearchView = false
                    }
                }) {
                    Icon(XhuIcons.close, contentDescription = "关闭搜索")
                }
            }
        )
    }

    fun back() {
        if (showSearchView) {
            showSearchView = false
            searchText = ""
            viewModel.loadList("")
        } else {
            navController.popBackStack()
        }
    }

    HandleBackPress(enabled = showSearchView, backPressed = ::back)
    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    AnimatedContent(
                        targetState = showSearchView,
                        transitionSpec = {
                            val enterAnimation = scaleIn(initialScale = 0.9f) + fadeIn()
                            val exitAnimation = scaleOut(targetScale = 0.9f) + fadeOut()
                            (enterAnimation togetherWith exitAnimation).using(
                                SizeTransform(clip = false)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        label = "courseColorSearchBar",
                    ) { targetShowSearchView ->
                        if (targetShowSearchView) {
                            val colors = SearchBarDefaults.colors()
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = SearchBarDefaults.inputFieldShape,
                                color = colors.containerColor,
                                contentColor = contentColorFor(colors.containerColor),
                                tonalElevation = SearchBarDefaults.TonalElevation,
                                content = inputField,
                            )
                        } else {
                            Text(
                                text = "自定义课程颜色",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = ::back) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    if (!showSearchView) {
                        IconButton(onClick = { showSearchView = true }) {
                            Icon(
                                painter = XhuIcons.Action.search,
                                contentDescription = "搜索课程",
                            )
                        }
                    }
                }
            )
        },
    ) { paddingValues ->
        val layoutDirection = LocalLayoutDirection.current
        if (list.isEmpty()) {
            Box(modifier = Modifier.padding(paddingValues)) {
                StateScreen(
                    title = if (searchText.isNotBlank()) "未找到匹配课程" else "暂无课程数据",
                    imageRes = painterResource(Res.drawable.state_no_data),
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = paddingValues.calculateTopPadding(),
                        start = paddingValues.calculateStartPadding(layoutDirection),
                        end = paddingValues.calculateEndPadding(layoutDirection),
                    ),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 10.dp,
                    bottom = 16.dp + paddingValues.calculateBottomPadding(),
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(list, key = { it.first }) { item ->
                    BuildCourseColorCard(
                        item = item,
                        onClick = {
                            editingCourseName = item.first
                            editingCourseColor = item.second.data
                            colorDialogState.show()
                        },
                        onResetClick = {
                            pendingResetCourseName = item.first
                            resetColorConfirmState.show()
                        },
                    )
                }
            }
        }
    }

    XhuColorPickerDialog(
        state = colorDialogState,
        title = editingCourseName.ifBlank { "修改课程颜色" },
        initialColor = editingCourseColor,
        extraButtonText = "恢复默认",
        onExtraButtonClick = {
            viewModel.updateColor(editingCourseName, null)
        },
        onSelectColor = { newColor ->
            viewModel.updateColor(editingCourseName, newColor)
        },
    )

    XhuConfirmDialog(
        state = resetColorConfirmState,
        title = "恢复默认颜色",
        message = "确认将课程「$pendingResetCourseName」的自定义颜色恢复为默认配色吗？",
        confirmText = "恢复默认",
        onConfirm = {
            if (pendingResetCourseName.isNotBlank()) {
                viewModel.updateColor(pendingResetCourseName, null)
                pendingResetCourseName = ""
            }
        },
    )
}

@Composable
private fun BuildCourseColorCard(
    item: Pair<String, Customisable<Color>>,
    onClick: () -> Unit,
    onResetClick: () -> Unit,
) {
    val isCustom = item.second.custom
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = XhuColor.cardBackground),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = CircleShape,
                color = item.second.data,
                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
                modifier = Modifier.size(34.dp),
            ) {}

            Text(
                text = item.first,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )

            if (isCustom) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                ) {
                    Text(
                        text = "已自定义",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                    )
                }
                IconButton(
                    onClick = onResetClick,
                    modifier = Modifier.size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.RestartAlt,
                        contentDescription = "恢复默认颜色",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }
    }
}
