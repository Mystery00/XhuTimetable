package vip.mystery0.xhu.timetable.config

import android.os.Environment
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import vip.mystery0.xhu.timetable.context
import java.io.File

actual val externalPictureDir: PlatformFile
    get() = PlatformFile(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!)

actual val externalDownloadDir: PlatformFile
    get() = PlatformFile(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)!!)

actual val externalCacheDownloadDir: PlatformFile
    get() = PlatformFile(File(context.externalCacheDir, "update"))

actual val externalDocumentsDir: PlatformFile
    get() = PlatformFile(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!)

actual val customImageDir: PlatformFile
    get() = PlatformFile(externalPictureDir, "custom")

actual fun clearDownloadDir() {
    File(externalDownloadDir.absolutePath()).deleteRecursively()
}