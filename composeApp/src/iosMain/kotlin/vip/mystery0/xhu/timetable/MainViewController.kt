package vip.mystery0.xhu.timetable

import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import platform.UIKit.UIStatusBarStyle
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import vip.mystery0.xhu.timetable.ui.navigation.RouteInit
import vip.mystery0.xhu.timetable.ui.theme.NightMode
import vip.mystery0.xhu.timetable.ui.theme.SystemAppearanceManager
import vip.mystery0.xhu.timetable.ui.theme.Theme

fun MainViewController(): UIViewController = CustomComposeUIViewController()

private class CustomComposeUIViewController : UIViewController(
    nibName = null,
    bundle = null,
) {
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private var statusBarStyle: UIStatusBarStyle = UIStatusBarStyleLightContent

    private var composeView: UIView? = null

    @OptIn(ExperimentalForeignApi::class)
    override fun viewDidLoad() {
        super.viewDidLoad()

        val composeViewController = ComposeUIViewController {
            App(RouteInit)
        }
        addChildViewController(composeViewController)
        view.addSubview(composeViewController.view)
        composeViewController.view.setFrame(view.bounds())
        composeViewController.didMoveToParentViewController(this)
        this.composeView = composeViewController.view

        coroutineScope.launch {
            SystemAppearanceManager.isDarkState.collectLatest { isDark ->
                statusBarStyle = if (isDark) {
                    UIStatusBarStyleDarkContent
                } else {
                    UIStatusBarStyleLightContent
                }
                setNeedsStatusBarAppearanceUpdate()
            }
        }
    }

    override fun preferredStatusBarStyle(): UIStatusBarStyle = statusBarStyle

    override fun viewDidDisappear(animated: Boolean) {
        super.viewDidDisappear(animated)
        coroutineScope.cancel()
    }
}