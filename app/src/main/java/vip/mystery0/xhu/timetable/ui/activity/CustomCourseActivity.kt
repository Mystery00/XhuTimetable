package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BackdropScaffold
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.response.AllCourseResponse
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.ui.component.CourseIndexSelector
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatChinaDateTime
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.CustomCourseViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

class CustomCourseActivity : BaseSelectComposeActivity() {
    private val viewModel: CustomCourseViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)
        val userSelectStatus = viewModel.userSelect.collectAsState()
        val yearSelectStatus = viewModel.yearSelect.collectAsState()
        val termSelectStatus = viewModel.termSelect.collectAsState()

        val saveLoadingState by viewModel.saveLoadingState.collectAsState()

        val userDialog = remember { mutableStateOf(false) }
        val yearDialog = remember { mutableStateOf(false) }
        val termDialog = remember { mutableStateOf(false) }

        val showSelect = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
        val scaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Concealed)
        val scope = rememberCoroutineScope()

        var createType by remember { mutableStateOf(CreateType.INPUT) }

        val weekDialog = remember { mutableStateOf(false) }

        var customCourse by remember { mutableStateOf(CustomCourseResponse.init()) }
        var courseName by remember { mutableStateOf(customCourse.courseName) }
        var weekList by remember { mutableStateOf(customCourse.weekList) }
        val day = remember { mutableStateOf(customCourse.day) }
        val startDayTime = remember { mutableIntStateOf(customCourse.startDayTime) }
        val endDayTime = remember { mutableIntStateOf(customCourse.endDayTime) }
        var location by remember { mutableStateOf(customCourse.location) }
        var teacher by remember { mutableStateOf(customCourse.teacher) }

        var searchCourse by remember { mutableStateOf<AllCourseResponse?>(null) }

        suspend fun updateCustomCourse(data: CustomCourseResponse) {
            withContext(Dispatchers.Default) {
                customCourse = data
                courseName = data.courseName
                weekList = data.weekList
                day.value = data.day
                startDayTime.intValue = data.startDayTime
                endDayTime.intValue = data.endDayTime
                location = data.location
                teacher = data.teacher
            }
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
            viewModel.updateChange()
            finish()
        }

        BackHandler {
            onBack()
        }

        val focusManager = LocalFocusManager.current
        if (!saveLoadingState.loading) {
            LaunchedEffect(key1 = "autoHideSheet", block = {
                scope.launch {
                    showSelect.hide()
                    focusManager.clearFocus()
                }
            })
        }

        BackdropScaffold(
            modifier = Modifier,
            scaffoldState = scaffoldState,
            appBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
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
                BuildSelectBackLayerContent(
                    scaffoldState = scaffoldState,
                    selectUserState = userSelectStatus,
                    selectYearState = yearSelectStatus,
                    selectTermState = termSelectStatus,
                    showUserDialog = userDialog,
                    showYearDialog = yearDialog,
                    showTermDialog = termDialog,
                    onDataLoad = {
                        viewModel.loadCustomCourseList()
                    }
                )
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
                                            focusManager.clearFocus()
                                            scope.launch {
                                                showSelect.hide()
                                            }
                                        },
                                    painter = XhuIcons.CustomCourse.close,
                                    contentDescription = null,
                                )
                                Spacer(modifier = Modifier.weight(1F))
                                if (customCourse.courseId != 0L && !saveLoadingState.loading && createType == CreateType.INPUT) {
                                    TextButton(
                                        onClick = {
                                            viewModel.deleteCustomCourse(customCourse.courseId)
                                        }) {
                                        Text(text = "删除", color = Color.Red)
                                    }
                                }
                                if (!saveLoadingState.loading) {
                                    TextButton(
                                        onClick = {
                                            if (createType == CreateType.INPUT) {
                                                viewModel.saveCustomCourse(
                                                    customCourse.courseId,
                                                    CustomCourseRequest.buildOf(
                                                        courseName,
                                                        weekList,
                                                        day.value,
                                                        startDayTime.intValue,
                                                        endDayTime.intValue,
                                                        location,
                                                        teacher,
                                                    )
                                                )
                                            } else {
                                                searchCourse?.let {
                                                    viewModel.saveCustomCourse(
                                                        null,
                                                        CustomCourseRequest.buildOf(it)
                                                    )
                                                }
                                            }
                                        }) {
                                        Text(text = "保存")
                                    }
                                }
                                if (saveLoadingState.loading) {
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
                                    val selectedBackgroundColor = MaterialTheme.colorScheme.primary
                                    val contentColor = MaterialTheme.colorScheme.primary
                                    val backgroundColor = XhuColor.Common.whiteBackground
                                    OutlinedButton(
                                        shape = RoundedCornerShape(
                                            topStart = 8.dp,
                                            bottomStart = 8.dp
                                        ),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (createType == CreateType.INPUT) selectedContentColor else contentColor,
                                            containerColor = if (createType == CreateType.INPUT) selectedBackgroundColor else backgroundColor,
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
                                            containerColor = if (createType == CreateType.SELECT) selectedBackgroundColor else backgroundColor,
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
                                                colors = TextFieldDefaults.colors(
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
                                                value = teacher,
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
                                                onValueChange = { teacher = it },
                                                colors = TextFieldDefaults.colors(
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
                                                colors = TextFieldDefaults.colors(
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
                                                                if (inList) MaterialTheme.colorScheme.primary else XhuColor.customCourseWeekColorBackground
                                                            val textColor =
                                                                if (inList) MaterialTheme.colorScheme.onPrimary else Color.Black
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
                                            CourseIndexSelector(
                                                index = startDayTime.intValue to endDayTime.intValue,
                                                modifier = Modifier
                                                    .weight(1F)
                                                    .height(36.dp),
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
                                                text = day.value.getDisplayName(
                                                    TextStyle.SHORT,
                                                    Locale.CHINA
                                                ),
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.weight(1F))
                                }

                                CreateType.SELECT -> {
                                    val allCoursePager =
                                        viewModel.allCoursePageState.collectAndHandleState(viewModel::handleLoadState)

                                    var searchCourseName by remember { mutableStateOf("") }
                                    var searchTeacherName by remember { mutableStateOf("") }
                                    val searchCourseIndex = remember { mutableIntStateOf(0) }
                                    val searchDay = remember { mutableIntStateOf(0) }

                                    val searchWeekDialog = remember { mutableStateOf(false) }
                                    val searchCourseIndexDialog = remember { mutableStateOf(false) }

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
                                                colors = TextFieldDefaults.colors(
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
                                                colors = TextFieldDefaults.colors(
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
                                                if (searchCourseIndex.intValue == 0) "选择节次" else "第 ${searchCourseIndex.intValue} 节"
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
                                            val dayText: String =
                                                if (searchDay.intValue == 0) {
                                                    "选择星期"
                                                } else {
                                                    val dayOfWeek = DayOfWeek.of(searchDay.intValue)
                                                    dayOfWeek.getDisplayName(
                                                        TextStyle.SHORT,
                                                        Locale.CHINA
                                                    ) ?: "选择星期"
                                                }
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
                                                text = "从下面的列表中选择需要添加的课程\n当前结果数量：${allCoursePager.itemCount}",
                                            )
                                            OutlinedButton(
                                                onClick = {
                                                    searchCourse = null
                                                    val selectedCourseIndex =
                                                        if (searchCourseIndex.intValue == 0) null else searchCourseIndex.intValue
                                                    val dayOfWeek =
                                                        if (searchDay.intValue == 0) null else DayOfWeek.of(
                                                            searchDay.intValue
                                                        )
                                                    viewModel.loadSearchCourseList(
                                                        searchCourseName,
                                                        searchTeacherName,
                                                        selectedCourseIndex,
                                                        dayOfWeek,
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
                                    Box(
                                        modifier = Modifier
                                            .weight(1F)
                                            .defaultMinSize(minHeight = 180.dp)
                                            .fillMaxWidth()
                                            .background(XhuColor.Common.grayBackground),
                                    ) {
                                        LazyColumn(
                                            contentPadding = PaddingValues(4.dp),
                                        ) {
                                            itemsIndexed(allCoursePager) { item ->
                                                BuildSearchResultItem(
                                                    item,
                                                    checked = item == searchCourse
                                                ) {
                                                    searchCourse = item
                                                }
                                            }
                                            when (pager.loadState.append) {
                                                is LoadState.Loading -> {
                                                    item { BuildPageFooter(text = "数据加载中，请稍后……") }
                                                }

                                                is LoadState.Error -> {
                                                    item { BuildPageFooter(text = "数据加载失败，请重试") }
                                                }

                                                is LoadState.NotLoading -> {
                                                    item { BuildPageFooter(text = "o(´^｀)o 再怎么滑也没有啦~") }
                                                }
                                            }
                                        }
                                    }
                                    ShowWeekDialog(day = searchDay, show = searchWeekDialog)
                                    ShowSearchCourseIndexDialog(
                                        courseIndex = searchCourseIndex,
                                        show = searchCourseIndexDialog
                                    )
                                }
                            }
                        }
                    }) {
                    val lazyListState = rememberLazyListState()
                    val refreshing by viewModel.refreshing.collectAsState()
                    val isScrollingUp = lazyListState.isScrollingUp()
                    BuildPaging(
                        state = lazyListState,
                        paddingValues = PaddingValues(4.dp),
                        pager = pager,
                        refreshing = refreshing,
                        key = { index -> pager[index]?.courseId ?: index },
                        itemContent = @Composable { item ->
                            BuildItem(item) {
                                scope.launch {
                                    updateCustomCourse(item)
                                    showSelect.show()
                                }
                            }
                        }
                    ) {
                        AnimatedVisibility(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(24.dp),
                            visible = isScrollingUp,
                            enter = slideInVertically(initialOffsetY = { it }),
                            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
                        ) {
                            FloatingActionButton(
                                onClick = {
                                    scope.launch {
                                        updateCustomCourse(CustomCourseResponse.init())
                                        showSelect.show()
                                    }
                                }) {
                                Icon(
                                    imageVector = Icons.Rounded.Add,
                                    contentDescription = null,
                                    tint = XhuColor.Common.whiteText,
                                )
                            }
                        }
                    }
                }
            })
        ShowUserDialog(selectState = userSelectStatus, show = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        ShowYearDialog(selectState = yearSelectStatus, show = yearDialog, onSelect = {
            viewModel.selectYear(it.value)
        })
        ShowTermDialog(selectState = termSelectStatus, show = termDialog, onSelect = {
            viewModel.selectTerm(it.value)
        })
        ShowWeekDialog(dayOfWeek = day, show = weekDialog)
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
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
        var selected by remember { mutableIntStateOf(courseIndex.value) }
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
        dayOfWeek: MutableState<DayOfWeek>? = null,
        day: MutableState<Int>? = null,
        show: MutableState<Boolean>,
    ) {
        var selectedWeek: DayOfWeek? = null
        if (dayOfWeek != null) {
            selectedWeek = dayOfWeek.value
        } else if (day != null && day.value != 0) {
            selectedWeek = DayOfWeek.of(day.value)
        }
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
                        items(DayOfWeek.values()) { item ->
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
                                Text(text = item.getDisplayName(TextStyle.SHORT, Locale.CHINA))
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            selected?.let {
                                if (dayOfWeek != null) {
                                    dayOfWeek.value = it
                                } else if (day != null) {
                                    day.value = it.value
                                }
                            }
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
    item: CustomCourseResponse,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = MutableInteractionSource(),
            ),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
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
                    text = buildString {
                        append("上课星期：")
                        append("第")
                        append(item.weekStr)
                        append(" 每周")
                        append(item.day.getDisplayName(TextStyle.SHORT, Locale.CHINA))
                    }
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
                Text(text = "上课时间：第 ${item.startDayTime}-${item.endDayTime} 节")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "创建时间：${item.createTime.formatChinaDateTime()}"
                )
            }
        }
    }
}

@Composable
private fun BuildSearchResultItem(
    item: AllCourseResponse,
    checked: Boolean,
    onClick: () -> Unit,
) {
    val border =
        if (checked) BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary) else null
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = MutableInteractionSource(),
            ),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.courseName,
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
                    text = buildString {
                        append("上课星期：")
                        append("第")
                        append(item.weekList.formatWeekString())
                        append(" 每周")
                        append(item.day.getDisplayName(TextStyle.SHORT, Locale.CHINA))
                    }
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
                Text(text = "上课时间：第 ${item.startDayTime}-${item.endDayTime} 节")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "更新时间：${item.updateTime.formatChinaDateTime()}"
                )
            }
        }
    }
}

enum class CreateType {
    INPUT,
    SELECT,
}