package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import vip.mystery0.xhu.timetable.config.SessionManager

val profileCourseTitle: TabTitle = @Composable {
    Text(text = "我的", modifier = Modifier.align(Alignment.Center))
}

val profileCourseContent: TabContent = @Composable { viewModel ->
    Row {
        Text(SessionManager.mainUser.info.userName)
    }
}