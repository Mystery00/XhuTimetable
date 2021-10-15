package vip.mystery0.xhu.timetable.ui.activity

import android.content.Intent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import vip.mystery0.xhu.timetable.ui.theme.ProfileImages
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons
import vip.mystery0.xhu.timetable.ui.theme.XhuImages

val profileCourseTitle: TabTitle = @Composable {
    Text(text = "我的", modifier = Modifier.align(Alignment.Center))
}

val profileCourseContent: TabContent = @Composable { ext ->
    val activity = ext.activity
    val viewModel = ext.viewModel
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .verticalScroll(rememberScrollState()),
    ) {
        Row(
            modifier = Modifier
                .height(96.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val mainUser by viewModel.mainUser.collectAsState()
            val profileImage = mainUser?.let {
                it.profileImage ?: ProfileImages.hash(it.info.userName, it.info.sex == "男")
            } ?: XhuImages.defaultProfileImage
            Image(
                painter = if (profileImage is Painter) profileImage else rememberImagePainter(data = profileImage),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(60.dp),
            )
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    mainUser?.info?.userName ?: "账号未登录",
                    fontSize = 17.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                )
                Text(mainUser?.info?.className ?: "", fontSize = 14.sp, color = Color.Gray)
            }
            Icon(
                imageVector = Icons.TwoTone.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(12.dp),
                tint = more,
            )
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
        BuildProfileItem(
            painter = XhuIcons.Profile.feedback,
            title = "意见反馈",
            click = {
                activity.toastString("暂不支持")
            }
        )
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

private val divider = Color(0xFFf0f0f0)
private val dividerSmall = Color(0xFFeaeaea)
private val more = Color(0xFF979797)

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
            color = Color.Black,
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
            imageVector = Icons.TwoTone.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier
                .padding(end = 12.dp, start = if (showBadge) 10.dp else 12.dp)
                .size(12.dp),
            tint = more,
        )
    }
}