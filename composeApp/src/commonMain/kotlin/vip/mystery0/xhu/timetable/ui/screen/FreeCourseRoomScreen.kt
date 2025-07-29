package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeler.sheets.option.OptionDialog
import com.maxkeppeler.sheets.option.models.DisplayMode
import com.maxkeppeler.sheets.option.models.Option
import com.maxkeppeler.sheets.option.models.OptionConfig
import com.maxkeppeler.sheets.option.models.OptionSelection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.datetime.DayOfWeek
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.ui.component.BuildPaging
import vip.mystery0.xhu.timetable.ui.component.ShowSingleSelectDialog
import vip.mystery0.xhu.timetable.ui.component.collectAndHandleState
import vip.mystery0.xhu.timetable.ui.component.xhuHeader
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatChina
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.AreaSelect
import vip.mystery0.xhu.timetable.viewmodel.CourseRoomViewModel
import vip.mystery0.xhu.timetable.viewmodel.IntSelect

@Composable
fun FreeCourseRoomScreen() {
    val viewModel = koinViewModel<CourseRoomViewModel>()

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
                title = { Text(text = "空闲教室查询") },
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
            key = { index -> pager[index]?.roomNo ?: index },
            itemContent = @Composable { item ->
                BuildItem(item)
            }
        )
    }

    CourseRoomBottomSheet(openBottomSheet, scope)

    HandleErrorMessage(flow = viewModel.errorMessage)
}

@Composable
private fun ShowAreaDialog(
    selectList: List<AreaSelect>,
    useCaseState: UseCaseState,
    onSelect: (AreaSelect) -> Unit,
) {
    ShowSingleSelectDialog(
        dialogTitle = "请选择要查询的区域",
        selectList = selectList,
        useCaseState = useCaseState,
        onSelect = onSelect,
    )
}

@Composable
private fun ShowListDialog(
    selectList: List<IntSelect>,
    useCaseState: UseCaseState,
    title: String,
    columnSize: Int,
    onSelect: (List<Int>) -> Unit,
) {
    if (selectList.isEmpty()) return
    var config = OptionConfig(mode = DisplayMode.LIST)
    if (columnSize > 1) {
        config = OptionConfig(mode = DisplayMode.GRID_VERTICAL, gridColumns = columnSize)
    }
    OptionDialog(
        header = xhuHeader(title = title),
        state = useCaseState,
        selection = OptionSelection.Multiple(
            options = selectList.map {
                Option(
                    titleText = it.title,
                    selected = it.selected,
                )
            },
        ) { selectedIndices, _ ->
            onSelect(selectedIndices.map { selectList[it].value })
        },
        config = config,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseRoomBottomSheet(
    openBottomSheet: MutableState<Boolean>,
    scope: CoroutineScope,
) {
    val viewModel = koinViewModel<CourseRoomViewModel>()

    val areaSelect by viewModel.areaSelect.select.collectAsState()
    val weekSelect by viewModel.weekSelect.select.collectAsState()
    val daySelect by viewModel.daySelect.select.collectAsState()
    val timeSelect by viewModel.timeSelect.select.collectAsState()

    val areaDialog by viewModel.areaSelect.selectDialog.collectAsState()
    val weekDialog by viewModel.weekSelect.selectDialog.collectAsState()
    val dayDialog by viewModel.daySelect.selectDialog.collectAsState()
    val timeDialog by viewModel.timeSelect.selectDialog.collectAsState()

    val sheetState = rememberModalBottomSheetState()

    ShowAreaDialog(areaSelect, areaDialog) {
        viewModel.changeArea(it.value)
    }
    ShowListDialog(weekSelect, weekDialog, "请选择要查询的周次", 4) {
        viewModel.changeWeek(it)
    }
    ShowListDialog(daySelect, dayDialog, "请选择要查询的星期", 2) {
        viewModel.changeDay(it)
    }
    ShowListDialog(timeSelect, timeDialog, "请选择要查询的节次", 4) {
        viewModel.changeTime(it)
    }

    fun dismissSheet() {
        scope
            .launch { sheetState.hide() }
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
                    areaDialog.show()
                }) {
                val selected = areaSelect.firstOrNull { it.selected }
                val text = selected?.let { "查询区域：${it.title}" } ?: "请选择要查询的区域"
                Text(text = text)
            }
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    weekDialog.show()
                }) {
                val weekList = weekSelect.filter { it.selected }.map { it.value }
                val text = if (weekList.isEmpty()) "请选择要查询的周次"
                else "查询周次：${weekList.formatWeekString()}"
                Text(text = text)
            }
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    dayDialog.show()
                }) {
                val dayList = daySelect.filter { it.selected }.map {
                    DayOfWeek(it.value).formatChina()
                }
                val text = if (dayList.isEmpty()) "请选择要查询的星期"
                else "查询星期：${dayList.joinToString(separator = ",")}"
                Text(text = text)
            }
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    timeDialog.show()
                }) {
                val timeList = timeSelect.filter { it.selected }.map { it.value }
                val text = if (timeList.isEmpty()) "请选择要查询的节次"
                else "查询节次：第${timeList.joinToString(",")}节"
                Text(text = text)
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    scope.launch {
                        viewModel.search()
                        dismissSheet()
                    }
                }) {
                Text(text = "查询")
            }
        }
    }
}

@Composable
private fun BuildItem(
    item: ClassroomResponse,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "教室编号：${item.roomNo}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                )
                Spacer(modifier = Modifier.weight(1F))
                val color = ColorPool.hash(item.roomName)
                FilterChip(
                    modifier = Modifier.widthIn(max = 156.dp),
                    selected = true,
                    onClick = {},
                    label = {
                        Text(text = item.roomName, textAlign = TextAlign.Center)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = color.copy(alpha = 0.2F),
                        selectedLabelColor = color,
                    )
                )
            }
            HorizontalDivider(modifier = Modifier.fillMaxWidth())
            if (item.campus.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "校区：${item.campus}",
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            if (item.roomType.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(14.dp),
                        imageVector = Icons.Filled.MeetingRoom,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                    )
                    Text(
                        text = "场地类型：${item.roomType}",
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            if (item.roomRemark.isNotBlank()) {
                Card {
                    Text(
                        text = "备注：${item.roomRemark}",
                        modifier = Modifier.fillMaxWidth()
                            .padding(12.dp),
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 12.sp,
                    )
                }
            }
        }
    }
}