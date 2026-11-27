import { DisplayItem, HomeState, SettingItem, SettingsState, WeekPage, WeekCell } from '../model/NativeModels';
import { NativeClient } from '../platform/NativeClient';
import { FeatureKindRequest, FeatureSettingRequest } from './FeatureContracts';
import { FeatureError, featureError } from './FeatureControls';
import { dialogWidth } from './DialogLayout';
import { ProfileTemplateDialog, ProfileTemplateToken, ProfileUiIcon } from './ProfileUiControls';
import { emptyDisplayItem, localImage, ScheduleCard, weekCellContainer, readableCourseText, withOpacity } from './AppComponents';

interface AppearanceRequest {
  values: Record<string, string>;
}

interface AppearanceSlider {
  key: string;
  title: string;
  min: number;
  max: number;
  unit: string;
  scale: number;
}

function defaultAppearance(): Record<string, string> {
  const values: Record<string, string> = {};
  values['todayBackgroundAlpha'] = '1';
  values['weekItemHeight'] = '72';
  values['weekBackgroundAlpha'] = '0.8';
  values['weekItemCorner'] = '4';
  values['weekTitleTemplate'] = '{courseName}\n@{location}';
  values['weekNotTitleTemplate'] = '[非本周]\n{courseName}\n@{location}';
  values['weekTitleTextSize'] = '10';
  values['backgroundImageBlur'] = '0';
  return values;
}

interface PreviewCourse {
  item: DisplayItem;
  conflicts: number;
}

// 仅用于没有本地课程时展示样式，预览区明确标注“预览样例”。
function appearanceSamples(): DisplayItem[] {
  const titles: string[] = ['高等数学', '大学英语', '程序设计', '大学物理', '线性代数', '体育'];
  const colors: string[] = ['#4F86C6', '#5C9D70', '#A678BA', '#C7895A', '#5A9F9A', '#AD7690'];
  return titles.map((title: string, index: number) => {
    const item = emptyDisplayItem();
    item.id = 'appearance-sample-' + index.toString();
    item.title = title;
    item.subtitle = '示例教师';
    item.location = '示例教室';
    item.time = '08:00 - 09:40';
    item.startTime = '08:00';
    item.endTime = '09:40';
    item.color = colors[index];
    item.status = index === 0 || index === 5 ? '非本周' : '';
    return item;
  });
}

@Component
export struct CustomUiContent {
  client: NativeClient | undefined = undefined;
  @Prop navigationInset: number = 0;
  onBack: () => void = () => {};
  onChanged: () => void = () => {};
  registerBackHandler: (handler: (() => boolean) | undefined) => void = () => {};
  @State private draft: Record<string, string> = defaultAppearance();
  @State private dirty: boolean = false;
  @State private loaded: boolean = false;
  @State private loading: boolean = false;
  @State private busy: boolean = false;
  @State private error: string = '';
  @State private previewMode: number = 0;
  @State private previewOffset: number = 0;
  @State private previewCourses: PreviewCourse[] = [];
  @State private previewScale: number = 1;
  @State private todayCourses: DisplayItem[] = [];
  @State private previewBackground: string = 'main_bg.png';
  @State private previewLoading: boolean = false;
  @StorageProp('harmonyDark') private dark: boolean = false;
  private active: boolean = false;
  private epoch: number = 0;
  private previewEpoch: number = 0;
  private baseline: string = '';
  private failedAction: string = 'load';
  private exitDialog: boolean = false;
  private dialog?: CustomDialogController;
  private samples: DisplayItem[] = appearanceSamples();
  private todaySlider: AppearanceSlider = {
    key: 'todayBackgroundAlpha', title: '卡片不透明度', min: 30, max: 100, unit: '%', scale: 100
  };
  private weekSliders: AppearanceSlider[] = [
    { key: 'weekItemHeight', title: '格子高度', min: 50, max: 150, unit: ' dp', scale: 1 },
    { key: 'weekBackgroundAlpha', title: '课程格子不透明度', min: 30, max: 100, unit: '%', scale: 100 },
    { key: 'weekItemCorner', title: '格子圆角大小', min: 0, max: 24, unit: ' dp', scale: 1 },
    { key: 'weekTitleTextSize', title: '格子文字大小', min: 8, max: 18, unit: ' sp', scale: 1 },
    { key: 'backgroundImageBlur', title: '背景动态模糊半径', min: 0, max: 30, unit: ' dp', scale: 1 }
  ];
  private tokens: ProfileTemplateToken[] = [
    { label: '课程名称', value: '{courseName}' }, { label: '上课地点', value: '{location}' },
    { label: '教师名称', value: '{teacherName}' }
  ];

  aboutToAppear(): void {
    this.active = true;
    this.registerBackHandler(() => {
      if (!this.dirty && !this.busy) return false;
      this.back();
      return true;
    });
    this.load();
    this.loadPreview();
  }

  aboutToDisappear(): void {
    this.active = false;
    this.epoch++;
    this.previewEpoch++;
    this.dialog?.close();
    this.registerBackHandler(undefined);
  }

  private async load(): Promise<void> {
    const current = ++this.epoch;
    this.loading = true;
    this.error = '';
    this.failedAction = 'load';
    const request: FeatureKindRequest = { kind: 'customUi' };
    try {
      const state = JSON.parse(await this.client!.request('settings', request)) as SettingsState;
      if (!this.active || current !== this.epoch) return;
      const values = defaultAppearance();
      const keys = Object.keys(values);
      state.items.forEach((item: SettingItem) => {
        if (!keys.includes(item.key)) return;
        if (item.key === 'weekTitleTemplate' || item.key === 'weekNotTitleTemplate') values[item.key] = item.value;
        else if (Number.isFinite(Number(item.value))) values[item.key] = Number(item.value).toString();
      });
      this.draft = values;
      this.baseline = JSON.stringify(values);
      this.dirty = false;
      this.loaded = true;
    } catch (error) {
      if (this.active && current === this.epoch) this.error = featureError(error as Error);
    } finally {
      if (this.active && current === this.epoch) this.loading = false;
    }
  }

  private async loadPreview(): Promise<void> {
    const current = ++this.previewEpoch;
    this.previewLoading = true;
    try {
      const state = JSON.parse(await this.client!.request('home')) as HomeState;
      if (!this.active || current !== this.previewEpoch) return;
      const courses: PreviewCourse[] = [];
      const ids = new Set<string>();
      // 优先当前周，同一门课保留首次出现时的状态及所在格的冲突数。
      const weeks = state.weeks.filter((week: WeekPage) => week.week === state.week)
        .concat(state.weeks.filter((week: WeekPage) => week.week !== state.week));
      weeks.forEach((week: WeekPage) => week.cells.forEach((cell: WeekCell) => cell.courses.forEach((course: DisplayItem) => {
        if (ids.has(course.id)) return;
        ids.add(course.id);
        courses.push({ item: course, conflicts: cell.courses.length - 1 });
      })));
      if (courses.length === 0) {
        state.today.forEach((course: DisplayItem) => courses.push({ item: course, conflicts: 0 }));
      }
      this.previewCourses = courses;
      this.todayCourses = state.today;
      this.previewBackground = state.background || 'main_bg.png';
    } catch {
      // 预览请求不阻止编辑；无数据时仅展示带明确标签的样例。
    } finally {
      if (this.active && current === this.previewEpoch) this.previewLoading = false;
    }
  }

  private value(key: string): number { return Number(this.draft[key]); }

  private change(key: string, value: string): void {
    if (!this.loaded || this.busy || this.loading) return;
    const values: Record<string, string> = {};
    Object.keys(this.draft).forEach((item: string) => { values[item] = this.draft[item]; });
    values[key] = value;
    this.draft = values;
    this.dirty = JSON.stringify(values) !== this.baseline;
  }

  private async save(exit: boolean = false): Promise<void> {
    if (!this.loaded || this.loading || this.busy || !this.active) return;
    this.busy = true;
    this.error = '';
    const current = this.epoch;
    const values: Record<string, string> = {};
    Object.keys(this.draft).forEach((key: string) => { values[key] = this.draft[key]; });
    const defaults = defaultAppearance();
    ['weekTitleTemplate', 'weekNotTitleTemplate'].forEach((key: string) => {
      if (!values[key].trim()) values[key] = defaults[key];
    });
    this.failedAction = 'save';
    const request: AppearanceRequest = { values: values };
    try {
      await this.client!.request('saveAppearance', request);
      if (!this.active || current !== this.epoch) return;
      this.draft = values;
      this.baseline = JSON.stringify(values);
      this.dirty = false;
      this.onChanged();
      this.getUIContext().getPromptAction().showToast({ message: '界面配置已保存' });
      if (exit) this.onBack();
    } catch (error) {
      if (this.active && current === this.epoch) this.error = featureError(error as Error);
    } finally {
      if (this.active && current === this.epoch) this.busy = false;
    }
  }

  private async back(): Promise<void> {
    if (!this.active || this.busy || this.exitDialog) return;
    if (!this.dirty) { this.onBack(); return; }
    this.exitDialog = true;
    try {
      const saveChanges = await new Promise<boolean | undefined>((resolve) => {
        this.getUIContext().showAlertDialog({
          title: '有未保存的更改', message: '你修改了课表界面参数但尚未保存，是否保存后退出？',
          width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
          primaryButton: { value: '直接放弃', fontColor: $r('app.color.text_secondary'), action: () => resolve(false) },
          secondaryButton: { value: '保存并退出', fontColor: $r('app.color.primary'), action: () => resolve(true) },
          cancel: () => resolve(undefined)
        });
      });
      if (!this.active || saveChanges === undefined) return;
      if (saveChanges) await this.save(true);
      else { this.dirty = false; this.onBack(); }
    } catch {
      // 弹窗未能显示时保留草稿。
    } finally { this.exitDialog = false; }
  }

  private confirmReset(): void {
    if (!this.loaded || this.busy || this.loading) return;
    this.getUIContext().showAlertDialog({
      width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
      title: '恢复默认界面配置',
      message: '确认将所有课表界面参数和模板重置为默认值吗？',
      primaryButton: { value: '取消', action: () => {} },
      secondaryButton: { value: '恢复默认', action: () => this.reset() } });
  }

  private async reset(): Promise<void> {
    if (this.busy || !this.active) return;
    this.busy = true;
    this.error = '';
    this.failedAction = 'reset';
    const request: FeatureSettingRequest = { key: 'resetUi', value: '' };
    try {
      await this.client!.request('setSetting', request);
      if (!this.active) return;
      // 与 Android reset 一样立即持久化，并清除未保存标记。
      this.draft = defaultAppearance();
      this.baseline = JSON.stringify(this.draft);
      this.dirty = false;
      this.onChanged();
      this.getUIContext().getPromptAction().showToast({ message: '已恢复默认设置' });
    } catch (error) {
      if (this.active) this.error = featureError(error as Error);
    } finally { if (this.active) this.busy = false; }
  }

  private editTemplate(key: string): void {
    if (!this.loaded || this.loading || this.busy) return;
    this.dialog?.close();
    this.dialog = new CustomDialogController({ builder: ProfileTemplateDialog({
      title: key === 'weekTitleTemplate' ? '本周课程格子模板' : '非本周课程格子模板', initialValue: this.draft[key],
      resetValue: defaultAppearance()[key], tokens: this.tokens, onConfirm: (value: string) => this.change(key, value)
    }), customStyle: true, autoCancel: true });
    this.dialog.open();
  }

  private weekCourse(slot: number): PreviewCourse {
    if (this.previewCourses.length > 0) return this.previewCourses[(this.previewOffset + slot) % this.previewCourses.length];
    return { item: this.samples[(this.previewOffset + slot) % this.samples.length], conflicts: slot === 2 ? 1 : 0 };
  }

  private todayItem(): DisplayItem {
    if (this.todayCourses.length > 0) return this.todayCourses[this.previewOffset % this.todayCourses.length];
    const course = this.weekCourse(0).item;
    const card = emptyDisplayItem();
    card.id = course.id;
    card.title = course.title;
    card.subtitle = course.subtitle;
    card.location = course.location;
    card.color = course.color;
    card.startTime = course.startTime;
    card.endTime = course.endTime;
    card.time = course.fields?.find(field => field.key === 'periods')?.value ?? '';
    return card;
  }

  private samplePreview(): boolean {
    return this.previewCourses.length === 0;
  }

  private weekBackground(item: DisplayItem): ResourceColor {
    return item.status === '非本周' ? withOpacity($r('app.color.container_highest'), 0.82) :
      weekCellContainer(item.color, this.dark, this.value('weekBackgroundAlpha'));
  }

  private weekTextLines(slot: number, span: number): number {
    const badgeHeight = span === 1 && this.weekCourse(slot).conflicts > 0 ? 12 : 0;
    const available = this.value('weekItemHeight') * span * this.previewScale - 11 - badgeHeight;
    return Math.max(1, Math.floor(available / (this.value('weekTitleTextSize') * this.previewScale * 1.25)));
  }

  private previewTitle(item: DisplayItem): string {
    const key = item.status === '非本周' ? 'weekNotTitleTemplate' : 'weekTitleTemplate';
    return (this.draft[key].trim() ? this.draft[key] : defaultAppearance()[key])
      .split('{courseName}').join(item.title).split('{location}').join(item.location).split('{teacherName}').join(item.subtitle);
  }

  @Builder
  private header() {
    Stack() {
      Text('自定义课表界面').fontSize(16).lineHeight(21.12).fontWeight(600).fontColor($r('app.color.text_primary'))
        .textAlign(TextAlign.Center).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
        .margin({ left: 96, right: 96 })
      Row() {
        Button({ type: ButtonType.Circle }) { ProfileUiIcon({ kind: 'back', tint: $r('app.color.text_primary') }) }
          .width(48).height(48).backgroundColor(Color.Transparent).id('custom-ui-back').accessibilityText('返回')
          .onClick(() => this.back())
        Blank()
        if (this.loading || this.busy) {
          LoadingProgress().width(24).height(24).color($r('app.color.primary')).margin({ left: 12, right: 12 })
        } else {
          Button({ type: ButtonType.Circle }) { ProfileUiIcon({ kind: 'refresh', tint: $r('app.color.text_primary') }) }
            .width(48).height(48).backgroundColor(Color.Transparent).enabled(this.loaded)
            .id('custom-ui-reset').accessibilityText('恢复默认').onClick(() => this.confirmReset())
        }
        Button({ type: ButtonType.Circle }) {
          ProfileUiIcon({ kind: 'check', tint: this.dirty ? $r('app.color.primary') : $r('app.color.text_primary') })
        }.width(48).height(48).backgroundColor(Color.Transparent).enabled(this.loaded && !this.busy && !this.loading)
          .id('custom-ui-save').accessibilityText('保存设置').onClick(() => this.save())
      }.width('100%')
    }.height(64).width('100%').padding({ left: 4, right: 4 })
  }

  @Builder
  private weekCell(slot: number, span: number) {
    Column() {
      Stack({ alignContent: Alignment.BottomEnd }) {
        Column() {
          Text(this.previewTitle(this.weekCourse(slot).item)).width('100%').textAlign(TextAlign.Center)
            .fontSize(this.value('weekTitleTextSize') * this.previewScale)
            .lineHeight(this.value('weekTitleTextSize') * this.previewScale * 1.25)
            .fontWeight(this.weekCourse(slot).item.status === '非本周' ? FontWeight.Normal : FontWeight.Medium)
            .fontColor(this.weekCourse(slot).item.status === '非本周' ? $r('app.color.text_secondary') :
              readableCourseText(this.weekBackground(this.weekCourse(slot).item)))
            .maxLines(this.weekTextLines(slot, span)).textOverflow({ overflow: TextOverflow.Ellipsis })
        }.width('100%').height('100%').justifyContent(FlexAlign.Start)
          .padding({ bottom: span === 1 && this.weekCourse(slot).conflicts > 0 ? 12 : 0 })
        if (this.weekCourse(slot).conflicts > 0) {
          Text('+' + this.weekCourse(slot).conflicts).fontSize(8).lineHeight(10).fontWeight(FontWeight.Bold)
            .fontColor(Color.White).padding({ left: 3, right: 3, top: 1, bottom: 1 })
            .backgroundColor('#73000000').borderRadius(4)
        }
      }.width('100%').height('100%').padding({ left: 3, right: 3, top: 4, bottom: 4 })
        .backgroundColor(this.weekBackground(this.weekCourse(slot).item)).borderRadius(this.value('weekItemCorner')).clip(true)
    }.width('100%').height(this.value('weekItemHeight') * span * this.previewScale).padding(1.5)
  }

  @Builder
  private preview() {
    Column() {
      Stack({ alignContent: Alignment.TopEnd }) {
        Image(localImage(this.previewBackground)).width('100%').height('100%').objectFit(ImageFit.Cover)
          .blur(this.value('backgroundImageBlur')).accessibilityLevel('no')
        if (this.previewMode === 0) {
          Column() {
            Scroll() {
              Row() {
                ForEach([0, 1, 2], (column: number) => {
                  Column() {
                    this.weekCell(column * 2, column === 1 ? 2 : 1)
                    this.weekCell(column * 2 + 1, column === 1 ? 1 : 2)
                  }.layoutWeight(1)
                }, (column: number) => column.toString())
              }.width(260 * this.previewScale).alignItems(VerticalAlign.Top)
            }.width('100%').height('100%').align(Alignment.Top).scrollBar(BarState.Off)
          }.width('100%').height('100%').padding(12 * this.previewScale)
        } else {
          Column() { ScheduleCard({ item: this.todayItem(), cardOpacity: this.value('todayBackgroundAlpha') }) }
            .width('100%').height('100%').padding(12).justifyContent(FlexAlign.Center)
        }
        Row() {
          if (this.samplePreview()) {
            Text('预览样例').fontSize(11).fontColor($r('app.color.on_secondary_container'))
              .padding({ left: 8, right: 8, top: 4, bottom: 4 }).backgroundColor($r('app.color.secondary_container')).borderRadius(8)
          }
          Blank()
          Button({ type: ButtonType.Circle }) {
            if (this.previewLoading) { LoadingProgress().width(18).height(18).color($r('app.color.primary')) }
            else { ProfileUiIcon({ kind: 'refresh', iconSize:18, tint: $r('app.color.on_secondary_container') }) }
          }.width(34).height(34).backgroundColor($r('app.color.secondary_container')).id('custom-ui-preview-refresh')
            .accessibilityText('换一组示例课程').onClick(() => {
              this.previewOffset++;
              if (this.samplePreview() && !this.previewLoading) this.loadPreview();
            })
        }.width('100%').padding(8).alignItems(VerticalAlign.Top)
      }.width('100%').height(230 * this.previewScale).clip(true)
        .onAreaChange((_old: Area, area: Area) => {
          // 参考图中 380 dp 宽的预览卡容纳 260 dp 的三列课格；窄屏按相同比例展示。
          const width = Number(area.width);
          if (width > 0) this.previewScale = Math.min(1, width / 380);
        })
      Row() {
        ForEach([0, 1], (mode: number) => {
          Button({ type: ButtonType.Normal }) {
            Row({ space: 6 }) {
              if (this.previewMode === mode) { ProfileUiIcon({ kind: 'check', iconSize:16, tint: $r('app.color.on_secondary_container') }) }
              Text(mode === 0 ? '本周课表预览' : '今日卡片预览').fontSize(12)
                .fontColor(this.previewMode === mode ? $r('app.color.on_secondary_container') : $r('app.color.text_primary'))
            }
          }.height(40).layoutWeight(1).backgroundColor(this.previewMode === mode ? $r('app.color.secondary_container') : Color.Transparent)
            .border({ width: 1, color: $r('app.color.outline') })
            .borderRadius({ topLeft: mode === 0 ? 20 : 0, bottomLeft: mode === 0 ? 20 : 0,
              topRight: mode === 1 ? 20 : 0, bottomRight: mode === 1 ? 20 : 0 })
            .id(mode === 0 ? 'custom-ui-preview-week' : 'custom-ui-preview-today').onClick(() => { this.previewMode = mode; })
        }, (mode: number) => mode.toString())
      }.padding({ left: 12, right: 12, top: 8, bottom: 8 }).width('100%')
    }.width('100%').borderRadius(22).clip(true).backgroundColor($r('app.color.surface'))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.5) })
  }

  @Builder
  private groupTitle(title: string) {
    Text(title).fontSize(14).fontWeight(600).fontColor($r('app.color.primary')).width('100%')
      .padding({ left: 8, top: 8, bottom: 6 })
  }

  @Builder
  private divider() { Divider().color(withOpacity($r('app.color.outline'), 0.35)).margin({ left: 16, right: 16 }) }

  @Builder
  private slider(item: AppearanceSlider) {
    Column() {
      Row() {
        Text(item.title).fontSize(16).fontColor($r('app.color.text_primary')).layoutWeight(1)
        Text(Math.round(this.value(item.key) * item.scale).toString() + item.unit).fontSize(12).fontFamily('monospace').fontWeight(600)
          .fontColor($r('app.color.on_secondary_container')).padding({ left: 8, right: 8, top: 3, bottom: 3 })
          .backgroundColor($r('app.color.secondary_container')).borderRadius(8)
      }.width('100%')
      Slider({ value: this.value(item.key) * item.scale, min: item.min, max: item.max, step: 1, style: SliderStyle.OutSet })
        .width('100%').height(48).trackThickness(16).trackBorderRadius(8)
        .selectedColor($r('app.color.primary')).trackColor($r('app.color.secondary_container'))
        .blockStyle({ type: SliderBlockType.SHAPE, shape: new Rect({ width: 4, height: 44, radius: 2 }) })
        .blockSize({ width: 4, height: 44 }).blockColor($r('app.color.primary'))
        .enabled(this.loaded && !this.busy && !this.loading)
        .id('custom-ui-slider-' + item.key).onChange((value: number) => this.change(item.key, (value / item.scale).toString()))
    }.padding({ left: 16, right: 16, top: 10, bottom: 10 }).width('100%')
  }

  @Builder
  private templateRow(key: string, title: string) {
    Column({ space: 4 }) {
      Text(title).fontSize(16).fontColor($r('app.color.text_primary'))
      Text(this.draft[key].split('\n').join(' ↵ ')).fontSize(12).fontColor($r('app.color.text_secondary'))
    }.width('100%').alignItems(HorizontalAlign.Start).padding(16).id('custom-ui-template-' + key)
      .onClick(() => this.editTemplate(key))
  }

  build() {
    Column() {
      this.header()
      Column() { this.preview() }.width('100%').padding({ left: 16, right: 16, top: 8, bottom: 8 })
      Scroll() {
        Column() {
          if (this.error.length > 0) {
            FeatureError({ message: this.error, onRetry: () => {
              if (this.failedAction === 'load') this.load();
              else if (this.failedAction === 'reset') this.reset();
              else this.save();
            } })
          }
          Column() {
            this.groupTitle('今日课程页面')
            Column() { this.slider(this.todaySlider) }.width('100%').backgroundColor($r('app.color.card')).borderRadius(20)
              .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) })
          }.width('100%').padding({ left: 16, right: 16, top: 6, bottom: 6 })
          Column() {
            this.groupTitle('本周课程页面')
            Column() {
              ForEach(this.weekSliders, (item: AppearanceSlider, index: number) => {
                if (index > 0) { this.divider() }
                this.slider(item)
              }, (item: AppearanceSlider) => item.key)
            }.width('100%').backgroundColor($r('app.color.card')).borderRadius(20)
              .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) })
          }.width('100%').padding({ left: 16, right: 16, top: 6, bottom: 6 })
          Column() {
            this.groupTitle('格子文本模板')
            Column() {
              this.templateRow('weekTitleTemplate', '本周课程模板')
              this.divider()
              this.templateRow('weekNotTitleTemplate', '非本周课程模板')
            }.width('100%').backgroundColor($r('app.color.card')).borderRadius(20)
              .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) })
          }.width('100%').padding({ left: 16, right: 16, top: 6, bottom: 6 })
        }.width('100%').padding({ bottom: 24 + this.navigationInset })
      }.layoutWeight(1).width('100%').scrollBar(BarState.Auto).id('custom-ui-scroll')
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
  }
}
