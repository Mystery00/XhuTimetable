package vip.mystery0.xhu.timetable.ui.activity

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.publicDeviceId
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme

class MainActivity : BaseComposeActivity() {
    @Composable
    override fun BuildContent() {
        Column {
            Row {
                Greeting(publicDeviceId)
            }
            Row {
                Greeting(R.string.app_name.asString())
            }
            Row {
                Greeting(R.string.app_version_name.asString())
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
fun DefaultPreview() {
    XhuTimetableTheme {
        Greeting("Android")
    }
}