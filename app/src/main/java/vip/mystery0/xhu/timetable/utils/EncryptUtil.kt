package vip.mystery0.xhu.timetable.utils

import android.os.Build
import android.util.Base64
import java.io.File
import java.io.FileInputStream
import java.nio.channels.FileChannel
import java.security.KeyFactory
import java.security.MessageDigest
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

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

fun String.encryptRsa(publicKey: String, transformation: String, keySize: Int = 1024): String =
    bytes2HexString(
        rsaTemplate(
            toByteArray(),
            publicKey.toByteArray(),
            keySize,
            transformation,
            true
        )
    )

fun String.encryptRsa2Base64(
    publicKey: String,
    transformation: String,
    keySize: Int = 1024
): String =
    bytes2HexString(
        base64Encode(
            rsaTemplate(
                toByteArray(),
                publicKey.toByteArray(),
                keySize,
                transformation,
                true
            )
        )
    )

fun String.decryptBase64Rsa(
    privateKey: String,
    transformation: String,
    keySize: Int = 1024
): String =
    bytes2HexString(
        rsaTemplate(
            base64Decode(toByteArray()),
            privateKey.toByteArray(),
            keySize,
            transformation,
            false
        )
    )

fun String.decryptRsa(
    privateKey: String,
    transformation: String,
    keySize: Int = 1024
): String =
    bytes2HexString(
        rsaTemplate(
            hexString2Bytes(this),
            privateKey.toByteArray(),
            keySize,
            transformation,
            false
        )
    )

private fun rsaTemplate(
    data: ByteArray,
    key: ByteArray,
    keySize: Int,
    transformation: String,
    isEncrypt: Boolean
): ByteArray {
    if (data.isEmpty() || key.isEmpty()) {
        return ByteArray(0)
    }
    val keyFactory = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        KeyFactory.getInstance("RSA", "BC")
    } else {
        KeyFactory.getInstance("RSA")
    }
    val rsaKey = if (isEncrypt) {
        val keySpec = X509EncodedKeySpec(key)
        keyFactory.generatePublic(keySpec)
    } else {
        val keySpec = PKCS8EncodedKeySpec(key)
        keyFactory.generatePrivate(keySpec)
    }
    if (rsaKey == null) return ByteArray(0)
    val cipher = Cipher.getInstance(transformation)
    cipher.init(if (isEncrypt) Cipher.ENCRYPT_MODE else Cipher.DECRYPT_MODE, rsaKey)
    val len = data.size
    var maxLen = keySize / 8
    if (isEncrypt) {
        val lowerTrans = transformation.lowercase(Locale.getDefault())
        if (lowerTrans.endsWith("pkcs1padding")) {
            maxLen -= 11
        }
    }
    val count = len / maxLen
    return if (count > 0) {
        var ret = ByteArray(0)
        var buff = ByteArray(maxLen)
        var index = 0
        for (i in 0 until count) {
            System.arraycopy(data, index, buff, 0, maxLen)
            ret = joins(ret, cipher.doFinal(buff))
            index += maxLen
        }
        if (index != len) {
            val restLen = len - index
            buff = ByteArray(restLen)
            System.arraycopy(data, index, buff, 0, restLen)
            ret = joins(ret, cipher.doFinal(buff))
        }
        ret
    } else {
        cipher.doFinal(data)
    }
}

private fun joins(prefix: ByteArray, suffix: ByteArray): ByteArray {
    val ret = ByteArray(prefix.size + suffix.size)
    System.arraycopy(prefix, 0, ret, 0, prefix.size)
    System.arraycopy(suffix, 0, ret, prefix.size, suffix.size)
    return ret
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