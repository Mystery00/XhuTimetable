package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.model.request.CustomCourseRequest
import vip.mystery0.xhu.timetable.model.response.AllCourseResponse
import vip.mystery0.xhu.timetable.model.response.CustomCourseResponse
import vip.mystery0.xhu.timetable.ui.component.CourseIndexSelector
import vip.mystery0.xhu.timetable.ui.component.XhuDialogState
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
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

    @OptIn(
        ExperimentalMaterial3Api::class,
        ExperimentalFoundationApi::class
    )
    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)
        val userSelect by viewModel.userSelect.collectAsState()
        val yearSelect by viewModel.yearSelect.collectAsState()
        val termSelect by viewModel.termSelect.collectAsState()

        val userDialog = rememberXhuDialogState()
        val yearDialog = rememberXhuDialogState()
        val termDialog = rememberXhuDialogState()

        val scope = rememberCoroutineScope()

        val openBottomSheet = rememberSaveable { mutableStateOf(false) }

        val customCourseState = remember { mutableStateOf(CustomCourseResponse.init()) }

        fun updateCustomCourse(data: CustomCourseResponse) {
            customCourseState.value = data
            openBottomSheet.value = true
        }

        fun onBack() {
            if (openBottomSheet.value) {
                openBottomSheet.value = false
                return
            }
            viewModel.updateChange()
            finish()
        }

        BackHandler {
            onBack()
        }

        Scaffold(
            topBar = {
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
                )
            }
        ) { paddingValues ->
            val lazyListState = rememberLazyListState()
            val refreshing by viewModel.refreshing.collectAsState()
            val isScrollingUp = lazyListState.isScrollingUp()
            BuildPaging(
                state = lazyListState,
                paddingValues = paddingValues,
                pager = pager,
                refreshing = refreshing,
                alwaysShowList = true,
                listContent = {
                    stickyHeader {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            BuildSelectBackLayerContent(
                                userSelect = userSelect,
                                yearSelect = yearSelect,
                                termSelect = termSelect,
                                showUserDialog = userDialog,
                                showYearDialog = yearDialog,
                                showTermDialog = termDialog,
                                onDataLoad = {
                                    viewModel.loadCustomCourseList()
                                }
                            )
                        }
                    }
                    itemsIndexed(
                        pager,
                        key = { index -> pager[index]?.courseId ?: index }) { item ->
                        BuildItem(item) {
                            scope.launch {
                                updateCustomCourse(item)
                            }
                        }
                    }
                },
            ) @Composable {
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
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                        )
                    }
                }
            }
        }

        ShowUserDialog(selectList = userSelect, show = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        ShowYearDialog(selectList = yearSelect, show = yearDialog, onSelect = {
            viewModel.selectYear(it.value)
        })
        ShowTermDialog(selectList = termSelect, show = termDialog, onSelect = {
            viewModel.selectTerm(it.value)
        })
        CustomCourseBottomSheet(customCourseState.value, openBottomSheet, scope)
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
        }
    }

    @Composable
    private fun ShowSearchCourseIndexDialog(
        courseIndex: MutableState<Int>,
        state: XhuDialogState,
    ) {
        val options = (1..11).toList()
        ShowSelectDialog(
            dialogTitle = "请选择需要搜索的节次",
            options = options,
            selectIndex = options.indexOf(courseIndex.value),
            itemTransform = { "第 $it 节" },
            state = state,
            onSelect = { _, option ->
                courseIndex.value = option
            },
        )
    }

    @Composable
    private fun ShowWeekDialog(
        state: XhuDialogState,
        initDayOfWeek: DayOfWeek?,
        onSelect: (DayOfWeek) -> Unit,
    ) {
        val options = DayOfWeek.values().toList()
        ShowSelectDialog(
            dialogTitle = "请选择上课星期",
            options = options,
            selectIndex = options.indexOf(initDayOfWeek),
            itemTransform = { it.getDisplayName(TextStyle.SHORT, Locale.CHINA) },
            state = state,
            onSelect = { _, option ->
                onSelect(option)
            },
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun CustomCourseBottomSheet(
        customCourse: CustomCourseResponse,
        openBottomSheet: MutableState<Boolean>,
        scope: CoroutineScope,
    ) {
        var createType by remember { mutableStateOf(CreateType.INPUT) }

        val weekDialog = rememberXhuDialogState()
        val sheetState = rememberModalBottomSheetState()

        var courseName by remember { mutableStateOf(customCourse.courseName) }
        var weekList by remember { mutableStateOf(customCourse.weekList) }
        var day by remember { mutableStateOf(customCourse.day) }
        var startDayTime by remember { mutableIntStateOf(customCourse.startDayTime) }
        var endDayTime by remember { mutableIntStateOf(customCourse.endDayTime) }
        var location by remember { mutableStateOf(customCourse.location) }
        var teacher by remember { mutableStateOf(customCourse.teacher) }

        LaunchedEffect(customCourse) {
            courseName = customCourse.courseName
            weekList = customCourse.weekList
            day = customCourse.day
            startDayTime = customCourse.startDayTime
            endDayTime = customCourse.endDayTime
            location = customCourse.location
            teacher = customCourse.teacher
        }

        var searchCourse by remember { mutableStateOf<AllCourseResponse?>(null) }

        val saveLoadingState by viewModel.saveLoadingState.collectAsState()

        val focusManager = LocalFocusManager.current

        ShowWeekDialog(
            state = weekDialog,
            initDayOfWeek = day,
        ) {
            day = it
        }

        fun dismissSheet() {
            focusManager.clearFocus()
            scope
                .launch { sheetState.hide() }
                .invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        openBottomSheet.value = false
                    }
                }
        }

        LaunchedEffect(saveLoadingState.loading) {
            if (!saveLoadingState.loading && saveLoadingState.actionSuccess && openBottomSheet.value) {
                dismissSheet()
            }
        }

        if (!openBottomSheet.value) {
            return
        }

        ModalBottomSheet(
            onDismissRequest = {
                focusManager.clearFocus()
                openBottomSheet.value = false
            },
            sheetState = sheetState,
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    modifier = Modifier
                        .padding(12.dp)
                        .clickable {
                            dismissSheet()
                        },
                    painter = XhuIcons.CustomCourse.close,
                    contentDescription = null,
                )
                Spacer(modifier = Modifier.weight(1F))
                if (saveLoadingState.loading) {
                    TextButton(
                        enabled = false,
                        onClick = {}) {
                        Text(text = "保存中...")
                    }
                } else {
                    if (customCourse.courseId != 0L && createType == CreateType.INPUT) {
                        TextButton(
                            onClick = {
                                viewModel.deleteCustomCourse(customCourse.courseId)
                            }) {
                            Text(text = "删除", color = Color.Red)
                        }
                    }
                    TextButton(
                        onClick = {
                            if (createType == CreateType.INPUT) {
                                viewModel.saveCustomCourse(
                                    customCourse.courseId,
                                    CustomCourseRequest.buildOf(
                                        courseName,
                                        weekList,
                                        day,
                                        startDayTime,
                                        endDayTime,
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
            }
            if (customCourse.courseId == 0L) {
                Row(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                ) {
                    val selectedContentColor = MaterialTheme.colorScheme.onPrimary
                    val selectedBackgroundColor = MaterialTheme.colorScheme.primary
                    val contentColor = MaterialTheme.colorScheme.onPrimary
                    val backgroundColor = MaterialTheme.colorScheme.inversePrimary
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
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
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
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
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
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                )
                            )
                        }
                        Row(
                            modifier = Modifier
                                .defaultMinSize(minHeight = 64.dp),
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
                                                if (inList) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer
                                            val textColor =
                                                if (inList) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer
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
                                startIndex = startDayTime,
                                endIndex = endDayTime,
                                modifier = Modifier
                                    .weight(1F)
                                    .height(36.dp),
                            ) { startIndex, endIndex ->
                                startDayTime = startIndex
                                endDayTime = endIndex
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                modifier = Modifier
                                    .weight(1F)
                                    .clickable(
                                        onClick = {
                                            weekDialog.show()
                                        },
                                        indication = null,
                                        interactionSource = MutableInteractionSource(),
                                    ),
                                text = day.getDisplayName(
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

                    val searchWeekDialog = rememberXhuDialogState()
                    val searchCourseIndexDialog = rememberXhuDialogState()

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
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
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
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    errorContainerColor = Color.Transparent,
                                    disabledContainerColor = Color.Transparent,
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
                                            searchCourseIndexDialog.show()
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
                                            searchWeekDialog.show()
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
                        color = XhuColor.Common.divider
                    )
                    BuildPaging(
                        paddingValues = PaddingValues(4.dp),
                        pager = allCoursePager,
                        refreshing = false,
                        listContent = {
                            items(
                                allCoursePager.itemCount,
                                key = { index -> index },
                            ) { index ->
                                val item = allCoursePager[index] ?: return@items
                                BuildSearchResultItem(
                                    item,
                                    checked = item == searchCourse
                                ) {
                                    searchCourse = item
                                }
                            }
                        },
                    )
                    Box(
                        modifier = Modifier
                            .weight(1F)
                            .defaultMinSize(minHeight = 180.dp)
                            .fillMaxWidth(),
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
                            when (allCoursePager.loadState.append) {
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
                    ShowWeekDialog(
                        state = searchWeekDialog,
                        initDayOfWeek = if (searchDay.intValue == 0) null else DayOfWeek.of(
                            searchDay.intValue
                        ),
                    ) {
                        searchDay.intValue = it.value
                    }
                    ShowSearchCourseIndexDialog(
                        courseIndex = searchCourseIndex,
                        state = searchCourseIndexDialog
                    )
                }
            }
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