package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.SettingsBackupRestore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.maxkeppeker.sheets.core.models.base.SelectionButton
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.input.InputDialog
import com.maxkeppeler.sheets.input.models.InputCustomView
import com.maxkeppeler.sheets.input.models.InputSelection
import me.zhanghai.compose.preference.SliderPreference
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.Platform
import vip.mystery0.xhu.timetable.model.CustomUi
import vip.mystery0.xhu.timetable.model.TitleTemplate
import vip.mystery0.xhu.timetable.model.format
import vip.mystery0.xhu.timetable.platform
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.component.preference.XhuSettingsMenuLink
import vip.mystery0.xhu.timetable.ui.component.xhuHeader
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.viewmodel.CustomUiViewModel
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun CustomUiScreen() {
    val viewModel = koinViewModel<CustomUiViewModel>()

    val navController = LocalNavController.current!!

    val randomWeekCourse by viewModel.randomWeekCourse.collectAsState()
    val randomTodayCourse by viewModel.randomTodayCourse.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "自定义课表界面") },
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
                        viewModel.reset()
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.SettingsBackupRestore,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                    IconButton(onClick = {
                        viewModel.save()
                        showToast("设置已保存")
                    }) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        },
    ) { paddingValues ->
        val weekTitleTemplate by viewModel.weekTitleTemplate.collectAsState()
        val weekNotTitleTemplate by viewModel.weekNotTitleTemplate.collectAsState()
        val customUi by viewModel.customUi.collectAsState()

        val showCustomTitleTemplateDialog = rememberUseCaseState()
        val showCustomNotTitleTemplateDialog = rememberUseCaseState()

        Column(modifier = Modifier.padding(paddingValues)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(360.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(XhuImages.defaultBackgroundImage),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(customUi.backgroundImageBlur.dp),
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(modifier = Modifier.width(240.dp)) {
                            Row {
                                //列数
                                val columnSize = 3
                                //每一列数量
                                val size = randomWeekCourse.size / columnSize
                                for (columnIndex in 0 until columnSize) {
                                    val start = columnIndex * size
                                    val end = (columnIndex + 1) * size
                                    val list = randomWeekCourse.subList(start, end)
                                    Column(modifier = Modifier.weight(1F)) {
                                        list.forEachIndexed { rowIndex, course ->
                                            val step = when {
                                                columnIndex == 0 && rowIndex == 1 -> 2
                                                columnIndex == 2 && rowIndex == 1 -> 2
                                                columnIndex == 1 && rowIndex == 0 -> 2
                                                else -> 1
                                            }
                                            val title =
                                                if (course.thisWeek) course.format(customUi.weekTitleTemplate)
                                                else course.format(customUi.weekNotTitleTemplate)
                                            BuildWeekItem(
                                                customUi = customUi,
                                                backgroundColor = course.backgroundColor,
                                                itemStep = step,
                                                title = title,
                                                textColor = if (course.thisWeek) Color.White else Color.Gray,
                                                showMore = Random.nextBoolean(),
                                            ) {
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        if (randomTodayCourse.isNotEmpty()) {
                            DrawCourseCard(
                                customUi = customUi,
                                course = randomTodayCourse.first(),
                                multiAccountMode = false,
                                showStatus = false,
                            )
                        }
                    }
                }
            }
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                XhuSettingsGroup(title = {
                    Text(text = "操作")
                }) {
                    XhuSettingsMenuLink(
                        title = { Text(text = "刷新课程列表") },
                        onClick = {
                            viewModel.refreshRandomCourse()
                        }
                    )
                }
                XhuSettingsGroup(title = {
                    Text(text = "今日课程页面")
                }) {
                    val todayBackgroundAlpha by viewModel.todayBackgroundAlpha.collectAsState()
                    BuildSeekBar(
                        title = "卡片透明度",
                        value = todayBackgroundAlpha * 100,
                        start = 30F,
                        end = 100F,
                        listener = { newValue ->
                            viewModel.todayBackgroundAlpha.value = newValue / 100F
                            viewModel.update()
                        })
                }
                XhuSettingsGroup(title = {
                    Text(text = "本周课程页面")
                }) {
                    val weekItemHeight by viewModel.weekItemHeight.collectAsState()
                    BuildSeekBar(
                        title = "格子高度",
                        value = weekItemHeight,
                        start = 50F,
                        end = 150F,
                        listener = { newValue ->
                            viewModel.weekItemHeight.value = newValue
                            viewModel.update()
                        })
                    val weekBackgroundAlpha by viewModel.weekBackgroundAlpha.collectAsState()
                    BuildSeekBar(
                        title = "背景色透明度",
                        value = weekBackgroundAlpha * 100,
                        start = 30F,
                        end = 100F,
                        listener = { newValue ->
                            viewModel.weekBackgroundAlpha.value = newValue / 100F
                            viewModel.update()
                        })
                    val weekItemCorner by viewModel.weekItemCorner.collectAsState()
                    BuildSeekBar(
                        title = "圆角大小",
                        value = weekItemCorner,
                        start = 0F,
                        end = 50F,
                        listener = { newValue ->
                            viewModel.weekItemCorner.value = newValue
                            viewModel.update()
                        })
                    val weekTitleTextSize by viewModel.weekTitleTextSize.collectAsState()
                    BuildSeekBar(
                        title = "文字大小",
                        value = weekTitleTextSize,
                        start = 5F,
                        end = 36F,
                        listener = { newValue ->
                            viewModel.weekTitleTextSize.value = newValue
                            viewModel.update()
                        })
                    val backgroundImageBlur by viewModel.backgroundImageBlur.collectAsState()
                    BuildSeekBar(
                        enabled = isBlurEnable(),
                        title = "动态模糊半径",
                        value = backgroundImageBlur,
                        start = 0F,
                        end = 30F,
                        listener = { newValue ->
                            viewModel.backgroundImageBlur.value = newValue
                            viewModel.update()
                        })
                    if (platform() == Platform.ANDROID) {
                        XhuSettingsMenuLink(
                            title = { Text(text = "动态模糊说明") },
                            subtitle = { Text(text = "仅 Android 12+ 可使用") },
                        )
                    }
                    XhuSettingsMenuLink(
                        title = { Text(text = "格子文本模板") },
                        subtitle = {
                            Text(text = "本周课程的文本格式")
                        },
                        onClick = {
                            showCustomTitleTemplateDialog.show()
                        }
                    )
                    XhuSettingsMenuLink(
                        title = { Text(text = "非本周格子文本模板") },
                        subtitle = {
                            Text(text = "非本周课程的文本格式")
                        },
                        onClick = {
                            showCustomNotTitleTemplateDialog.show()
                        }
                    )
                    XhuSettingsMenuLink(title = { Text(text = "点击右上角的保存按钮才会生效") })
                }
            }
        }
        BuildTitleTemplateDialog(
            value = weekTitleTemplate,
            resetValue = CustomUi.DEFAULT.weekTitleTemplate,
            useCaseState = showCustomTitleTemplateDialog,
            listener = { newValue ->
                viewModel.weekTitleTemplate.value = newValue
                viewModel.update()
            })
        BuildTitleTemplateDialog(
            value = weekNotTitleTemplate,
            resetValue = CustomUi.DEFAULT.weekNotTitleTemplate,
            useCaseState = showCustomNotTitleTemplateDialog,
            listener = { newValue ->
                viewModel.weekNotTitleTemplate.value = newValue
                viewModel.update()
            })
    }
}

@Composable
private fun BuildTitleTemplateDialog(
    value: String,
    resetValue: String,
    useCaseState: UseCaseState,
    listener: (String) -> Unit,
) {
    val valueState = remember { mutableStateOf(value) }

    val inputOptions = listOf(
        InputCustomView(view = {
            Column(modifier = Modifier.padding(4.dp)) {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = valueState.value,
                    onValueChange = {
                        valueState.value = it
                    })
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(
                        text = "课程名称",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                valueState.value += "{${TitleTemplate.COURSE_NAME.tpl}}"
                            })
                    Text(
                        text = "上课地点",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                valueState.value += "{${TitleTemplate.LOCATION.tpl}}"
                            })
                    Text(
                        text = "教师名称",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(4.dp)
                            .clickable {
                                valueState.value += "{${TitleTemplate.TEACHER_NAME.tpl}}"
                            })
                }
            }
        }),
    )
    InputDialog(
        header = xhuHeader(title = "请输入模板内容"),
        state = useCaseState,
        selection = InputSelection(
            input = inputOptions,
            onPositiveClick = {
                listener(valueState.value)
            },
            extraButton = SelectionButton(text = "重置"),
            onExtraButtonClick = {
                valueState.value = resetValue
                listener(resetValue)
            },
        )
    )
}

@Composable
private fun BuildSeekBar(
    enabled: Boolean = true,
    title: String,
    value: Float,
    start: Float,
    end: Float,
    listener: (Float) -> Unit,
) {
    SliderPreference(
        value = 1F,
        onValueChange = {},
        sliderValue = value,
        onSliderValueChange = {
            listener(it)
        },
        title = { Text(text = title) },
        valueRange = start..end,
        valueSteps = (end - start + 1).roundToInt(),
        enabled = enabled,
        icon = {},
    )
}

expect fun isBlurEnable(): Boolean