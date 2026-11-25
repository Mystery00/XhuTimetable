import { cryptoFramework } from '@kit.CryptoArchitectureKit';
import { util } from '@kit.ArkTS';

interface LoginEncryptionRequest {
  password: string;
  serverPublicKey: string;
  nonce: string;
}

interface LoginEncryptionResult {
  encryptedPassword: string;
  clientPublicKey: string;
}

function parseRequest(requestJson: string): LoginEncryptionRequest {
  let request: LoginEncryptionRequest;
  try {
    request = JSON.parse(requestJson) as LoginEncryptionRequest;
  } catch {
    // JSON 解析器的原始错误可能含密码片段，不能透传给桥接层。
    throw new Error('登录加密请求不是有效 JSON');
  }
  if (request === null || typeof request !== 'object' || Array.isArray(request) ||
    typeof request.password !== 'string' || typeof request.serverPublicKey !== 'string' ||
    typeof request.nonce !== 'string') {
    throw new Error('登录加密请求字段无效');
  }
  return request;
}

// 原生 HKDFSpec 需要 API 12；所有密钥运算均由系统 CryptoFramework 执行。
export async function encryptLoginPassword(requestJson: string): Promise<string> {
  const request = parseRequest(requestJson);
  const base64 = new util.Base64Helper();
  const nonce = request.nonce.length === 0 ? new Uint8Array(0) : base64.decodeSync(request.nonce);
  const generator = cryptoFramework.createAsyKeyGenerator('ECC521');
  // convertKey 按 DER 中的曲线导入，因此需显式限制为协议要求的 P-521。
  const server = await generator.convertKey({ data: base64.decodeSync(request.serverPublicKey) }, null);
  if (server.pubKey.getAsyKeySpec(cryptoFramework.AsyKeySpecItem.ECC_FIELD_SIZE_NUM) !== 521) {
    throw new Error('登录服务器公钥必须使用 P-521');
  }
  const client = await generator.generateKeyPair();
  let sharedSecret = new Uint8Array(0);
  let keyData = new Uint8Array(0);
  let plaintext = new Uint8Array(0);
  let key: cryptoFramework.SymKey | undefined = undefined;
  try {
    sharedSecret = (await cryptoFramework.createKeyAgreement('ECC521')
      .generateSecret(client.priKey, server.pubKey)).data;
    const hkdfSpec: cryptoFramework.HKDFSpec = {
      algName: 'HKDF',
      key: sharedSecret,
      salt: nonce,
      info: new Uint8Array(0),
      keySize: 32
    };
    keyData = (await cryptoFramework.createKdf('HKDF|SHA256|EXTRACT_AND_EXPAND')
      .generateSecret(hkdfSpec)).data;
    key = await cryptoFramework.createSymKeyGenerator('AES256').convertKey({ data: keyData });
    // nonce 是 HKDF salt 及 GCM AAD，IV 必须另取新的 12 字节安全随机数。
    const iv = await cryptoFramework.createRandom().generateRandom(12);
    const params: cryptoFramework.GcmParamsSpec = {
      algName: 'GcmParamsSpec',
      iv: iv,
      aad: { data: nonce },
      authTag: { data: new Uint8Array(16) }
    };
    const cipher = cryptoFramework.createCipher('AES256|GCM|NoPadding');
    await cipher.init(cryptoFramework.CryptoMode.ENCRYPT_MODE, key, params);
    let ciphertext = new Uint8Array(0);
    // 系统编码接口对空字符串可能返回 undefined。
    if (request.password.length > 0) {
      plaintext = util.TextEncoder.create('utf-8').encodeInto(request.password);
      ciphertext = (await cipher.update({ data: plaintext })).data;
    }
    // 鸿蒙 GCM 的 update 输出密文，doFinal(null) 单独输出 128 位认证标签。
    const tag = await cipher.doFinal(null);
    if (tag === null || tag.data.length !== 16 || ciphertext.length !== plaintext.length) {
      throw new Error('登录加密结果长度无效');
    }
    const encrypted = new Uint8Array(iv.data.length + ciphertext.length + tag.data.length);
    encrypted.set(iv.data, 0);
    encrypted.set(ciphertext, iv.data.length);
    encrypted.set(tag.data, iv.data.length + ciphertext.length);
    const result: LoginEncryptionResult = {
      encryptedPassword: base64.encodeToStringSync(encrypted),
      clientPublicKey: base64.encodeToStringSync(client.pubKey.getEncoded().data)
    };
    return JSON.stringify(result);
  } finally {
    plaintext.fill(0);
    sharedSecret.fill(0);
    keyData.fill(0);
    key?.clearMem();
    client.priKey.clearMem();
  }
}
