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
import androidx.compose.ui.unit.dp
import com.google.accompanist.placeholder.PlaceholderHighlight
import com.google.accompanist.placeholder.material.placeholder
import com.google.accompanist.placeholder.material.shimmer
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.utils.formatWeekString
import vip.mystery0.xhu.timetable.viewmodel.CourseRoom
import vip.mystery0.xhu.timetable.viewmodel.CourseRoomViewModel
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.*

class CourseRoomActivity : BaseComposeActivity() {
    private val viewModel: CourseRoomViewModel by viewModels()

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    override fun BuildContent() {
        val courseRoomListState by viewModel.courseRoomListState.collectAsState()

        val scope = rememberCoroutineScope()
        val scaffoldState: BackdropScaffoldState =
            rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)

        val areaDialog = remember { mutableStateOf(false) }
        val weekDialog = remember { mutableStateOf(false) }
        val dayDialog = remember { mutableStateOf(false) }
        val timeDialog = remember { mutableStateOf(false) }

        fun onBack() {
            if (scaffoldState.isConcealed) {
                finish()
            } else {
                scope.launch {
                    scaffoldState.conceal()
                }
            }
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
                        val select by viewModel.areaSelect.collectAsState()
                        val selected = select.firstOrNull { it.selected }
                        val text = selected?.let { "???????????????${it.area}" } ?: "???????????????????????????"
                        Text(text = text)
                    }
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            weekDialog.value = true
                        }) {
                        val week by viewModel.week.collectAsState()
                        val text =
                            if (week.isEmpty()) "???????????????????????????" else "???????????????${week.formatWeekString()}"
                        Text(text = text)
                    }
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            dayDialog.value = true
                        }) {
                        val day by viewModel.day.collectAsState()
                        val text =
                            if (day.isEmpty()) "???????????????????????????" else "???????????????${
                                day.joinToString(separator = ",") {
                                    DayOfWeek.of(it).getDisplayName(
                                        TextStyle.SHORT,
                                        Locale.CHINA
                                    )
                                }
                            }"
                        Text(text = text)
                    }
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            timeDialog.value = true
                        }) {
                        val time by viewModel.time.collectAsState()
                        val text =
                            if (time.isEmpty()) "???????????????????????????" else "??????????????????${time.joinToString(",")}???"
                        Text(text = text)
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.search()
                        }) {
                        Text(text = "??????")
                    }
                }
            }, frontLayerContent = {
                SwipeRefresh(
                    modifier = Modifier.fillMaxSize(),
                    state = rememberSwipeRefreshState(courseRoomListState.loading),
                    onRefresh = { },
                    swipeEnabled = false,
                ) {
                    val list = courseRoomListState.courseRoomList
                    if (courseRoomListState.loading || list.isNotEmpty()) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(XhuColor.Common.grayBackground),
                            contentPadding = PaddingValues(4.dp),
                        ) {
                            if (courseRoomListState.loading) {
                                items(3) {
                                    BuildItem(
                                        CourseRoom.PLACEHOLDER,
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
                    } else {
                        BuildNoDataLayout()
                    }
                }
            })
        ShowAreaDialog(show = areaDialog)
        val week by viewModel.week.collectAsState()
        ShowListDialog(
            show = weekDialog,
            list = week,
            title = "???????????????????????????",
            itemSize = 20,
            block = { item -> "???${item}???" },
            onConfirm = {
                viewModel.changeWeek(it)
            })
        val day by viewModel.day.collectAsState()
        ShowListDialog(
            show = dayDialog,
            list = day,
            title = "???????????????????????????",
            itemSize = 7,
            block = { item -> DayOfWeek.of(item).getDisplayName(TextStyle.FULL, Locale.CHINA) },
            onConfirm = {
                viewModel.changeDay(it)
            })
        val time by viewModel.time.collectAsState()
        ShowListDialog(
            show = timeDialog,
            list = time,
            title = "???????????????????????????",
            itemSize = 11,
            block = { item -> "???${item}???" },
            onConfirm = {
                viewModel.changeTime(it)
            })
        if (courseRoomListState.errorMessage.isNotBlank()) {
            courseRoomListState.errorMessage.toast(true)
        }
    }

    @Composable
    private fun ShowAreaDialog(
        show: MutableState<Boolean>,
    ) {
        val areaSelect by viewModel.areaSelect.collectAsState()
        val selectedArea = areaSelect.firstOrNull { it.selected } ?: return
        if (show.value) {
            var selected by remember { mutableStateOf(selectedArea) }
            AlertDialog(
                onDismissRequest = {
                    show.value = false
                },
                title = {
                    Text(text = "???????????????????????????")
                },
                text = {
                    Column {
                        LazyColumn {
                            items(areaSelect.size) { index ->
                                val item = areaSelect[index]
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
                                    Text(text = item.area)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.changeArea(selected.area)
                            show.value = false
                        },
                    ) {
                        Text("??????")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            show.value = false
                        }
                    ) {
                        Text("??????")
                    }
                }
            )
        }
    }

    @Composable
    private fun ShowListDialog(
        show: MutableState<Boolean>,
        list: List<Int>,
        title: String,
        itemSize: Int,
        block: (Int) -> String,
        onConfirm: (List<Int>) -> Unit,
    ) {
        val array = Array(itemSize) { i -> list.contains(i + 1) }
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
                            val line = if (itemSize > 12) itemSize / 2 else itemSize
                            val panelSize = if (itemSize > 12) 2 else 1
                            items(line) { index ->
                                Row {
                                    for (i in 0 until panelSize) {
                                        val item = index * panelSize + i
                                        if (item < itemSize) {
                                            var selected by remember { mutableStateOf(array[item]) }
                                            Row(
                                                modifier = Modifier
                                                    .weight(1F),
                                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                            ) {
                                                Checkbox(
                                                    checked = selected,
                                                    onCheckedChange = {
                                                        selected = it
                                                        array[item] = it
                                                    },
                                                )
                                                Text(text = block(item + 1))
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
                            onConfirm(array.mapIndexed { index, b -> if (b) index + 1 else null }
                                .filterNotNull())
                            show.value = false
                        },
                    ) {
                        Text("??????")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            show.value = false
                        }
                    ) {
                        Text("??????")
                    }
                }
            )
        }
    }
}

@Composable
private fun BuildItem(
    item: CourseRoom,
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
            if (item.roomNo.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.no, contentDescription = null)
                    Text(text = "???????????????${item.roomNo}")
                }
            }
            if (item.roomName.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.name, contentDescription = null)
                    Text(text = "???????????????${item.roomName}")
                }
            }
            if (item.seat.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.seat, contentDescription = null)
                    Text(
                        text = "????????????${item.seat}"
                    )
                }
            }
            if (item.region.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.region, contentDescription = null)
                    Text(text = "???????????????${item.region}")
                }
            }
            if (item.type.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CourseRoom.type, contentDescription = null)
                    Text(text = "???????????????${item.type}")
                }
            }
        }
    }
}