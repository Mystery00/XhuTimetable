package vip.mystery0.xhu.timetable.ui.activity

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import vip.mystery0.xhu.timetable.appName
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.SessionManager
import vip.mystery0.xhu.timetable.module.getRepo
import vip.mystery0.xhu.timetable.publicDeviceId
import vip.mystery0.xhu.timetable.repository.StartRepo
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme

class MainActivity : BaseComposeActivity() {
    companion object {
        private const val TAG = "MainActivity"
    }

    private val startRepo: StartRepo
        get() = getRepo()

    @Composable
    override fun BuildContent() {
        Column {
            Row {
                Greeting(publicDeviceId)
            }
            Row {
                Greeting(appName)
            }
            Row {
                Greeting(appVersionName)
            }
            Row {
                Greeting(SessionManager.mainUser.toString())
            }
            Row {
                Button(onClick = {
                    Log.i(TAG, "BuildContent: ${startRepo.javaClass.name}")
                    startRepo.javaClass.name.toast(true)
                }) {
                    Text(text = "测试")
                }
            }
        }
    }
}

@Composable
private fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    XhuTimetableTheme {
        Greeting("Android")
    }
}