package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import kotlinx.coroutines.Dispatchers
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.store.Menu
import vip.mystery0.xhu.timetable.config.trackEvent
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.event.MenuNavigator
import vip.mystery0.xhu.timetable.ui.component.TabAction
import vip.mystery0.xhu.timetable.ui.component.TabContent
import vip.mystery0.xhu.timetable.ui.component.TabTitle
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.navigation.RouteAccountManagement
import vip.mystery0.xhu.timetable.ui.navigation.navigateAndSave
import vip.mystery0.xhu.timetable.ui.theme.UserAvatarImage
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerProfileViewModel

private val QUICK_TOOL_ITEMS = setOf(
    MenuItem.QUERY_SCORE,
    MenuItem.QUERY_EXAM,
    MenuItem.EXP_SCORE,
    MenuItem.QUERY_FREE_ROOM,
    MenuItem.QUERY_TIMETABLE,
)

val profileCourseTitleBar: TabTitle = @Composable {
    Text(
        text = "我的",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
    )
}

// 我的页面不显示课表刷新按钮
val profileCourseActions: TabAction = @Composable {
    false
}

val profileCourseContent: TabContent = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val profileViewModel = koinViewModel<PagerProfileViewModel>()

    val navController = LocalNavController.current
    val uriHandler = LocalUriHandler.current
    val context = LocalPlatformContext.current
    val coroutineScope = rememberCoroutineScope()

    var profileExpanded by remember { mutableStateOf(false) }
    val mainUser by viewModel.mainUser.collectAsState()
    val menuList by profileViewModel.menu.collectAsState()
    val hasUnReadNotice by profileViewModel.hasUnReadNotice.collectAsState()
    val hasUnReadFeedback by profileViewModel.hasUnReadFeedback.collectAsState()

    val (quickTools, groupedMenus) = remember(menuList) {
        val quick = mutableListOf<Pair<Menu, MenuItem>>()
        val remainingGroups = mutableListOf<List<Pair<Menu, MenuItem>>>()
        menuList.forEach { group ->
            val rest = mutableListOf<Pair<Menu, MenuItem>>()
            group.forEach { menu ->
                val item = MenuItem.parseKey(menu.key.uppercase())
                if (item in QUICK_TOOL_ITEMS) {
                    quick.add(menu to item)
                } else {
                    rest.add(menu to item)
                }
            }
            if (rest.isNotEmpty()) {
                remainingGroups.add(rest)
            }
        }
        quick to remainingGroups
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        // 个人学籍名片卡片
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (mainUser != null) profileExpanded = !profileExpanded
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    UserAvatarImage(
                        userName = mainUser?.info?.name,
                        isMale = mainUser?.info?.gender == Gender.MALE,
                        contentDescription = null,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(3.dp),
                    ) {
                        Text(
                            text = mainUser?.info?.name ?: "账号未登录",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        mainUser?.info?.let { info ->
                            Text(
                                text = info.className.ifBlank { info.studentNo },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                            ) {
                                Text(
                                    text = "学号 ${info.studentNo}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                )
                            }
                        }
                    }
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (profileExpanded) 180f else 0f,
                        label = "profileExpandRotate",
                    )
                    Icon(
                        imageVector = Icons.Rounded.ExpandMore,
                        contentDescription = if (profileExpanded) "收起详情" else "展开详情",
                        modifier = Modifier
                            .size(24.dp)
                            .rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                AnimatedVisibility(
                    visible = profileExpanded && mainUser != null,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    mainUser?.info?.let { userInfo ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                            )
                            ProfileInfoGrid(
                                items = buildList {
                                    add("性别" to userInfo.gender.showTitle)
                                    add("年级" to "${userInfo.xhuGrade}级")
                                    if (userInfo.college.isNotBlank()) add("学院" to userInfo.college)
                                    if (userInfo.majorName.isNotBlank()) add("专业" to userInfo.majorName)
                                    if (userInfo.majorDirection.isNotBlank()) add("方向" to userInfo.majorDirection)
                                }
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                            ) {
                                TextButton(
                                    onClick = { navController.navigateAndSave(RouteAccountManagement) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ManageAccounts,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text("切换或管理账号")
                                }
                            }
                        }
                    }
                }
            }
        }

        // 高频教务工具金刚区宫格卡片
        if (quickTools.isNotEmpty()) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Text(
                        text = "常用查询",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 6.dp),
                    )
                    val rows = quickTools.chunked(if (quickTools.size == 4) 4 else 5.coerceAtMost(quickTools.size))
                    rows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                        ) {
                            rowItems.forEach { (menu, item) ->
                                QuickToolCell(
                                    painter = item.icon(),
                                    title = menu.title,
                                    modifier = Modifier.weight(1f),
                                    onClick = {
                                        coroutineScope.safeLaunch(Dispatchers.Main) {
                                            trackEvent("点击菜单 ${menu.key}")
                                            item.action(MenuNavigator(navController, uriHandler, context), menu)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 其余功能按原有分组渲染为 M3 圆角分组卡片
        groupedMenus.forEach { group ->
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    group.forEachIndexed { index, (menu, item) ->
                        val showBadge = when (item) {
                            MenuItem.NOTICE -> hasUnReadNotice
                            MenuItem.FEEDBACK -> hasUnReadFeedback
                            else -> false
                        }
                        BuildProfileItem(
                            painter = item.icon(),
                            title = menu.title,
                            showBadge = showBadge,
                            click = {
                                coroutineScope.safeLaunch(Dispatchers.Main) {
                                    trackEvent("点击菜单 ${menu.key}")
                                    item.action(MenuNavigator(navController, uriHandler, context), menu)
                                }
                            }
                        )
                        if (index < group.lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.padding(start = 56.dp, end = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProfileInfoGrid(items: List<Pair<String, String>>) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                row.forEach { (label, value) ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = value,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                if (row.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun QuickToolCell(
    painter: Painter,
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            modifier = Modifier.size(44.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    painter = painter,
                    contentDescription = title,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun BuildProfileItem(
    painter: Painter,
    title: String,
    showBadge: Boolean = false,
    click: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = click)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        if (showBadge) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(8.dp),
            ) {}
        }
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
        )
    }
}
