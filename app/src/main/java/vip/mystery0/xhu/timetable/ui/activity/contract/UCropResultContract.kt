package vip.mystery0.xhu.timetable.ui.activity.contract

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
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
        val outputUri = intent?.getParcelableExtra(UCrop.EXTRA_OUTPUT_URI, Uri::class.java)
        return outputUri?.toFile()
    }
}