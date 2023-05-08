package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.compose.BackHandler
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreItemResponse
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreResponse
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ExpScoreViewModel

@OptIn(ExperimentalMaterialApi::class)
class ExpScoreActivity : BaseSelectComposeActivity() {
    private val viewModel: ExpScoreViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    override fun BuildContent() {
        val expScoreListState by viewModel.expScoreListState.collectAsState()
        val userSelectStatus = viewModel.userSelect.collectAsState()
        val yearSelectStatus = viewModel.yearSelect.collectAsState()
        val termSelectStatus = viewModel.termSelect.collectAsState()

        val showSelect = rememberModalBottomSheetState(ModalBottomSheetValue.Expanded)
        val scope = rememberCoroutineScope()

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
                    }
                )
            },
        ) { paddingValues ->
            Box {
                ModalBottomSheetLayout(
                    sheetState = showSelect,
                    scrimColor = Color.Black.copy(alpha = 0.32f),
                    sheetContent = {
                        BuildSelectSheetLayout(
                            bottomSheetState = showSelect,
                            selectUserState = userSelectStatus,
                            selectYearState = yearSelectStatus,
                            selectTermState = termSelectStatus,
                            showUserDialog = userDialog,
                            showYearDialog = yearDialog,
                            showTermDialog = termDialog,
                        ) {
                            viewModel.loadExpScoreList()
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
                                    val placeHolder = ExperimentScoreResponse.PLACEHOLDER
                                    stickyHeader {
                                        Row(
                                            modifier = Modifier
                                                .background(XhuColor.Common.whiteBackground)
                                                .padding(12.dp),
                                        ) {
                                            Text(text = placeHolder.courseName)
                                            Spacer(modifier = Modifier.weight(1f))
                                            Text(text = "${placeHolder.totalScore}分")
                                        }
                                    }
                                    items(3) {
                                        BuildItem(
                                            ExperimentScoreItemResponse.PLACEHOLDER,
                                            true,
                                        )
                                    }
                                } else {
                                    scoreList.forEach {
                                        stickyHeader {
                                            Row(
                                                modifier = Modifier
                                                    .background(XhuColor.Common.whiteBackground)
                                                    .padding(12.dp),
                                            ) {
                                                Text(text = it.courseName)
                                                Spacer(modifier = Modifier.weight(1f))
                                                Text(text = "${it.totalScore}分")
                                            }
                                        }
                                        items(it.itemList.size) { index ->
                                            val item = it.itemList[index]
                                            BuildItem(item)
                                        }
                                    }
                                }
                            }
                        } else {
                            BuildNoDataLayout()
                        }
                    }
                }
            }
        }
        ShowUserDialog(selectState = userSelectStatus, show = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        ShowYearDialog(selectState = yearSelectStatus, show = yearDialog, onSelect = {
            viewModel.selectYear(it.value)
        })
        ShowTermDialog(selectState = termSelectStatus, show = termDialog, onSelect = {
            viewModel.selectTerm(it.value)
        })

        if (expScoreListState.errorMessage.isNotBlank()) {
            expScoreListState.errorMessage.toast(true)
        }
        BackHandler(
            enabled = showSelect.isVisible,
            onBack = {
                onBack()
            }
        )
    }

    override fun onStart() {
        super.onStart()
        pushDynamicShortcuts<ExpScoreActivity>(iconResId = R.drawable.ic_exp_score)
    }
}

@Composable
private fun BuildItem(
    item: ExperimentScoreItemResponse,
    placeHolder: Boolean = false,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            ,
        backgroundColor = XhuColor.cardBackground,
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1F)) {
                    Text(
                        text = item.experimentProjectName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = XhuColor.Common.blackText,
                    )
                    Text(
                        text = item.mustTest,
                        color = XhuColor.Common.grayText,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = "实验学分：${item.credit}",
                        color = XhuColor.Common.grayText,
                        fontSize = 14.sp,
                    )
                    if (item.scoreDescription.isNotBlank()) {
                        Text(
                            text = "成绩说明：${item.scoreDescription}",
                            color = XhuColor.Common.grayText,
                            fontSize = 14.sp,
                        )
                    }
                }
                Text(
                    text = "${item.score}",
                    color = XhuColor.Common.blackText,
                    fontSize = 16.sp,
                )
            }
        }
    }
}