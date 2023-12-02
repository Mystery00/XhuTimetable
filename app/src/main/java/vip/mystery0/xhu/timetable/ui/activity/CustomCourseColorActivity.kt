package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
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
import com.vanpra.composematerialdialogs.MaterialDialog
import com.vanpra.composematerialdialogs.MaterialDialogState
import com.vanpra.composematerialdialogs.color.ARGBPickerState
import com.vanpra.composematerialdialogs.color.ColorPalette
import com.vanpra.composematerialdialogs.color.colorChooser
import com.vanpra.composematerialdialogs.rememberMaterialDialogState
import com.vanpra.composematerialdialogs.title
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.viewmodel.CustomCourseColorViewModel

class CustomCourseColorActivity : BaseComposeActivity() {
    private val viewModel: CustomCourseColorViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun BuildContent() {
        val list by viewModel.listState.collectAsState()
        val dialogState = rememberMaterialDialogState()
        var courseName by remember { mutableStateOf("") }
        var currentColor by remember { mutableStateOf(Color.Black) }
        var showSearchView by remember { mutableStateOf(false) }
        var searchText by remember { mutableStateOf("") }

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
                            onChangeClick = {
                                courseName = list[index].first
                                currentColor = list[index].second
                                dialogState.show()
                            })
                    }
                }
            }
            if (list.isEmpty()) {
                BuildNoDataLayout()
            }
        }
        BuildColorSelector(
            dialogState = dialogState,
            courseName = courseName,
            currentColor = currentColor,
        )
    }

    @Composable
    private fun BuildColorSelector(
        dialogState: MaterialDialogState,
        courseName: String,
        currentColor: Color,
    ) {
        var selectedColor = currentColor
        MaterialDialog(
            dialogState = dialogState,
            buttons = {
                positiveButton("确定") {
                    viewModel.updateColor(courseName, selectedColor)
                }
                negativeButton("重置为默认") {
                    viewModel.updateColor(courseName, null)
                }
            }) {
            title("请选择需要修改的颜色")
            val colors = ArrayList(ColorPalette.Primary).apply { add(0, currentColor) }
            colorChooser(colors = colors, argbPickerState = ARGBPickerState.WithoutAlphaSelector) {
                selectedColor = it
            }
        }
    }
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