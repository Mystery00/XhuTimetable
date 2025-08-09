package vip.mystery0.xhu.timetable

import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController
import vip.mystery0.xhu.timetable.ui.navigation.RouteInit

fun MainViewController(): UIViewController = ComposeUIViewController {
    App(RouteInit)
}