package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.model.CustomCourse
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.CustomCourseViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
class CustomCourseActivity : BaseComposeActivity() {
    private val viewModel: CustomCourseViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BuildContent() {
        val customCourseListState by viewModel.customCourseListState.collectAsState()

        val showSelect = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
        val scaffoldState: BackdropScaffoldState =
            rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
        val scope = rememberCoroutineScope()

        val userDialog = remember { mutableStateOf(false) }
        val yearDialog = remember { mutableStateOf(false) }
        val termDialog = remember { mutableStateOf(false) }
        val courseIndex1Dialog = remember { mutableStateOf(false) }
        val courseIndex2Dialog = remember { mutableStateOf(false) }
        val weekDialog = remember { mutableStateOf(false) }

        var customCourse by remember { mutableStateOf(CustomCourse.EMPTY) }
        val courseIndex = remember { mutableStateOf(customCourse.courseIndex) }
        val day = remember { mutableStateOf(customCourse.day) }

        fun onBack() {
            if (scaffoldState.isRevealed) {
                scope.launch {
                    scaffoldState.conceal()
                }
                return
            }
            if (showSelect.isVisible) {
                scope.launch {
                    showSelect.hide()
                }
                return
            }
            finish()
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
                        IconButton(onClick = {
                            scope.launch {
//                                if (!showSelect.isVisible) {
//                                    showOption = !showOption
//                                }
                            }
                        }) {
                            Icon(
                                painter = XhuIcons.Action.view,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }
                )
            }, backLayerContent = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                            modifier = Modifier.weight(1F),
                            onClick = {
                                userDialog.value = true
                            }) {
                            val userSelect by viewModel.userSelect.collectAsState()
                            val selected = userSelect.firstOrNull { it.selected }
                            val userString =
                                selected?.let { "${it.userName}(${it.studentId})" } ?: "查询中"
                            Text(text = "查询用户：$userString")
                        }
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                            onClick = {
                                viewModel.loadCustomCourseList()
                                scope.launch {
                                    scaffoldState.conceal()
                                }
                            }) {
                            Icon(painter = XhuIcons.CustomCourse.pull, contentDescription = null)
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                            modifier = Modifier.weight(1F),
                            onClick = {
                                yearDialog.value = true
                            }) {
                            val yearSelect by viewModel.yearSelect.collectAsState()
                            val yearString =
                                yearSelect.firstOrNull { it.selected }?.let { "${it.year}学年" }
                                    ?: "查询中"
                            Text(text = yearString)
                        }
                        OutlinedButton(
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                            modifier = Modifier.weight(1F),
                            onClick = {
                                termDialog.value = true
                            }) {
                            val termSelect by viewModel.termSelect.collectAsState()
                            val termString =
                                termSelect.firstOrNull { it.selected }?.let { "第${it.term}学期" }
                                    ?: "查询中"
                            Text(text = termString)
                        }
                    }
                }
            }, frontLayerContent = {
                ModalBottomSheetLayout(
                    sheetState = showSelect,
                    scrimColor = Color.Black.copy(alpha = 0.32f),
                    sheetContent = {
                        var courseName by remember { mutableStateOf(customCourse.courseName) }
                        var teacherName by remember { mutableStateOf(customCourse.teacherName) }
                        var weekList by remember { mutableStateOf(customCourse.week) }
                        var location by remember { mutableStateOf(customCourse.location) }
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier
                                        .padding(12.dp)
                                        .clickable {
                                            scope.launch {
                                                showSelect.hide()
                                            }
                                        },
                                    painter = XhuIcons.CustomCourse.close,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.weight(1F))
                                TextButton(
                                    onClick = {
                                        scope.launch {
                                            showSelect.hide()
                                        }
                                    }) {
                                    Text(text = "保存")
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier.padding(12.dp),
                                    painter = XhuIcons.CustomCourse.title,
                                    contentDescription = null
                                )
                                TextField(
                                    modifier = Modifier.weight(1F),
                                    value = courseName,
                                    placeholder = {
                                        Text(text = "课程名称（必填）")
                                    },
                                    onValueChange = { courseName = it })
                            }
                            Row(
                                modifier = Modifier
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier.padding(12.dp),
                                    painter = XhuIcons.CustomCourse.teacher,
                                    contentDescription = null
                                )
                                TextField(
                                    modifier = Modifier.weight(1F),
                                    value = teacherName,
                                    placeholder = {
                                        Text(text = "任课教师（选填）")
                                    },
                                    onValueChange = { teacherName = it })
                            }
                            Row(
                                modifier = Modifier
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier.padding(12.dp),
                                    painter = XhuIcons.CustomCourse.week,
                                    contentDescription = null
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(text = "上课周数（不能为空）${weekList.formatWeekString()}")
                                    LazyRow(content = {
                                        items(20) { index ->
                                            Box(
                                                modifier = Modifier.padding(1.dp),
                                                contentAlignment = Alignment.Center,
                                            ) {
                                                val item = index + 1
                                                val inList = item in weekList
                                                val color =
                                                    if (inList) MaterialTheme.colors.primary else XhuColor.customCourseWeekColorBackground
                                                val textColor =
                                                    if (inList) MaterialTheme.colors.onPrimary else XhuColor.Common.blackText
                                                Surface(
                                                    shape = CircleShape,
                                                    modifier = Modifier
                                                        .padding(horizontal = 6.dp)
                                                        .size(36.dp)
                                                        .clickable(
                                                            onClick = {
                                                                weekList = if (inList) {
                                                                    val newList =
                                                                        weekList.toMutableList()
                                                                    newList.remove(item)
                                                                    newList
                                                                } else {
                                                                    val newList =
                                                                        weekList.toMutableList()
                                                                    newList.add(item)
                                                                    newList
                                                                }
                                                            },
                                                            indication = null,
                                                            interactionSource = MutableInteractionSource(),
                                                        ),
                                                    color = color
                                                ) {}
                                                Text(text = "$item", color = textColor)
                                            }
                                        }
                                    })
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier.padding(12.dp),
                                    painter = XhuIcons.CustomCourse.location,
                                    contentDescription = null
                                )
                                TextField(
                                    modifier = Modifier.weight(1F),
                                    value = location,
                                    placeholder = {
                                        Text(text = "上课地点（选填）")
                                    },
                                    onValueChange = { location = it })
                            }
                            Row(
                                modifier = Modifier
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Image(
                                    modifier = Modifier.padding(12.dp),
                                    painter = XhuIcons.CustomCourse.location,
                                    contentDescription = null
                                )
                                Text(
                                    modifier = Modifier
                                        .weight(1F)
                                        .clickable(
                                            onClick = {
                                                courseIndex1Dialog.value = true
                                                courseIndex2Dialog.value = true
                                            },
                                            indication = null,
                                            interactionSource = MutableInteractionSource(),
                                        ),
                                    text = "第 ${courseIndex.value[0]}-${courseIndex.value[1]} 节",
                                )
                                Text(
                                    modifier = Modifier
                                        .weight(1F)
                                        .clickable(
                                            onClick = {
                                                weekDialog.value = true
                                            },
                                            indication = null,
                                            interactionSource = MutableInteractionSource(),
                                        ),
                                    text = DayOfWeek.of(day.value)
                                        .getDisplayName(TextStyle.SHORT, Locale.CHINA),
                                )
                            }
                            Spacer(modifier = Modifier.weight(1F))
                        }
                    }) {
                    Box {
                        SwipeRefresh(
                            modifier = Modifier.fillMaxSize(),
                            state = rememberSwipeRefreshState(customCourseListState.loading),
                            onRefresh = { },
                            swipeEnabled = false,
                        ) {
                            val list = customCourseListState.customCourseList
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(XhuColor.Common.grayBackground),
                                contentPadding = PaddingValues(4.dp),
                            ) {
                                if (customCourseListState.loading) {
                                    items(3) {
                                        BuildItem(
                                            CustomCourse.PLACEHOLDER,
                                            true,
                                        )
                                    }
                                } else {
                                    items(list.size) { index ->
                                        val item = list[index]
                                        BuildItem(item)
                                    }
                                }
                            }
                            if (!customCourseListState.loading && list.isEmpty()) {
                                BuildNoDataLayout()
                            }
                        }
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp),
                            onClick = {
                                scope.launch {
                                    showSelect.animateTo(targetValue = ModalBottomSheetValue.Expanded)
                                }
                            }) {
                            Icon(XhuIcons.CustomCourse.add, contentDescription = null)
                        }
                    }
                }
            })
        ShowUserDialog(show = userDialog)
        ShowYearDialog(show = yearDialog)
        ShowTermDialog(show = termDialog)
        ShowCourseIndexDialog(
            courseIndex = courseIndex,
            first = courseIndex1Dialog,
            second = courseIndex2Dialog
        )
        ShowWeekDialog(week = day, show = weekDialog)
        if (customCourseListState.errorMessage.isNotBlank()) {
            customCourseListState.errorMessage.toast(true)
        }
    }

    @Composable
    private fun ShowUserDialog(
        show: MutableState<Boolean>,
    ) {
        val userSelect by viewModel.userSelect.collectAsState()
        val selectedUser = userSelect.firstOrNull { it.selected } ?: return
        if (show.value) {
            var selected by remember { mutableStateOf(selectedUser) }
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = "请选择要查询的学生")
                },
                text = {
                    LazyColumn {
                        items(userSelect.size) { index ->
                            val item = userSelect[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        selected = item
                                    },
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected == item, onClick = null)
                                Text(text = "${item.userName}(${item.studentId})")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.selectUser(selected.studentId)
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
    private fun ShowYearDialog(
        show: MutableState<Boolean>,
    ) {
        val yearSelect by viewModel.yearSelect.collectAsState()
        val selectedYear = yearSelect.firstOrNull { it.selected } ?: return
        if (show.value) {
            var selected by remember { mutableStateOf(selectedYear) }
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = "请选择要查询的学生")
                },
                text = {
                    Column {
                        LazyColumn {
                            items(yearSelect.size) { index ->
                                val item = yearSelect[index]
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() },
                                        ) {
                                            selected = item
                                        },
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    RadioButton(selected = selected == item, onClick = null)
                                    Text(text = item.year)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.selectYear(selected.year)
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
    private fun ShowTermDialog(
        show: MutableState<Boolean>,
    ) {
        val termSelect by viewModel.termSelect.collectAsState()
        val selectedTerm = termSelect.firstOrNull { it.selected } ?: return
        if (show.value) {
            var selected by remember { mutableStateOf(selectedTerm) }
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = "请选择要查询的学期")
                },
                text = {
                    LazyColumn {
                        items(termSelect.size) { index ->
                            val item = termSelect[index]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        selected = item
                                    },
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected == item, onClick = null)
                                Text(text = "${item.term}")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.selectTerm(selected.term)
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
    private fun ShowCourseIndexDialog(
        courseIndex: MutableState<List<Int>>,
        first: MutableState<Boolean>,
        second: MutableState<Boolean>,
    ) {
        var saveData by remember { mutableStateOf(courseIndex.value) }
        if (first.value) {
            var selected by remember { mutableStateOf(1) }
            AlertDialog(
                onDismissRequest = {
                    first.value = false
                },
                title = {
                    Text(text = "请选择开始上课的节次")
                },
                text = {
                    LazyColumn {
                        items(11) { index ->
                            val item = index + 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        selected = item
                                    },
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected == item, onClick = null)
                                Text(text = "第 $item 节")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            saveData = listOf(selected, courseIndex.value[1])
                            first.value = false
                        },
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            saveData = courseIndex.value
                            first.value = false
                            second.value = false
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        } else if (second.value) {
            var selected by remember { mutableStateOf(saveData[0]) }
            AlertDialog(
                onDismissRequest = {
                    second.value = false
                },
                title = {
                    Text(text = "请选择结束上课的节次")
                },
                text = {
                    LazyColumn {
                        items(12 - saveData[0]) { index ->
                            val item = index + saveData[0]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        selected = item
                                    },
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected == item, onClick = null)
                                Text(text = "第 $item 节")
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            saveData = listOf(saveData[0], selected)
                            second.value = false
                        },
                    ) {
                        Text("确认")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            saveData = courseIndex.value
                            first.value = false
                            second.value = false
                        }
                    ) {
                        Text("取消")
                    }
                }
            )
        } else {
            courseIndex.value = saveData
        }
    }

    @Composable
    private fun ShowWeekDialog(
        week: MutableState<Int>,
        show: MutableState<Boolean>,
    ) {
        val array = arrayOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
        val selectedWeek = week.value
        if (show.value) {
            var selected by remember { mutableStateOf(selectedWeek) }
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = "请选择上课星期")
                },
                text = {
                    LazyColumn {
                        items(array.size) { index ->
                            val item = index + 1
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        selected = item
                                    },
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(selected = selected == item, onClick = null)
                                Text(text = array[index])
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            week.value = selected
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
}

@Composable
private fun BuildItem(
    item: CustomCourse,
    placeHolder: Boolean = false,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .placeholder(
                visible = placeHolder,
                highlight = PlaceholderHighlight.shimmer(),
            ),
        backgroundColor = XhuColor.cardBackground,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(modifier = Modifier.fillMaxWidth(), text = item.courseName)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.teacher, contentDescription = null)
                Text(text = item.teacherName)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.week, contentDescription = null)
                Text(text = "${item.weekString} ${item.day}")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.location, contentDescription = null)
                Text(text = item.location)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.time, contentDescription = null)
                Text(text = item.courseIndex.joinToString())
            }
        }
    }
}