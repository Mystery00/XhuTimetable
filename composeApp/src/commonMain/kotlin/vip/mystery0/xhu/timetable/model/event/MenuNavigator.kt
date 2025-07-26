package vip.mystery0.xhu.timetable.model.event

import androidx.compose.ui.platform.UriHandler
import androidx.navigation.NavController
import coil3.PlatformContext
import vip.mystery0.xhu.timetable.ui.component.showSharePanel
import vip.mystery0.xhu.timetable.ui.navigation.navigateAndSave

data class MenuNavigator(
    val navController: NavController,
    val uriHandler: UriHandler,
    val platformContext: PlatformContext,
) {
    fun navigateTo(route: Any) {
        navController.navigateAndSave(route)
    }

    fun toCustomTabs(url: String) {
        uriHandler.openUri(url)
    }

    fun showSharePanel(shareText: String) {
        showSharePanel(platformContext, shareText)
    }
}
