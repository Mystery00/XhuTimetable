package vip.mystery0.xhu.timetable.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.automirrored.twotone.ArrowForwardIos
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import org.koin.compose.viewmodel.koinViewModel
import vip.mystery0.xhu.timetable.config.coroutine.safeLaunch
import vip.mystery0.xhu.timetable.config.trackEvent
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.event.MenuNavigator
import vip.mystery0.xhu.timetable.ui.component.TabContent
import vip.mystery0.xhu.timetable.ui.component.TabTitle
import vip.mystery0.xhu.timetable.ui.navigation.LocalNavController
import vip.mystery0.xhu.timetable.ui.theme.ProfileImages
import vip.mystery0.xhu.timetable.ui.theme.XhuImages
import vip.mystery0.xhu.timetable.viewmodel.MainViewModel
import vip.mystery0.xhu.timetable.viewmodel.PagerProfileViewModel

val profileCourseTitleBar: TabTitle = @Composable {
    Text(text = "我的")
}

val profileCourseContent: TabContent = @Composable {
    val viewModel = koinViewModel<MainViewModel>()
    val profileViewModel = koinViewModel<PagerProfileViewModel>()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState()),
    ) {
        val navController = LocalNavController.current!!
        val uriHandler = LocalUriHandler.current
        val context = LocalPlatformContext.current
        val coroutineScope = rememberCoroutineScope()
        var profileExpanded by remember { mutableStateOf(false) }
        val mainUser by viewModel.mainUser.collectAsState()

        AnimatedContent(
            label = "个人信息动画",
            targetState = profileExpanded,
            transitionSpec = {
                fadeIn() togetherWith fadeOut()
            },
        ) { targetExpanded ->
            Column {
                Row(
                    modifier = Modifier
                        .height(96.dp)
                        .clickable(
                            interactionSource = MutableInteractionSource(),
                            indication = null,
                            onClick = {
                                profileExpanded = if (mainUser == null) false else !targetExpanded
                            }
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val profileImage = mainUser?.let {
                        it.profileImage ?: ProfileImages.hash(
                            it.info.name,
                            it.info.gender == Gender.MALE
                        )
                    } ?: XhuImages.defaultProfileImage
                    Image(
                        painter = profileImage as? Painter
                            ?: rememberAsyncImagePainter(model = profileImage),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                    Column(modifier = Modifier.weight(1F)) {
                        var text = "账号未登录"
                        mainUser?.info?.let {
                            text = if (targetExpanded) {
                                "${it.name}(${it.studentNo})"
                            } else {
                                it.name
                            }
                        }
                        Text(
                            text,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            mainUser?.info?.className ?: "",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (profileExpanded) 90F else 0F,
                        label = "个人信息动画",
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.TwoTone.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(12.dp)
                            .rotate(rotationAngle),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
                if (targetExpanded) {
                    mainUser?.info?.let { userInfo ->
                        Text(
                            buildString {
                                appendLine("性别：${userInfo.gender.showTitle}")
                                appendLine("年级：${userInfo.xhuGrade}")
                                if (userInfo.majorName.isNotBlank()) appendLine("专业：${userInfo.majorName}")
                                if (userInfo.college.isNotBlank()) appendLine("学院：${userInfo.college}")
                                if (userInfo.majorDirection.isNotBlank()) appendLine("专业方向：${userInfo.majorDirection}")
                            },
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 48.dp),
                        )
                    }
                }
            }
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(MaterialTheme.colorScheme.surfaceContainer)
        )

        val menuList by profileViewModel.menu.collectAsState()
        val hasUnReadNotice by profileViewModel.hasUnReadNotice.collectAsState()
        val hasUnReadFeedback by profileViewModel.hasUnReadFeedback.collectAsState()
        menuList.forEach {
            val iterator = it.iterator()
            while (iterator.hasNext()) {
                val menu = iterator.next()
                val item = MenuItem.parseKey(menu.key.uppercase())
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
                    })
                if (iterator.hasNext()) {
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
            }
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )
        }
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
            .height(48.dp)
            .clickable(onClick = click),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .padding(horizontal = 12.dp)
        )
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(1F)
                .padding(vertical = 12.dp),
        )
        if (showBadge) {
            Surface(
                shape = CircleShape,
                modifier = Modifier.size(6.dp),
                color = Color.Red
            ) {}
        }
        Icon(
            imageVector = Icons.AutoMirrored.TwoTone.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 12.dp, start = if (showBadge) 10.dp else 12.dp)
                .size(12.dp),
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}