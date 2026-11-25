import { cryptoFramework } from '@kit.CryptoArchitectureKit';
import { util } from '@kit.ArkTS';
import { describe, expect, it } from '@ohos/hypium';
import { encryptLoginPassword } from '../../../main/ets/platform/LoginEncryption';

interface LoginRequest {
  password: string;
  serverPublicKey: string;
  nonce: string;
}

interface LoginResult {
  encryptedPassword: string;
  clientPublicKey: string;
}

// 以下密钥仅为 Node/OpenSSL 新生成的公开测试夹具，不对应任何账户或实际服务器。
// 固定向量使用 secp521r1、HKDF-SHA256（空 info）、AES-256-GCM，由 Node 独立生成。
const SERVER_PRIVATE_KEY = 'MIHuAgEAMBAGByqGSM49AgEGBSuBBAAjBIHWMIHTAgEBBEIA97jGyUu3C+gg2QzYM6Yb+2gKf0IOUWbCk+40AmA5Db4AjPccAo1fPf4p9qN+hwHQgMqJ/X2kNCna/2LMNNC+q56hgYkDgYYABADGB13+tSPi3goV3nNGscu5BXrz8KxY7KdAtu/fIPJWkjpwfTfn3XgedmximGJyRUT4lHgsJsuyWPgcwR7O17u36wBYMEDk8NiGXC/uYRxzlj48T7L0Uj9GP/R5gDYY05JmQzogvKk/0Nh3mwn3sY02b5tfoV6LdK7fLfDPxTzbedJycQ==';
const SERVER_PUBLIC_KEY = 'MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQAxgdd/rUj4t4KFd5zRrHLuQV68/CsWOynQLbv3yDyVpI6cH035914HnZsYphickVE+JR4LCbLslj4HMEezte7t+sAWDBA5PDYhlwv7mEcc5Y+PE+y9FI/Rj/0eYA2GNOSZkM6ILypP9DYd5sJ97GNNm+bX6Fei3Su3y3wz8U823nScnE=';
const FIXTURE_CLIENT_PUBLIC_KEY = 'MIGbMBAGByqGSM49AgEGBSuBBAAjA4GGAAQAFcZHcc3s4qLe07Jn5RCZDTKseXeHIf+W4b1KBqrA643vss9Ix4b4uEs4GTVCVtXwX+XSP9qEhX9hak6LCkvWEBgBgDRJejso8QY2tLnSz1+7xHYp3fRAw0hF3DA4z0tyv4pAK9ZdozQDzymTmtrgBIDqZ5BsB8dDqKepshd3sZ/9lEI=';
const FIXTURE_SECRET = '007310ca9df81966a5b186466949336857c523c9fbf2d0b19157c1bfef0dc2845f9c962fffc565ad6de4a0614e0ebf1dbbd9e19a4245a389be75dc6eb14d01ba5a01';
const FIXTURE_AES_KEY = '7680cadc173a56f1287c30efd7da70993b469174a7276dffc0c96aaa55c6fe8b';
const FIXTURE_ENVELOPE = 'EBESExQVFhcYGRob+SiW54w4YuMeGK5GF9QNdOJFDolYef8WW4+QxnXhg5EMIYk8EA==';
// 32 字节 nonce，刻意不同于 12 字节 IV；密码包含补充平面汉字和空字符。
const NONCE = 'AAECAwQFBgcICQoLDA0ODxAREhMUFRYXGBkaGxwdHh8=';
const PASSWORD = '中文密码𠮷\u0000test';
const base64 = new util.Base64Helper();

function hex(bytes: Uint8Array): string {
  let result = '';
  for (let i = 0; i < bytes.length; i++) {
    result += bytes[i].toString(16).padStart(2, '0');
  }
  return result;
}

async function hmac(keyData: Uint8Array, input: Uint8Array): Promise<Uint8Array> {
  const key = await cryptoFramework.createSymKeyGenerator('HMAC').convertKey({ data: keyData });
  try {
    const mac = cryptoFramework.createMac('SHA256');
    await mac.init(key);
    await mac.update({ data: input });
    return (await mac.doFinal()).data;
  } finally {
    key.clearMem();
  }
}

// 服务端参考推导不调用生产模块或原生 HKDF，独立执行 RFC 5869 的 Extract + 单块 Expand。
async function serverHkdf(secret: Uint8Array, salt: Uint8Array): Promise<Uint8Array> {
  const prk = await hmac(salt.length === 0 ? new Uint8Array(32) : salt, secret);
  try {
    return await hmac(prk, new Uint8Array([1]));
  } finally {
    prk.fill(0);
  }
}

async function serverSecret(clientPublicKey: string): Promise<Uint8Array> {
  const generator = cryptoFramework.createAsyKeyGenerator('ECC521');
  const server = await generator.convertKey(null, { data: base64.decodeSync(SERVER_PRIVATE_KEY) });
  try {
    const client = await generator.convertKey({ data: base64.decodeSync(clientPublicKey) }, null);
    expect(client.pubKey.getAsyKeySpec(cryptoFramework.AsyKeySpecItem.ECC_FIELD_SIZE_NUM)).assertEqual(521);
    return (await cryptoFramework.createKeyAgreement('ECC521').generateSecret(server.priKey, client.pubKey)).data;
  } finally {
    server.priKey.clearMem();
  }
}

async function serverKey(clientPublicKey: string): Promise<Uint8Array> {
  const secret = await serverSecret(clientPublicKey);
  try {
    return await serverHkdf(secret, base64.decodeSync(NONCE));
  } finally {
    secret.fill(0);
  }
}

async function decryptEnvelope(envelope: Uint8Array, keyData: Uint8Array, aad: Uint8Array): Promise<string> {
  const key = await cryptoFramework.createSymKeyGenerator('AES256').convertKey({ data: keyData });
  try {
    const params: cryptoFramework.GcmParamsSpec = {
      algName: 'GcmParamsSpec',
      iv: { data: envelope.slice(0, 12) },
      aad: { data: aad },
      authTag: { data: envelope.slice(envelope.length - 16) }
    };
    const cipher = cryptoFramework.createCipher('AES256|GCM|NoPadding');
    await cipher.init(cryptoFramework.CryptoMode.DECRYPT_MODE, key, params);
    const ciphertext = envelope.slice(12, envelope.length - 16);
    // 单次 doFinal 完成解密及认证，认证失败不能返回明文。
    const plaintext = await cipher.doFinal(ciphertext.length === 0 ? null : { data: ciphertext });
    return plaintext === null ? '' : util.TextDecoder.create('utf-8').decodeToString(plaintext.data);
  } finally {
    key.clearMem();
  }
}

async function decryptResult(result: LoginResult): Promise<string> {
  const key = await serverKey(result.clientPublicKey);
  try {
    return await decryptEnvelope(base64.decodeSync(result.encryptedPassword), key, base64.decodeSync(NONCE));
  } finally {
    key.fill(0);
  }
}

async function encrypt(password: string = PASSWORD): Promise<LoginResult> {
  const request: LoginRequest = { password: password, serverPublicKey: SERVER_PUBLIC_KEY, nonce: NONCE };
  return JSON.parse(await encryptLoginPassword(JSON.stringify(request))) as LoginResult;
}

export default function loginEncryptionTest(): void {
  describe('LoginEncryption', () => {
    it('HKDF 符合 RFC5869 空 info 向量', 0, async () => {
      // RFC 5869 A.3 的前 32 字节，防止双方复制同一个错误推导而相互通过。
      const expected = '8da4e775a563c18f715f802a063c5a31b8a11f5c5ee1879ec3454e5f3c738d2d';
      const ikm = new Uint8Array(22).fill(0x0b);
      expect(hex(await serverHkdf(ikm, new Uint8Array(0)))).assertEqual(expected);
      const spec: cryptoFramework.HKDFSpec = {
        algName: 'HKDF', key: ikm, salt: new Uint8Array(0), info: new Uint8Array(0), keySize: 32
      };
      const actual = await cryptoFramework.createKdf('HKDF|SHA256|EXTRACT_AND_EXPAND').generateSecret(spec);
      expect(hex(actual.data)).assertEqual(expected);
    });

    it('固定 OpenSSL 向量保留 ECDH 前导零并解密中文', 0, async () => {
      const secret = await serverSecret(FIXTURE_CLIENT_PUBLIC_KEY);
      expect(hex(secret)).assertEqual(FIXTURE_SECRET);
      const key = await serverHkdf(secret, base64.decodeSync(NONCE));
      expect(hex(key)).assertEqual(FIXTURE_AES_KEY);
      const envelope = base64.decodeSync(FIXTURE_ENVELOPE);
      expect(envelope.length).assertEqual(12 + 21 + 16);
      expect(hex(envelope.slice(0, 12))).assertEqual('101112131415161718191a1b');
      expect(await decryptEnvelope(envelope, key, base64.decodeSync(NONCE))).assertEqual(PASSWORD);
    });

    it('真实输出可按服务端协议解密且布局正确', 0, async () => {
      const result = await encrypt();
      expect(base64.decodeSync(result.encryptedPassword).length).assertEqual(12 + 21 + 16);
      expect(await decryptResult(result)).assertEqual(PASSWORD);
    });

    it('并发相同请求每次产生独立客户端密钥和 IV', 0, async () => {
      const results = await Promise.all([encrypt(), encrypt()]);
      expect(results[0].clientPublicKey === results[1].clientPublicKey).assertFalse();
      const first = base64.decodeSync(results[0].encryptedPassword);
      const second = base64.decodeSync(results[1].encryptedPassword);
      expect(hex(first.slice(0, 12)) === hex(second.slice(0, 12))).assertFalse();
      expect(await decryptResult(results[0])).assertEqual(PASSWORD);
      expect(await decryptResult(results[1])).assertEqual(PASSWORD);
    });

    it('IV 密文标签或 AAD 被篡改时认证失败', 0, async () => {
      const result = await encrypt();
      const key = await serverKey(result.clientPublicKey);
      const envelope = base64.decodeSync(result.encryptedPassword);
      const aad = base64.decodeSync(NONCE);
      try {
        for (const offset of [0, 12, envelope.length - 1]) {
          const tampered = envelope.slice();
          tampered[offset] ^= 1;
          await expect(decryptEnvelope(tampered, key, aad)).assertPromiseIsRejected();
        }
        const wrongAad = aad.slice();
        wrongAad[0] ^= 1;
        // 密钥保持不变，只修改 AAD，确保生产加密确实认证了 nonce。
        await expect(decryptEnvelope(envelope, key, wrongAad)).assertPromiseIsRejected();
      } finally {
        key.fill(0);
      }
    });

    it('空密码仍输出完整的 IV 和认证标签', 0, async () => {
      const result = await encrypt('');
      expect(base64.decodeSync(result.encryptedPassword).length).assertEqual(28);
      expect(await decryptResult(result)).assertEqual('');
    });

    it('拒绝不完整请求及错误曲线', 0, async () => {
      for (const json of ['{', 'null', '[]', '{}', '{"password":1,"serverPublicKey":"x","nonce":"x"}']) {
        await expect(encryptLoginPassword(json)).assertPromiseIsRejected();
      }
      const wrongCurve = await cryptoFramework.createAsyKeyGenerator('ECC256').generateKeyPair();
      try {
        const request: LoginRequest = {
          password: PASSWORD,
          serverPublicKey: base64.encodeToStringSync(wrongCurve.pubKey.getEncoded().data),
          nonce: NONCE
        };
        await expect(encryptLoginPassword(JSON.stringify(request))).assertPromiseIsRejected();
      } finally {
        wrongCurve.priKey.clearMem();
      }
    });
  });
}
