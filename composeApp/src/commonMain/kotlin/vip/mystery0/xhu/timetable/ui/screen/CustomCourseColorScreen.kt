package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.maxkeppeker.sheets.core.models.base.Header
import com.maxkeppeker.sheets.core.models.base.SelectionButton
import com.maxkeppeker.sheets.core.models.base.UseCaseState
import com.maxkeppeker.sheets.core.models.base.rememberUseCaseState
import com.maxkeppeler.sheets.color.ColorDialog
import com.maxkeppeler.sheets.color.models.ColorConfig
import com.maxkeppeler.sheets.color.models.ColorSelection
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.ui.component.BuildSearchText
import vip.mystery0.xhu.timetable.ui.component.StateScreen
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.ColorPool
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.CustomCourseColorViewModel
import xhutimetable.composeapp.generated.resources.Res
import xhutimetable.composeapp.generated.resources.state_no_data

@Composable
fun CustomCourseColorScreen() {
    val viewModel = koinViewModel<CustomCourseColorViewModel>()

    val navController = LocalNavController.current!!

    val list by viewModel.listState.collectAsState()

    val showColorDialog = rememberUseCaseState()

    var courseName by remember { mutableStateOf("") }
    var showSearchView by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.init()
    }
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = "自定义课程颜色") },
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
                    if (showSearchView) {
                        BuildSearchText(
                            searchText = searchText,
                            placeholderText = "请输入课程名称",
                            onSearchTextChanged = {
                                searchText = it
                                viewModel.loadList(it)
                            },
                            onClearClick = {
                                searchText = ""
                                viewModel.loadList("")
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
                .padding(paddingValues),
            contentPadding = PaddingValues(vertical = 4.dp),
        ) {
            if (list.isNotEmpty()) {
                items(list.size) { index ->
                    BuildItem(
                        item = list[index],
                        onChangeClick = {
                            courseName = list[index].first
                            showColorDialog.show()
                        })
                }
            }
        }
        if (list.isEmpty()) {
            StateScreen(
                title = "暂无数据",
                imageRes = painterResource(Res.drawable.state_no_data),
            )
        }
    }
    BuildColorSelector(
        useCaseState = showColorDialog,
        onSetDefault = {
            viewModel.updateColor(courseName, null)
        },
        onSetColor = {
            viewModel.updateColor(courseName, it)
        }
    )
}

@Composable
private fun BuildColorSelector(
    useCaseState: UseCaseState,
    onSetDefault: () -> Unit,
    onSetColor: (Color) -> Unit,
) {
    ColorDialog(
        header = Header.Default(
            title = "请选择需要修改的颜色",
        ),
        state = useCaseState,
        selection = ColorSelection(
            extraButton = SelectionButton(text = "重置为默认"),
            onExtraButtonClick = {
                useCaseState.hide()
                onSetDefault()
            },
            onSelectColor = {
                onSetColor(Color(it))
            }
        ),
        config = ColorConfig(
            templateColors = ColorPool.templateColors,
            allowCustomColorAlphaValues = false,
        )
    )
}

@Composable
private fun BuildItem(
    item: Pair<String, Color>,
    onChangeClick: (Pair<String, Color>) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = XhuColor.cardBackground,
        ),
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                shape = CircleShape,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .size(36.dp),
                color = item.second
            ) {}
            Text(
                text = item.first,
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .weight(1F),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            )
            TextButton(onClick = {
                onChangeClick(item)
            }) {
                Text(
                    text = "修改"
                )
            }
        }
    }
}