import { iconTint } from './IconTint';
import { DisplayItem, ListResult } from '../model/NativeModels';
import { NativeClient } from '../platform/NativeClient';
import { FeedbackStatus, FeatureTextRequest, FeatureUrlRequest } from './FeatureContracts';
import { FeatureError, FeatureHeader, dateText, featureError } from './FeatureControls';
import { withOpacity } from './HomeStyle';

// 与 Android UserInput 的表情表保持相同顺序。
const FEEDBACK_EMOJIS: string[] = [
  '\ud83d\ude00', '\ud83d\ude01', '\ud83d\ude02', '\ud83d\ude04',
  '\ud83d\ude05', '\ud83d\ude09', '\ud83d\ude0a', '\ud83d\ude0e',
  '\ud83d\ude0d', '\ud83d\ude18', '\ud83e\udd17', '\ud83e\udd14',
  '\ud83d\ude10', '\ud83d\ude44', '\ud83d\ude0f', '\ud83d\ude25',
  '\ud83d\ude2e', '\ud83d\ude34', '\ud83d\ude0c', '\ud83d\ude1c',
  '\ud83d\ude14', '\ud83d\ude15', '\ud83d\ude43', '\ud83d\ude37',
  '\ud83d\ude22', '\ud83d\ude2d', '\ud83d\ude31', '\ud83d\ude21',
  '\ud83d\udc4d', '\ud83d\ude4f', '\ud83c\udf89', '\u2764\ufe0f'
];
const FEEDBACK_MAX_LENGTH: number = 2000;

interface FeedbackHistoryRequest {
  kind: string;
  page: number;
  beforeId: string;
}

@Component
export struct FeedbackContent {
  client: NativeClient | undefined = undefined;
  onBack: () => void = () => {};
  @State private items: DisplayItem[] = [];
  @State private status: FeedbackStatus = { state: 'DISCONNECTED', message: '', adminOnline: false };
  @State private loading: boolean = false;
  @State private refreshing: boolean = false;
  @State private connecting: boolean = false;
  @State private sending: boolean = false;
  @State private message: string = '';
  @State private emojiPanel: boolean = false;
  private input: TextAreaController = new TextAreaController();
  private selectionStart: number = 0;
  private selectionEnd: number = 0;
  private draftRevision: number = 0;
  @State private error: string = '';
  @State private historyError: string = '';
  private failedOlder: boolean = false;
  @State private hasMore: boolean = false;
  @State private viewport: number = 0;
  private page: number = 0;
  private active: boolean = false;
  private unsubscribe?: () => void;
  private scroller: Scroller = new Scroller();
  private statusLoading: boolean = false;
  private statusQueued: boolean = false;
  private scrollBottom: boolean = true;
  private atBottom: boolean = true;
  private contentHeight: number = 0;
  private olderQueued: boolean = false;
  private latestQueued: boolean = false;

  aboutToAppear(): void {
    this.active = true;
    this.unsubscribe = this.client!.subscribe(() => {
      if (!this.active) return;
      this.refreshStatus();
      this.load(false);
    });
    this.connect(false);
    this.load(false);
  }

  aboutToDisappear(): void {
    this.active = false;
    this.unsubscribe?.();
    this.unsubscribe = undefined;
    this.client!.request('feedbackClose').catch(() => {});
  }

  private connected(): boolean { return this.status.state.toUpperCase() === 'CONNECTED'; }
  private stateText(): string {
    const state = this.status.state.toUpperCase();
    if (state === 'CONNECTING') return '正在连接…';
    if (state === 'CONNECTED') return this.status.adminOnline ? '管理员在线' : '已连接 · 留言即可收到回复';
    if (state === 'FAILED' || state === 'DISCONNECTED') return '连接已断开 · 点击重连';
    return this.status.message || '连接状态未知 · 点击重连';
  }

  private async refreshStatus(): Promise<void> {
    if (!this.active) return;
    if (this.statusLoading) { this.statusQueued = true; return; }
    this.statusLoading = true;
    try {
      const result = JSON.parse(await this.client!.request('feedbackStatus')) as FeedbackStatus;
      if (this.active) this.status = result;
    } catch (error) {
      if (this.active) this.error = featureError(error as Error);
    } finally {
      this.statusLoading = false;
      if (this.active && this.statusQueued) { this.statusQueued = false; this.refreshStatus(); }
    }
  }

  private async connect(reconnect: boolean): Promise<void> {
    if (this.connecting || !this.active) return;
    this.connecting = true;
    this.error = '';
    try {
      await Promise.all([this.client!.request(reconnect ? 'feedbackReconnect' : 'feedbackOpen'), this.refreshStatus()]);
    } catch (error) {
      if (this.active) this.error = featureError(error as Error);
    } finally {
      if (this.active) { this.connecting = false; await this.refreshStatus(); }
    }
  }

  private keepMessagePosition(id: string): void {
    const ui = this.getUIContext();
    const components = ui.getComponentUtils();
    // 相对消息列测量气泡，忽略下拉动画、日期分隔线变化和同时追加的新消息。
    const top = (): number => components.getRectangleById('feedback-message-' + id).windowOffset.y -
      components.getRectangleById('feedback-history-content').windowOffset.y;
    const previous = top();
    const observer = ui.getUIObserver();
    const restore = (): void => {
      observer.off('didLayout', restore);
      if (this.active && this.items.some((item: DisplayItem) => item.id === id)) {
        this.scroller.scrollBy(0, ui.px2vp(top() - previous));
      }
    };
    observer.on('didLayout', restore);
  }

  private async load(older: boolean): Promise<void> {
    if (!this.active) return;
    if (older && !this.hasMore) { this.refreshing = false; return; }
    if (this.loading) {
      if (older) this.olderQueued = true;
      else this.latestQueued = true;
      return;
    }
    this.loading = true;
    const request: FeedbackHistoryRequest = { kind: 'feedback', page: older ? this.page + 1 : 1,
      beforeId: older ? this.items[0].id : '' };
    try {
      const result = JSON.parse(await this.client!.request('query', request)) as ListResult;
      if (!this.active) return;
      const messages = result.items.slice().reverse();
      if (older) {
        const ids = new Set(this.items.map((item: DisplayItem) => item.id));
        const added = messages.filter((item: DisplayItem) => !ids.has(item.id));
        if (added.length > 0) {
          this.atBottom = false;
          this.scrollBottom = false;
          this.keepMessagePosition(request.beforeId);
          this.items = added.concat(this.items);
        }
        this.page = result.page;
        this.hasMore = result.hasMore && added.length > 0;
      } else {
        this.scrollBottom = this.atBottom || this.items.length === 0;
        const overlap = result.hasMore && messages.length > 0 ?
          this.items.findIndex((item: DisplayItem) => item.id === messages[0].id) : -1;
        this.items = overlap > 0 ? this.items.slice(0, overlap).concat(messages) : messages;
        if (overlap < 0 || (overlap === 0 && this.page <= 1)) {
          this.page = result.page;
          this.hasMore = result.hasMore;
        }
      }
      if (this.failedOlder === older || !this.hasMore) this.historyError = '';
    } catch (error) {
      if (this.active) {
        this.failedOlder = older;
        this.historyError = featureError(error as Error);
      }
    } finally {
      if (this.active) {
        this.loading = false;
        if (this.olderQueued) { this.olderQueued = false; this.load(this.items.length > 0); }
        else this.refreshing = false;
        if (this.latestQueued) { this.latestQueued = false; this.load(false); }
      }
    }
  }

  private setDraft(value: string): void {
    if (this.message === value) return;
    this.draftRevision++;
    this.message = value;
  }

  private toggleEmojiPanel(): void {
    const focus = this.getUIContext().getFocusController();
    if (this.emojiPanel) {
      focus.requestFocus('feedback-input');
    } else {
      this.emojiPanel = true;
      focus.clearFocus();
    }
  }

  private insertEmoji(emoji: string): void {
    const start = Math.max(0, Math.min(this.message.length, this.selectionStart, this.selectionEnd));
    const end = Math.max(start, Math.min(this.message.length, Math.max(this.selectionStart, this.selectionEnd)));
    const value = this.message.slice(0, start) + emoji + this.message.slice(end);
    if (value.length > FEEDBACK_MAX_LENGTH) return;
    this.setDraft(value);
    this.selectionStart = start + emoji.length;
    this.selectionEnd = this.selectionStart;
    this.input.caretPosition(this.selectionEnd);
  }

  private canSend(): boolean {
    return this.connected() && !this.sending && this.message.trim().length > 0;
  }

  private async send(): Promise<void> {
    if (!this.canSend()) return;
    const revision = this.draftRevision;
    const text = this.message.trim();
    this.sending = true;
    this.error = '';
    const request: FeatureTextRequest = { content: text };
    try {
      await this.client!.request('feedbackSend', request);
      if (!this.active) return;
      if (this.draftRevision === revision) {
        this.setDraft('');
        this.selectionStart = 0;
        this.selectionEnd = 0;
        this.emojiPanel = false;
      }
      this.atBottom = true;
      this.scrollBottom = true;
      this.scroller.scrollEdge(Edge.Bottom);
      this.load(false);
    } catch (error) {
      if (this.active) this.error = featureError(error as Error);
    } finally {
      if (this.active) { this.sending = false; this.refreshStatus(); }
    }
  }

  private parseTime(item: DisplayItem): Date { return new Date(item.time.replace(' ', 'T')); }
  private separator(index: number): string {
    const item = this.items[index];
    const now = this.parseTime(item);
    if (Number.isNaN(now.getTime())) return item.time;
    const previous = index > 0 ? this.parseTime(this.items[index - 1]) : undefined;
    if (!previous || Number.isNaN(previous.getTime()) || dateText(previous) !== dateText(now)) return dateText(now);
    if (now.getTime() - previous.getTime() > 3600000) return now.getHours().toString().padStart(2, '0') + ':' + now.getMinutes().toString().padStart(2, '0');
    return '';
  }

  private async openUrl(url: string): Promise<void> {
    if (!url) return;
    const request: FeatureUrlRequest = { url: url };
    try { await this.client!.request('openUrl', request); }
    catch (error) { if (this.active) this.error = featureError(error as Error); }
  }

  @Builder
  private messageBubble(item: DisplayItem, separator: string) {
    Column() {
      if (separator.length > 0) {
        Row({ space: 12 }) {
          Divider().layoutWeight(1).color($r('app.color.outline')).opacity(0.45)
          Text(separator).fontSize(11).fontColor($r('app.color.text_secondary'))
          Divider().layoutWeight(1).color($r('app.color.outline')).opacity(0.45)
        }.width('100%').padding({ left: 24, right: 24, top: 10, bottom: 10 })
      }
      Row() {
        Column() {
          if (item.image.length > 0) {
            Image(item.image).width(240).height(180).objectFit(ImageFit.Contain).onClick(() => this.openUrl(item.url))
          }
          Text() {
            ForEach(item.detail.replace(/[\r\n]+$/, '').split(/(https?:\/\/[^\s<>"，。；]+)/i), (part: string) => {
              if (/^https?:\/\//i.test(part)) {
                Span(part).decoration({ type: TextDecorationType.Underline })
                  .fontColor(item.status === 'sent' ? $r('app.color.on_primary') : $r('app.color.primary'))
                  .onClick(() => this.openUrl(part))
              } else {
                Span(part)
              }
            })
          }.fontSize(14).copyOption(CopyOptions.LocalDevice).id('feedback-text-' + item.id)
            .fontColor(item.status === 'sent' ? $r('app.color.on_primary') : $r('app.color.text_primary'))
          if (item.url.length > 0 && item.image.length === 0) {
            Button('打开附件').fontSize(12).backgroundColor(Color.Transparent)
              .fontColor(item.status === 'sent' ? $r('app.color.on_primary') : $r('app.color.primary')).onClick(() => this.openUrl(item.url))
          }
        }.id('feedback-message-' + item.id).constraintSize({ maxWidth: 290 }).padding({ left: 14, right: 14, top: 10, bottom: 10 })
          .backgroundColor(item.status === 'sent' ? $r('app.color.primary') : $r('app.color.container_high'))
          .borderRadius({ topLeft: item.status === 'sent' ? 18 : 4, topRight: 18, bottomLeft: 18, bottomRight: item.status === 'sent' ? 4 : 18 })
          .alignItems(HorizontalAlign.Start)
      }.width('100%').justifyContent(item.status === 'sent' ? FlexAlign.End : FlexAlign.Start)
        .padding({ left: 14, right: 14, top: 4, bottom: 4 })
    }.width('100%')
  }

  @Builder
  private composer() {
    Column() {
      Divider().color(withOpacity($r('app.color.outline'), 0.4))
      Row({ space: 8 }) {
        Button({ type: ButtonType.Circle }) {
          SymbolGlyph(this.emojiPanel ? $r('sys.symbol.keyboard') : $r('sys.symbol.face_smiling')).fontSize(24)
            .fontColor([this.emojiPanel ? $r('app.color.primary') : $r('app.color.text_secondary')])
        }.id('feedback-emoji-toggle').width(42).height(42).padding(0).backgroundColor(Color.Transparent)
          .focusOnTouch(false).accessibilityText(this.emojiPanel ? '关闭表情' : '输入表情').onClick(() => this.toggleEmojiPanel())
        TextArea({ text: this.message, controller: this.input, placeholder: '输入你的问题或建议…' })
          .layoutWeight(1).id('feedback-input').maxLines(5).maxLength(FEEDBACK_MAX_LENGTH)
          .constraintSize({ minHeight: 42, maxHeight: 128 }).padding({ left: 16, right: 16, top: 10, bottom: 10 })
          .fontSize(14).lineHeight(20).fontColor($r('app.color.text_primary')).caretColor($r('app.color.primary'))
          .placeholderColor(withOpacity($r('app.color.text_secondary'), 0.7)).backgroundColor($r('app.color.container_lowest'))
          .border({ width: 0.8, color: withOpacity($r('app.color.outline'), 0.55), radius: 22 })
          .enterKeyType(EnterKeyType.Send).onSubmit(() => this.send()).onChange((value: string) => this.setDraft(value))
          .onTextSelectionChange((start: number, end: number) => {
            if (!this.emojiPanel && start >= 0 && end >= 0) { this.selectionStart = start; this.selectionEnd = end; }
          })
          .onFocus(() => {
            if (this.emojiPanel) {
              this.emojiPanel = false;
              this.input.setTextSelection(this.selectionStart, this.selectionEnd, { menuPolicy: MenuPolicy.HIDE });
            }
            this.atBottom = true;
            this.scrollBottom = true;
            this.scroller.scrollEdge(Edge.Bottom);
          })
        Button({ type: ButtonType.Circle }) {
          if (this.sending) { LoadingProgress().width(19).height(19).color($r('app.color.on_primary')) }
          else { Image($rawfile('shared/ic_action_send.svg')).width(19).height(19)
            .colorFilter(iconTint(this.canSend() ? $r('app.color.on_primary') : $r('app.color.text_secondary'))) }
        }.id('feedback-send').width(42).height(42).padding(0).accessibilityText('发送').focusOnTouch(false)
          .backgroundColor(this.canSend() || this.sending ? $r('app.color.primary') : $r('app.color.container_highest'))
          .enabled(this.canSend()).onClick(() => this.send())
      }.width('100%').alignItems(VerticalAlign.Bottom).padding({ left: 10, right: 10, top: 8, bottom: 8 })
      if (this.emojiPanel) {
        Divider().color(withOpacity($r('app.color.outline'), 0.35))
        Grid() {
          ForEach(FEEDBACK_EMOJIS, (emoji: string, index: number) => {
            GridItem() {
              Button(emoji).id('feedback-emoji-' + index).width('100%').height(40).padding(0)
                .fontSize(20).fontColor($r('app.color.text_primary')).borderRadius(10).backgroundColor(Color.Transparent)
                .focusOnTouch(false).onClick(() => this.insertEmoji(emoji))
            }
          }, (emoji: string) => emoji)
        }.id('feedback-emoji-panel').columnsTemplate('1fr 1fr 1fr 1fr 1fr 1fr 1fr 1fr')
          .width('100%').height(176).padding(8).backgroundColor($r('app.color.surface'))
      }
    }.width('100%').backgroundColor($r('app.color.container'))
  }

  build() {
    Column() {
      FeatureHeader({ title: '意见反馈', action: this.connected() ? '' : '重新连接', actionIcon: this.connected() ? '' : 'refresh',
        subtitle: this.stateText(), subtitleColor: this.status.state.toUpperCase() === 'CONNECTING' ? '#FFA000' :
          (this.connected() ? (this.status.adminOnline ? '#2E7D32' : $r('app.color.primary')) : $r('app.color.danger')),
        titleActionEnabled: !this.connected() && !this.connecting, onTitle: () => this.connect(true),
        busy: this.connecting, onBack: () => this.onBack(), onAction: () => this.connect(true) })
      if (this.error.length > 0) { FeatureError({ message: this.error, onRetry: () => { this.error = ''; if (this.connected()) this.load(false); else this.connect(true); } }) }
      else if (this.historyError.length > 0) {
        FeatureError({ message: this.historyError, onRetry: () => this.load(this.failedOlder && this.items.length > 0) })
      }
      Refresh({ refreshing: $$this.refreshing }) {
        Scroll(this.scroller) {
          Column() {
            ForEach(this.items, (item: DisplayItem, index: number) => {
              this.messageBubble(item, this.separator(index))
            }, (item: DisplayItem, index: number) => JSON.stringify([item.id, item.detail, item.status, item.url, item.image, this.separator(index)]))
          }.id('feedback-history-content').width('100%').constraintSize({ minHeight: this.viewport }).justifyContent(FlexAlign.End)
            .padding({ top: 12, bottom: 12 }).onAreaChange((_old: Area, area: Area) => {
              this.contentHeight = Number(area.height);
              if (this.scrollBottom) { this.scrollBottom = false; this.scroller.scrollEdge(Edge.Bottom); }
            })
        }.id('feedback-history').width('100%').height('100%').scrollBar(BarState.Auto)
          .onSizeChange((_old: SizeOptions, size: SizeOptions) => {
            this.viewport = Number(size.height);
            if (this.atBottom) this.scroller.scrollEdge(Edge.Bottom);
          })
          .onScroll(() => { this.atBottom = this.scroller.currentOffset().yOffset + this.viewport >= this.contentHeight - 40; })
      }.id('feedback-refresh').layoutWeight(1).width('100%').onRefreshing(() => this.load(this.items.length > 0))
      this.composer()
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
  }
}
