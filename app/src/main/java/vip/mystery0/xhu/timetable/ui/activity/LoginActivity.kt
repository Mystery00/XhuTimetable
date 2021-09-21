package vip.mystery0.xhu.timetable.ui.activity

import androidx.activity.viewModels
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.flowlayout.FlowRow
import vip.mystery0.xhu.timetable.R
import vip.mystery0.xhu.timetable.appVersionName
import vip.mystery0.xhu.timetable.base.BaseComposeActivity
import vip.mystery0.xhu.timetable.config.DataHolder
import vip.mystery0.xhu.timetable.model.response.Version
import vip.mystery0.xhu.timetable.ui.theme.XhuTimetableTheme
import vip.mystery0.xhu.timetable.viewmodel.LoginViewModel

class LoginActivity : BaseComposeActivity() {
    private val viewModel: LoginViewModel by viewModels()

    private val version: Version
        get() = DataHolder.version!!

    @Composable
    override fun BuildContent() {
        val updateDialog = viewModel.updateDialogState.collectAsState()
        if (updateDialog.value) {
            AlertDialog(onDismissRequest = {
                viewModel.closeUpdateDialog()
            }, title = {
                Text(text = stringResource(R.string.dialog_update_title))
            }, text = {
                Text(
                    text = stringResource(
                        R.string.dialog_update_text,
                        version.versionName,
                        appVersionName,
                        version.updateLog
                    )
                )
            }, confirmButton = {
                FlowRow {
                    TextButton(onClick = {
                        "点击了2按钮".toast()
                        viewModel.closeUpdateDialog()
                    }) {
                        Text(text = stringResource(id = R.string.action_download_patch))
                    }
                    TextButton(onClick = {
                        "点击了1按钮".toast()
                        viewModel.closeUpdateDialog()
                    }) {
                        Text(text = stringResource(id = R.string.action_download_apk))
                    }
                }
            }, dismissButton = {
                TextButton(onClick = {
                    "点击了忽略按钮".toast()
                    viewModel.closeUpdateDialog()
                }) {
                    Text(text = stringResource(id = R.string.action_download_cancel))
                }
            })
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DefaultPreview() {
    XhuTimetableTheme {
        Text("Android")
    }
}