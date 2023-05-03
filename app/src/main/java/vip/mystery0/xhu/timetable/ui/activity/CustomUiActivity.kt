package vip.mystery0.xhu.timetable.ui.activity

import android.os.Build
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Scaffold
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.alorma.compose.settings.ui.SettingsMenuLink
import com.google.accompanist.flowlayout.FlowRow
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.model.TitleTemplate
import vip.mystery0.xhu.timetable.model.format
import vip.mystery0.xhu.timetable.ui.preference.XhuSettingsGroup
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.viewmodel.CustomUiViewModel
import kotlin.math.roundToInt
import kotlin.random.Random

class CustomUiActivity : BaseComposeActivity() {
    private val viewModel: CustomUiViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val randomCourse by viewModel.randomCourse.collectAsState()
        val scope = rememberCoroutineScope()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
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
                                viewModel.reset()
                            }
                        }) {
                            Icon(
                                painter = XhuIcons.reset,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        IconButton(onClick = {
                            viewModel.save()
                            "设置已保存".toast()
                        }) {
                            Icon(
                                painter = XhuIcons.Action.done,
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

            val showCustomTitleTemplateDialog = remember { mutableStateOf(false) }
            val showCustomNotTitleTemplateDialog = remember { mutableStateOf(false) }
            Column(modifier = Modifier.padding(paddingValues)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
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
                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                        ) {
                            Spacer(modifier = Modifier.weight(2.15F))
                            BoxWithConstraints(modifier = Modifier.weight(3F)) {
                                Row {
                                    //列数
                                    val columnSize = 3
                                    //每一列数量
                                    val size = randomCourse.size / columnSize
                                    for (columnIndex in 0 until columnSize) {
                                        val start = columnIndex * size
                                        val end = (columnIndex + 1) * size
                                        val list = randomCourse.subList(start, end)
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
                            Spacer(modifier = Modifier.weight(2.15F))
                        }
                    }
                }
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    XhuSettingsGroup(title = {
                        Text(text = "操作")
                    }) {
                        SettingsMenuLink(
                            title = { Text(text = "刷新课程列表") },
                            onClick = {
                                viewModel.refreshRandomCourse()
                            }
                        )
                    }
                    XhuSettingsGroup(title = {
                        Text(text = "参数调整")
                    }) {
                        val weekItemHeight by viewModel.weekItemHeight.collectAsState()
                        BuildSeekBar(
                            title = "格子高度",
                            value = weekItemHeight,
                            start = 50,
                            end = 150,
                            listener = { newValue ->
                                viewModel.weekItemHeight.value = newValue
                                viewModel.update()
                            })
                        val weekBackgroundAlpha by viewModel.weekBackgroundAlpha.collectAsState()
                        BuildSeekBar(
                            title = "背景色透明度",
                            value = (weekBackgroundAlpha * 100).toInt(),
                            start = 30,
                            end = 100,
                            listener = { newValue ->
                                viewModel.weekBackgroundAlpha.value = newValue / 100F
                                viewModel.update()
                            })
                        val weekItemCorner by viewModel.weekItemCorner.collectAsState()
                        BuildSeekBar(
                            title = "圆角大小",
                            value = weekItemCorner,
                            start = 0,
                            end = 50,
                            listener = { newValue ->
                                viewModel.weekItemCorner.value = newValue
                                viewModel.update()
                            })
                        val weekTitleTextSize by viewModel.weekTitleTextSize.collectAsState()
                        BuildSeekBar(
                            title = "文字大小",
                            value = weekTitleTextSize,
                            start = 5,
                            end = 36,
                            listener = { newValue ->
                                viewModel.weekTitleTextSize.value = newValue
                                viewModel.update()
                            })
                        val backgroundImageBlur by viewModel.backgroundImageBlur.collectAsState()
                        BuildSeekBar(
                            enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S,
                            title = "动态模糊半径",
                            value = backgroundImageBlur,
                            start = 0,
                            end = 30,
                            listener = { newValue ->
                                viewModel.backgroundImageBlur.value = newValue
                                viewModel.update()
                            })
                        SettingsMenuLink(
                            title = { Text(text = "动态模糊说明") },
                            subtitle = {
                                Text(text = "仅 Android 12+ 可使用")
                            },
                            onClick = {
                            }
                        )
                        SettingsMenuLink(
                            title = { Text(text = "格子文本模板") },
                            subtitle = {
                                Text(text = "本周课程的文本格式")
                            },
                            onClick = {
                                showCustomTitleTemplateDialog.value = true
                            }
                        )
                        SettingsMenuLink(
                            title = { Text(text = "非本周格子文本模板") },
                            subtitle = {
                                Text(text = "非本周课程的文本格式")
                            },
                            onClick = {
                                showCustomNotTitleTemplateDialog.value = true
                            }
                        )
                        SettingsMenuLink(
                            title = { Text(text = "点击右上角的保存按钮才会生效") },
                            onClick = {
                            }
                        )
                    }
                }
            }
            BuildTitleTemplateDialog(
                value = weekTitleTemplate,
                show = showCustomTitleTemplateDialog,
                listener = { newValue ->
                    viewModel.weekTitleTemplate.value = newValue
                    viewModel.update()
                })
            BuildTitleTemplateDialog(
                value = weekNotTitleTemplate,
                show = showCustomNotTitleTemplateDialog,
                listener = { newValue ->
                    viewModel.weekNotTitleTemplate.value = newValue
                    viewModel.update()
                })
        }
    }

    @Composable
    private fun BuildTitleTemplateDialog(
        value: String,
        show: MutableState<Boolean>,
        listener: (String) -> Unit,
    ) {
        if (!show.value) {
            return
        }
        val valueState = remember { mutableStateOf(value) }
        AlertDialog(
            onDismissRequest = {
                show.value = false
            },
            title = {
                Text(text = "请输入模板内容")
            },
            text = {
                Column {
                    OutlinedTextField(value = valueState.value, onValueChange = {
                        valueState.value = it
                    })
                    FlowRow {
                        Text(text = "课程名称",
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    valueState.value += "{${TitleTemplate.COURSE_NAME.tpl}}"
                                })
                        Text(text = "上课地点",
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    valueState.value += "{${TitleTemplate.LOCATION.tpl}}"
                                })
                        Text(text = "教师名称",
                            color = MaterialTheme.colors.primary,
                            modifier = Modifier
                                .padding(4.dp)
                                .clickable {
                                    valueState.value += "{${TitleTemplate.TEACHER_NAME.tpl}}"
                                })
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        listener(valueState.value)
                        show.value = false
                    },
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        show.value = false
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun BuildSeekBar(
    enabled: Boolean = true,
    title: String,
    value: Int,
    start: Int,
    end: Int,
    listener: (Int) -> Unit,
) {
    val range = end - start
    val currentValue: Float = (value - start) / range.toFloat()
    val valueState = remember { mutableStateOf(currentValue) }
    valueState.value = currentValue
    SettingsMenuLink(
        title = { Text(text = title) },
        subtitle = {
            Slider(
                enabled = enabled,
                value = valueState.value,
                onValueChange = {
                    valueState.value = it
                },
                modifier = Modifier.fillMaxWidth(),
                steps = end - start + 1,
                onValueChangeFinished = {
                    val newValue = (start + range * valueState.value).roundToInt()
                    listener(newValue)
                },
            )
        },
        onClick = {}
    )
}