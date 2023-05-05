package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.model.response.ClassroomResponse
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.AreaSelect
import vip.mystery0.xhu.timetable.viewmodel.CourseRoomViewModel
import vip.mystery0.xhu.timetable.viewmodel.IntSelect
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

class CourseRoomActivity : BaseComposeActivity() {
    private val viewModel: CourseRoomViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun BuildContent() {
        val pager = viewModel.pageState.collectAsLazyPagingItems()

        val areaSelectStatus = viewModel.areaSelect.collectAsState()
        val weekSelectStatus = viewModel.weekSelect.collectAsState()
        val daySelectStatus = viewModel.daySelect.collectAsState()
        val timeSelectStatus = viewModel.timeSelect.collectAsState()

        val areaDialog = remember { mutableStateOf(false) }
        val weekDialog = remember { mutableStateOf(false) }
        val dayDialog = remember { mutableStateOf(false) }
        val timeDialog = remember { mutableStateOf(false) }

        val scaffoldState = rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
        val scope = rememberCoroutineScope()

        fun onBack() {
            if (scaffoldState.isRevealed) {
                scope.launch {
                    scaffoldState.conceal()
                }
                return
            }
            finish()
        }

        BackHandler {
            onBack()
        }

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
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            areaDialog.value = true
                        }) {
                        val selected = areaSelectStatus.value.firstOrNull { it.selected }
                        val text = selected?.let { "查询区域：${it.title}" } ?: "请选择要查询的区域"
                        Text(text = text)
                    }
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            weekDialog.value = true
                        }) {
                        val weekList =
                            weekSelectStatus.value.filter { it.selected }.map { it.value }
                        val text = if (weekList.isEmpty()) "请选择要查询的周次"
                        else "查询周次：${weekList.formatWeekString()}"
                        Text(text = text)
                    }
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            dayDialog.value = true
                        }) {
                        val dayList = daySelectStatus.value.filter { it.selected }.map {
                            DayOfWeek.of(it.value)
                                .getDisplayName(TextStyle.SHORT, Locale.CHINA)
                        }
                        val text = if (dayList.isEmpty()) "请选择要查询的星期"
                        else "查询星期：${dayList.joinToString(separator = ",")}"
                        Text(text = text)
                    }
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            timeDialog.value = true
                        }) {
                        val timeList =
                            timeSelectStatus.value.filter { it.selected }.map { it.value }
                        val text = if (timeList.isEmpty()) "请选择要查询的节次"
                        else "查询节次：第${timeList.joinToString(",")}节"
                        Text(text = text)
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            scope.launch {
                                viewModel.search()
                                scaffoldState.conceal()
                            }
                        }) {
                        Text(text = "查询")
                    }
                }
            }, frontLayerContent = {
                SwipeRefresh(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberSwipeRefreshState(isRefreshing = !viewModel.init && pager.loadState.refresh is LoadState.Loading),
                    onRefresh = {
                        pager.refresh()
                    },
                    swipeEnabled = false,
                ) {
                    if (pager.itemCount == 0) {
                        BuildNoDataLayout()
                        return@SwipeRefresh
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(XhuColor.Common.grayBackground),
                        contentPadding = PaddingValues(4.dp),
                    ) {
                        itemsIndexed(pager) { _, item ->
                            item?.let {
                                BuildItem(it)
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
            })
        ShowAreaDialog(areaSelectStatus, areaDialog) {
            viewModel.changeArea(it.value)
        }
        ShowListDialog(weekSelectStatus, weekDialog, "请选择要查询的周次", 2) {
            viewModel.changeWeek(it)
        }
        ShowListDialog(daySelectStatus, dayDialog, "请选择要查询的星期", 1) {
            viewModel.changeDay(it)
        }
        ShowListDialog(timeSelectStatus, timeDialog, "请选择要查询的节次", 1) {
            viewModel.changeTime(it)
        }
        val errorMessage by viewModel.errorMessage.collectAsState()
        if (errorMessage.second.isNotBlank()) {
            errorMessage.second.toast(true)
        }
    }

    @Composable
    private fun ShowAreaDialog(
        selectState: State<List<AreaSelect>>,
        show: MutableState<Boolean>,
        onSelect: (AreaSelect) -> Unit,
    ) {
        val select = selectState.value
        val selectedValue = select.firstOrNull { it.selected } ?: select.first()
        var selected by remember { mutableStateOf(selectedValue) }
        if (show.value) {
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = "请选择要查询的区域")
                },
                text = {
                    Column {
                        LazyColumn {
                            items(select.size) { index ->
                                val item = select[index]
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
                                    Text(text = item.title)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onSelect(selected)
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
    private fun ShowListDialog(
        selectState: State<List<IntSelect>>,
        show: MutableState<Boolean>,
        title: String,
        columnSize: Int,
        onSelect: (List<Int>) -> Unit,
    ) {
        val selectList = selectState.value.filter { it.selected }.map { it.value }
        var selected by remember { mutableStateOf(selectList) }
        if (show.value) {
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = title)
                },
                text = {
                    Column {
                        LazyColumn {
                            val itemSize = selectState.value.size
                            val line = itemSize / columnSize
                            items(line) { index ->
                                Row {
                                    for (i in 0 until columnSize) {
                                        val itemIndex = index * columnSize + i
                                        if (itemIndex < itemSize) {
                                            Row(
                                                modifier = Modifier
                                                    .weight(1F),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                val value = selectState.value[itemIndex]
                                                Checkbox(
                                                    checked = selected.contains(value.value),
                                                    onCheckedChange = {
                                                        selected = if (it) {
                                                            //选中
                                                            selected + value.value
                                                        } else {
                                                            //取消选中
                                                            selected - value.value
                                                        }
                                                    },
                                                )
                                                Text(text = value.title)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onSelect(selected)
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
    item: ClassroomResponse,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        backgroundColor = XhuColor.cardBackground,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (item.roomNo.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.no, contentDescription = null)
                    Text(text = "教室编号：${item.roomNo}")
                }
            }
            if (item.roomName.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.name, contentDescription = null)
                    Text(text = "教室名称：${item.roomName}")
                }
            }
            if (item.seatCount.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.seat, contentDescription = null)
                    Text(
                        text = "座位数：${item.seatCount}"
                    )
                }
            }
            if (item.campus.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.region, contentDescription = null)
                    Text(text = "校区：${item.campus}")
                }
            }
            if (item.roomType.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.type, contentDescription = null)
                    Text(text = "场地类型：${item.roomType}")
                }
            }
            if (item.roomRemark.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.remark, contentDescription = null)
                    Text(text = "备注：${item.roomRemark}")
                }
            }
        }
    }
}