package vip.mystery0.xhu.timetable.utils

import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.security.MessageDigest
import java.util.Locale

fun String.md5(): String = bytes2HexString(hashTemplate(toByteArray(), "MD5"))

fun File.md5(): String {
    val messageDigest = MessageDigest.getInstance("MD5")
    messageDigest.update(
        FileInputStream(this)
            .channel
            .map(FileChannel.MapMode.READ_ONLY, 0, length())
    )
    val bytes = messageDigest.digest()
    return bytes2HexString(bytes)
}

fun String.sha1(): String = bytes2HexString(hashTemplate(toByteArray(), "SHA-1"))

fun String.sha256(): String = bytes2HexString(hashTemplate(toByteArray(), "SHA-256"))

fun String.sha512(): String = bytes2HexString(hashTemplate(toByteArray(), "SHA-512"))

fun String.sha384(): String = bytes2HexString(hashTemplate(toByteArray(), "SHA-384"))

private fun hashTemplate(data: ByteArray, algorithm: String): ByteArray {
    val md = MessageDigest.getInstance(algorithm)
    md.update(data)
    return md.digest()
}

private val HEX_DIGITS_UPPER = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
)
private val HEX_DIGITS_LOWER = charArrayOf(
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
)

private fun bytes2HexString(bytes: ByteArray?, isUpperCase: Boolean = true): String {
    if (bytes == null) return ""
    val hexDigits = if (isUpperCase) HEX_DIGITS_UPPER else HEX_DIGITS_LOWER
    val len = bytes.size
    if (len <= 0) return ""
    val ret = CharArray(len shl 1)
    var i = 0
    var j = 0
    while (i < len) {
        ret[j++] = hexDigits[bytes[i].toInt() shr 4 and 0x0f]
        ret[j++] = hexDigits[bytes[i].toInt() and 0x0f]
        i++
    }
    return String(ret)
}

private fun hexString2Bytes(string: String): ByteArray {
    var hexString = string
    if (hexString.isBlank()) return ByteArray(0)
    var len = hexString.length
    if (len % 2 != 0) {
        hexString = "0$hexString"
        len++
    }
    val hexBytes = hexString.uppercase(Locale.getDefault()).toCharArray()
    val ret = ByteArray(len shr 1)
    var i = 0
    while (i < len) {
        ret[i shr 1] = (hex2Dec(hexBytes[i]) shl 4 or hex2Dec(
            hexBytes[i + 1]
        )).toByte()
        i += 2
    }
    return ret
}

private fun hex2Dec(hexChar: Char): Int =
    when (hexChar) {
        in '0'..'9' -> {
            hexChar - '0'
        }
        in 'A'..'F' -> {
            hexChar - 'A' + 10
        }
        else -> {
            throw IllegalArgumentException()
        }
    }

fun String.base64(): String =
    bytes2HexString(base64Encode(toByteArray()))

fun String.deBase64(): String =
    bytes2HexString(base64Decode(toByteArray()))

private fun base64Encode(input: ByteArray): ByteArray =
    if (input.isEmpty())
        ByteArray(0)
    else
        Base64.encode(input, Base64.NO_WRAP)

private fun base64Decode(input: ByteArray): ByteArray =
    if (input.isEmpty())
        ByteArray(0)
    else
        Base64.decode(input, Base64.NO_WRAP)