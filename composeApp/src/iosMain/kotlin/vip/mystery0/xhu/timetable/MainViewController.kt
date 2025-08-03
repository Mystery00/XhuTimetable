package vip.mystery0.xhu.timetable

import androidx.compose.ui.window.ComposeUIViewController
import vip.mystery0.xhu.timetable.ui.navigation.RouteInit

fun MainViewController() = ComposeUIViewController {
    App(RouteInit)
}