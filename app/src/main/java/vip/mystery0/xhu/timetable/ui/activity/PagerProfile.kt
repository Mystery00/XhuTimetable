package vip.mystery0.xhu.timetable.ui.activity

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import vip.mystery0.xhu.timetable.ui.theme.*

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
        var profileExpanded by remember { mutableStateOf(false) }
        val mainUser by viewModel.mainUser.collectAsState()
        AnimatedContent(
            targetState = profileExpanded,
            transitionSpec = {
                fadeIn() with fadeOut()
            }
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
                        it.profileImage ?: ProfileImages.hash(it.info.userName, it.info.sex == "男")
                    } ?: XhuImages.defaultProfileImage
                    Image(
                        painter = if (profileImage is Painter) profileImage else rememberImagePainter(
                            data = profileImage
                        ),
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
                                "${it.userName}(${it.studentId})"
                            } else {
                                it.userName
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
                    Icon(
                        imageVector = MaterialIcons.TwoTone.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .size(12.dp),
                        tint = more,
                    )
                }
                if (targetExpanded) {
                    mainUser?.info?.let { userInfo ->
                        Text(
                            buildString {
                                if (userInfo.sex.isNotBlank()) appendLine("性别：${userInfo.sex}")
                                if (userInfo.grade.isNotBlank()) appendLine("年级：${userInfo.grade}")
                                if (userInfo.profession.isNotBlank()) appendLine("专业：${userInfo.profession}")
                                if (userInfo.institute.isNotBlank()) appendLine("学院：${userInfo.institute}")
                                if (userInfo.direction.isNotBlank()) appendLine("专业方向：${userInfo.direction}")
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
        val dividerHeight = 0.33.dp
        BuildProfileItem(
            painter = XhuIcons.Profile.exam,
            title = "考试查询",
            click = {
                activity.intentTo(ExamActivity::class)
            })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(
            painter = XhuIcons.Profile.score,
            title = "成绩查询",
            click = {
                activity.intentTo(ScoreActivity::class)
            })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(
            painter = XhuIcons.Profile.classroom,
            title = "空闲教室",
            click = {
                activity.toastString("暂不支持")
            })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(divider),
        )
        BuildProfileItem(
            painter = XhuIcons.Profile.accountSettings,
            title = "账号管理",
            click = {
                activity.intentTo(AccountSettingsActivity::class)
            })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(
            painter = XhuIcons.Profile.classSettings,
            title = "课程设置",
            click = {
                activity.intentTo(ClassSettingsActivity::class)
            })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(
            painter = XhuIcons.Profile.settings,
            title = "软件设置",
            click = {
                activity.intentTo(SettingsActivity::class)
            })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(divider),
        )
        val hasUnReadNotice by viewModel.hasUnReadNotice.collectAsState()
        BuildProfileItem(
            painter = XhuIcons.Profile.notice,
            title = "通知公告",
            showBadge = hasUnReadNotice,
            click = {
                activity.intentTo(NoticeActivity::class)
            }
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        val hasUnReadFeedback by viewModel.hasUnReadFeedback.collectAsState()
        BuildProfileItem(
            painter = XhuIcons.Profile.feedback,
            title = "意见反馈",
            showBadge = hasUnReadFeedback,
            click = {
                activity.intentTo(FeedbackActivity::class)
            })
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(
            painter = XhuIcons.Profile.share,
            title = "分享应用",
            click = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, shareText.random())
                    type = "text/plain"
                }
                activity.startActivity(Intent.createChooser(shareIntent, "分享西瓜课表到"))
            },
        )
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(divider),
        )
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

private val shareText = arrayListOf(
    "查课查课表，我就用西瓜课表~ 下载链接：https://xgkb.mystery0.vip",
    "西瓜子都在用的课表~ 下载链接：https://xgkb.mystery0.vip"
)

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