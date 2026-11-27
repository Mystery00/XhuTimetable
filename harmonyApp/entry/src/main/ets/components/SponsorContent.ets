import { NativeClient } from '../platform/NativeClient';
import { FeatureUrlRequest } from './FeatureContracts';
import { FeatureEmpty, FeatureError, featureError } from './FeatureControls';
import { ProfilePageHeader } from './ProfileUiControls';
import { withOpacity } from './AppComponents';

interface SponsorQueryRequest {
  kind: string;
}

interface SponsorItem {
  id: string;
  name: string;
  avatar: string;
  dateText: string;
  amount: string;
  color: string;
}

interface SponsorQueryResult {
  title: string;
  link: string;
  count: number;
  items: SponsorItem[];
}

const HEART_ICON = 'M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z';
const OPEN_LINK_ICON = 'M14 3h7v7M10 14L21 3M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6';

@Component
export struct SponsorContent {
  client: NativeClient | undefined = undefined;
  @Prop navigationInset: number = 0;
  onBack: () => void = () => {};
  @State private items: SponsorItem[] = [];
  @State private link: string = '';
  @State private count: number = 0;
  @State private loading: boolean = false;
  @State private refreshing: boolean = false;
  @State private error: string = '';
  @State private failedAvatars: string[] = [];
  private active: boolean = false;
  private epoch: number = 0;

  aboutToAppear(): void {
    this.active = true;
    this.load(false);
  }

  aboutToDisappear(): void {
    this.active = false;
    this.epoch++;
  }

  private async load(isRefresh: boolean): Promise<void> {
    if (this.client === undefined || !this.active) return;
    if (isRefresh && this.loading) {
      this.refreshing = false;
      return;
    }
    const current = ++this.epoch;
    if (isRefresh) {
      this.refreshing = true;
    } else {
      this.loading = true;
    }
    this.error = '';
    const request: SponsorQueryRequest = { kind: 'sponsors' };
    try {
      const result = JSON.parse(await this.client.request('query', request)) as SponsorQueryResult;
      if (!this.active || current !== this.epoch) return;
      this.items = result.items ?? [];
      this.link = result.link ?? '';
      this.count = result.count ?? this.items.length;
      this.failedAvatars = [];
    } catch (error) {
      if (!this.active || current !== this.epoch) return;
      const message = featureError(error as Error);
      if (this.items.length > 0) {
        this.getUIContext().getPromptAction().showToast({ message: message, duration: 2500 });
      } else {
        this.error = message;
      }
    } finally {
      if (this.active && current === this.epoch) {
        this.loading = false;
        this.refreshing = false;
      }
    }
  }

  private async openSponsorLink(): Promise<void> {
    if (this.client === undefined || this.link.length === 0 || !this.active) return;
    const request: FeatureUrlRequest = { url: this.link };
    try {
      await this.client.request('openUrl', request);
    } catch (error) {
      if (this.active) {
        this.getUIContext().getPromptAction().showToast({ message: featureError(error as Error) });
      }
    }
  }

  private initialOf(name: string): string {
    const trimmed = name.trim();
    return trimmed.length > 0 ? trimmed.charAt(0).toUpperCase() : '赞';
  }

  private markAvatarFailed(id: string): void {
    if (!this.failedAvatars.includes(id)) {
      this.failedAvatars = this.failedAvatars.concat([id]);
    }
  }

  @Builder
  private headerCard() {
    Column({ space: 14 }) {
      Row({ space: 12 }) {
        Stack() {
          Circle().width(44).height(44).fill($r('app.color.primary_container'))
          Shape() {
            Path().commands(HEART_ICON).fill($r('app.color.primary')).stroke(Color.Transparent)
          }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(22).height(22)
        }.width(44).height(44)

        Column({ space: 4 }) {
          Text('感谢每一位支持者')
            .fontSize(16)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary'))
            .width('100%')
          Text('西瓜课表由开发者利用课余时间持续维护，感谢大家的支持让项目走得更远。')
            .fontSize(12)
            .lineHeight(18)
            .fontColor($r('app.color.text_secondary'))
            .width('100%')
        }.layoutWeight(1).alignItems(HorizontalAlign.Start)
      }.width('100%').alignItems(VerticalAlign.Top)

      Row() {
        Text('共 ' + this.count.toString() + ' 位支持者')
          .fontSize(12)
          .fontWeight(FontWeight.Medium)
          .fontColor($r('app.color.text_secondary'))
          .padding({ left: 10, right: 10, top: 6, bottom: 6 })
          .borderRadius(10)
          .backgroundColor(withOpacity($r('app.color.surface'), 0.85))
        Blank()
        if (this.link.length > 0) {
          Button({ type: ButtonType.Normal }) {
            Row({ space: 6 }) {
              Shape() {
                Path().commands(OPEN_LINK_ICON).fill(Color.Transparent).stroke($r('app.color.on_secondary_container'))
                  .strokeWidth('2px').strokeLineCap(LineCapStyle.Round).strokeLineJoin(LineJoinStyle.Round)
              }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(15).height(15)
              Text('了解更多支持方式')
                .fontSize(13)
                .fontWeight(FontWeight.Medium)
                .fontColor($r('app.color.on_secondary_container'))
            }.padding({ left: 12, right: 12, top: 8, bottom: 8 })
          }
          .backgroundColor($r('app.color.secondary_container'))
          .borderRadius(12)
          .height(36)
          .id('sponsor-more-link')
          .onClick(() => this.openSponsorLink())
        }
      }.width('100%').alignItems(VerticalAlign.Center)
    }
    .width('100%')
    .padding(18)
    .borderRadius(22)
    .backgroundColor(withOpacity($r('app.color.primary_container'), 0.45))
    .border({ width: 0.5, color: withOpacity($r('app.color.primary'), 0.18) })
  }

  @Builder
  private sponsorCard(item: SponsorItem) {
    Row({ space: 12 }) {
      Stack() {
        Circle().width(46).height(46).fill(item.color)
        Text(this.initialOf(item.name))
          .fontSize(16)
          .fontWeight(600)
          .fontColor('#FFFFFF')
        if (item.avatar.length > 0 && !this.failedAvatars.includes(item.id)) {
          Image(item.avatar)
            .width(46)
            .height(46)
            .borderRadius(23)
            .objectFit(ImageFit.Cover)
            .onError(() => this.markAvatarFailed(item.id))
        }
      }.width(46).height(46).clip(true).borderRadius(23)

      Column({ space: 4 }) {
        Text(item.name.length > 0 ? item.name : '匿名支持者')
          .fontSize(15)
          .fontWeight(600)
          .fontColor($r('app.color.text_primary'))
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
          .width('100%')
        Text(item.dateText)
          .fontSize(12)
          .fontColor($r('app.color.text_secondary'))
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
          .width('100%')
      }.layoutWeight(1).alignItems(HorizontalAlign.Start)

      if (item.amount.length > 0) {
        Text(item.amount)
          .fontSize(12)
          .fontWeight(600)
          .fontColor($r('app.color.on_primary_container'))
          .padding({ left: 10, right: 10, top: 5, bottom: 5 })
          .borderRadius(12)
          .backgroundColor(withOpacity($r('app.color.primary_container'), 0.72))
      }
    }
    .width('100%')
    .padding({ left: 14, right: 14, top: 12, bottom: 12 })
    .borderRadius(18)
    .backgroundColor($r('app.color.card'))
    .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) })
  }

  build() {
    Column() {
      ProfilePageHeader({ title: '赞助鸣谢', busy: this.loading && this.items.length > 0, onBack: () => this.onBack() })
      Refresh({ refreshing: $$this.refreshing }) {
        if (this.loading && this.items.length === 0) {
          Column({ space: 12 }) {
            LoadingProgress().width(36).height(36).color($r('app.color.primary'))
            Text('正在加载鸣谢名单…').fontSize(14).fontColor($r('app.color.text_secondary'))
          }.width('100%').height('100%').justifyContent(FlexAlign.Center)
        } else if (this.error.length > 0 && this.items.length === 0) {
          Column() {
            FeatureError({ message: this.error, onRetry: () => this.load(false) })
          }.width('100%').height('100%').padding(20).justifyContent(FlexAlign.Center)
        } else if (this.items.length === 0) {
          Scroll() {
            FeatureEmpty({ route: 'sponsors', title: '暂无赞助名单', description: '下拉刷新，稍后再来看看' })
          }.width('100%').height('100%').scrollBar(BarState.Off)
        } else {
          List({ space: 12 }) {
            ListItem() {
              this.headerCard()
            }
            ListItem() {
              Row() {
                Text('鸣谢名单')
                  .fontSize(13)
                  .fontWeight(600)
                  .fontColor($r('app.color.primary'))
                Blank()
                Text('共 ' + this.count.toString() + ' 位')
                  .fontSize(12)
                  .fontColor($r('app.color.text_secondary'))
              }.width('100%').padding({ left: 4, right: 4, top: 2, bottom: 2 })
            }
            ForEach(this.items, (item: SponsorItem) => {
              ListItem() {
                this.sponsorCard(item)
              }
            }, (item: SponsorItem) => item.id)
          }
          .width('100%')
          .height('100%')
          .padding({ left: 16, right: 16, top: 12 })
          .contentEndOffset(24 + this.navigationInset)
          .scrollBar(BarState.Auto)
        }
      }
      .onRefreshing(() => { this.load(true); })
      .layoutWeight(1)
      .width('100%')
    }
    .width('100%')
    .height('100%')
    .backgroundColor($r('app.color.background'))
  }
}
