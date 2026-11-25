package vip.mystery0.xhu.timetable.config

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.toKotlinxIoPath
import kotlinx.cinterop.alloc
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import platform.posix.S_IFDIR
import platform.posix.S_IFMT
import platform.posix.lstat
import platform.posix.stat

private fun directory(base: PlatformFile, child: String): PlatformFile =
    PlatformFile(base, child).also { it.createDirectories() }

actual val externalDocumentsDir: PlatformFile get() = FileKit.filesDir
actual val externalPictureDir: PlatformFile get() = directory(FileKit.filesDir, "Pictures")
actual val externalDownloadDir: PlatformFile get() = directory(FileKit.filesDir, "Downloads")
actual val externalCacheDownloadDir: PlatformFile get() = directory(FileKit.cacheDir, "downloads")
actual val customImageDir: PlatformFile get() = directory(externalPictureDir, "custom")

actual fun clearDownloadDir() {
    deleteDownloadEntry(Path(FileKit.filesDir.toKotlinxIoPath(), "Downloads"))
}

private fun deleteDownloadEntry(path: Path) {
    val isDirectory = memScoped {
        val metadata = alloc<stat>()
        lstat(path.toString(), metadata.ptr) == 0 &&
            (metadata.st_mode.toUInt() and S_IFMT.toUInt()) == S_IFDIR.toUInt()
    }
    if (isDirectory) {
        SystemFileSystem.list(path).forEach(::deleteDownloadEntry)
    }
    SystemFileSystem.delete(path, mustExist = false)
}
