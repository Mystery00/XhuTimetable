package vip.mystery0.xhu.timetable.ui.activity

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.model.CustomCourse
import vip.mystery0.xhu.timetable.model.event.EventType
import vip.mystery0.xhu.timetable.model.event.UIEvent
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.CustomCourseViewModel
import vip.mystery0.xhu.timetable.viewmodel.SearchCourse
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
class CustomCourseActivity : BaseComposeActivity() {
    private val viewModel: CustomCourseViewModel by viewModels()

    companion object {
        private const val INTENT_HIDE_SELECTOR = "hideSelector"
        fun hideSelector(): Intent.() -> Unit {
            return {
                putExtra(INTENT_HIDE_SELECTOR, true)
            }
        }
    }

    @Composable
    override fun BuildContent() {
        val customCourseListState by viewModel.customCourseListState.collectAsState()
        val saveCustomCourseState by viewModel.saveCustomCourseState.collectAsState()

        val showSelect = rememberModalBottomSheetState(
            initialValue = ModalBottomSheetValue.Hidden,
            confirmStateChange = {
                !customCourseListState.loading && !saveCustomCourseState.loading
            })
        val initBackdropValue = if (intent.getBooleanExtra(INTENT_HIDE_SELECTOR, false)) {
            BackdropValue.Concealed
        }else{
            BackdropValue.Revealed
        }
        val scaffoldState: BackdropScaffoldState =
            rememberBackdropScaffoldState(
                initialValue = initBackdropValue,
                confirmStateChange = {
                    !showSelect.isVisible && !customCourseListState.loading && !saveCustomCourseState.loading
                })
        val scope = rememberCoroutineScope()

        var createType by remember { mutableStateOf(CreateType.INPUT) }

        val userDialog = remember { mutableStateOf(false) }
        val yearDialog = remember { mutableStateOf(false) }
        val termDialog = remember { mutableStateOf(false) }
        val courseIndex1Dialog = remember { mutableStateOf(false) }
        val courseIndex2Dialog = remember { mutableStateOf(false) }
        val weekDialog = remember { mutableStateOf(false) }

        var customCourse by remember { mutableStateOf(CustomCourse.EMPTY) }
        var courseName by remember { mutableStateOf(customCourse.courseName) }
        var teacherName by remember { mutableStateOf(customCourse.teacherName) }
        var weekList by remember { mutableStateOf(customCourse.week) }
        var location by remember { mutableStateOf(customCourse.location) }
        val courseIndex = remember { mutableStateOf(customCourse.courseIndex) }
        val day = remember { mutableStateOf(customCourse.day) }

        var searchCourse by remember { mutableStateOf(SearchCourse.EMPTY) }

        fun updateCustomCourse(data: CustomCourse) {
            customCourse = data
            courseName = data.courseName
            teacherName = data.teacherName
            weekList = data.week
            location = data.location
            courseIndex.value = data.courseIndex
            day.value = data.day
        }

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
            if (viewModel.changeCustomCourse) {
                eventBus.post(UIEvent(EventType.CHANGE_SHOW_CUSTOM_COURSE))
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
                                if (showSelect.isVisible) {
                                    showSelect.hide()
                                    return@launch
                                }
                                if (scaffoldState.isRevealed) {
                                    scaffoldState.conceal()
                                } else {
                                    scaffoldState.reveal()
                                }
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
                        Button(
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
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
                                if (customCourse.courseId != 0L && !saveCustomCourseState.loading && createType == CreateType.INPUT) {
                                    TextButton(
                                        onClick = {
                                            viewModel.delete(customCourse.courseId)
                                        }) {
                                        Text(text = "删除", color = Color.Red)
                                    }
                                }
                                if (!saveCustomCourseState.loading) {
                                    TextButton(
                                        onClick = {
                                            if (createType == CreateType.INPUT) {
                                                viewModel.saveCustomCourse(
                                                    customCourse.courseId,
                                                    courseName,
                                                    teacherName,
                                                    weekList,
                                                    location,
                                                    courseIndex.value,
                                                    day.value,
                                                )
                                            } else {
                                                viewModel.saveCustomCourse(
                                                    0L,
                                                    searchCourse.name,
                                                    searchCourse.teacher,
                                                    searchCourse.week,
                                                    searchCourse.location,
                                                    listOf(
                                                        searchCourse.time.first(),
                                                        searchCourse.time.last()
                                                    ),
                                                    searchCourse.day,
                                                )
                                            }
                                        }) {
                                        Text(text = "保存")
                                    }
                                }
                                if (saveCustomCourseState.loading) {
                                    TextButton(
                                        enabled = false,
                                        onClick = {
                                        }) {
                                        Text(text = "保存操作中...")
                                    }
                                }
                            }
                            if (customCourse.courseId == 0L) {
                                Row(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.CenterHorizontally),
                                ) {
                                    val selectedContentColor = Color.White
                                    val selectedBackgroundColor = MaterialTheme.colors.primary
                                    val contentColor = MaterialTheme.colors.primary
                                    val backgroundColor = XhuColor.Common.whiteBackground
                                    OutlinedButton(
                                        shape = RoundedCornerShape(
                                            topStart = 8.dp,
                                            bottomStart = 8.dp
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (createType == CreateType.INPUT) selectedContentColor else contentColor,
                                            backgroundColor = if (createType == CreateType.INPUT) selectedBackgroundColor else backgroundColor,
                                        ),
                                        onClick = {
                                            createType = CreateType.INPUT
                                        }) {
                                        Text(text = "手动输入")
                                    }
                                    OutlinedButton(
                                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (createType == CreateType.SELECT) selectedContentColor else contentColor,
                                            backgroundColor = if (createType == CreateType.SELECT) selectedBackgroundColor else backgroundColor,
                                        ),
                                        onClick = {
                                            createType = CreateType.SELECT
                                        }) {
                                        Text(text = "蹭课选择")
                                    }
                                }
                            } else {
                                createType = CreateType.INPUT
                            }
                            when (createType) {
                                CreateType.INPUT -> {
                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            TextField(
                                                modifier = Modifier.weight(1F),
                                                value = courseName,
                                                placeholder = {
                                                    Text(text = "（必填）")
                                                },
                                                label = {
                                                    Text(text = "课程名称")
                                                },
                                                leadingIcon = {
                                                    Image(
                                                        painter = XhuIcons.CustomCourse.title,
                                                        contentDescription = null
                                                    )
                                                },
                                                onValueChange = { courseName = it },
                                                colors = TextFieldDefaults.textFieldColors(
                                                    backgroundColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent,
                                                )
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            TextField(
                                                modifier = Modifier.weight(1F),
                                                value = teacherName,
                                                placeholder = {
                                                    Text(text = "（选填）")
                                                },
                                                label = {
                                                    Text(text = "任课教师")
                                                },
                                                leadingIcon = {
                                                    Image(
                                                        painter = XhuIcons.CustomCourse.teacher,
                                                        contentDescription = null
                                                    )
                                                },
                                                onValueChange = { teacherName = it },
                                                colors = TextFieldDefaults.textFieldColors(
                                                    backgroundColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent,
                                                )
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            TextField(
                                                modifier = Modifier.weight(1F),
                                                value = location,
                                                placeholder = {
                                                    Text(text = "（选填）")
                                                },
                                                label = {
                                                    Text(text = "上课地点")
                                                },
                                                leadingIcon = {
                                                    Image(
                                                        painter = XhuIcons.CustomCourse.location,
                                                        contentDescription = null
                                                    )
                                                },
                                                onValueChange = { location = it },
                                                colors = TextFieldDefaults.textFieldColors(
                                                    backgroundColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent,
                                                )
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Image(
                                                modifier = Modifier.padding(12.dp),
                                                painter = XhuIcons.CustomCourse.week,
                                                contentDescription = null
                                            )
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(text = "上课周数${if (weekList.isEmpty()) "（不能为空）" else ""} ${weekList.formatWeekString()}")
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
                                                                if (inList) MaterialTheme.colors.onPrimary else Color.Black
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
                                                .defaultMinSize(minHeight = 48.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Image(
                                                modifier = Modifier.padding(12.dp),
                                                painter = XhuIcons.CustomCourse.time,
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
                                    }
                                    Spacer(modifier = Modifier.weight(1F))
                                }

                                CreateType.SELECT -> {
                                    var searchCourseName by remember { mutableStateOf("") }
                                    var searchTeacherName by remember { mutableStateOf("") }
                                    val searchCourseIndex = remember { mutableStateOf(0) }
                                    val searchDay = remember { mutableStateOf(0) }

                                    val searchWeekDialog = remember { mutableStateOf(false) }
                                    val searchCourseIndexDialog = remember { mutableStateOf(false) }

                                    val searchCourseListState by viewModel.searchCourseListState.collectAsState()

                                    Column {
                                        Row(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            TextField(
                                                modifier = Modifier.weight(1F),
                                                value = searchCourseName,
                                                placeholder = {
                                                    Text(text = "请输入需要搜索的课程名称")
                                                },
                                                label = {
                                                    Text(text = "课程名称")
                                                },
                                                leadingIcon = {
                                                    Image(
                                                        painter = XhuIcons.CustomCourse.title,
                                                        contentDescription = null
                                                    )
                                                },
                                                onValueChange = { searchCourseName = it },
                                                colors = TextFieldDefaults.textFieldColors(
                                                    backgroundColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent,
                                                )
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            TextField(
                                                modifier = Modifier.weight(1F),
                                                value = searchTeacherName,
                                                placeholder = {
                                                    Text(text = "请输入需要搜索的教师名称")
                                                },
                                                label = {
                                                    Text(text = "任课教师")
                                                },
                                                leadingIcon = {
                                                    Image(
                                                        painter = XhuIcons.CustomCourse.teacher,
                                                        contentDescription = null
                                                    )
                                                },
                                                onValueChange = { searchTeacherName = it },
                                                colors = TextFieldDefaults.textFieldColors(
                                                    backgroundColor = Color.Transparent,
                                                    unfocusedIndicatorColor = Color.Transparent,
                                                )
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Image(
                                                modifier = Modifier.padding(12.dp),
                                                painter = XhuIcons.CustomCourse.time,
                                                contentDescription = null
                                            )
                                            val courseIndexText =
                                                if (searchCourseIndex.value == 0) "选择节次" else "第 ${searchCourseIndex.value} 节"
                                            Text(
                                                modifier = Modifier
                                                    .weight(1F)
                                                    .clickable(
                                                        onClick = {
                                                            searchCourseIndexDialog.value = true
                                                        },
                                                        indication = null,
                                                        interactionSource = MutableInteractionSource(),
                                                    ),
                                                text = courseIndexText,
                                            )
                                            val dayText =
                                                if (searchDay.value == 0) "选择星期" else DayOfWeek.of(
                                                    searchDay.value
                                                )
                                                    .getDisplayName(TextStyle.SHORT, Locale.CHINA)
                                            Text(
                                                modifier = Modifier
                                                    .weight(1F)
                                                    .clickable(
                                                        onClick = {
                                                            searchWeekDialog.value = true
                                                        },
                                                        indication = null,
                                                        interactionSource = MutableInteractionSource(),
                                                    ),
                                                text = dayText,
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .defaultMinSize(minHeight = 48.dp)
                                                .padding(horizontal = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Text(
                                                modifier = Modifier.weight(1F),
                                                text = "从下面的列表中选择需要添加的课程\n当前结果数量：${searchCourseListState.searchCourseList.size}",
                                            )
                                            OutlinedButton(
                                                enabled = !searchCourseListState.loading,
                                                onClick = {
                                                    searchCourse = SearchCourse.EMPTY
                                                    val selectedCourseIndex =
                                                        if (searchCourseIndex.value == 0) null else searchCourseIndex.value
                                                    val selectedDay =
                                                        if (searchDay.value == 0) null else searchDay.value
                                                    viewModel.loadSearchCourseList(
                                                        searchCourseName,
                                                        searchTeacherName,
                                                        selectedCourseIndex,
                                                        selectedDay,
                                                    )
                                                }) {
                                                Text(text = "查询")
                                            }
                                        }
                                    }
                                    Divider(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 8.dp, bottom = 4.dp),
                                        color = XhuColor.Common.divider,
                                    )
                                    SwipeRefresh(
                                        modifier = Modifier
                                            .weight(1F)
                                            .defaultMinSize(minHeight = 180.dp)
                                            .fillMaxWidth()
                                            .background(XhuColor.Common.grayBackground),
                                        state = rememberSwipeRefreshState(searchCourseListState.loading),
                                        onRefresh = {
                                        },
                                        swipeEnabled = false,
                                    ) {
                                        LazyColumn(
                                            contentPadding = PaddingValues(4.dp),
                                        ) {
                                            if (searchCourseListState.loading) {
                                                items(3) {
                                                    BuildSearchResultItem(
                                                        item = SearchCourse.PLACEHOLDER,
                                                        placeHolder = true,
                                                        checked = false,
                                                    ) {}
                                                }
                                            } else {
                                                val list = searchCourseListState.searchCourseList
                                                items(list.size) { index ->
                                                    val item = list[index]
                                                    BuildSearchResultItem(
                                                        item,
                                                        checked = item == searchCourse
                                                    ) {
                                                        searchCourse = item
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    ShowWeekDialog(week = searchDay, show = searchWeekDialog)
                                    ShowSearchCourseIndexDialog(
                                        courseIndex = searchCourseIndex,
                                        show = searchCourseIndexDialog
                                    )
                                }
                            }
                        }
                    }) {
                    Box {
                        SwipeRefresh(
                            modifier = Modifier.fillMaxSize(),
                            state = rememberSwipeRefreshState(customCourseListState.loading),
                            onRefresh = {
                            },
                            swipeEnabled = false,
                        ) {
                            val list = customCourseListState.customCourseList
                            if (customCourseListState.loading || list.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(XhuColor.Common.grayBackground),
                                    contentPadding = PaddingValues(4.dp),
                                ) {
                                    if (customCourseListState.loading) {
                                        scope.launch {
                                            showSelect.hide()
                                        }
                                        items(3) {
                                            BuildItem(
                                                CustomCourse.PLACEHOLDER,
                                                true,
                                            ) {}
                                        }
                                    } else {
                                        items(list.size) { index ->
                                            val item = list[index]
                                            BuildItem(item) {
                                                updateCustomCourse(item)
                                                scope.launch {
                                                    showSelect.animateTo(targetValue = ModalBottomSheetValue.Expanded)
                                                }
                                            }
                                        }
                                    }
                                }
                            } else {
                                BuildNoDataLayout()
                            }
                        }
                        FloatingActionButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp),
                            onClick = {
                                if (!customCourseListState.loading) {
                                    updateCustomCourse(CustomCourse.EMPTY)
                                    scope.launch {
                                        showSelect.animateTo(targetValue = ModalBottomSheetValue.Expanded)
                                    }
                                }
                            }) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                tint = Color.White
                            )
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
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
        }
        val init by viewModel.init.collectAsState()
        if (init) {
            LaunchedEffect(key1 = "init", block = {
                viewModel.loadCustomCourseList()
            })
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
    private fun ShowSearchCourseIndexDialog(
        courseIndex: MutableState<Int>,
        show: MutableState<Boolean>,
    ) {
        if (!show.value) {
            return
        }
        var selected by remember { mutableStateOf(courseIndex.value) }
        AlertDialog(
            onDismissRequest = {
                show.value = false
            },
            title = {
                Text(text = "请选择需要搜索的节次")
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
                        courseIndex.value = selected
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
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .placeholder(
                visible = placeHolder,
                highlight = PlaceholderHighlight.shimmer(),
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = MutableInteractionSource(),
            ),
        backgroundColor = XhuColor.cardBackground,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.courseName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (item.teacherName.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CustomCourse.teacher, contentDescription = null)
                    Text(text = "教师名称：${item.teacherName}")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.week, contentDescription = null)
                Text(
                    text = "上课星期：第${item.weekString} 每周${
                        DayOfWeek.of(item.day).getDisplayName(TextStyle.SHORT, Locale.CHINA)
                    }"
                )
            }
            if (item.location.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CustomCourse.location, contentDescription = null)
                    Text(text = "上课地点：${item.location}")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.time, contentDescription = null)
                var courseIndex = item.courseIndex
                if (courseIndex.size == 1) {
                    courseIndex = listOf(courseIndex[0], courseIndex[0])
                }
                Text(text = "上课时间：第 ${courseIndex[0]}-${courseIndex[1]} 节")
            }
        }
    }
}

@Composable
private fun BuildSearchResultItem(
    item: SearchCourse,
    placeHolder: Boolean = false,
    checked: Boolean,
    onClick: () -> Unit,
) {
    val border =
        if (checked) BorderStroke(width = 1.dp, color = MaterialTheme.colors.primary) else null
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .placeholder(
                visible = placeHolder,
                highlight = PlaceholderHighlight.shimmer(),
            )
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = MutableInteractionSource(),
            ),
        backgroundColor = XhuColor.cardBackground,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.name,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (item.teacher.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CustomCourse.teacher, contentDescription = null)
                    Text(text = "教师名称：${item.teacher}")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.week, contentDescription = null)
                Text(
                    text = "上课星期：第${item.weekString} 每周${
                        DayOfWeek.of(item.day).getDisplayName(TextStyle.SHORT, Locale.CHINA)
                    }"
                )
            }
            if (item.location.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CustomCourse.location, contentDescription = null)
                    Text(text = "上课地点：${item.location}")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Image(painter = XhuIcons.CustomCourse.time, contentDescription = null)
                val courseIndex: String = if (item.time.size == 1) {
                    "${item.time[0]}"
                } else {
                    "${item.time.first()}-${item.time.last()}"
                }
                Text(text = "上课时间：第 $courseIndex 节")
            }
        }
    }
}

enum class CreateType {
    INPUT,
    SELECT,
}