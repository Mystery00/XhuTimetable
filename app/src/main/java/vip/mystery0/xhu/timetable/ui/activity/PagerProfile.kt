package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import vip.mystery0.xhu.timetable.model.Gender
import vip.mystery0.xhu.timetable.model.event.MenuItem
import vip.mystery0.xhu.timetable.trackEvent
import vip.mystery0.xhu.timetable.ui.theme.MaterialIcons
import vip.mystery0.xhu.timetable.ui.theme.ProfileImages
import vip.mystery0.xhu.timetable.ui.theme.XhuColor
import vip.mystery0.xhu.timetable.ui.theme.XhuImages

val profileCourseTitle: TabTitle = @Composable {
    Text(text = "我的", modifier = Modifier.align(Alignment.Center))
}

@OptIn(ExperimentalAnimationApi::class)
val profileCourseContent: TabContent = @Composable { ext ->
    val activity = ext.activity
    val viewModel = ext.viewModel
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .verticalScroll(rememberScrollState()),
    ) {
        val coroutineScope = rememberCoroutineScope()
        var profileExpanded by remember { mutableStateOf(true) }
        val mainUser by viewModel.mainUser.collectAsState()
        AnimatedContent(
            label = "个人信息动画",
            targetState = profileExpanded,
            transitionSpec = {
                fadeIn() with fadeOut()
            },
        ) { targetExpanded ->
            Column {
                Row(
                    modifier = Modifier
                        .height(96.dp)
                        .clickable {
                            profileExpanded = if (mainUser == null) false else !targetExpanded
                        },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val profileImage = mainUser?.let {
                        it.profileImage ?: ProfileImages.hash(
                            it.info.name,
                            it.info.gender == Gender.MALE
                        )
                    } ?: XhuImages.defaultProfileImage
                    Image(
                        painter = if (profileImage is Painter)
                            profileImage
                        else
                            rememberAsyncImagePainter(model = profileImage),
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(60.dp)
                            .clip(CircleShape)
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
                            color = MaterialTheme.colors.onBackground,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(mainUser?.info?.className ?: "", fontSize = 14.sp, color = Color.Gray)
                    }
                    val rotationAngle by animateFloatAsState(
                        targetValue = if (profileExpanded) 90F else 0F,
                        label = "个人信息动画",
                    )
                    Icon(
                        imageVector = MaterialIcons.TwoTone.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(12.dp)
                            .rotate(rotationAngle),
                        tint = more,
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
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 48.dp),
                        )
                    }
                }
            }
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(divider),
        )

        val menuList by viewModel.menu.collectAsState()
        val hasUnReadNotice by viewModel.hasUnReadNotice.collectAsState()
        val hasUnReadFeedback by viewModel.hasUnReadFeedback.collectAsState()
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
                        coroutineScope.launch {
                            trackEvent("点击菜单 ${menu.key}")
                            item.action(activity, menu)
                        }
                    })
                if (iterator.hasNext()) {
                    Divider(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(0.33.dp)
                            .background(dividerSmall),
                    )
                }
            }
            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(divider),
            )
        }
    }
}

private val divider: Color
    @Composable
    get() = XhuColor.Profile.divider
private val dividerSmall: Color
    @Composable
    get() = XhuColor.Profile.dividerSmall
private val more: Color
    @Composable
    get() = XhuColor.Profile.more

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
            color = MaterialTheme.colors.onBackground,
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
            imageVector = MaterialIcons.TwoTone.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 12.dp, start = if (showBadge) 10.dp else 12.dp)
                .size(12.dp),
            tint = more,
        )
    }
}