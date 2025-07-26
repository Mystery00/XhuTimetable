package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.base.HandleErrorMessage
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreItemResponse
import vip.mystery0.xhu.timetable.ui.component.BuildSelectFilterChipContent
import vip.mystery0.xhu.timetable.ui.component.ShowTermDialog
import vip.mystery0.xhu.timetable.ui.component.ShowUserDialog
import vip.mystery0.xhu.timetable.ui.component.ShowYearDialog
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ExpScoreViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun QueryExpScoreScreen() {
    val viewModel = koinViewModel<ExpScoreViewModel>()

    val navController = LocalNavController.current!!

    val expScoreListState by viewModel.expScoreListState.collectAsState()

    val userSelect by viewModel.userSelect.select.collectAsState()
    val yearSelect by viewModel.yearSelect.select.collectAsState()
    val termSelect by viewModel.termSelect.select.collectAsState()

    val userDialog by viewModel.userSelect.selectDialog.collectAsState()
    val yearDialog by viewModel.yearSelect.selectDialog.collectAsState()
    val termDialog by viewModel.termSelect.selectDialog.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.init()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "实验成绩查询") },
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
            )
        },
    ) { paddingValues ->
        Box {
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize(),
            ) {
                PullToRefreshBox(
                    isRefreshing = expScoreListState.loading,
                    onRefresh = {
                        viewModel.loadExpScoreList()
                    },
                ) {
                    val scoreList = expScoreListState.scoreList
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        stickyHeader {
                            BuildSelectFilterChipContent(
                                userSelect = userSelect,
                                yearSelect = yearSelect,
                                termSelect = termSelect,
                                showUserDialog = userDialog,
                                showYearDialog = yearDialog,
                                showTermDialog = termDialog,
                                onDataLoad = {
                                    viewModel.loadExpScoreList()
                                }
                            )
                        }
                        if (expScoreListState.loading || scoreList.isNotEmpty()) {
                            scoreList.forEach { score ->
                                stickyHeader {
                                    Row(
                                        modifier = Modifier
                                            .background(MaterialTheme.colorScheme.surfaceContainer)
                                            .padding(12.dp),
                                    ) {
                                        Text(text = score.courseName)
                                        Spacer(modifier = Modifier.weight(1f))
                                        Text(text = "${score.totalScore}分")
                                    }
                                }
                                items(score.itemList.size) { index ->
                                    val item = score.itemList[index]
                                    BuildItem(item)
                                }
                            }
                        } else {
                            item {
                                StateScreen(
                                    title = "暂无考试",
                                    buttonText = "再查一次",
                                    imageRes = painterResource(Res.drawable.state_no_data),
                                    verticalArrangement = Arrangement.Top,
                                    onButtonClick = {
                                        viewModel.loadExpScoreList()
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    ShowUserDialog(selectList = userSelect, useCaseState = userDialog, onSelect = {
        viewModel.selectUser(it.studentId)
    })
    ShowYearDialog(selectList = yearSelect, useCaseState = yearDialog, onSelect = {
        viewModel.selectYear(it.value)
    })
    ShowTermDialog(selectList = termSelect, useCaseState = termDialog, onSelect = {
        viewModel.selectTerm(it.value)
    })

    HandleErrorMessage(errorMessage = expScoreListState.errorMessage) {
        viewModel.clearErrorMessage()
    }
}

@Composable
private fun BuildItem(item: ExperimentScoreItemResponse) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
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
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = item.mustTest,
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp,
                    )
                    Text(
                        text = "实验学分：${item.credit}",
                        color = MaterialTheme.colorScheme.outline,
                        fontSize = 14.sp,
                    )
                    if (item.scoreDescription.isNotBlank()) {
                        Text(
                            text = "成绩说明：${item.scoreDescription}",
                            color = MaterialTheme.colorScheme.outline,
                            fontSize = 14.sp,
                        )
                    }
                }
                Text(
                    text = "${item.score}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                )
            }
        }
    }
}