export interface PlatformServices {
  requestHttp(requestJson: string): Promise<string>;
  cancelHttp(requestId: string): Promise<string>;
  encryptLoginPassword(requestJson: string): Promise<string>;
  nativeMessage(message: string): Promise<string>;
  readLicenses(payload: string): Promise<string>;
  openUrl(url: string): Promise<string>;
  pickImage(payload: string): Promise<string>;
  setColorMode(value: string): Promise<string>;
  connectFeedback(payload: string): Promise<string>;
  sendFeedback(content: string): Promise<string>;
  closeFeedback(payload: string): Promise<string>;
  feedbackSnapshot(payload: string): Promise<string>;
  copyText(text: string): Promise<string>;
  shareText(text: string): Promise<string>;
  shareFiles(pathsJson: string): Promise<string>;
  terminate(payload: string): Promise<string>;
}

export const initializeCore: (filesDir: string, cacheDir: string, appInfo: string, services: PlatformServices) => void;
export const dispatch: (request: string) => void;
export const disposeCore: () => void;
export const updateNetworkState: (connected: boolean) => void;
