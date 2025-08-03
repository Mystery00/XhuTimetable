package vip.mystery0.xhu.timetable.config

import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSCachesDirectory
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

private fun getNsDirectoryPath(directory: ULong): String {
    return (NSSearchPathForDirectoriesInDomains(directory, NSUserDomainMask, true).firstOrNull() as? String)
        ?: error("Could not retrieve path for directory type: $directory")
}

@OptIn(ExperimentalForeignApi::class)
private fun getEnsuredPlatformDirectory(baseDirType: ULong, vararg components: String): PlatformFile {
    var currentPath = getNsDirectoryPath(baseDirType)
    val fileManager = NSFileManager.defaultManager

    if (!fileManager.fileExistsAtPath(currentPath)) {
        fileManager.createDirectoryAtPath(currentPath, true, null, null)
    }

    components.forEach { component ->
        currentPath = "$currentPath/$component"
        if (!fileManager.fileExistsAtPath(currentPath)) {
            fileManager.createDirectoryAtPath(currentPath, true, null, null)
        }
    }
    return PlatformFile(currentPath) // Assumes PlatformFile constructor takes a string path
}

actual val externalDocumentsDir: PlatformFile
    get() = getEnsuredPlatformDirectory(NSDocumentDirectory)

actual val externalPictureDir: PlatformFile
    get() = getEnsuredPlatformDirectory(NSDocumentDirectory, "Pictures")

actual val externalDownloadDir: PlatformFile
    get() = getEnsuredPlatformDirectory(NSDocumentDirectory, "Downloads")

actual val externalCacheDownloadDir: PlatformFile
    get() = getEnsuredPlatformDirectory(NSCachesDirectory, "update")

actual val customImageDir: PlatformFile
    get() {
        return getEnsuredPlatformDirectory(NSDocumentDirectory, "Pictures", "custom")
    }

@OptIn(ExperimentalForeignApi::class)
actual fun clearDownloadDir() {
    val downloadDirPath = externalDownloadDir.absolutePath()
    val fileManager = NSFileManager.defaultManager
    if (fileManager.fileExistsAtPath(downloadDirPath)) {
        val success = fileManager.removeItemAtPath(downloadDirPath, null)
        if (!success) {
            Logger.w("failed to clear download directory: $downloadDirPath")
        }
    }
}
