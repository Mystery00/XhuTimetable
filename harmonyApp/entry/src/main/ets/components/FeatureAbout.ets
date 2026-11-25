import { bundleManager } from '@kit.AbilityKit';
import { DisplayItem, ListResult } from '../model/NativeModels';
import { NativeClient } from '../platform/NativeClient';
import { FeatureUrlRequest } from './FeatureContracts';
import { FeatureError, featureError } from './FeatureControls';
import { ProfilePageHeader } from './ProfileUiControls';
import { DIALOG_CARD_WIDTH } from './DialogLayout';

interface LicenseQuery {
  kind: string;
  page: number;
}

@CustomDialog
struct LicenseDetailsDialog {
  controller?: CustomDialogController;
  title: string = '';
  content: string = '';
  url: string = '';
  onOpen: (url: string) => void = () => {};

  build() {
    Column({ space: 16 }) {
      Text(this.title).fontSize(20).fontWeight(600).fontColor($r('app.color.text_primary')).width('100%')
      Scroll() {
        Text(this.content).fontSize(12).lineHeight(18).fontColor($r('app.color.text_secondary')).width('100%')
          .copyOption(CopyOptions.LocalDevice)
      }.width('100%').constraintSize({ maxHeight: 420 }).scrollBar(BarState.Auto)
      Row() {
        if (this.url.length > 0) {
          Button('访问项目网站').fontSize(14).backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
            .onClick(() => this.onOpen(this.url))
        }
        Blank()
        Button('关闭').fontSize(14).backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
          .onClick(() => this.controller?.close())
      }.width('100%')
    }.width(DIALOG_CARD_WIDTH).padding(24).backgroundColor($r('app.color.container_high')).borderRadius(28)
  }
}

@Component
export struct FeatureAbout {
  client: NativeClient | undefined = undefined;
  onBack: () => void = () => {};
  onNavigate: (route: string) => void = () => {};
  @State private version: string = '';
  @State private versionError: string = '';
  @State private items: DisplayItem[] = [];
  @State private error: string = '';
  @State private loading: boolean = false;
  @State private hasMore: boolean = false;
  private page: number = 0;
  private active: boolean = false;
  private epoch: number = 0;
  private dialog?: CustomDialogController;

  aboutToAppear(): void { this.active = true; this.loadVersion(); this.load(); }
  aboutToDisappear(): void { this.active = false; this.epoch++; this.dialog?.close(); }

  private loadVersion(): void {
    try {
      const bundle = bundleManager.getBundleInfoForSelfSync(bundleManager.BundleFlag.GET_BUNDLE_INFO_DEFAULT);
      this.version = `${bundle.versionName}(${bundle.versionCode})`;
      this.versionError = '';
    } catch (error) {
      this.versionError = featureError(error as Error);
    }
  }

  private async load(): Promise<void> {
    if (this.loading || !this.active) return;
    const current = ++this.epoch;
    this.loading = true;
    this.error = '';
    const request: LicenseQuery = { kind: 'licenses', page: this.page + 1 };
    try {
      const result = JSON.parse(await this.client!.request('query', request)) as ListResult;
      if (!this.active || current !== this.epoch) return;
      this.items = this.items.concat(result.items);
      this.page = result.page;
      this.hasMore = result.hasMore;
    } catch (error) {
      if (this.active && current === this.epoch) this.error = featureError(error as Error);
    } finally {
      if (this.active && current === this.epoch) this.loading = false;
    }
  }

  private async open(url: string): Promise<void> {
    if (url.length === 0 || !this.active) return;
    const request: FeatureUrlRequest = { url: url };
    try { await this.client!.request('openUrl', request); }
    catch (error) { if (this.active) this.error = featureError(error as Error); }
  }

  private showLicense(item: DisplayItem): void {
    this.dialog?.close();
    this.dialog = new CustomDialogController({ builder: LicenseDetailsDialog({ title: item.title,
      content: item.detail || item.badge, url: item.url, onOpen: (url: string) => this.open(url) }),
      customStyle: true, autoCancel: true });
    this.dialog.open();
  }

  build() {
    Column() {
      ProfilePageHeader({ title: '开源依赖库', busy: this.loading, onBack: () => this.onBack() })
      List() {
        ListItem() {
          Column({ space: 4 }) {
            Image($rawfile('shared/ic_app_icon.svg')).width(120).height(120).objectFit(ImageFit.Cover).borderRadius(60)
              .accessibilityText('西瓜课表图标')
            Text('西瓜课表').fontSize(16).fontColor($r('app.color.text_primary'))
            if (this.versionError.length > 0) {
              FeatureError({ message: this.versionError, onRetry: () => this.loadVersion() })
            } else {
              Text(this.version).fontSize(16).fontColor($r('app.color.text_primary'))
            }
          }.width('100%').padding({ top: 32, bottom: 32 })
        }
        ForEach(this.items, (item: DisplayItem) => {
          ListItem() {
            Column({ space: 6 }) {
              Row({ space: 12 }) {
                Text(item.title).fontSize(16).fontWeight(FontWeight.Medium).fontColor($r('app.color.text_primary')).layoutWeight(1)
                Text(item.subtitle).fontSize(12).fontColor($r('app.color.text_secondary'))
              }.width('100%')
              if (item.badge.length > 0) {
                Text(item.badge).fontSize(12).fontColor($r('app.color.on_secondary_container'))
                  .padding({ left: 8, right: 8, top: 4, bottom: 4 }).backgroundColor($r('app.color.secondary_container')).borderRadius(8)
              }
            }.alignItems(HorizontalAlign.Start).width('100%').padding({ left: 16, right: 16, top: 16, bottom: 16 })
              .id('license-' + item.id).onClick(() => this.showLicense(item))
          }
        }, (item: DisplayItem) => item.id)
        if (this.error.length > 0) {
          ListItem() { FeatureError({ message: this.error, onRetry: () => this.load() }) }
        } else if (this.loading) {
          ListItem() {
            Text('正在加载…').width('100%').height(48).textAlign(TextAlign.Center).fontSize(14)
              .fontColor($r('app.color.text_secondary'))
          }
        }
      }.layoutWeight(1).width('100%').contentEndOffset(24).scrollBar(BarState.Auto)
        .onReachEnd(() => { if (this.hasMore && this.error.length === 0) this.load(); })
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
  }
}
