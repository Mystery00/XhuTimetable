package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
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
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.model.response.ScoreItem
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ScoreViewModel

@OptIn(ExperimentalMaterialApi::class)
class ScoreActivity : BaseComposeActivity() {
    private val viewModel: ScoreViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BuildContent() {
        val scoreListState by viewModel.scoreListState.collectAsState()

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
                var showGpa by remember { mutableStateOf(false) }
                var showCredit by remember { mutableStateOf(false) }
                var showCourseType by remember { mutableStateOf(true) }
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
                            Text(text = "??????????????????????????????")
                            OutlinedButton(
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    userDialog.value = true
                                }) {
                                val userSelect by viewModel.userSelect.collectAsState()
                                val selected = userSelect.firstOrNull { it.selected }
                                val userString =
                                    selected?.let { "${it.userName}(${it.studentId})" } ?: "?????????"
                                Text(text = "???????????????$userString")
                            }
                            OutlinedButton(
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = XhuColor.Common.grayText),
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    yearDialog.value = true
                                }) {
                                val yearSelect by viewModel.yearSelect.collectAsState()
                                val yearString =
                                    yearSelect.firstOrNull { it.selected }?.let { "${it.year}??????" }
                                        ?: "?????????"
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
                                    termSelect.firstOrNull { it.selected }?.let { "???${it.term}??????" }
                                        ?: "?????????"
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
                                Text(text = "??????")
                            }
                        }
                    }
                ) {
                    SwipeRefresh(
                        modifier = Modifier
                            .padding(paddingValues)
                            .fillMaxSize(),
                        state = rememberSwipeRefreshState(scoreListState.loading),
                        onRefresh = { },
                        swipeEnabled = false,
                    ) {
                        val scoreList = scoreListState.scoreList
                        val failedScoreList = scoreListState.failedScoreList
                        if (scoreListState.loading || scoreList.isNotEmpty() || failedScoreList.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(XhuColor.Common.grayBackground),
                                contentPadding = PaddingValues(4.dp),
                            ) {
                                if (scoreListState.loading) {
                                    stickyHeader {
                                        Text(
                                            text = "??????????????????",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(XhuColor.Common.whiteBackground)
                                                .padding(12.dp),
                                        )
                                    }
                                    items(3) {
                                        BuildItem(
                                            ScoreItem.PLACEHOLDER,
                                            showGpa,
                                            showCredit,
                                            showCourseType,
                                            true,
                                        )
                                    }
                                } else {
                                    if (scoreList.isNotEmpty()) {
                                        stickyHeader {
                                            Text(
                                                text = "??????????????????",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(XhuColor.Common.whiteBackground)
                                                    .padding(12.dp),
                                            )
                                        }
                                    }
                                    items(scoreList.size) { index ->
                                        val item = scoreList[index]
                                        BuildItem(item, showGpa, showCredit, showCourseType)
                                    }
                                }
                                if (scoreListState.loading) {
                                    stickyHeader {
                                        Text(
                                            text = "?????????????????????",
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(XhuColor.Common.whiteBackground)
                                                .padding(12.dp),
                                        )
                                    }
                                    item {
                                        BuildItem(
                                            ScoreItem.PLACEHOLDER,
                                            showGpa,
                                            showCredit,
                                            showCourseType,
                                            true,
                                        )
                                    }
                                } else {
                                    if (failedScoreList.isNotEmpty()) {
                                        stickyHeader {
                                            Text(
                                                text = "?????????????????????",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(XhuColor.Common.whiteBackground)
                                                    .padding(12.dp),
                                            )
                                        }
                                    }
                                    items(failedScoreList.size) { index ->
                                        val item = failedScoreList[index]
                                        BuildItem(item, showGpa, showCredit, showCourseType)
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
                                    showGpa = !showGpa
                                    showOption = false
                                }) {
                                Checkbox(checked = showGpa, onCheckedChange = null)
                                Text(text = "????????????")
                            }
                            Row(modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    showCredit = !showCredit
                                    showOption = false
                                }) {
                                Checkbox(checked = showCredit, onCheckedChange = null)
                                Text(text = "????????????")
                            }
                            Row(modifier = Modifier
                                .padding(8.dp)
                                .clickable {
                                    showCourseType = !showCourseType
                                    showOption = false
                                }) {
                                Checkbox(checked = showCourseType, onCheckedChange = null)
                                Text(text = "??????????????????")
                            }
                        }
                    }
                }
            }
        }
        ShowUserDialog(show = userDialog)
        ShowYearDialog(show = yearDialog)
        ShowTermDialog(show = termDialog)
        if (scoreListState.errorMessage.isNotBlank()) {
            scoreListState.errorMessage.toast(true)
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
                    Text(text = "???????????????????????????")
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
                    Text(text = "???????????????????????????")
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
                    Text(text = "???????????????????????????")
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
    item: ScoreItem,
    showGpa: Boolean,
    showCredit: Boolean,
    showCourseType: Boolean,
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
                Column(modifier = Modifier.weight(1F)) {
                    Text(
                        text = item.courseName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = XhuColor.Common.blackText,
                    )
                    if (showCourseType) {
                        Text(
                            text = item.courseType,
                            color = XhuColor.Common.grayText,
                        )
                    }
                }
                Text(
                    text = buildString {
                        if (showCredit) {
                            append(item.credit)
                            append("/")
                        }
                        if (showGpa) {
                            append(item.gpa)
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