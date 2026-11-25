import { NativeClient } from '../platform/NativeClient';
import { withOpacity } from './AppComponents';
import { FeatureIcon, featureError } from './FeatureControls';
import { DIALOG_CARD_WIDTH } from './DialogLayout';

// AutoScoreStatusResponse 的 Instant 序列化为毫秒时间戳，LocalDate 为 yyyy-MM-dd。
interface AutoScoreStatus {
  hasActiveTask: boolean;
  status: string | null;
  expireDate: string | null;
  nextCheckTime: number | null;
  lastCheckTime: number | null;
  lastCheckResult: string | null;
}

@CustomDialog
export struct ScoreReminderDialog {
  controller?: CustomDialogController;
  client: NativeClient | undefined = undefined;
  multipleAccounts: boolean = false;
  @State private status: AutoScoreStatus | null = null;
  @State private busy: boolean = false;
  @State private confirming: boolean = false;
  @State private error: string = '';
  private visible: boolean = false;

  aboutToAppear(): void { this.visible = true; this.refresh(); }
  aboutToDisappear(): void { this.visible = false; }

  private isActive(): boolean {
    return this.status?.hasActiveTask === true && this.status?.status === 'ACTIVE';
  }

  private statusLabel(): string {
    if (!this.status) return this.busy ? '查询中…' : '状态未知';
    if (this.isActive()) return '已开启';
    switch (this.status.status) {
      case 'SUSPENDED': return '已暂停';
      case 'EXPIRED': return '已到期';
      case 'CANCELLED': return '已取消';
      default: return '未开启';
    }
  }

  private dateLabel(iso: string | null): string {
    return iso ? iso.slice(0, 4) + '年' + iso.slice(5, 7) + '月' + iso.slice(8, 10) + '日' : '暂无';
  }

  private checkTime(millis: number | null): string {
    if (millis === null) return '暂无';
    // 成绩任务使用北京时间，不随设备时区变化。
    const chinaTime = new Date(millis + 8 * 60 * 60 * 1000).toISOString();
    return this.dateLabel(chinaTime) + ' ' + chinaTime.slice(11, 13) + '时' + chinaTime.slice(14, 16) + '分';
  }

  private async refresh(): Promise<void> {
    if (!this.visible || this.busy) return;
    this.busy = true;
    this.error = '';
    try {
      const status = JSON.parse(await this.client!.request('autoScoreStatus')) as AutoScoreStatus;
      if (this.visible) this.status = status;
    } catch (error) {
      if (this.visible) {
        this.status = null;
        this.error = featureError(error as Error);
      }
    } finally {
      if (this.visible) this.busy = false;
    }
  }

  private async stop(): Promise<void> {
    if (!this.visible || !this.confirming || this.busy || !this.isActive()) return;
    this.busy = true;
    try {
      await this.client!.request('stopAutoScore');
    } catch (error) {
      if (this.visible) this.error = featureError(error as Error);
      return;
    } finally {
      if (this.visible) {
        this.status = null;
        this.confirming = false;
        this.busy = false;
      }
    }
    await this.refresh();
  }

  @Builder
  private meta(label: string, value: string) {
    Row({ space: 12 }) {
      Text(label).fontSize(12).fontColor($r('app.color.text_secondary'))
      Text(value).fontSize(12).fontWeight(FontWeight.Medium).fontColor($r('app.color.text_primary'))
        .layoutWeight(1).textAlign(TextAlign.End)
    }.width('100%').alignItems(VerticalAlign.Top)
  }

  @Builder
  private subscription() {
    Column({ space: 10 }) {
      Row({ space: 10 }) {
        Column({ space: 2 }) {
          Text(this.multipleAccounts ? '当前订阅状态（主账号）' : '当前订阅状态').fontSize(11).fontColor($r('app.color.text_secondary'))
          Text(this.statusLabel()).fontSize(16).fontWeight(FontWeight.Bold)
            .fontColor(this.isActive() ? $r('app.color.primary') : $r('app.color.text_primary'))
        }.layoutWeight(1).alignItems(HorizontalAlign.Start)
        if (this.isActive()) {
          Button('取消订阅').height(40).padding({ left: 24, right: 24 }).fontSize(14)
            .backgroundColor(Color.Transparent).fontColor($r('app.color.text_secondary'))
            .border({ width: 1, color: $r('app.color.outline') }).enabled(!this.busy)
            .onClick(() => { this.confirming = true; })
        } else {
          Button('开启提醒').fontSize(14).enabled(false)
        }
      }.padding({ left: 14, right: 14, top: 10, bottom: 10 }).width('100%').borderRadius(14)
        .backgroundColor($r('app.color.container_lowest')).border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.45) })
      if (this.status) {
        Column({ space: 4 }) {
          this.meta('任务过期时间', this.dateLabel(this.status.expireDate))
          this.meta('下次检查时间', this.checkTime(this.status.nextCheckTime))
          this.meta('上次执行时间', this.checkTime(this.status.lastCheckTime))
          this.meta('上次执行结果', this.status.lastCheckResult || '未知')
        }.width('100%')
      }
      if (this.status?.status === 'SUSPENDED') {
        Text('自动查分已暂停，请检查账号状态。').fontSize(12).fontColor($r('app.color.danger')).width('100%')
      }
      if (!this.isActive()) {
        Text('鸿蒙端尚未接入推送，暂不能开启提醒。').fontSize(12).fontColor($r('app.color.text_secondary')).width('100%')
      }
      if (this.error.length > 0) {
        Text(this.error).fontSize(12).fontColor($r('app.color.danger')).width('100%')
      }
    }.width('100%')
  }

  build() {
    Column() {
      Column({ space: 10 }) {
        Stack() { FeatureIcon({ name: 'notifications-active', tint: this.confirming ? $r('app.color.danger') : $r('app.color.primary') }) }
          .width(28).height(28).accessibilityLevel('no')
        Text(this.confirming ? '取消成绩变更提醒' : '成绩变更提醒').fontSize(24).lineHeight(32).fontWeight(600)
          .fontColor($r('app.color.text_primary'))
      }.width('100%').alignItems(HorizontalAlign.Start).padding({ left: 24, right: 24, top: 24, bottom: 12 })
      if (this.confirming) {
        Text('取消后，服务端将停止主账号当前的成绩查询订阅任务。').fontSize(14).fontColor($r('app.color.text_secondary'))
          .width('100%').padding({ left: 20, right: 20, top: 8, bottom: 8 })
      } else {
        Scroll() { this.subscription() }.constraintSize({ maxHeight: 320 }).width('100%')
          .padding({ left: 20, right: 20, top: 8, bottom: 8 })
      }
      Row({ space: 8 }) {
        if (this.confirming) {
          Blank()
          Button('返回').height(40).fontSize(14).backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
            .enabled(!this.busy).onClick(() => { this.confirming = false; })
          Button(this.busy ? '取消中…' : '取消订阅').height(40).fontSize(14).backgroundColor($r('app.color.error_container'))
            .fontColor($r('app.color.on_error_container')).enabled(!this.busy).onClick(() => this.stop())
        } else {
          Button(this.busy ? '刷新中…' : '刷新状态').height(40).fontSize(14).backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
            .enabled(!this.busy).onClick(() => this.refresh())
          Blank()
          Button('关闭').height(40).fontSize(14).backgroundColor(Color.Transparent).fontColor($r('app.color.primary')).onClick(() => this.controller?.close())
        }
      }.width('100%').padding({ left: 16, right: 20, top: 8, bottom: 16 })
    }.width(DIALOG_CARD_WIDTH).backgroundColor($r('app.color.container_high')).borderRadius(28)
      .shadow({ radius: 8, color: '#33000000', offsetY: 4 })
  }
}

@Component
export struct ScoreListHeader {
  @Prop expanded: boolean = true;
  onToggle: () => void = () => {};

  build() {
    Row() {
      Text('课程成绩').fontSize(14).fontWeight(600).fontColor($r('app.color.text_primary')).layoutWeight(1)
      Button() {
        Row({ space: 4 }) {
          Shape() {
            Path().commands(this.expanded ? 'M7 4 L12 9 L17 4 M7 20 L12 15 L17 20' : 'M7 9 L12 4 L17 9 M7 15 L12 20 L17 15')
              .fill(Color.Transparent).stroke($r('app.color.primary')).strokeWidth('2px')
              .strokeLineCap(LineCapStyle.Round).strokeLineJoin(LineJoinStyle.Round)
          }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(16).height(16)
          Text(this.expanded ? '收起详情' : '展开详情').fontSize(14).fontColor($r('app.color.primary'))
        }
      }.backgroundColor(Color.Transparent).onClick(() => this.onToggle())
    }.padding({ left: 20, right: 12, top: 8, bottom: 4 }).width('100%')
  }
}
