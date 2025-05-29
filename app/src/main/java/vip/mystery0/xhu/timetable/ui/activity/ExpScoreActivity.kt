package vip.mystery0.xhu.timetable.ui.activity

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BaseSelectComposeActivity
import vip.mystery0.xhu.timetable.model.response.ExperimentScoreItemResponse
import vip.mystery0.xhu.timetable.ui.component.rememberXhuDialogState
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.ExpScoreViewModel

class ExpScoreActivity : BaseSelectComposeActivity() {
    private val viewModel: ExpScoreViewModel by viewModels()

    @OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val expScoreListState by viewModel.expScoreListState.collectAsState()
        val userSelect by viewModel.userSelect.collectAsState()
        val yearSelect by viewModel.yearSelect.collectAsState()
        val termSelect by viewModel.termSelect.collectAsState()

        val userDialog = rememberXhuDialogState()
        val yearDialog = rememberXhuDialogState()
        val termDialog = rememberXhuDialogState()

        LaunchedEffect(Unit) {
            viewModel.init()
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    navigationIcon = {
                        IconButton(onClick = {
                            finish()
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
                                    BuildNoDataLayout()
                                }
                            }
                        }
                    }
                }
            }
        }
        ShowUserDialog(selectList = userSelect, show = userDialog, onSelect = {
            viewModel.selectUser(it.studentId)
        })
        ShowYearDialog(selectList = yearSelect, show = yearDialog, onSelect = {
            viewModel.selectYear(it.value)
        })
        ShowTermDialog(selectList = termSelect, show = termDialog, onSelect = {
            viewModel.selectTerm(it.value)
        })

        HandleErrorMessage(errorMessage = expScoreListState.errorMessage) {
            viewModel.clearErrorMessage()
        }
    }

    override fun onStart() {
        super.onStart()
        pushDynamicShortcuts<ExpScoreActivity>(iconResId = R.drawable.ic_exp_score)
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