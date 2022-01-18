package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.runtime.*
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
                        val text = selected?.let { "查询区域：${it})" } ?: "请选择要查询的区域"
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
                            if (week.isEmpty()) "请选择要查询的周次" else "查询周次：${week.formatWeekString()}"
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
                            if (day.isEmpty()) "请选择要查询的星期" else "查询星期：${
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
                            if (time.isEmpty()) "请选择要查询的节次" else "查询节次：第${time.joinToString(",")}节"
                        Text(text = text)
                    }
                    OutlinedButton(
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            viewModel.search()
                            scope.launch {
                                scaffoldState.conceal()
                            }
                        }) {
                        Text(text = "查询")
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
                    if (!courseRoomListState.loading && list.isEmpty()) {
                        BuildNoDataLayout()
                    }
                }
            })
        if (courseRoomListState.errorMessage.isNotBlank()) {
            courseRoomListState.errorMessage.toast(true)
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
                    Image(painter = XhuIcons.CustomCourse.teacher, contentDescription = null)
                    Text(text = "教室编号：${item.roomNo}")
                }
            }
            if (item.roomName.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CustomCourse.teacher, contentDescription = null)
                    Text(text = "教室名称：${item.roomName}")
                }
            }
            if (item.seat.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CustomCourse.week, contentDescription = null)
                    Text(
                        text = "座位数：${item.seat}"
                    )
                }
            }
            if (item.region.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CustomCourse.location, contentDescription = null)
                    Text(text = "所在地区：${item.region}")
                }
            }
            if (item.type.isNotBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(painter = XhuIcons.CustomCourse.location, contentDescription = null)
                    Text(text = "教室类型：${item.type}")
                }
            }
        }
    }
}