package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LocalContentAlpha
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.model.AcademicReport
import vip.mystery0.xhu.timetable.toCustomTabs
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.AcademicReportViewModel
import java.time.format.DateTimeFormatter

class AcademicReportActivity : BaseComposeActivity() {
    private val viewModel: AcademicReportViewModel by viewModels()

    @Composable
    override fun BuildContent() {
        val list by viewModel.listState.collectAsState()
        var showSearchView by remember { mutableStateOf(false) }
        var searchText by remember { mutableStateOf("") }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = title.toString()) },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = MaterialTheme.colors.onPrimary,
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
                    actions = {
                        if (showSearchView) {
                            BuildSearchText(
                                searchText = searchText,
                                placeholderText = "请输入要搜索的关键词",
                                onSearchTextChanged = {
                                    searchText = it
                                    viewModel.loadList(it)
                                },
                                onClearClick = {
                                    showSearchView = false
                                }
                            )
                        } else {
                            IconButton(onClick = {
                                showSearchView = true
                            }) {
                                Icon(
                                    painter = XhuIcons.Action.search,
                                    contentDescription = null,
                                )
                            }
                        }
                    }
                )
            },
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(XhuColor.Common.grayBackground)
                    .padding(paddingValues),
                contentPadding = PaddingValues(4.dp),
            ) {
                if (list.isNotEmpty()) {
                    items(list.size) { index ->
                        BuildItem(
                            item = list[index],
                            onClick = {
                                toCustomTabs(it.detailUrl)
                            })
                        Divider(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(XhuColor.Common.grayBackground),
                        )
                    }
                }
            }
            if (list.isEmpty()) {
                BuildNoDataLayout()
            }
        }
    }
}

@Composable
private fun BuildItem(
    item: AcademicReport,
    onClick: (AcademicReport) -> Unit
) {
    val date = item.reportTime.toLocalDate()
    Row(
        modifier = Modifier
            .padding(4.dp)
            .height(IntrinsicSize.Min)
            .clickable(onClick = { onClick(item) })
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxHeight(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(64.dp)
                    .background(MaterialTheme.colors.primary)
                    .weight(1F),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = date.dayOfWeek.getDisplayName(
                        java.time.format.TextStyle.SHORT,
                        java.util.Locale.CHINA
                    ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(64.dp)
                    .background(XhuColor.Common.whiteBackground)
                    .weight(1F),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = date.format(DateTimeFormatter.ofPattern("MM月dd日")),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = XhuColor.Common.grayText,
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 72.dp),
        ) {
            Text(
                text = item.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(2.dp),
                maxLines = 2,
            )
            Text(
                text = "地点：${item.location}",
                fontSize = 14.sp,
                color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current * 0.8f),
                modifier = Modifier.padding(2.dp),
                maxLines = 1,
            )
            Text(
                text = "报告人：${item.speaker}",
                fontSize = 14.sp,
                color = LocalContentColor.current.copy(alpha = LocalContentAlpha.current * 0.8f),
                modifier = Modifier.padding(2.dp),
                maxLines = 1,
            )
        }
    }
}