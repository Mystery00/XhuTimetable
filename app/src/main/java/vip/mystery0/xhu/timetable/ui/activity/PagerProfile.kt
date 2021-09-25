package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import vip.mystery0.xhu.timetable.config.SessionManager

val profileCourseTitle: TabTitle = @Composable {
    Text(text = "我的")
}

val profileCourseContent: TabContent = @Composable { viewModel ->
    Row {
        Text(SessionManager.mainUser.info.userName)
    }
}