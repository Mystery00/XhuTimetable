package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.model.response.SchoolTimetableResponse
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.component.dialog.XhuSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.dialog.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.SchoolTimetableViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_search_result

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolTimetableScreen() {
    val viewModel = koinViewModel<SchoolTimetableViewModel>()
    val navController = LocalNavController.current
    val pager = viewModel.pageState.collectAndHandleState(viewModel::handleLoadState)
    val scope = rememberCoroutineScope()

    val openFilterSheet = rememberSaveable { mutableStateOf(false) }
    var courseName by rememberSaveable { mutableStateOf("") }
    var teacherName by rememberSaveable { mutableStateOf("") }

    val campusSelect by viewModel.campusSelect.select.collectAsState()
    val collegeSelect by viewModel.collegeSelect.select.collectAsState()
    val majorSelect by viewModel.majorSelect.select.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "全校课表查询",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            painter = XhuIcons.back,
                            contentDescription = "返回",
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { openFilterSheet.value = true }) {
                        Icon(
                            imageVector = Icons.Rounded.FilterList,
                            contentDescription = "搜索与筛选",
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
            listHeader = {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        FilterChip(
                            selected = campusSelect.any { it.selected },
                            onClick = { openFilterSheet.value = true },
                            label = { Text(campusSelect.firstOrNull { it.selected }?.title ?: "校区") },
                        )
                        FilterChip(
                            selected = collegeSelect.any { it.selected },
                            onClick = { openFilterSheet.value = true },
                            label = { Text(collegeSelect.firstOrNull { it.selected }?.title ?: "学院") },
                        )
                        FilterChip(
                            selected = majorSelect.any { it.selected },
                            onClick = { openFilterSheet.value = true },
                            label = { Text(majorSelect.firstOrNull { it.selected }?.title ?: "专业") },
                        )
                        if (courseName.isNotBlank()) {
                            FilterChip(
                                selected = true,
                                onClick = { openFilterSheet.value = true },
                                label = { Text("课程：$courseName") },
                            )
                        }
                        if (teacherName.isNotBlank()) {
                            FilterChip(
                                selected = true,
                                onClick = { openFilterSheet.value = true },
                                label = { Text("教师：$teacherName") },
                            )
                        }
                    }
                }
            },
            itemContent = { item ->
                BuildSchoolCourseCard(item) {
                    viewModel.saveAsCustomCourse(item)
                }
            },
            emptyState = {
                val loadingErrorMessage by viewModel.loadingErrorMessage.collectAsState()
                if (viewModel.init) {
                    StateScreen(
                        title = "按条件搜索全校课程",
                        subtitle = "支持按校区、学院、专业、课程名或教师名查找，并可一键蹭课加入课表",
                        buttonText = "开始搜索",
                        imageRes = painterResource(Res.drawable.state_no_search_result),
                        onButtonClick = { openFilterSheet.value = true }
                    )
                } else {
                    StateScreen(
                        title = loadingErrorMessage ?: "未找到匹配课程",
                        buttonText = "修改搜索条件",
                        imageRes = painterResource(Res.drawable.state_no_search_result),
                        onButtonClick = { openFilterSheet.value = true }
                    )
                }
            }
        )

        SchoolTimetableFilterSheet(
            openBottomSheet = openFilterSheet,
            scope = scope,
            courseName = courseName,
            onCourseNameChange = { courseName = it },
            teacherName = teacherName,
            onTeacherNameChange = { teacherName = it },
        )

        HandleErrorMessage(flow = viewModel.errorMessage)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SchoolTimetableFilterSheet(
    openBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
    courseName: String,
    onCourseNameChange: (String) -> Unit,
    teacherName: String,
    onTeacherNameChange: (String) -> Unit,
) {
    val viewModel = koinViewModel<SchoolTimetableViewModel>()

    val campusSelect by viewModel.campusSelect.select.collectAsState()
    val collegeSelect by viewModel.collegeSelect.select.collectAsState()
    val majorSelect by viewModel.majorSelect.select.collectAsState()

    val campusDialogState = rememberXhuDialogState()
    val collegeDialogState = rememberXhuDialogState()
    val majorDialogState = rememberXhuDialogState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    fun dismissSheet() {
        scope.safeLaunch { sheetState.hide() }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                openBottomSheet.value = false
            }
        }
    }

    XhuSingleSelectDialog(
        state = campusDialogState,
        title = "选择校区",
        options = campusSelect,
        selectedIndex = campusSelect.indexOfFirst { it.selected },
        itemLabel = { it.title },
        onSelect = { _, item -> viewModel.selectCampus(item.value) },
    )
    XhuSingleSelectDialog(
        state = collegeDialogState,
        title = "选择学院",
        options = collegeSelect,
        selectedIndex = collegeSelect.indexOfFirst { it.selected },
        itemLabel = { it.title },
        onSelect = { _, item -> viewModel.selectCollege(item.value) },
    )
    XhuSingleSelectDialog(
        state = majorDialogState,
        title = "选择专业",
        options = majorSelect,
        selectedIndex = majorSelect.indexOfFirst { it.selected },
        itemLabel = { it.title },
        onSelect = { _, item -> viewModel.selectMajor(item.value) },
    )

    if (!openBottomSheet.value) return

    ModalBottomSheet(
        onDismissRequest = { openBottomSheet.value = false },
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(start = 20.dp, end = 20.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "全校课表搜索",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val campus = campusSelect.firstOrNull { it.selected }
                    FilterSelectorRow(
                        icon = Icons.Rounded.LocationCity,
                        label = "查询校区",
                        value = campus?.title ?: "全部校区",
                        highlighted = campus != null,
                        onClick = { campusDialogState.show() },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                    val college = collegeSelect.firstOrNull { it.selected }
                    FilterSelectorRow(
                        icon = Icons.Rounded.AccountBalance,
                        label = "查询学院",
                        value = college?.title ?: "全部学院",
                        highlighted = college != null,
                        onClick = { collegeDialogState.show() },
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 52.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                    )
                    val major = majorSelect.firstOrNull { it.selected }
                    FilterSelectorRow(
                        icon = Icons.Rounded.School,
                        label = "查询专业",
                        value = major?.title ?: if (majorSelect.isEmpty()) "请先选学院" else "全部专业",
                        highlighted = major != null,
                        onClick = {
                            if (majorSelect.isNotEmpty()) majorDialogState.show()
                        },
                    )
                }
            }

            OutlinedTextField(
                value = courseName,
                onValueChange = onCourseNameChange,
                label = { Text("课程名称（选填）") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = teacherName,
                onValueChange = onTeacherNameChange,
                label = { Text("教师姓名（选填）") },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                onClick = {
                    viewModel.loadSchoolTimetable(
                        courseName = courseName.trim(),
                        teacherName = teacherName.trim(),
                    )
                    dismissSheet()
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "查询课程",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun BuildSchoolCourseCard(course: SchoolTimetableResponse, onClick: () -> Unit) {
    val accentColor = ColorPool.hash(course.courseName)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = XhuColor.cardBackground,
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(start = 12.dp, top = 14.dp, end = 16.dp, bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.78f),
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Person,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                            Text(
                                text = course.teacher.ifBlank { "未知教师" },
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 9.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        SchoolCourseMetaRow(
                            icon = Icons.Rounded.Schedule,
                            tint = MaterialTheme.colorScheme.primary,
                            text = course.showTimeString.ifBlank { "暂无时间数据" },
                        )
                        SchoolCourseMetaRow(
                            icon = Icons.Rounded.LocationOn,
                            tint = MaterialTheme.colorScheme.tertiary,
                            text = course.location.ifBlank { "暂无地点数据" },
                        )
                    }
                }

                if (course.customCourseList.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "共 ${course.customCourseList.size} 个上课时段",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        )
                        FilledTonalButton(
                            onClick = onClick,
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "蹭课 · 加入课表",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SchoolCourseMetaRow(
    icon: ImageVector,
    tint: Color,
    text: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(15.dp),
            tint = tint,
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
