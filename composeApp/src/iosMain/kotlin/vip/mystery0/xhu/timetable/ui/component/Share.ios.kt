package vip.mystery0.xhu.timetable.ui.component

import coil3.PlatformContext
import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.UIKit.popoverPresentationController
import vip.mystery0.xhu.timetable.config.toast.showLongToast

actual fun showSharePanel(context: PlatformContext, shareText: String) {
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    if (rootViewController != null) {
        val activityItems = listOf(shareText)
        val activityViewController = UIActivityViewController(
            activityItems = activityItems,
            applicationActivities = null,
        )
        activityViewController.popoverPresentationController?.sourceView = rootViewController.view
        rootViewController.presentViewController(
            activityViewController,
            animated = true,
            completion = null
        )
    } else {
        showLongToast("could not find root view controller to present share sheet")
    }
}