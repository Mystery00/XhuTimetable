package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberImagePainter
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.ui.theme.XhuIcons

val profileCourseTitle: TabTitle = @Composable {
    Text(text = "我的", modifier = Modifier.align(Alignment.Center))
}

val profileCourseContent: TabContent = @Composable { viewModel ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(0.33.dp)
                .background(dividerSmall),
        )
        Row(
            modifier = Modifier
                .height(96.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                rememberImagePainter(R.mipmap.ic_launcher),
                contentDescription = null,
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .size(60.dp),
            )
            Column(modifier = Modifier.weight(1F)) {
                Text(
                    SessionManager.mainUser.info.userName,
                    fontSize = 17.sp,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                )
                Text(SessionManager.mainUser.info.className, fontSize = 14.sp, color = Color.Gray)
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
        BuildProfileItem(painter = XhuIcons.Profile.exam, title = "考试查询")
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(painter = XhuIcons.Profile.score, title = "成绩查询")
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(painter = XhuIcons.Profile.classroom, title = "空闲教室")
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(divider),
        )
        BuildProfileItem(painter = XhuIcons.Profile.accountSettings, title = "账号管理")
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(painter = XhuIcons.Profile.classSettings, title = "课程设置")
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(painter = XhuIcons.Profile.settings, title = "软件设置")
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(divider),
        )
        BuildProfileItem(painter = XhuIcons.Profile.notice, title = "通知公告")
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .height(dividerHeight)
                .background(dividerSmall),
        )
        BuildProfileItem(painter = XhuIcons.Profile.share, title = "分享应用")
    }
}

private val divider = Color(0xFFf0f0f0)
private val dividerSmall = Color(0xFFeaeaea)
private val more = Color(0xFF979797)

@Composable
private fun BuildProfileItem(painter: Painter, title: String, click: () -> Unit = {}) {
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
                .size(24.dp)
        )
        Text(
            text = title,
            color = Color.Black,
            fontSize = 14.sp,
            modifier = Modifier
                .weight(1F)
                .padding(vertical = 12.dp),
        )
        Icon(
            imageVector = Icons.TwoTone.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(12.dp),
            tint = more,
        )
    }
}