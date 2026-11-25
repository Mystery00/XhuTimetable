package vip.mystery0.xhu.timetable.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import org.koin.core.component.KoinComponent
import vip.mystery0.xhu.timetable.ui.screen.SplashImageScreen
import vip.mystery0.xhu.timetable.ui.theme.SetSystemAppearance
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import vip.mystery0.xhu.timetable.ui.theme.applyInitialImmersiveEdgeToEdge
import vip.mystery0.xhu.timetable.ui.theme.isDarkMode

class SplashImageActivity : ComponentActivity(), KoinComponent {
    companion object {
        private const val INTENT_SPLASH_FILE_PATH = "splashFilePath"
        private const val INTENT_SPLASH_ID = "splashId"

        fun setParams(
            intent: Intent,
            splashFilePath: String,
            splashId: Long,
        ) {
            intent.putExtra(INTENT_SPLASH_FILE_PATH, splashFilePath)
            intent.putExtra(INTENT_SPLASH_ID, splashId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applyInitialImmersiveEdgeToEdge()
        super.onCreate(savedInstanceState)
        super.onNewIntent(intent)
        val splashFilePath = intent.getStringExtra(INTENT_SPLASH_FILE_PATH)
        val splashId = intent.getLongExtra(INTENT_SPLASH_ID, -1)
        setContent {
            XhuTimetableTheme {
                SetSystemAppearance(isDark = isDarkMode())
                SplashImageScreen(splashFilePath, splashId) {
                    startActivity(Intent(this, NavActivity::class.java))
                    finish()
                }
            }
        }
    }
}