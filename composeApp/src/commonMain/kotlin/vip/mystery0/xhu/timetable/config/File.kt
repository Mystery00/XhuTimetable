package vip.mystery0.xhu.timetable.config

import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.absolutePath

expect val externalPictureDir: PlatformFile

expect val externalDownloadDir: PlatformFile

expect val externalCacheDownloadDir: PlatformFile

expect val externalDocumentsDir: PlatformFile

expect val customImageDir: PlatformFile

fun PlatformFile.checkEqual(other: PlatformFile): Boolean =
    absolutePath() == other.absolutePath()

expect fun clearDownloadDir()