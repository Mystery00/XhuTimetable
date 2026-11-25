package vip.mystery0.xhu.timetable.utils

import dev.whyoleg.cryptography.CryptographyProvider
import dev.whyoleg.cryptography.algorithms.AES
import dev.whyoleg.cryptography.algorithms.EC
import dev.whyoleg.cryptography.algorithms.ECDH
import dev.whyoleg.cryptography.algorithms.HKDF
import dev.whyoleg.cryptography.algorithms.SHA256
import kotlin.io.encoding.Base64

actual suspend fun encryptLoginPassword(
    password: String,
    serverPublicKey: String,
    nonce: String,
): LoginEncryptionResult {
    val clientKeyPair = CryptographyProvider.Default.get(ECDH)
        .keyPairGenerator(EC.Curve.P521)
        .generateKeyBlocking()
    val decodedServerPublicKey = CryptographyProvider.Default.get(ECDH)
        .publicKeyDecoder(curve = EC.Curve.P521)
        .decodeFromByteArrayBlocking(
            EC.PublicKey.Format.DER,
            Base64.decode(serverPublicKey),
        )
    val decodedNonce = Base64.decode(nonce)
    val sharedSecret = clientKeyPair.privateKey.sharedSecretGenerator()
        .generateSharedSecretToByteArrayBlocking(decodedServerPublicKey)
    val cipher = CryptographyProvider.Default.get(AES.GCM)
        .keyDecoder()
        .decodeFromByteArrayBlocking(
            AES.Key.Format.RAW,
            CryptographyProvider.Default.get(HKDF)
                .secretDerivation(SHA256, AES.Key.Size.B256, decodedNonce)
                .deriveSecretToByteArrayBlocking(sharedSecret),
        )
        .cipher()
    // nonce 用作认证附加数据，库生成的随机 IV 会附在密文头部。
    val encryptedPassword = Base64.encode(
        cipher.encryptBlocking(password.encodeToByteArray(), decodedNonce)
    )
    val clientPublicKey = Base64.encode(
        clientKeyPair.publicKey.encodeToByteArrayBlocking(EC.PublicKey.Format.DER)
    )
    return LoginEncryptionResult(encryptedPassword, clientPublicKey)
}
