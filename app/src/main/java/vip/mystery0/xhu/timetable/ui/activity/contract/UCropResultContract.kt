package vip.mystery0.xhu.timetable.ui.activity.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.net.toFile
import com.yalantis.ucrop.UCrop
import java.io.File

class UCropResultContract : ActivityResultContract<UCrop, File?>() {
    override fun createIntent(context: Context, input: UCrop): Intent = input.getIntent(context)

    override fun parseResult(resultCode: Int, intent: Intent?): File? {
        if (resultCode != Activity.RESULT_OK) {
            return null
        }
        @Suppress("DEPRECATION")
        val outputUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent?.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI, Uri::class.java)
        } else {
            intent?.getParcelableExtra<Uri>(UCrop.EXTRA_OUTPUT_URI)
        }
        return outputUri?.toFile()
    }
}