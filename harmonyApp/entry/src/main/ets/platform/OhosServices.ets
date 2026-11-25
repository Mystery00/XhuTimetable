import { bundleManager, common, ConfigurationConstant } from '@kit.AbilityKit';
import { url, util } from '@kit.ArkTS';
import { uniformTypeDescriptor } from '@kit.ArkData';
import { BusinessError, deviceInfo, pasteboard } from '@kit.BasicServicesKit';
import { fileIo, fileUri } from '@kit.CoreFileKit';
import { connection, http, webSocket } from '@kit.NetworkKit';
import { hilog } from '@kit.PerformanceAnalysisKit';
import { systemShare } from '@kit.ShareKit';
import { photoAccessHelper } from '@kit.MediaLibraryKit';
import { encryptLoginPassword as encryptPassword } from './LoginEncryption';

interface AppInfo {
  bundleName: string;
  versionName: string;
  versionCode: number;
  systemVersion: string;
  manufacturer: string;
  model: string;
  networkConnected: boolean;
}

interface HttpHeader {
  name: string;
  value: string;
}

interface HttpBridgeRequest {
  requestId: string;
  url: string;
  method: string;
  headers: HttpHeader[];
  bodyBase64: string;
  connectTimeoutMs: number;
  readTimeoutMs: number;
}

interface HttpBridgeResponse {
  status: number;
  headers: HttpHeader[];
  bodyBase64: string;
}

interface ActiveHttpRequest {
  client: http.HttpRequest;
  cancel: (error: Error) => void;
}

interface FeedbackRequest {
  url: string;
  headers: HttpHeader[];
}

type FeedbackState = 'CONNECTED' | 'CONNECTING' | 'DISCONNECTED' | 'FAILED';

interface FeedbackSnapshot {
  state: FeedbackState;
  message: string;
  events: string[];
}

class FeedbackConnection {
  socket: webSocket.WebSocket;
  key: string;
  ready: boolean = false;
  timer: number = -1;
  promise: Promise<string>;
  resolve: ((value: string) => void) | undefined = undefined;
  reject: ((error: Error) => void) | undefined = undefined;
  pendingSends: Set<(error: Error) => void> = new Set();

  constructor(socket: webSocket.WebSocket, key: string) {
    this.socket = socket;
    this.key = key;
    this.promise = new Promise<string>((resolve, reject) => {
      this.resolve = resolve;
      this.reject = reject;
    });
  }
}

export class OhosServices {
  private context: common.UIAbilityContext;
  private activeHttpRequests: Map<string, ActiveHttpRequest> = new Map();
  private networkConnection: connection.NetConnection | undefined = undefined;
  private networkCallback: ((connected: boolean) => void) | undefined = undefined;
  private networkRegistered: boolean = false;
  private networkUnregistering: boolean = false;
  private disposed: boolean = false;
  private messageHandler: ((message: string) => void) | undefined = undefined;
  private feedbackConnection: FeedbackConnection | undefined = undefined;
  private feedbackState: FeedbackState = 'DISCONNECTED';
  private feedbackMessage: string = '反馈连接已关闭';
  private feedbackEvents: string[] = [];

  constructor(context: common.UIAbilityContext) {
    this.context = context;
  }

  setMessageHandler(handler: ((message: string) => void) | undefined): void {
    this.messageHandler = this.disposed ? undefined : handler;
  }

  async nativeMessage(message: string): Promise<string> {
    if (!this.disposed) {
      this.messageHandler?.(message);
    }
    return '';
  }

  async openUrl(value: string): Promise<string> {
    if (this.disposed) {
      throw new Error('系统服务已释放');
    }
    if (!/^https?:\/\//i.test(value) || /[\u0000- \u007f]/.test(value)) {
      throw new Error('仅支持完整的 HTTP 或 HTTPS 链接');
    }
    let target: url.URL;
    try {
      target = new url.URL(value);
    } catch {
      throw new Error('链接格式无效');
    }
    if ((target.protocol !== 'http:' && target.protocol !== 'https:') ||
      target.hostname.length === 0 || target.username.length > 0 || target.password.length > 0) {
      throw new Error('链接格式无效');
    }
    await this.context.openLink(target.href, { appLinkingOnly: false });
    return '';
  }

  async pickImage(payload: string): Promise<string> {
    if (this.disposed) {
      throw new Error('系统服务已释放');
    }
    const options = new photoAccessHelper.PhotoSelectOptions();
    options.MIMEType = photoAccessHelper.PhotoViewMIMETypes.IMAGE_TYPE;
    options.maxSelectNumber = 1;
    const result = await new photoAccessHelper.PhotoViewPicker().select(options);
    if (result.photoUris.length === 0) {
      return '';
    }
    if (this.disposed) {
      throw new Error('系统服务已释放');
    }
    const source = await fileIo.open(result.photoUris[0], fileIo.OpenMode.READ_ONLY);
    try {
      const stat = await fileIo.stat(source.fd);
      if (!stat.isFile() || stat.size <= 0) {
        throw new Error('请选择可读取的图片');
      }
      if (stat.size > 20 * 1024 * 1024) {
        throw new Error('图片大小不能超过 20 MB');
      }
      const suffix = result.photoUris[0].split('?')[0].split('.').pop()?.toLowerCase() ?? '';
      const extension = ['jpg', 'jpeg', 'png', 'webp', 'heic', 'heif', 'gif', 'bmp'].includes(suffix) ? suffix : 'img';
      const destination = this.context.filesDir + '/image-' + util.generateRandomUUID(true) + '.' + extension;
      try {
        await fileIo.copyFile(source.fd, destination);
        if ((await fileIo.stat(destination)).size > 20 * 1024 * 1024 || this.disposed) {
          throw new Error('图片导入已取消，请重新选择');
        }
        return destination;
      } catch {
        if (await fileIo.access(destination)) {
          await fileIo.unlink(destination);
        }
        throw new Error('图片导入失败，请重新选择');
      }
    } finally {
      await fileIo.close(source);
    }
  }

  async setColorMode(value: string): Promise<string> {
    if (this.disposed) {
      throw new Error('系统服务已释放');
    }
    let mode: ConfigurationConstant.ColorMode;
    switch (value) {
      case 'ON': mode = ConfigurationConstant.ColorMode.COLOR_MODE_DARK; break;
      case 'OFF': mode = ConfigurationConstant.ColorMode.COLOR_MODE_LIGHT; break;
      case 'AUTO':
      case 'MATERIAL_YOU': mode = ConfigurationConstant.ColorMode.COLOR_MODE_NOT_SET; break;
      default: throw new Error('无效的外观模式');
    }
    this.context.getApplicationContext().setColorMode(mode);
    return '';
  }

  async connectFeedback(payload: string): Promise<string> {
    if (this.disposed) {
      throw new Error('系统服务已释放');
    }
    const request = this.parseFeedbackRequest(payload);
    const key = JSON.stringify(request);
    if (this.feedbackConnection?.key === key) {
      return this.feedbackConnection.promise;
    }
    if (this.feedbackConnection !== undefined) {
      this.releaseFeedback(this.feedbackConnection, new Error('反馈连接已关闭'));
    }
    this.feedbackEvents = [];
    let socket: webSocket.WebSocket;
    try {
      socket = webSocket.createWebSocket();
    } catch {
      this.updateFeedbackState('FAILED', '反馈连接失败，请稍后重试');
      throw new Error('反馈连接失败，请稍后重试');
    }
    const active = new FeedbackConnection(socket, key);
    this.feedbackConnection = active;
    const failed = (): void => {
      if (this.feedbackConnection === active) {
        this.releaseFeedback(active, new Error('反馈连接失败，请稍后重试'), 'FAILED');
      }
    };
    active.timer = setTimeout(failed, 20000);
    try {
      socket.on('open', (error: BusinessError) => {
        if (this.feedbackConnection !== active) {
          return;
        }
        if (error) {
          failed();
          return;
        }
        clearTimeout(active.timer);
        active.ready = true;
        active.resolve?.('');
        active.resolve = undefined;
        active.reject = undefined;
        this.updateFeedbackState('CONNECTED', '');
      });
      socket.on('message', (error: BusinessError, value: string | ArrayBuffer) => {
        if (this.feedbackConnection !== active) {
          return;
        }
        if (error) {
          failed();
        } else if (typeof value === 'string') {
          // 仅缓存原始文本帧，业务协议由共享 Kotlin 模型解析。
          this.feedbackEvents.push(value);
          if (this.feedbackEvents.length > 128) {
            this.feedbackEvents.shift();
          }
          this.notifyFeedbackChanged();
        }
      });
      socket.on('error', failed);
      socket.on('close', (error: BusinessError, result: webSocket.CloseResult) => {
        if (this.feedbackConnection !== active) {
          return;
        }
        if (error || result?.code !== 1000) {
          failed();
        } else {
          this.releaseFeedback(active, new Error('反馈连接已关闭'));
        }
      });
      this.updateFeedbackState('CONNECTING', '反馈连接中');
      // 状态订阅者可能同步退出页面，释放后不能再发起握手。
      if (this.feedbackConnection === active) {
        socket.connect(request.url, { header: this.httpRequestHeaders(request.headers) }).then((connected: boolean) => {
          if (!connected) {
            failed();
          }
        }).catch(failed);
      }
    } catch {
      failed();
    }
    return active.promise;
  }

  async sendFeedback(content: string): Promise<string> {
    const active = this.feedbackConnection;
    if (this.disposed || active === undefined) {
      throw new Error('反馈连接已关闭');
    }
    await active.promise;
    if (this.feedbackConnection !== active || !active.ready) {
      throw new Error('反馈连接已关闭');
    }
    return new Promise<string>((resolve, reject) => {
      let settled = false;
      let timer = -1;
      const cancel = (error: Error): void => {
        if (!settled) {
          settled = true;
          clearTimeout(timer);
          active.pendingSends.delete(cancel);
          reject(error);
        }
      };
      const failed = (reason: Error): void => {
        if (!settled) {
          cancel(reason);
          this.releaseFeedback(active, reason, 'FAILED');
        }
      };
      timer = setTimeout(() => failed(new Error('反馈发送超时，请稍后重试')), 20000);
      active.pendingSends.add(cancel);
      try {
        active.socket.send(content).then((sent: boolean) => {
          if (settled) {
            return;
          }
          if (!sent || this.feedbackConnection !== active || !active.ready) {
            failed(new Error('反馈发送失败，请稍后重试'));
            return;
          }
          settled = true;
          clearTimeout(timer);
          active.pendingSends.delete(cancel);
          resolve('');
        }).catch(() => failed(new Error('反馈发送失败，请稍后重试')));
      } catch {
        failed(new Error('反馈发送失败，请稍后重试'));
      }
    });
  }

  async closeFeedback(payload: string): Promise<string> {
    this.resetFeedback();
    return '';
  }

  async feedbackSnapshot(payload: string): Promise<string> {
    const snapshot: FeedbackSnapshot = {
      state: this.feedbackState,
      message: this.feedbackMessage,
      events: this.feedbackEvents
    };
    this.feedbackEvents = [];
    return JSON.stringify(snapshot);
  }

  async readLicenses(payload: string): Promise<string> {
    const data = await this.context.resourceManager.getRawFileContent('composeResources/xhutimetable.composeapp.generated.resources/files/aboutlibraries.json');
    return new util.TextDecoder('utf-8').decodeToString(data);
  }

  async encryptLoginPassword(requestJson: string): Promise<string> {
    return encryptPassword(requestJson);
  }

  async requestHttp(requestJson: string): Promise<string> {
    if (this.disposed) {
      throw new Error('系统服务已释放');
    }
    const request = this.parseHttpRequest(requestJson);
    if (this.activeHttpRequests.has(request.requestId)) {
      throw new Error('HTTP 请求标识已在使用');
    }
    // ARRAY_BUFFER 和 usingCache 均从 API 9 起提供；不覆盖系统 TLS 校验配置。
    const options: http.HttpRequestOptions = {
      method: this.httpMethod(request.method),
      header: this.httpRequestHeaders(request.headers),
      expectDataType: http.HttpDataType.ARRAY_BUFFER,
      usingCache: false,
      usingProtocol: http.HttpProtocol.HTTP1_1,
      connectTimeout: request.connectTimeoutMs,
      readTimeout: request.readTimeoutMs
    };
    const base64 = new util.Base64Helper();
    if (request.bodyBase64.length > 0) {
      let body: Uint8Array;
      try {
        body = base64.decodeSync(request.bodyBase64);
      } catch {
        throw new Error('HTTP 请求体不是有效 Base64');
      }
      if (body.byteLength > 0) {
        options.extraData = body.buffer.slice(body.byteOffset, body.byteOffset + body.byteLength) as ArrayBuffer;
      }
    }
    const client = http.createHttp();
    try {
      const response = await new Promise<http.HttpResponse>((resolve, reject) => {
        this.activeHttpRequests.set(request.requestId, {
          client: client,
          cancel: (error: Error): void => reject(error)
        });
        try {
          client.request(request.url, options).then(resolve, (error: BusinessError) => {
            reject(this.httpNetworkError(error));
          });
        } catch (error) {
          reject(this.httpNetworkError(error as BusinessError));
        }
      });
      // 即使 SDK 已排队返回成功，先取得所有权的取消操作仍然必须生效。
      if (this.activeHttpRequests.get(request.requestId)?.client !== client) {
        throw new Error('HTTP 请求已取消');
      }
      let bodyBase64 = '';
      if (response.result instanceof ArrayBuffer) {
        if (response.result.byteLength > 0) {
          bodyBase64 = base64.encodeToStringSync(new Uint8Array(response.result));
        }
      } else if (response.result !== '') {
        throw new Error('HTTP 响应体不是二进制数据');
      }
      const result: HttpBridgeResponse = {
        status: response.responseCode,
        headers: this.httpResponseHeaders(response.header),
        bodyBase64: bodyBase64
      };
      // 4xx/5xx 同样返回，由共享层决定 HTTP 状态的业务含义。
      return JSON.stringify(result);
    } finally {
      this.releaseHttpRequest(request.requestId, client);
    }
  }

  async cancelHttp(requestId: string): Promise<string> {
    const active = this.activeHttpRequests.get(requestId);
    if (active !== undefined) {
      // 不依赖 destroy 后 SDK 是否继续回调，主动结束桥接等待。
      active.cancel(new Error('HTTP 请求已取消'));
      this.releaseHttpRequest(requestId, active.client);
    }
    return '';
  }

  async copyText(text: string): Promise<string> {
    const data = pasteboard.createData(pasteboard.MIMETYPE_TEXT_PLAIN, text);
    await pasteboard.getSystemPasteboard().setData(data);
    return '';
  }

  async shareText(text: string): Promise<string> {
    const record: systemShare.SharedRecord = {
      utd: uniformTypeDescriptor.UniformDataType.PLAIN_TEXT,
      content: text
    };
    return this.showSharePanel(new systemShare.SharedData(record));
  }

  async shareFiles(pathsJson: string): Promise<string> {
    let paths: string[];
    try {
      paths = JSON.parse(pathsJson) as string[];
    } catch {
      // 不透传可能包含输入内容的 JSON 解析错误。
      throw new Error('分享文件列表不是有效 JSON');
    }
    if (!Array.isArray(paths) || paths.length === 0) {
      throw new Error('分享文件列表必须是非空字符串数组');
    }
    for (const path of paths) {
      if (typeof path !== 'string' || !path.startsWith('/') || path.includes('\u0000')) {
        throw new Error('分享文件必须使用沙盒绝对路径');
      }
    }

    const firstRecord = await this.createFileRecord(paths[0]);
    const data = new systemShare.SharedData(firstRecord);
    for (let index = 1; index < paths.length; index++) {
      data.addRecord(await this.createFileRecord(paths[index]));
    }
    return this.showSharePanel(data);
  }

  async terminate(payload: string): Promise<string> {
    await this.context.terminateSelf();
    return '';
  }

  getAppInfo(): string {
    const bundle = bundleManager.getBundleInfoForSelfSync(bundleManager.BundleFlag.GET_BUNDLE_INFO_DEFAULT);
    const info: AppInfo = {
      bundleName: bundle.name,
      versionName: bundle.versionName,
      versionCode: bundle.versionCode,
      systemVersion: deviceInfo.osFullName,
      manufacturer: deviceInfo.manufacture,
      model: deviceInfo.productModel,
      networkConnected: connection.hasDefaultNetSync()
    };
    return JSON.stringify(info);
  }

  observeNetwork(callback: (connected: boolean) => void): void {
    if (this.disposed) {
      throw new Error('系统服务已释放');
    }
    // 首次查询也验证 GET_NETWORK_INFO 权限，失败直接交给调用方。
    const connected = connection.hasDefaultNetSync();
    this.networkCallback = callback;
    if (this.networkConnection === undefined) {
      // 不传 NetSpecifier，监听系统默认网络。
      const observer = connection.createNetConnection();
      this.networkConnection = observer;
      try {
        observer.on('netAvailable', () => this.refreshNetworkState());
        observer.on('netLost', () => this.refreshNetworkState());
        observer.on('netUnavailable', () => this.refreshNetworkState());
        observer.register((error: BusinessError) => {
          if (error) {
            this.networkConnection = undefined;
            this.networkCallback = undefined;
            this.logNetworkError('register', error);
            return;
          }
          this.networkRegistered = true;
          if (this.disposed) {
            // dispose 可能先于异步注册完成，注册成功后仍须注销。
            this.releaseNetworkObserver();
          } else {
            this.refreshNetworkState();
          }
        });
      } catch (error) {
        this.networkConnection = undefined;
        this.networkCallback = undefined;
        throw error as Error;
      }
    }
    callback(connected);
  }

  dispose(): void {
    this.disposed = true;
    this.messageHandler = undefined;
    this.resetFeedback();
    this.activeHttpRequests.forEach((active: ActiveHttpRequest, requestId: string) => {
      active.cancel(new Error('系统服务已释放'));
      this.releaseHttpRequest(requestId, active.client);
    });
    this.networkCallback = undefined;
    this.releaseNetworkObserver();
  }

  private parseFeedbackRequest(payload: string): FeedbackRequest {
    let request: FeedbackRequest;
    try {
      request = JSON.parse(payload) as FeedbackRequest;
    } catch {
      throw new Error('反馈连接参数无效');
    }
    if (request === null || typeof request !== 'object' || Array.isArray(request) ||
      typeof request.url !== 'string' || !/^wss:\/\//i.test(request.url) ||
      /[\u0000- \u007f]/.test(request.url) || !Array.isArray(request.headers)) {
      throw new Error('反馈连接参数无效');
    }
    let target: url.URL;
    try {
      target = new url.URL(request.url);
    } catch {
      throw new Error('反馈连接地址无效');
    }
    if (target.protocol !== 'wss:' || target.hostname.length === 0 ||
      target.username.length > 0 || target.password.length > 0) {
      throw new Error('反馈连接地址无效');
    }
    for (const header of request.headers) {
      if (header === null || typeof header !== 'object' || typeof header.name !== 'string' ||
        !/^[!#$%&'*+.^_`|~0-9A-Za-z-]+$/.test(header.name) || typeof header.value !== 'string' ||
        /[\r\n\u0000]/.test(header.value)) {
        throw new Error('反馈连接请求头无效');
      }
    }
    return request;
  }

  private resetFeedback(): void {
    this.feedbackEvents = [];
    if (this.feedbackConnection !== undefined) {
      this.releaseFeedback(this.feedbackConnection, new Error('反馈连接已关闭'));
    }
    this.updateFeedbackState('DISCONNECTED', '反馈连接已关闭');
  }

  private updateFeedbackState(state: FeedbackState, message: string): void {
    if (this.feedbackState === state && this.feedbackMessage === message) {
      return;
    }
    this.feedbackState = state;
    this.feedbackMessage = message;
    if (!this.disposed) {
      this.messageHandler?.('{"id":"","action":"feedbackState"}');
    }
  }

  private releaseFeedback(active: FeedbackConnection, reason: Error, state: FeedbackState = 'DISCONNECTED'): void {
    if (this.feedbackConnection !== active) {
      return;
    }
    this.feedbackConnection = undefined;
    active.key = '';
    active.ready = false;
    clearTimeout(active.timer);
    active.reject?.(reason);
    active.resolve = undefined;
    active.reject = undefined;
    active.pendingSends.forEach((cancel: (error: Error) => void) => cancel(reason));
    active.pendingSends.clear();
    try {
      active.socket.off('open');
      active.socket.off('message');
      active.socket.off('close');
      active.socket.off('error');
    } catch {
      hilog.warn(0, 'OhosServices', 'Feedback listener release failed');
    }
    try {
      active.socket.close().catch(() => {
        hilog.warn(0, 'OhosServices', 'Feedback connection release failed');
      });
    } catch {
      hilog.warn(0, 'OhosServices', 'Feedback connection release failed');
    }
    this.updateFeedbackState(state, state === 'FAILED' ? '反馈连接失败，请稍后重试' : '反馈连接已关闭');
  }

  private notifyFeedbackChanged(): void {
    if (!this.disposed) {
      this.messageHandler?.('{"id":"","action":"changed"}');
    }
  }

  private parseHttpRequest(requestJson: string): HttpBridgeRequest {
    let request: HttpBridgeRequest;
    try {
      request = JSON.parse(requestJson) as HttpBridgeRequest;
    } catch {
      // JSON 解析器的原始错误可能包含 URL、请求头或请求体。
      throw new Error('HTTP 请求不是有效 JSON');
    }
    if (request === null || typeof request !== 'object' || Array.isArray(request) ||
      typeof request.requestId !== 'string' || request.requestId.length === 0 ||
      typeof request.url !== 'string' || !/^https?:\/\//i.test(request.url) ||
      typeof request.method !== 'string' || !Array.isArray(request.headers) ||
      typeof request.bodyBase64 !== 'string' ||
      !Number.isInteger(request.connectTimeoutMs) || request.connectTimeoutMs < 0 ||
      !Number.isInteger(request.readTimeoutMs) || request.readTimeoutMs < 0) {
      throw new Error('HTTP 请求字段无效');
    }
    for (const header of request.headers) {
      if (header === null || typeof header !== 'object' ||
        typeof header.name !== 'string' || !/^[!#$%&'*+.^_`|~0-9A-Za-z-]+$/.test(header.name) ||
        typeof header.value !== 'string' || /[\r\n\u0000]/.test(header.value)) {
        throw new Error('HTTP 请求头无效');
      }
    }
    return request;
  }

  private httpMethod(method: string): http.RequestMethod {
    switch (method) {
      case 'OPTIONS': return http.RequestMethod.OPTIONS;
      case 'GET': return http.RequestMethod.GET;
      case 'HEAD': return http.RequestMethod.HEAD;
      case 'POST': return http.RequestMethod.POST;
      case 'PUT': return http.RequestMethod.PUT;
      case 'DELETE': return http.RequestMethod.DELETE;
      case 'TRACE': return http.RequestMethod.TRACE;
      case 'CONNECT': return http.RequestMethod.CONNECT;
      default: throw new Error('系统 HTTP SDK 不支持该请求方法');
    }
  }

  private httpRequestHeaders(headers: HttpHeader[]): Object {
    const combined = new Map<string, HttpHeader>();
    for (const header of headers) {
      const key = header.name.toLowerCase();
      const previous = combined.get(key);
      if (previous === undefined) {
        combined.set(key, { name: header.name, value: header.value });
      } else {
        previous.value += (key === 'cookie' ? '; ' : ', ') + header.value;
      }
    }
    // SDK 使用单值对象接收请求头；JSON 构造也能保留 __proto__ 等合法字段名。
    const fields: string[] = [];
    combined.forEach((header: HttpHeader) => {
      fields.push(JSON.stringify(header.name) + ':' + JSON.stringify(header.value));
    });
    return JSON.parse('{' + fields.join(',') + '}') as Object;
  }

  private httpResponseHeaders(headers: Object): HttpHeader[] {
    const values = headers as Record<string, string | string[]>;
    const result: HttpHeader[] = [];
    for (const name of Object.keys(values)) {
      const value = values[name];
      if (Array.isArray(value)) {
        for (const item of value) {
          result.push({ name: name, value: item });
        }
      } else {
        // 不按逗号切分，Set-Cookie 的 Expires 日期本身包含逗号。
        result.push({ name: name, value: value });
      }
    }
    return result;
  }

  private httpNetworkError(error: BusinessError): Error {
    // 系统原始错误消息可能带 URL；桥接只传固定文本和数值错误码。
    const code = error === null || error === undefined ? undefined : error.code;
    return new Error(typeof code === 'number' && Number.isFinite(code)
      ? 'HTTP 网络请求失败，错误码：' + code : 'HTTP 网络请求失败');
  }

  private releaseHttpRequest(requestId: string, client: http.HttpRequest): void {
    if (this.activeHttpRequests.get(requestId)?.client !== client) {
      return;
    }
    // JS 单线程下先删除所有权，完成、取消、dispose 只能有一处调用 destroy。
    this.activeHttpRequests.delete(requestId);
    try {
      client.destroy();
    } catch {
      // 单个原生对象释放异常不能阻止 dispose 继续释放其他请求和网络监听。
      hilog.error(0x0000, 'OhosServices', 'HTTP request release failed');
    }
  }

  private async createFileRecord(path: string): Promise<systemShare.SharedRecord> {
    const stat = await fileIo.stat(path);
    if (!stat.isFile() || !(await fileIo.access(path, fileIo.AccessModeType.READ))) {
      throw new Error('分享目标必须是可读取的文件');
    }
    const filename = path.substring(path.lastIndexOf('/') + 1);
    const extensionIndex = filename.lastIndexOf('.');
    const utd = extensionIndex > 0 && extensionIndex < filename.length - 1
      ? uniformTypeDescriptor.getUniformDataTypeByFilenameExtension(
        filename.substring(extensionIndex).toLowerCase(), uniformTypeDescriptor.UniformDataType.FILE)
      : uniformTypeDescriptor.UniformDataType.FILE;
    const record: systemShare.SharedRecord = {
      utd: utd,
      uri: fileUri.getUriFromPath(path),
      title: filename
    };
    return record;
  }

  private async showSharePanel(data: systemShare.SharedData): Promise<string> {
    // API 11 即可打开系统面板；不依赖 API 18 新增的 shareCompleted 事件。
    const controller = new systemShare.ShareController(data);
    await controller.show(this.context, {
      selectionMode: systemShare.SelectionMode.SINGLE,
      previewMode: systemShare.SharePreviewMode.DETAIL
    });
    return '';
  }

  private refreshNetworkState(): void {
    if (this.disposed || this.networkCallback === undefined) {
      return;
    }
    let connected: boolean;
    try {
      // 默认网络切换时旧网络丢失不代表离线，必须重新读取当前状态。
      connected = connection.hasDefaultNetSync();
    } catch (error) {
      this.logNetworkError('query', error as BusinessError);
      return;
    }
    this.networkCallback(connected);
  }

  private releaseNetworkObserver(): void {
    const observer = this.networkConnection;
    if (observer === undefined || !this.networkRegistered || this.networkUnregistering) {
      return;
    }
    this.networkUnregistering = true;
    try {
      observer.unregister((error: BusinessError) => {
        this.networkUnregistering = false;
        if (error) {
          // 保留注册状态，允许后续 dispose 再次尝试释放。
          this.logNetworkError('unregister', error);
          return;
        }
        this.networkRegistered = false;
        this.networkConnection = undefined;
      });
    } catch (error) {
      this.networkUnregistering = false;
      throw error as Error;
    }
  }

  private logNetworkError(operation: string, error: BusinessError): void {
    // void 监听接口没有异步错误返回通道；仅记录固定操作名和系统错误码。
    hilog.error(0x0000, 'OhosServices', 'Network %{public}s failed: %{public}d', operation, error.code);
  }
}
