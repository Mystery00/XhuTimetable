@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.time.ExperimentalTime::class)

package io.github.vinceglb.filekit

import io.github.vinceglb.filekit.exceptions.BookmarkResolutionException
import io.github.vinceglb.filekit.exceptions.BookmarkResolutionFailure
import io.github.vinceglb.filekit.exceptions.FileKitException
import io.github.vinceglb.filekit.mimeType.MimeType
import kotlinx.cinterop.alloc
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.toKString
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.Serializable
import platform.posix.ENOENT
import platform.posix.ENOTDIR
import platform.posix.S_IFLNK
import platform.posix.S_IFMT
import platform.posix.errno
import platform.posix.free
import platform.posix.getcwd
import platform.posix.lstat
import platform.posix.realpath
import platform.posix.stat
import platform.posix.strerror
import platform.posix.unlink
import kotlin.time.Instant

private class OhosFilePath(val value: Path)

@Serializable(with = PlatformFileSerializer::class)
public actual class PlatformFile private constructor(private val location: OhosFilePath) {
    internal val nativePath: Path get() = location.value

    public actual override fun toString(): String = path

    override fun equals(other: Any?): Boolean = other is PlatformFile && nativePath == other.nativePath

    override fun hashCode(): Int = nativePath.hashCode()

    public actual companion object {
        internal fun fromPath(path: Path): PlatformFile {
            require('\u0000' !in path.toString()) { "文件路径不能包含空字符" }
            return PlatformFile(OhosFilePath(path))
        }
    }
}

public actual fun PlatformFile(path: Path): PlatformFile = PlatformFile.fromPath(path)

public actual fun PlatformFile.toKotlinxIoPath(): Path = nativePath

@PublishedApi
internal actual fun PlatformFile.withPath(path: Path): PlatformFile = PlatformFile(path)

public actual val PlatformFile.extension: String
    get() = name.substringAfterLast('.', "")

public actual val PlatformFile.nameWithoutExtension: String
    get() = name.substringBeforeLast('.', name)

public actual fun PlatformFile.absolutePath(): String {
    val absolute = if (nativePath.isAbsolute) {
        path
    } else {
        val directory = getcwd(null, 0.convert()) ?: throw posixFailure("获取当前目录失败")
        val current = try {
            directory.toKString()
        } finally {
            free(directory)
        }
        "$current/$path"
    }
    var candidate = absolute.trimEnd('/').ifEmpty { "/" }
    val missingSegments = mutableListOf<String>()
    while (true) {
        realpath(candidate, null)?.let { resolved ->
            val canonical = try {
                resolved.toKString()
            } finally {
                free(resolved)
            }
            return (listOf(canonical) + missingSegments.asReversed()).joinToString("/").normalizePosixPath()
        }
        val error = errno
        if (error != ENOENT && error != ENOTDIR || candidate == "/") {
            throw posixFailure("解析文件路径失败", error)
        }
        val separator = candidate.lastIndexOf('/')
        missingSegments += candidate.substring(separator + 1)
        candidate = if (separator <= 0) "/" else candidate.substring(0, separator)
    }
}

private fun String.normalizePosixPath(): String {
    val segments = mutableListOf<String>()
    split('/').forEach { segment ->
        when (segment) {
            "", "." -> Unit
            ".." -> if (segments.isNotEmpty()) segments.removeAt(segments.lastIndex)
            else -> segments += segment
        }
    }
    return segments.joinToString(separator = "/", prefix = "/")
}

public actual inline fun PlatformFile.list(block: (List<PlatformFile>) -> Unit) {
    block(list())
}

public actual fun PlatformFile.list(): List<PlatformFile> =
    SystemFileSystem.list(nativePath).map(::PlatformFile)

public actual fun PlatformFile.lastModified(): Instant = memScoped {
    val metadata = alloc<stat>()
    if (stat(path, metadata.ptr) != 0) throw posixFailure("读取文件修改时间失败")
    Instant.fromEpochSeconds(metadata.st_mtim.tv_sec, metadata.st_mtim.tv_nsec)
}

// POSIX stat 不提供可靠的文件创建时间。
public actual fun PlatformFile.createdAt(): Instant? = null

public actual fun PlatformFile.mimeType(): MimeType? {
    val value = when (extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        "gif" -> "image/gif"
        "svg" -> "image/svg+xml"
        "heic" -> "image/heic"
        "heif" -> "image/heif"
        "avif" -> "image/avif"
        "bmp" -> "image/bmp"
        "ico" -> "image/vnd.microsoft.icon"
        "tif", "tiff" -> "image/tiff"
        "pdf" -> "application/pdf"
        "json" -> "application/json"
        "txt", "log" -> "text/plain"
        "csv" -> "text/csv"
        "ics" -> "text/calendar"
        "html", "htm" -> "text/html"
        "xml" -> "application/xml"
        "zip" -> "application/zip"
        "gz" -> "application/gzip"
        "mp3" -> "audio/mpeg"
        "wav" -> "audio/wav"
        "ogg" -> "audio/ogg"
        "mp4", "m4v" -> "video/mp4"
        "mov" -> "video/quicktime"
        "webm" -> "video/webm"
        else -> return null
    }
    return MimeType.parse(value)
}

internal actual fun PlatformFile.deleteIfSymbolicLink(): Boolean = memScoped {
    val metadata = alloc<stat>()
    if (lstat(path, metadata.ptr) != 0) {
        val error = errno
        if (error == ENOENT || error == ENOTDIR) return@memScoped false
        throw posixFailure("检查文件链接失败", error)
    }
    if ((metadata.st_mode.toUInt() and S_IFMT.toUInt()) != S_IFLNK.toUInt()) return@memScoped false
    if (unlink(path) != 0) throw posixFailure("删除文件链接失败")
    true
}

public actual fun PlatformFile.startAccessingSecurityScopedResource(): Boolean = true

public actual fun PlatformFile.stopAccessingSecurityScopedResource() = Unit

private const val BOOKMARK_PREFIX = "filekit-ohos-v1:"

public actual suspend fun PlatformFile.bookmarkData(): BookmarkData =
    BookmarkData((BOOKMARK_PREFIX + absolutePath()).encodeToByteArray())

public actual fun PlatformFile.releaseBookmark() = Unit

public actual fun PlatformFile.Companion.fromBookmarkData(bookmarkData: BookmarkData): PlatformFile =
    resolveBookmarkData(bookmarkData).file

public actual fun PlatformFile.Companion.resolveBookmarkData(bookmarkData: BookmarkData): BookmarkResolution {
    val value = try {
        bookmarkData.bytes.decodeToString(throwOnInvalidSequence = true)
    } catch (error: CharacterCodingException) {
        throw BookmarkResolutionException(BookmarkResolutionFailure.INVALID_DATA, "文件书签不是有效 UTF-8", error)
    }
    if (!value.startsWith(BOOKMARK_PREFIX)) {
        throw BookmarkResolutionException(BookmarkResolutionFailure.INCOMPATIBLE_PLATFORM, "文件书签不属于此鸿蒙版本")
    }
    val path = value.removePrefix(BOOKMARK_PREFIX)
    if (!path.startsWith('/') || '\u0000' in path) {
        throw BookmarkResolutionException(BookmarkResolutionFailure.INVALID_DATA, "文件书签路径无效")
    }
    val file = PlatformFile(path)
    if (!file.exists()) {
        throw BookmarkResolutionException(BookmarkResolutionFailure.RESOURCE_UNAVAILABLE, "书签对应的文件不存在")
    }
    return BookmarkResolution(file, isStale = false, shouldRefresh = false)
}

private fun posixFailure(message: String, error: Int = errno): FileKitException =
    FileKitException("$message: ${strerror(error)?.toKString() ?: error.toString()}")
