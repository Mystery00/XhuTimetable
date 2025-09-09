package vip.mystery0.xhu.timetable.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import vip.mystery0.xhu.timetable.App
import vip.mystery0.xhu.timetable.ui.navigation.Nav
import vip.mystery0.xhu.timetable.ui.navigation.RouteLogin
import vip.mystery0.xhu.timetable.ui.navigation.RouteMain
import vip.mystery0.xhu.timetable.ui.navigation.RouteQueryExam
import vip.mystery0.xhu.timetable.ui.navigation.RouteSettings

class NavActivity : ComponentActivity() {
    companion object {
        const val EXTRA_INIT_ROUTE = "init_route"

        fun jumpIntent(context: Context, initRoute: String = ""): Intent =
            Intent(context, NavActivity::class.java).apply {
                putExtra(EXTRA_INIT_ROUTE, initRoute)
            }

        fun Activity.jumpToNav(initRoute: InitRoute? = null) {
            startActivity(jumpIntent(this, initRoute?.name ?: ""))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        FileKit.init(this)
        val initRoute = intent.getStringExtra(EXTRA_INIT_ROUTE)
        setContent {
            if (initRoute.isNullOrBlank()) {
                App(RouteMain)
            } else {
                val route = InitRoute.valueOf(initRoute.uppercase())
                App(route.route)
            }
        }
    }

    enum class InitRoute(val route: Nav) {
        LOGIN(RouteLogin(false)),
        EXAM(RouteQueryExam),
        SETTINGS(RouteSettings),
    }
}