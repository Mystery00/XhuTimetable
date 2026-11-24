package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import me.zhanghai.compose.preference.Preference
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.Platform
import vip.mystery0.xhu.timetable.base.HandleBackPress
import vip.mystery0.xhu.timetable.config.store.GlobalConfigStore
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.TitleTemplate
import vip.mystery0.xhu.timetable.model.format
import vip.mystery0.xhu.timetable.platform
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuConfirmDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuTemplateInputDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.viewmodel.CustomUiViewModel
import kotlin.math.roundToInt

@Composable
fun CustomUiScreen() {
    val viewModel = koinViewModel<CustomUiViewModel>()
    val navController = LocalNavController.current

    val randomWeekCourse by viewModel.randomWeekCourse.collectAsState()
    val randomTodayCourse by viewModel.randomTodayCourse.collectAsState()
    val weekTitleTemplate by viewModel.weekTitleTemplate.collectAsState()
    val weekNotTitleTemplate by viewModel.weekNotTitleTemplate.collectAsState()
    val customUi by viewModel.customUi.collectAsState()

    var previewMode by remember { mutableIntStateOf(0) } // 0 = 周课表预览, 1 = 今日卡片预览
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    val titleTemplateDialog = rememberXhuDialogState()
    val notTitleTemplateDialog = rememberXhuDialogState()
    val resetConfirmDialog = rememberXhuDialogState()
    val unsavedExitDialog = rememberXhuDialogState()

    fun handleBack() {
        if (hasUnsavedChanges) {
            unsavedExitDialog.show()
        } else {
            navController.popBackStack()
        }
    }

    HandleBackPress(backPressed = ::handleBack)

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "自定义课表界面",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = ::handleBack) {
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
                            contentDescription = "恢复默认",
                        )
                    }
                    IconButton(
                        onClick = {
                            viewModel.save()
                            hasUnsavedChanges = false
                            showToast("界面配置已保存")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "保存设置",
                            tint = if (hasUnsavedChanges) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        val userBg = remember { GlobalConfigStore.backgroundImage }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            // 顶部实时预览区（可切换周课表 / 今日卡片，背景使用用户真实背景）
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
            ) {
                Column {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(230.dp)
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                    ) {
                        if (userBg != null) {
                            AsyncImage(
                                model = userBg,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(customUi.backgroundImageBlur.dp),
                            )
                        } else {
                            Image(
                                painter = painterResource(XhuImages.defaultBackgroundImage),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .blur(customUi.backgroundImageBlur.dp),
                            )
                        }

                        AnimatedContent(
                            targetState = previewMode,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            label = "customUiPreviewMode",
                        ) { mode ->
                            if (mode == 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(rememberScrollState()),
                                    contentAlignment = Alignment.TopCenter,
                                ) {
                                    Row(modifier = Modifier.width(260.dp)) {
                                        val columnSize = 3
                                        val size = randomWeekCourse.size / columnSize
                                        for (columnIndex in 0 until columnSize) {
                                            val start = columnIndex * size
                                            val end = (columnIndex + 1) * size
                                            if (end <= randomWeekCourse.size) {
                                                val list = randomWeekCourse.subList(start, end)
                                                Column(modifier = Modifier.weight(1f)) {
                                                    list.forEachIndexed { rowIndex, course ->
                                                        val step = when {
                                                            columnIndex == 0 && rowIndex == 1 -> 2
                                                            columnIndex == 2 && rowIndex == 1 -> 2
                                                            columnIndex == 1 && rowIndex == 0 -> 2
                                                            else -> 1
                                                        }
                                                        val title = if (course.thisWeek) {
                                                            course.format(customUi.weekTitleTemplate)
                                                        } else {
                                                            course.format(customUi.weekNotTitleTemplate)
                                                        }
                                                        // 稳定冲突标记，彻底避免拖动滑块时闪烁
                                                        val stableConflict = if (columnIndex == 1 && rowIndex == 0) 1 else 0
                                                        BuildWeekItem(
                                                            customUi = customUi,
                                                            backgroundColor = course.backgroundColor,
                                                            itemStep = step,
                                                            title = title,
                                                            textColor = if (course.thisWeek) Color.White else Color.Gray,
                                                            conflictCount = stableConflict,
                                                            onClick = {},
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (randomTodayCourse.isNotEmpty()) {
                                        DrawCourseCard(
                                            customUi = customUi,
                                            course = randomTodayCourse.first(),
                                            multiAccountMode = false,
                                            showStatus = true,
                                        )
                                    }
                                }
                            }
                        }

                        // 右上角刷新预览样例按钮
                        FilledTonalIconButton(
                            onClick = { viewModel.refreshRandomCourse() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(8.dp)
                                .size(34.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "换一组示例课程",
                                modifier = Modifier.size(18.dp),
                            )
                        }
                    }

                    // 预览模式切换栏
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                    ) {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            listOf("本周课表预览", "今日卡片预览").forEachIndexed { index, label ->
                                SegmentedButton(
                                    selected = previewMode == index,
                                    onClick = { previewMode = index },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // 下方配置参数滚动区
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 24.dp),
            ) {
                XhuSettingsGroup(title = { Text("今日课程页面") }) {
                    val todayBackgroundAlpha by viewModel.todayBackgroundAlpha.collectAsState()
                    LabeledSliderItem(
                        title = "卡片不透明度",
                        valueText = "${(todayBackgroundAlpha * 100).roundToInt()}%",
                        value = todayBackgroundAlpha * 100f,
                        range = 30f..100f,
                        onValueChange = {
                            viewModel.todayBackgroundAlpha.value = it / 100f
                            viewModel.update()
                            hasUnsavedChanges = true
                        },
                    )
                }

                XhuSettingsGroup(title = { Text("本周课程页面") }) {
                    val weekItemHeight by viewModel.weekItemHeight.collectAsState()
                    LabeledSliderItem(
                        title = "格子高度",
                        valueText = "${weekItemHeight.roundToInt()} dp",
                        value = weekItemHeight,
                        range = 50f..150f,
                        onValueChange = {
                            viewModel.weekItemHeight.value = it
                            viewModel.update()
                            hasUnsavedChanges = true
                        },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                    val weekBackgroundAlpha by viewModel.weekBackgroundAlpha.collectAsState()
                    LabeledSliderItem(
                        title = "课程格子不透明度",
                        valueText = "${(weekBackgroundAlpha * 100).roundToInt()}%",
                        value = weekBackgroundAlpha * 100f,
                        range = 30f..100f,
                        onValueChange = {
                            viewModel.weekBackgroundAlpha.value = it / 100f
                            viewModel.update()
                            hasUnsavedChanges = true
                        },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                    val weekItemCorner by viewModel.weekItemCorner.collectAsState()
                    LabeledSliderItem(
                        title = "格子圆角大小",
                        valueText = "${weekItemCorner.roundToInt()} dp",
                        value = weekItemCorner,
                        range = 0f..24f,
                        onValueChange = {
                            viewModel.weekItemCorner.value = it
                            viewModel.update()
                            hasUnsavedChanges = true
                        },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                    val weekTitleTextSize by viewModel.weekTitleTextSize.collectAsState()
                    LabeledSliderItem(
                        title = "格子文字大小",
                        valueText = "${weekTitleTextSize.roundToInt()} sp",
                        value = weekTitleTextSize,
                        range = 8f..18f,
                        onValueChange = {
                            viewModel.weekTitleTextSize.value = it
                            viewModel.update()
                            hasUnsavedChanges = true
                        },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                    val backgroundImageBlur by viewModel.backgroundImageBlur.collectAsState()
                    LabeledSliderItem(
                        title = "背景动态模糊半径",
                        subtitle = if (platform() == Platform.ANDROID && !isBlurEnable()) "仅 Android 12 及以上支持" else null,
                        valueText = "${backgroundImageBlur.roundToInt()} dp",
                        value = backgroundImageBlur,
                        range = 0f..30f,
                        enabled = isBlurEnable(),
                        onValueChange = {
                            viewModel.backgroundImageBlur.value = it
                            viewModel.update()
                            hasUnsavedChanges = true
                        },
                    )
                }

                XhuSettingsGroup(title = { Text("格子文本模板") }) {
                    Preference(
                        title = { Text("本周课程模板") },
                        summary = { Text(weekTitleTemplate.replace("\n", " ↵ ")) },
                        onClick = { titleTemplateDialog.show() },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    )
                    Preference(
                        title = { Text("非本周课程模板") },
                        summary = { Text(weekNotTitleTemplate.replace("\n", " ↵ ")) },
                        onClick = { notTitleTemplateDialog.show() },
                    )
                }
            }
        }
    }

    val templateTokens = listOf(
        "课程名称" to "{${TitleTemplate.COURSE_NAME.tpl}}",
        "上课地点" to "{${TitleTemplate.LOCATION.tpl}}",
        "教师名称" to "{${TitleTemplate.TEACHER_NAME.tpl}}",
    )

    XhuTemplateInputDialog(
        state = titleTemplateDialog,
        title = "本周课程格子模板",
        initialValue = weekTitleTemplate,
        resetValue = CustomUi.DEFAULT.weekTitleTemplate,
        quickTokens = templateTokens,
        onConfirm = { newValue ->
            viewModel.weekTitleTemplate.value = newValue
            viewModel.update()
            hasUnsavedChanges = true
        },
    )

    XhuTemplateInputDialog(
        state = notTitleTemplateDialog,
        title = "非本周课程格子模板",
        initialValue = weekNotTitleTemplate,
        resetValue = CustomUi.DEFAULT.weekNotTitleTemplate,
        quickTokens = templateTokens,
        onConfirm = { newValue ->
            viewModel.weekNotTitleTemplate.value = newValue
            viewModel.update()
            hasUnsavedChanges = true
        },
    )

    XhuConfirmDialog(
        state = resetConfirmDialog,
        title = "恢复默认界面配置",
        message = "确认将所有课表界面参数和模板重置为默认值吗？",
        confirmText = "恢复默认",
        onConfirm = {
            viewModel.reset()
            hasUnsavedChanges = false
            showToast("已恢复默认设置")
        },
    )

    XhuConfirmDialog(
        state = unsavedExitDialog,
        title = "有未保存的更改",
        message = "你修改了课表界面参数但尚未保存，是否保存后退出？",
        confirmText = "保存并退出",
        dismissText = "直接放弃",
        onConfirm = {
            viewModel.save()
            hasUnsavedChanges = false
            showToast("设置已保存")
            navController.popBackStack()
        },
    )
}

@Composable
private fun LabeledSliderItem(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    subtitle: String? = null,
    enabled: Boolean = true,
    onValueChange: (Float) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f),
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            ) {
                Text(
                    text = valueText,
                    style = MaterialTheme.typography.labelMedium,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
        Slider(
            value = value.coerceIn(range.start, range.endInclusive),
            onValueChange = onValueChange,
            valueRange = range,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

expect fun isBlurEnable(): Boolean
