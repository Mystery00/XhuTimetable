package vip.mystery0.xhu.timetable.config.store

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.toKotlinxIoPath
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.io.IOException
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlinx.io.writeString
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

// 所有缓存和文件操作共用锁，避免并发修改同一快照导致更新丢失。
private val storeLock = SynchronizedObject()
private val storeSnapshots = mutableMapOf<Path, JsonObject>()

actual inline fun <reified T> Store.getValue(key: String, defaultValue: T): T =
    readOhosStoreValue(key, defaultValue) as T

actual inline fun <reified T> Store.setValue(key: String, value: T) {
    writeOhosStoreValue(key, value)
}

@PublishedApi
internal fun Store.readOhosStoreValue(key: String, defaultValue: Any?): Any? {
    val type = storeValueType(defaultValue)
    return synchronized(storeLock) {
        val entry = readStoreSnapshot(storeFile())[key]
        decodeStoreValue(entry, type) ?: defaultValue
    }
}

@PublishedApi
internal fun Store.writeOhosStoreValue(key: String, value: Any?) {
    val entry = encodeStoreValue(value)
    synchronized(storeLock) {
        val file = storeFile()
        val values = readStoreSnapshot(file).toMutableMap()
        values[key] = entry
        writeStoreSnapshot(file, JsonObject(values))
    }
}

actual fun Store.removeValue(key: String) {
    synchronized(storeLock) {
        val file = storeFile()
        val values = readStoreSnapshot(file).toMutableMap()
        values.remove(key)
        writeStoreSnapshot(file, JsonObject(values))
    }
}

actual fun Store.removeAllValue() {
    synchronized(storeLock) {
        writeStoreSnapshot(storeFile(), JsonObject(emptyMap()))
    }
}

private fun Store.storeFile(): Path {
    // 首次访问时才读取宿主注入的沙盒；路径只使用内部枚举 id，键不参与路径构造。
    val directory = Path(FileKit.filesDir.toKotlinxIoPath(), "stores")
    return Path(directory, "$id.json")
}

// 仅在 storeLock 内调用。读取失败不缓存，允许后续重试，且不把 I/O 错误当作空文件。
private fun readStoreSnapshot(file: Path): JsonObject {
    storeSnapshots[file]?.let { return it }
    val values = if (SystemFileSystem.exists(file)) {
        val text = SystemFileSystem.source(file).buffered().use { it.readString() }
        try {
            Json.parseToJsonElement(text) as? JsonObject ?: JsonObject(emptyMap())
        } catch (_: SerializationException) {
            // 解析异常可能含有密码或 token 的原文，不记录异常或文件内容。
            JsonObject(emptyMap())
        }
    } else {
        JsonObject(emptyMap())
    }
    storeSnapshots[file] = values
    return values
}

// 仅在 storeLock 内调用。提交失败保留旧缓存，并把错误交给调用方。
private fun writeStoreSnapshot(file: Path, values: JsonObject) {
    val directory = checkNotNull(file.parent)
    SystemFileSystem.createDirectories(directory)
    val temporaryFile = Path(directory, "${file.name}.tmp")
    try {
        SystemFileSystem.sink(temporaryFile).buffered().use { sink ->
            sink.writeString(values.toString())
            sink.flush()
        }
        // 临时文件与目标同目录；关闭文件后再原子替换，绝不先删除旧文件。
        SystemFileSystem.atomicMove(temporaryFile, file)
    } catch (failure: Exception) {
        try {
            SystemFileSystem.delete(temporaryFile, mustExist = false)
        } catch (_: IOException) {
            // 清理失败不掩盖原始写入错误，下次写入会截断遗留临时文件。
        }
        throw failure
    }
    // 同步完成写入和替换后才发布缓存，不启动后台写入任务。
    storeSnapshots[file] = values
}

private fun storeValueType(value: Any?): String = when (value) {
    is Boolean -> "Boolean"
    is Int -> "Int"
    is Long -> "Long"
    is Float -> "Float"
    is String -> "String"
    is Set<*> -> {
        require(value.all { it is String }) { "Unsupported type" }
        "StringSet"
    }
    // 与现有 Android/iOS Store 一致，不支持 Double、null 或任意对象。
    else -> throw IllegalArgumentException("Unsupported type")
}

private fun encodeStoreValue(value: Any?): JsonObject {
    val type = storeValueType(value)
    val content = when (value) {
        is Boolean -> JsonPrimitive(value)
        is Int -> JsonPrimitive(value)
        is Long -> JsonPrimitive(value)
        // 使用数值形式的 IEEE 754 位模式，精确保留负零、NaN 和无穷大。
        is Float -> JsonPrimitive(value.toRawBits())
        is String -> JsonPrimitive(value)
        is Set<*> -> JsonArray(value.map { JsonPrimitive(it as String) })
        else -> throw IllegalArgumentException("Unsupported type")
    }
    return JsonObject(mapOf("type" to JsonPrimitive(type), "value" to content))
}

private fun decodeStoreValue(element: JsonElement?, type: String): Any? {
    val entry = element as? JsonObject ?: return null
    if (entry["type"] != JsonPrimitive(type)) return null
    val content = entry["value"] ?: return null
    if (type == "StringSet") {
        val array = content as? JsonArray ?: return null
        val result = linkedSetOf<String>()
        for (item in array) {
            val text = item as? JsonPrimitive ?: return null
            if (!text.isString) return null
            result.add(text.content)
        }
        // 每次读取返回独立集合，调用方修改集合不会绕过持久化或污染缓存。
        return result
    }
    val primitive = content as? JsonPrimitive ?: return null
    if (type == "String") return primitive.takeIf { it.isString }?.content
    if (primitive.isString) return null
    return when (type) {
        "Boolean" -> primitive.booleanOrNull
        "Int" -> primitive.intOrNull
        "Long" -> primitive.longOrNull
        "Float" -> primitive.intOrNull?.let { Float.fromBits(it) }
        else -> null
    }
}
