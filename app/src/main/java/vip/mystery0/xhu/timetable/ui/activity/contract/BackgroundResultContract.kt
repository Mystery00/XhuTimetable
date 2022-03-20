package vip.mystery0.xhu.timetable.ui.activity.contract

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract

class BackgroundResultContract : ActivityResultContract<String, Intent?>() {
    override fun createIntent(context: Context, input: String): Intent {
        val intent = Intent(Intent.ACTION_PICK)
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? = intent
}