package vip.mystery0.xhu.timetable.ui.activity.contract

import android.content.Context
import android.content.Intent
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.activity.result.contract.ActivityResultContract
import java.io.File

class FontFileResultContract : ActivityResultContract<String, Intent?>() {
    override fun createIntent(context: Context, input: String): Intent {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.setDataAndType(null, "*/*")
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Intent? = intent
}