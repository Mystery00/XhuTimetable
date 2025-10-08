package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.EventNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.AppRegistration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.model.response.SchoolTimetableResponse
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.PageItemLayout
import vip.mystery0.xhu.timetable.ui.component.ShowSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.TextWithIcon
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.CampusSelect
import vip.mystery0.xhu.timetable.viewmodel.CollegeSelect
import vip.mystery0.xhu.timetable.viewmodel.MajorSelect
import vip.mystery0.xhu.timetable.viewmodel.SchoolTimetableViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_course_data

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolTimetableScreen() {
    val viewModel = koinViewModel<SchoolTimetableViewModel>()

    val navController = LocalNavController.current!!
    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)

    val scope = rememberCoroutineScope()

    val openBottomSheet = rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("全校课表查询") },
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
                        openBottomSheet.value = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        BuildPaging(
            paddingValues = paddingValues,
            pager = pager,
            refreshing = !viewModel.init && pager.loadState.refresh is LoadState.Loading,
            key = { index -> index },
            itemContent = @Composable { item ->
                BuildItem(item) {
                    viewModel.saveAsCustomCourse(item)
                }
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                StateScreen(
                    title = loadingErrorMessage ?: "暂无数据",
                    imageRes = painterResource(Res.drawable.state_no_course_data),
                    verticalArrangement = Arrangement.Top,
                )
            }
        )

        SchoolTimetableBottomSheet(openBottomSheet, scope)

        HandleErrorMessage(flow = viewModel.errorMessage)
    }
}

@Composable
private fun ShowCampusDialog(
    selectList: List<CampusSelect>,
    useCaseState: UseCaseState,
    onSelect: (CampusSelect) -> Unit,
) {
    ShowSingleSelectDialog(
        dialogTitle = "请选择要查询的校区",
        selectList = selectList,
        useCaseState = useCaseState,
        onSelect = onSelect,
    )
}

@Composable
private fun ShowCollegeDialog(
    selectList: List<CollegeSelect>,
    useCaseState: UseCaseState,
    onSelect: (CollegeSelect) -> Unit,
) {
    ShowSingleSelectDialog(
        dialogTitle = "请选择要查询的学院",
        selectList = selectList,
        useCaseState = useCaseState,
        onSelect = onSelect,
    )
}

@Composable
private fun ShowMajorDialog(
    selectList: List<MajorSelect>,
    useCaseState: UseCaseState,
    onSelect: (MajorSelect) -> Unit,
) {
    if (selectList.isEmpty()) {
        useCaseState.hide()
        return
    }
    ShowSingleSelectDialog(
        dialogTitle = "请选择要查询的专业",
        selectList = selectList,
        useCaseState = useCaseState,
        onSelect = onSelect,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchoolTimetableBottomSheet(
    openBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
) {
    val viewModel = koinViewModel<SchoolTimetableViewModel>()

    val campusSelect by viewModel.campusSelect.select.collectAsState()
    val collegeSelect by viewModel.collegeSelect.select.collectAsState()
    val majorSelect by viewModel.majorSelect.select.collectAsState()
    var courseName by remember { mutableStateOf("") }
    var teacherName by remember { mutableStateOf("") }

    val campusDialog by viewModel.campusSelect.selectDialog.collectAsState()
    val collegeDialog by viewModel.collegeSelect.selectDialog.collectAsState()
    val majorDialog by viewModel.majorSelect.selectDialog.collectAsState()

    val sheetState = rememberModalBottomSheetState()

    ShowCampusDialog(campusSelect, campusDialog) {
        viewModel.selectCampus(it.value)
    }
    ShowCollegeDialog(collegeSelect, collegeDialog) {
        viewModel.selectCollege(it.value)
    }
    ShowMajorDialog(majorSelect, majorDialog) {
        viewModel.selectMajor(it.value)
    }

    fun dismissSheet() {
        scope
            .safeLaunch { sheetState.hide() }
            .invokeOnCompletion {
                if (!sheetState.isVisible) {
                    openBottomSheet.value = false
                }
            }
    }

    if (!openBottomSheet.value) {
        return
    }

    ModalBottomSheet(
        onDismissRequest = {
            openBottomSheet.value = false
        },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    campusDialog.show()
                }) {
                val selected = campusSelect.firstOrNull { it.selected }
                val text = selected?.let { "查询校区：${it.title}" } ?: "请选择要查询的校区"
                Text(text = text)
            }
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    collegeDialog.show()
                }) {
                val selected = collegeSelect.firstOrNull { it.selected }
                val text = selected?.let { "查询学院：${it.title}" } ?: "请选择要查询的学院"
                Text(text = text)
            }
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    majorDialog.show()
                }) {
                val selected = majorSelect.firstOrNull { it.selected }
                val text = selected?.let { "查询专业：${it.title}" } ?: "请选择要查询的专业"
                Text(text = text)
            }
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("课程名称") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = teacherName,
                onValueChange = { teacherName = it },
                label = { Text("教师名称") },
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.loadSchoolTimetable(
                        courseName = courseName,
                        teacherName = teacherName,
                    )
                    dismissSheet()
                }) {
                Text(text = "查询")
            }
        }
    }
}


@Composable
private fun BuildItem(course: SchoolTimetableResponse, onClick: () -> Unit) {
    PageItemLayout(
        header = {
            Text("课程名称：${course.courseName}")
        },
        content = {
            ProvideTextStyle(
                value = LocalTextStyle.current.copy(fontSize = 13.sp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextWithIcon(
                        imageVector = Icons.Filled.Person,
                        text = "教师名称：${course.teacher}",
                    )
                    TextWithIcon(
                        imageVector = Icons.AutoMirrored.Filled.EventNote,
                        text = "上课时间：${course.showTimeString}",
                    )
                    TextWithIcon(
                        imageVector = Icons.Filled.LocationOn,
                        text = "上课地点：${course.location}",
                    )
                }
            }
        },
        footer = if (course.customCourseList.isEmpty()) null else {
            {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "点击添加到课表",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1F)
                            .clickable(onClick = onClick),
                        fontSize = 16.sp,
                    )
                    ActionButton(
                        text = "蹭课",
                        imageVector = Icons.Rounded.AppRegistration,
                        onClick = onClick
                    )
                }
            }
        }
    )
}