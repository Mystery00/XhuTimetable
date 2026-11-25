package vip.mystery0.xhu.timetable

import kotlinx.coroutines.runBlocking
import vip.mystery0.xhu.timetable.utils.encryptLoginPassword
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.Mac
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.io.encoding.Base64
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class LoginEncryptionTest {
    @Test
    fun serverCanDecryptChinesePasswordUsingClientDerPublicKey() = runBlocking {
        val server = serverKeyPair()
        val nonce = ByteArray(32) { (it + 1).toByte() }
        val password = "西瓜课表密码-P@ssw0rd"

        val result = encryptLoginPassword(
            password = password,
            serverPublicKey = Base64.encode(server.public.encoded),
            nonce = Base64.encode(nonce),
        )

        val clientPublicKey = KeyFactory.getInstance("EC").generatePublic(
            X509EncodedKeySpec(Base64.decode(result.clientPublicKey))
        )
        val agreement = KeyAgreement.getInstance("ECDH").apply {
            init(server.private)
            doPhase(clientPublicKey, true)
        }
        val extract = Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec(nonce, "HmacSHA256"))
        }
        val expand = Mac.getInstance("HmacSHA256").apply {
            init(SecretKeySpec(extract.doFinal(agreement.generateSecret()), "HmacSHA256"))
        }
        val key = expand.doFinal(byteArrayOf(1))
        val envelope = Base64.decode(result.encryptedPassword)
        assertEquals(password.encodeToByteArray().size + 12 + 16, envelope.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding").apply {
            init(
                Cipher.DECRYPT_MODE,
                SecretKeySpec(key, "AES"),
                GCMParameterSpec(128, envelope.copyOfRange(0, 12)),
            )
            updateAAD(nonce)
        }
        val plaintext = cipher.doFinal(envelope.copyOfRange(12, envelope.size))
        assertEquals(password, plaintext.decodeToString())
    }

    @Test
    fun eachEncryptionGeneratesAFreshClientKeyPair() = runBlocking {
        val server = serverKeyPair()
        val serverPublicKey = Base64.encode(server.public.encoded)
        val nonce = Base64.encode(ByteArray(32) { it.toByte() })

        val first = encryptLoginPassword("相同密码", serverPublicKey, nonce)
        val second = encryptLoginPassword("相同密码", serverPublicKey, nonce)

        assertNotEquals(first.clientPublicKey, second.clientPublicKey)
        assertNotEquals(first.encryptedPassword, second.encryptedPassword)
    }

    private fun serverKeyPair(): KeyPair = KeyPairGenerator.getInstance("EC").apply {
        initialize(ECGenParameterSpec("secp521r1"))
    }.generateKeyPair()
}
