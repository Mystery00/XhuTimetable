package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
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
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.model.response.ExpScoreResponse
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ExpScoreViewModel

@OptIn(ExperimentalMaterialApi::class)
class ExpScoreActivity : BaseComposeActivity() {
    private val viewModel: ExpScoreViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val expScoreListState by viewModel.expScoreListState.collectAsState()

        val showSelect = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded)
        val scope = rememberCoroutineScope()

        var showOption by remember { mutableStateOf(false) }

        val userDialog = remember { mutableStateOf(false) }
        val yearDialog = remember { mutableStateOf(false) }
        val termDialog = remember { mutableStateOf(false) }

        fun onBack() {
            if (showSelect.isVisible) {
                scope.launch {
                    showSelect.hide()
                }
                return
            }
            if (showOption) {
                showOption = false
                return
            }
            finish()
        }

        Scaffold(
            topBar = {
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
                                showOption = false
                                if (showSelect.isVisible) {
                                    showSelect.hide()
                                } else {
                                    showSelect.show()
                                }
                            }
                        }) {
                            Icon(
                                painter = XhuIcons.Action.more,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                        IconButton(onClick = {
                            scope.launch {
                                if (!showSelect.isVisible) {
                                    showOption = !showOption
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
            },
        ) { paddingValues ->
            Box {
                var showCredit by remember { mutableStateOf(false) }
                ModalBottomSheetLayout(
                    sheetState = showSelect,
                    scrimColor = Color.Black.copy(alpha = 0.32f),
                    sheetContent = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(text = "请选择需要查询的信息")
                            OutlinedButton(
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                                modifier = Modifier.fillMaxWidth(),
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
                                modifier = Modifier.fillMaxWidth(),
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
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    termDialog.value = true
                                }) {
                                val termSelect by viewModel.termSelect.collectAsState()
                                val termString =
                                    termSelect.firstOrNull { it.selected }?.let { "第${it.term}学期" }
                                        ?: "查询中"
                                Text(text = termString)
                            }
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    viewModel.loadScoreList()
                                    scope.launch {
                                        showSelect.hide()
                                    }
                                }) {
                                Text(text = "查询")
                            }
                        }
                    }
                ) {
                    SwipeRefresh(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        state = rememberSwipeRefreshState(expScoreListState.loading),
                        onRefresh = { },
                        swipeEnabled = false,
                    ) {
                        val scoreList = expScoreListState.scoreList
                        if (expScoreListState.loading || scoreList.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(XhuColor.Common.grayBackground),
                                contentPadding = PaddingValues(4.dp),
                            ) {
                                if (expScoreListState.loading) {
                                    items(3) {
                                        BuildItem(
                                            ExpScoreResponse.PLACEHOLDER,
                                            showCredit,
                                            true,
                                        )
                                    }
                                } else {
                                    items(scoreList.size) { index ->
                                        val item = scoreList[index]
                                        BuildItem(item, showCredit)
                                    }
                                }
                            }
                        } else {
                            BuildNoDataLayout()
                        }
                    }
                }
                AnimatedVisibility(
                    modifier = Modifier.align(Alignment.TopEnd),
                    visible = showOption,
                    enter = fadeIn(),
                    exit = fadeOut(),
                ) {
                    Card(
                        modifier = Modifier
                            .padding(4.dp),
                        elevation = 4.dp,
                    ) {
                        Column {
                            Row(modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    showCredit = !showCredit
                                    showOption = false
                                }) {
                                Checkbox(checked = showCredit, onCheckedChange = null)
                                Text(text = "显示学分")
                            }
                        }
                    }
                }
            }
        }
        ShowUserDialog(show = userDialog)
        ShowYearDialog(show = yearDialog)
        ShowTermDialog(show = termDialog)
        if (expScoreListState.errorMessage.isNotBlank()) {
            expScoreListState.errorMessage.toast(true)
        }
        BackHandler(
            enabled = showSelect.isVisible || showOption,
            onBack = {
                onBack()
            }
        )
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

    override fun onStart() {
        super.onStart()
        pushDynamicShortcuts<ScoreActivity>(this.javaClass.name, title.toString(), R.drawable.ic_exp_score)
    }
}

@Composable
private fun BuildItem(
    item: ExpScoreResponse,
    showCredit: Boolean,
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
            modifier = Modifier.padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.courseName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = XhuColor.Common.blackText,
                    modifier = Modifier.weight(1F),
                )
                Text(
                    text = buildString {
                        if (showCredit) {
                            append(item.credit)
                            append("/")
                        }
                        append(item.score)
                    },
                    color = XhuColor.Common.blackText,
                )
            }
        }
    }
}