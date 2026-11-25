package io.github.vinceglb.filekit

import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

public actual object FileKit

private lateinit var sandboxFiles: PlatformFile
private lateinit var sandboxCache: PlatformFile
private lateinit var sandboxDatabases: PlatformFile

public fun initializeFileKit(filesDir: String, cacheDir: String) {
    val files = PlatformFile(Path(filesDir))
    val cache = PlatformFile(Path(cacheDir))
    require(files.isAbsolute() && cache.isAbsolute()) { "鸿蒙沙盒目录必须使用绝对路径" }
    val databases = PlatformFile(files, "databases")
    SystemFileSystem.createDirectories(files.toKotlinxIoPath())
    SystemFileSystem.createDirectories(cache.toKotlinxIoPath())
    SystemFileSystem.createDirectories(databases.toKotlinxIoPath())
    sandboxFiles = files
    sandboxCache = cache
    sandboxDatabases = databases
}

public actual val FileKit.filesDir: PlatformFile get() = sandboxFiles

public actual val FileKit.cacheDir: PlatformFile get() = sandboxCache

public actual val FileKit.databasesDir: PlatformFile get() = sandboxDatabases

public actual val FileKit.projectDir: PlatformFile get() = sandboxFiles

public actual suspend fun FileKit.saveImageToGallery(bytes: ByteArray, filename: String): Result<Unit> =
    Result.failure(UnsupportedOperationException("鸿蒙预览版暂不支持此操作"))

public actual suspend fun FileKit.saveVideoToGallery(file: PlatformFile, filename: String): Result<Unit> =
    Result.failure(UnsupportedOperationException("鸿蒙预览版暂不支持此操作"))

public actual suspend fun FileKit.compressImage(
    bytes: ByteArray,
    imageFormat: ImageFormat,
    quality: Int,
    maxWidth: Int?,
    maxHeight: Int?,
): ByteArray = throw UnsupportedOperationException("鸿蒙预览版暂不支持此操作")
