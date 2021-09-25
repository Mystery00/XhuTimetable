package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.config.SessionManager

val weekCourseTitle: TabTitle = @Composable { viewModel ->
    val week = viewModel.week.collectAsState()
    Text(text = "第${week}周")
}

val weekCourseContent: TabContent = @Composable { viewModel ->
    Row {
        Text(appName)
    }
    Row {
        Text(appVersionName)
    }
    Row {
        Text(SessionManager.mainUser.info.userName)
    }
}