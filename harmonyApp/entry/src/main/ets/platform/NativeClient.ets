import { common } from '@kit.AbilityKit';
import nativeApi from 'libentry.so';
import { OhosServices } from './OhosServices';

interface NativeRequest {
  id: string;
  action: string;
  payload: Object;
}

interface NativeResponse {
  id: string;
  action?: string;
  data?: Object | null;
  error?: string;
}

interface PendingRequest {
  resolve: (value: string) => void;
  reject: (reason: Error) => void;
  timer: number;
}

export class NativeRequests {
  private static nextId: number = 0;
  private pending: Map<string, PendingRequest> = new Map();
  private listeners: Set<() => void> = new Set();
  private send: (request: string) => void;
  private disposed: boolean = false;
  onStateChanged: (() => void) | undefined = undefined;

  constructor(send: (request: string) => void) {
    this.send = send;
  }

  request(action: string, payload: Object = new Object()): Promise<string> {
    if (this.disposed) {
      return Promise.reject(new Error('业务连接已关闭'));
    }
    const id = Date.now().toString(36) + '-' + (++NativeRequests.nextId).toString(36);
    return new Promise<string>((resolve, reject) => {
      const timer = setTimeout(() => {
        const pending = this.take(id);
        pending?.reject(new Error('请求超时，请检查网络后重试'));
      }, action === 'pickBackground' || action === 'avatar' ? 180000 : 30000);
      this.pending.set(id, { resolve: resolve, reject: reject, timer: timer });
      try {
        const request: NativeRequest = { id: id, action: action, payload: payload };
        this.send(JSON.stringify(request));
      } catch {
        this.take(id)?.reject(new Error('业务调用启动失败，请重试'));
      }
    });
  }

  subscribe(listener: () => void): () => void {
    if (!this.disposed) {
      this.listeners.add(listener);
    }
    return () => { this.listeners.delete(listener); };
  }

  receive(message: string): void {
    if (this.disposed) {
      return;
    }
    let response: NativeResponse;
    try {
      response = JSON.parse(message) as NativeResponse;
    } catch {
      return;
    }
    if (response === null || typeof response !== 'object' || Array.isArray(response) ||
      typeof response.id !== 'string') {
      return;
    }
    if (response.id === '' && (response.action === 'changed' || response.action === 'feedbackState')) {
      if (response.action === 'changed') {
        this.onStateChanged?.();
      }
      this.listeners.forEach((listener: () => void) => listener());
      return;
    }
    const pending = this.take(response.id);
    if (pending === undefined) {
      return;
    }
    if (typeof response.error === 'string' && response.error.length > 0) {
      pending.reject(new Error(response.error));
    } else {
      pending.resolve(JSON.stringify(response.data ?? null));
    }
  }

  dispose(): void {
    if (this.disposed) {
      return;
    }
    this.disposed = true;
    this.onStateChanged = undefined;
    this.listeners.clear();
    this.pending.forEach((pending: PendingRequest) => {
      clearTimeout(pending.timer);
      pending.reject(new Error('业务连接已关闭'));
    });
    this.pending.clear();
  }

  private take(id: string): PendingRequest | undefined {
    const pending = this.pending.get(id);
    if (pending !== undefined) {
      this.pending.delete(id);
      clearTimeout(pending.timer);
    }
    return pending;
  }
}

export class NativeClient {
  private context: common.UIAbilityContext;
  private services: OhosServices;
  private requests: NativeRequests;
  private started: boolean = false;
  private disposed: boolean = false;
  onStateChanged: (() => void) | undefined = undefined;

  constructor(context: common.UIAbilityContext) {
    this.context = context;
    this.services = new OhosServices(context);
    this.requests = new NativeRequests((request: string) => nativeApi.dispatch(request));
    this.requests.onStateChanged = () => this.onStateChanged?.();
  }

  start(): void {
    if (this.disposed) {
      throw new Error('业务连接已关闭');
    }
    if (this.started) {
      return;
    }
    this.services.setMessageHandler((message: string) => this.requests.receive(message));
    try {
      const appInfo = this.services.getAppInfo();
      this.started = true;
      nativeApi.initializeCore(this.context.filesDir, this.context.cacheDir, appInfo, this.services);
      this.services.observeNetwork((connected: boolean) => nativeApi.updateNetworkState(connected));
    } catch {
      this.dispose();
      throw new Error('应用初始化失败，请重新打开');
    }
  }

  request(action: string, payload: Object = new Object()): Promise<string> {
    if (this.disposed) {
      return Promise.reject(new Error('业务连接已关闭'));
    }
    if (!this.started) {
      return Promise.reject(new Error('业务连接尚未初始化'));
    }
    return this.requests.request(action, payload);
  }

  subscribe(listener: () => void): () => void {
    return this.requests.subscribe(listener);
  }

  dispose(): void {
    if (this.disposed) {
      return;
    }
    this.disposed = true;
    this.onStateChanged = undefined;
    this.requests.dispose();
    this.services.setMessageHandler(undefined);
    try {
      if (this.started) {
        nativeApi.disposeCore();
      }
    } finally {
      this.started = false;
      this.services.dispose();
    }
  }
}
