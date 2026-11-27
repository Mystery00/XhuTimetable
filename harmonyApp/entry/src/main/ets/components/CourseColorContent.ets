import { DisplayItem, ListResult } from '../model/NativeModels';
import { NativeClient } from '../platform/NativeClient';
import { dialogWidth } from './DialogLayout';
import { withOpacity } from './AppComponents';
import { FeatureColorDialog, FeatureEmpty, FeatureError, FeatureIcon, featureError, featureTitle } from './FeatureControls';

interface CourseColorQuery {
  kind: string;
  keywords: string;
}

interface CourseColorChange {
  kind: string;
  id: string;
  values?: Record<string, string>;
}

@Component
export struct CourseColorContent {
  client: NativeClient | undefined = undefined;
  @Prop navigationInset: number = 0;
  onBack: () => void = () => {};
  registerBackHandler: (handler: (() => boolean) | undefined) => void = () => {};
  @State private courses: DisplayItem[] = [];
  @State private searching: boolean = false;
  @State private keywords: string = '';
  @State private loading: boolean = true;
  @State private saving: boolean = false;
  @State private error: string = '';
  private active: boolean = false;
  private queryVersion: number = 0;
  private dialog?: CustomDialogController;
  private listScroller: Scroller = new Scroller();

  aboutToAppear(): void {
    this.active = true;
    this.registerBackHandler(() => this.closeSearch());
    this.load();
  }

  aboutToDisappear(): void {
    this.active = false;
    this.queryVersion++;
    this.dialog?.close();
    this.registerBackHandler(undefined);
  }

  private closeSearch(): boolean {
    if (!this.searching) return false;
    this.searching = false;
    this.setKeywords('');
    return true;
  }

  private setKeywords(value: string): void {
    if (this.keywords === value) return;
    this.keywords = value;
    if (this.courses.length > 0) this.listScroller.scrollEdge(Edge.Top);
    this.load();
  }

  private async load(): Promise<void> {
    const version = ++this.queryVersion;
    this.loading = true;
    this.error = '';
    try {
      const query: CourseColorQuery = { kind: 'courseColors', keywords: this.keywords };
      const result = JSON.parse(await this.client!.request('query', query)) as ListResult;
      if (this.active && version === this.queryVersion) this.courses = result.items;
    } catch (error) {
      if (this.active && version === this.queryVersion) this.error = featureError(error as Error);
    } finally {
      if (this.active && version === this.queryVersion) this.loading = false;
    }
  }

  private choose(course: DisplayItem): void {
    if (this.saving) return;
    this.getUIContext().getFocusController().clearFocus();
    this.dialog = new CustomDialogController({
      builder: FeatureColorDialog({ title: course.title, initialValue: course.color,
        onConfirm: (color: string) => this.update(course.id, color), onReset: () => this.update(course.id) }),
      customStyle: true, autoCancel: true
    });
    this.dialog.open();
  }

  private confirmReset(course: DisplayItem): void {
    this.getUIContext().showAlertDialog({
      width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
      title: '恢复默认颜色', message: '确认将课程「' + course.title + '」的自定义颜色恢复为默认配色吗？',
      primaryButton: { value: '取消', action: () => {} },
      secondaryButton: { value: '恢复默认', action: () => this.update(course.id) }
    });
  }

  private async update(id: string, color?: string): Promise<void> {
    if (this.saving || !this.active) return;
    this.saving = true;
    this.error = '';
    try {
      const change: CourseColorChange = { kind: 'courseColors', id: id };
      if (color !== undefined) {
        const values: Record<string, string> = {};
        values['color'] = color;
        change.values = values;
      }
      await this.client!.request(color === undefined ? 'delete' : 'save', change);
      if (this.active) await this.load();
    } catch (error) {
      if (this.active) this.getUIContext().getPromptAction().showToast({ message: featureError(error as Error) });
    } finally {
      if (this.active) this.saving = false;
    }
  }

  @Builder
  private pageHeader() {
    Row() {
      Button({ type: ButtonType.Circle }) { FeatureIcon({ name: 'ic_back' }) }
        .width(48).height(48).backgroundColor(Color.Transparent).accessibilityText('返回')
        .onClick(() => { if (!this.closeSearch()) this.onBack(); })
      if (this.searching) {
        Row() {
          TextInput({ text: this.keywords, placeholder: '搜索课程名称…' }).id('course-color-keywords')
            .layoutWeight(1).height(48).fontSize(16).backgroundColor(Color.Transparent)
            .fontColor($r('app.color.text_primary')).enterKeyType(EnterKeyType.Search)
            .onChange((value: string) => this.setKeywords(value))
          Button({ type: ButtonType.Circle }) { FeatureIcon({ name: 'ic_action_close' }) }
            .width(48).height(48).backgroundColor(Color.Transparent).id('course-color-search-clear').accessibilityText('关闭搜索')
            .onClick(() => { if (this.keywords.length > 0) this.setKeywords(''); else this.closeSearch(); })
        }.layoutWeight(1).height(48).borderRadius(24).backgroundColor($r('app.color.container_high'))
      } else {
        Text(featureTitle('courseColors')).layoutWeight(1).fontSize(16).fontWeight(600).fontColor($r('app.color.text_primary'))
          .textAlign(TextAlign.Center)
        Button({ type: ButtonType.Circle }) { FeatureIcon({ name: 'ic_action_search' }) }
          .width(48).height(48).backgroundColor(Color.Transparent).id('course-color-search').accessibilityText('搜索课程')
          .onClick(() => { this.searching = true; })
      }
    }.width('100%').height(64).padding({ left: 4, right: 4 }).backgroundColor($r('app.color.surface'))
  }

  @Builder
  private courseRow(course: DisplayItem) {
    Stack({ alignContent: Alignment.End }) {
      Row({ space: 12 }) {
        Circle().width(34).height(34).fill(course.color).stroke(withOpacity($r('app.color.outline'), 0.45)).strokeWidth(1.5)
        Text(course.title).layoutWeight(1).fontSize(16).fontWeight(FontWeight.Medium).fontColor($r('app.color.text_primary'))
          .maxLines(2).textOverflow({ overflow: TextOverflow.Ellipsis })
        if (course.selected) {
          Text('已自定义').fontSize(11).fontWeight(600).padding({ left: 7, right: 7, top: 3, bottom: 3 })
            .backgroundColor($r('app.color.primary_container')).fontColor($r('app.color.on_primary_container')).borderRadius(8)
          Blank().width(36).height(36)
        }
      }.width('100%').padding({ left: 14, right: 14, top: 12, bottom: 12 })
        .id('course-color-row-' + course.id).onClick(() => this.choose(course))
      if (course.selected) {
        Button({ type: ButtonType.Circle }) { FeatureIcon({ name: 'restore', tint: $r('app.color.text_secondary') }).scale({ x: 0.84, y: 0.84 }) }
          .width(36).height(36).margin({ right: 14 }).backgroundColor(Color.Transparent)
          .id('course-color-reset-' + course.id).accessibilityText('恢复默认颜色').onClick(() => this.confirmReset(course))
      }
    }.width('100%').borderRadius(16).backgroundColor($r('app.color.card')).clip(true).enabled(!this.saving)
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.4) })
  }

  build() {
    Column() {
      this.pageHeader()
      if (this.error.length > 0) {
        FeatureError({ message: this.error, onRetry: () => this.load() })
      } else if (this.courses.length > 0) {
        List({ space: 8, scroller: this.listScroller }) {
          ForEach(this.courses, (course: DisplayItem) => {
            ListItem() { this.courseRow(course) }
          }, (course: DisplayItem) => JSON.stringify([course.id, course.color, course.selected ? 'custom' : 'default']))
        }.layoutWeight(1).width('100%').padding({ left: 16, right: 16 }).contentStartOffset(10).contentEndOffset(10 + this.navigationInset)
      } else {
        Column() {
          if (this.loading) { LoadingProgress().width(32).height(32).color($r('app.color.primary')) }
          else { FeatureEmpty({ title: this.keywords.trim().length > 0 ? '未找到匹配课程' : '暂无课程数据', description: '' }) }
        }.layoutWeight(1).width('100%').justifyContent(FlexAlign.Center)
      }
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
  }
}
