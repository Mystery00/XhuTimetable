import { inputMethod } from '@kit.IMEKit';
import { EditorField, EditorState, SelectOption } from '../model/NativeModels';
import { NativeClient } from '../platform/NativeClient';
import { FeatureEditorRequest, FeatureSaveRequest } from './FeatureContracts';
import { FEATURE_COLORS, FeatureChoiceDialog, FeatureColorDialog, FeatureError, FeatureIcon, featureError } from './FeatureControls';
import { SettingDateDialog, SettingTimeDialog } from './DateTimeDialogs';
import { DIALOG_CARD_WIDTH, dialogWidth } from './DialogLayout';
import { readableCourseText, withOpacity } from './AppComponents';
import { HomeIcon } from './HomeWidgets';
import { SheetSurface } from './SheetSurface';

@CustomDialog
struct CoursePeriodDialog {
  controller?: CustomDialogController;
  start: number = 1;
  end: number = 2;
  onConfirm: (start: number, end: number) => void = () => {};
  @State private first: number = 1;
  @State private last: number = 2;
  private periods: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];

  aboutToAppear(): void {
    this.first = Math.max(1, Math.min(11, this.start));
    this.last = Math.max(this.first, Math.min(11, this.end));
  }

  @Builder
  private slots(starting: boolean) {
    Flex({ wrap: FlexWrap.Wrap }) {
      ForEach(this.periods, (period: number) => {
        if (starting || period >= this.first) {
          Button(period.toString()).fontSize(14).width(44).height(36).borderRadius(10).margin({ right: 6, bottom: 6 })
            .fontColor((starting ? this.first : this.last) === period ? $r('app.color.on_primary_container') : $r('app.color.text_primary'))
            .backgroundColor((starting ? this.first : this.last) === period ? $r('app.color.primary_container') : $r('app.color.surface'))
            .onClick(() => {
              if (starting) { this.first = period; this.last = Math.max(period, this.last); }
              else this.last = period;
            })
        }
      }, (period: number) => period.toString())
    }.width('100%')
  }

  build() {
    Column({ space: 12 }) {
      Text('选择上课节次').fontSize(20).fontWeight(600).fontColor($r('app.color.text_primary')).width('100%')
      Text('第 ' + this.first + ' 节 - 第 ' + this.last + ' 节').fontSize(14).fontColor($r('app.color.text_secondary')).width('100%')
      Text('开始节次').fontSize(14).fontColor($r('app.color.text_secondary')).width('100%')
      this.slots(true)
      Text('结束节次').fontSize(14).fontColor($r('app.color.text_secondary')).width('100%')
      this.slots(false)
      Row({ space: 12 }) {
        Button('取消').layoutWeight(1).backgroundColor($r('app.color.container')).fontColor($r('app.color.text_secondary'))
          .onClick(() => this.controller?.close())
        Button('确定').layoutWeight(1).backgroundColor($r('app.color.primary')).fontColor($r('app.color.on_primary'))
          .onClick(() => { this.onConfirm(this.first, this.last); this.controller?.close(); })
      }.width('100%')
    }.width(DIALOG_CARD_WIDTH).padding(24).borderRadius(28).backgroundColor($r('app.color.surface'))
  }
}

@Component
export struct FeatureEditor {
  client: NativeClient | undefined = undefined;
  request: FeatureEditorRequest = { kind: '', id: '', user: '', year: 0, term: 0 };
  onBack: () => void = () => {};
  onSaved: () => void = () => {};
  onBusyChange: (busy: boolean) => void = () => {};
  @State private title: string = '编辑';
  @State private fields: EditorField[] = [];
  @State private focusedField: string = '';
  @State private loading: boolean = true;
  @State private saving: boolean = false;
  @State private error: string = '';
  private active: boolean = false;
  private failedOperation: 'load' | 'save' | 'delete' = 'load';
  private choiceDialog?: CustomDialogController;

  aboutToAppear(): void { this.active = true; this.load(); }
  aboutToDisappear(): void { this.active = false; this.choiceDialog?.close(); }

  private async load(): Promise<void> {
    this.loading = true;
    this.error = '';
    try {
      const result = JSON.parse(await this.client!.request('editor', this.request)) as EditorState;
      if (!this.active) return;
      this.title = result.title;
      this.fields = result.fields;
      if (this.value('countdown') === 'true') this.setValue('allDay', 'true');
    } catch (error) {
      if (this.active) this.error = featureError(error as Error);
    } finally {
      if (this.active) this.loading = false;
    }
  }

  // 所有事件按 key 重新读取当前字段；不要闭包捕获旧 EditorField，也不要以 value 生成组件 key。
  private setValue(key: string, value: string): void {
    if (this.saving || !this.active) return;
    this.fields = this.fields.map((field: EditorField): EditorField => ({
      key: field.key, title: field.title, value: field.key === key ? value : field.value,
      type: field.type, options: field.options
    }));
    if (key === 'countdown' && value === 'true') this.setValue('allDay', 'true');
    if (key === 'startDate' && this.value('endDate') < value) this.setValue('endDate', value);
  }

  private currentField(key: string): EditorField { return this.fields.find((field: EditorField) => field.key === key)!; }
  private value(key: string): string { return this.fields.find((field: EditorField) => field.key === key)?.value || ''; }
  private label(key: string): string {
    const field = this.currentField(key);
    return field.options.find((option: SelectOption) => option.value === field.value)?.label || field.value || '请选择';
  }

  private clearFocus(): void {
    this.getUIContext().getFocusController().clearFocus();
    inputMethod.getController().hideTextInput().catch(() => {});
  }

  private close(): void { if (!this.saving) { this.clearFocus(); this.onBack(); } }

  private choose(key: string): void {
    if (this.saving) return;
    this.clearFocus();
    const field = this.currentField(key);
    this.choiceDialog?.close();
    if (field.type === 'date') {
      this.choiceDialog = new CustomDialogController({
        builder: SettingDateDialog({ title: '选择' + (key === 'startDate' && this.value('countdown') === 'true' ? '目标日期' : field.title),
          initialValue: field.value,
          onConfirm: (value: string) => this.setValue(key, value) }),
        alignment: DialogAlignment.Center, customStyle: true, autoCancel: true
      });
    } else if (field.type === 'time') {
      this.choiceDialog = new CustomDialogController({
        builder: SettingTimeDialog({ title: '选择' + field.title, initialValue: field.value, allowDisable: false,
          onConfirm: (value: string) => this.setValue(key, value) }),
        alignment: DialogAlignment.Center, customStyle: true, autoCancel: true
      });
    } else {
      this.choiceDialog = new CustomDialogController({
        builder: FeatureChoiceDialog({ title: field.title, options: field.options, selected: [this.value(key)],
          onConfirm: (values: string[]) => this.setValue(key, values[0]) }),
        customStyle: true, autoCancel: true
      });
    }
    this.choiceDialog.open();
  }

  private choosePeriods(): void {
    if (this.saving) return;
    this.clearFocus();
    this.choiceDialog?.close();
    this.choiceDialog = new CustomDialogController({
      builder: CoursePeriodDialog({ start: Number(this.value('startPeriod')), end: Number(this.value('endPeriod')),
        onConfirm: (start: number, end: number) => { this.setValue('startPeriod', start.toString()); this.setValue('endPeriod', end.toString()); } }),
      customStyle: true, autoCancel: true
    });
    this.choiceDialog.open();
  }

  private moreColors(): void {
    if (this.saving) return;
    this.clearFocus();
    this.choiceDialog?.close();
    this.choiceDialog = new CustomDialogController({
      builder: FeatureColorDialog({ initialValue: this.value('color'), onConfirm: (value: string) => this.setValue('color', value) }),
      customStyle: true, autoCancel: true
    });
    this.choiceDialog.open();
  }

  private weekValues(): string[] { return this.value('weeks').split(',').filter((week: string) => week.length > 0); }
  private toggleWeek(week: string): void {
    const selected = this.weekValues();
    this.setValue('weeks', (selected.includes(week) ? selected.filter((value: string) => value !== week) : selected.concat([week]))
      .sort((a: string, b: string) => Number(a) - Number(b)).join(','));
  }

  private weekPreset(preset: number): void {
    this.setValue('weeks', this.currentField('weeks').options.filter((option: SelectOption) => preset === 0 ||
      (preset === 1 && Number(option.value) % 2 === 1) || (preset === 2 && Number(option.value) % 2 === 0))
      .map((option: SelectOption) => option.value).join(','));
  }

  private async save(): Promise<void> {
    if (this.saving || this.loading || this.fields.length === 0) return;
    this.clearFocus();
    this.choiceDialog?.close();
    this.saving = true;
    this.onBusyChange(true);
    this.error = '';
    this.failedOperation = 'save';
    const values: Record<string, string> = {};
    this.fields.forEach((field: EditorField) => { values[field.key] = field.value; });
    const request: FeatureSaveRequest = { kind: this.request.kind, id: this.request.id, user: this.request.user,
      year: this.request.year, term: this.request.term, values: values };
    try {
      await this.client!.request('save', request);
      if (this.active) this.onSaved();
    } catch (error) {
      if (this.active) this.error = featureError(error as Error);
    } finally {
      if (this.active) { this.saving = false; this.onBusyChange(false); }
    }
  }

  private confirmDelete(): void {
    if (this.saving) return;
    this.getUIContext().showAlertDialog({
      width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
      title: '确认删除', message: '删除后无法恢复，确定删除这条记录吗？',
      primaryButton: { value: '取消', action: () => {} },
      secondaryButton: { value: '删除', fontColor: $r('app.color.danger'), action: () => this.remove() }
    });
  }

  private async remove(): Promise<void> {
    if (this.saving) return;
    this.clearFocus();
    this.saving = true;
    this.onBusyChange(true);
    this.error = '';
    this.failedOperation = 'delete';
    try {
      await this.client!.request('delete', this.request);
      if (this.active) this.onSaved();
    } catch (error) {
      if (this.active) this.error = featureError(error as Error);
    } finally {
      if (this.active) { this.saving = false; this.onBusyChange(false); }
    }
  }

  private inputLabel(key: string): string {
    const title = this.currentField(key).title;
    if (key === 'courseName' || key === 'title') return title + ' *';
    if (key === 'remark' || (key === 'location' && this.request.kind === 'customThings')) return title + '（选填）';
    return title;
  }

  private inputHint(key: string): string {
    if (this.focusedField !== key) return this.inputLabel(key);
    if (key === 'courseName') return '例如：高等数学（旁听）';
    if (key === 'title') return '例如：英语四级考试 / 课程论文提交';
    return '选填';
  }

  @Builder
  private input(key: string) {
    Stack({ alignContent: Alignment.TopStart }) {
      Column() {
        if (this.currentField(key).type === 'multiline') {
          TextArea({ text: this.value(key), placeholder: this.inputHint(key) }).id('editor-input-' + key)
            .width('100%').constraintSize({ minHeight: key === 'remark' ? 76 : 56, maxHeight: 144 })
            .padding({ left: 16, right: 16, top: 16, bottom: 16 }).fontSize(16)
            .fontColor($r('app.color.text_primary')).placeholderColor($r('app.color.text_secondary')).backgroundColor(Color.Transparent)
            .accessibilityDescription(this.inputLabel(key)).onFocus(() => { this.focusedField = key; })
            .onBlur(() => { if (this.focusedField === key) this.focusedField = ''; })
            .onChange((value: string) => this.setValue(key, value))
        } else {
          TextInput({ text: this.value(key), placeholder: this.inputHint(key) }).id('editor-input-' + key)
            .height(56).width('100%').padding({ left: 16, right: 16 }).fontSize(16)
            .fontColor($r('app.color.text_primary')).placeholderColor($r('app.color.text_secondary')).backgroundColor(Color.Transparent)
            .accessibilityDescription(this.inputLabel(key)).onFocus(() => { this.focusedField = key; })
            .onBlur(() => { if (this.focusedField === key) this.focusedField = ''; })
            .onChange((value: string) => this.setValue(key, value))
        }
      }.width('100%').margin({ top: 8 }).borderRadius(14)
        .border({ width: this.focusedField === key ? 2 : 1, color: this.focusedField === key ? $r('app.color.primary') : withOpacity($r('app.color.text_secondary'), 0.72) })
      if (this.focusedField === key || this.value(key).length > 0) {
        Text(this.inputLabel(key)).fontSize(12).lineHeight(16).maxLines(1)
          .fontColor(this.focusedField === key ? $r('app.color.primary') : $r('app.color.text_secondary'))
          .backgroundColor($r('app.color.card')).padding({ left: 4, right: 4 }).margin({ left: 12 })
          .hitTestBehavior(HitTestMode.None).accessibilityLevel('no')
      }
    }.width('100%').enabled(!this.saving)
  }

  @Builder
  private daySelector() {
    Row({ space: 12 }) {
      HomeIcon({ name: 'today', iconSize: 20, tint: $r('app.color.primary') })
      Text('上课星期').fontSize(14).fontColor($r('app.color.text_primary'))
      Text(this.label('day')).fontSize(14).fontWeight(FontWeight.Medium).fontColor($r('app.color.primary')).layoutWeight(1).textAlign(TextAlign.End)
      Text('›').fontSize(20).lineHeight(20).width(18).textAlign(TextAlign.Center).fontColor($r('app.color.text_secondary'))
    }.padding(14).width('100%').id('editor-choice-day').onClick(() => this.choose('day'))
  }

  @Builder
  private courseFields() {
    this.input('courseName')
    Row({ space: 10 }) {
      Column() { this.input('teacher') }.layoutWeight(1)
      Column() { this.input('location') }.layoutWeight(1)
    }.width('100%')
    Column() {
      this.daySelector()
      Divider().margin({ left: 52 }).color($r('app.color.outline')).opacity(0.4)
      Row({ space: 12 }) {
        HomeIcon({ name: 'clock', iconSize: 20, tint: $r('app.color.primary') })
        Text('上课节次').fontSize(14).fontColor($r('app.color.text_primary'))
        Text('第 ' + this.value('startPeriod') + ' - ' + this.value('endPeriod') + ' 节').fontSize(14).fontWeight(FontWeight.Medium)
          .fontColor($r('app.color.primary')).layoutWeight(1).textAlign(TextAlign.End).id('editor-choice-endPeriod')
          .onClick(() => this.choosePeriods())
        Text('›').fontSize(20).lineHeight(20).width(18).textAlign(TextAlign.Center).fontColor($r('app.color.text_secondary'))
      }.padding(14).width('100%').id('editor-choice-startPeriod').onClick(() => this.choosePeriods())
    }.width('100%').borderRadius(16).backgroundColor($r('app.color.card')).border({ width: 0.5, color: $r('app.color.outline') })
    Column({ space: 10 }) {
      Row() {
        Text('上课周数 *').fontSize(14).fontWeight(600).fontColor($r('app.color.text_primary')).layoutWeight(1)
        Text(this.weekValues().length === 0 ? '未选择' : '已选 ' + this.weekValues().length + ' 周').fontSize(12).fontColor($r('app.color.primary'))
      }.width('100%')
      Row({ space: 6 }) {
        ForEach(['全选', '单周', '双周', '清空'], (label: string, index: number) => {
          Button(label).height(26).padding({ left: 10, right: 10 }).fontSize(11).borderRadius(8).fontColor($r('app.color.on_secondary_container'))
            .backgroundColor($r('app.color.secondary_container')).onClick(() => this.weekPreset(index))
        })
      }.width('100%')
      Grid() {
        ForEach(this.currentField('weeks').options, (option: SelectOption) => {
          GridItem() {
            Text(option.value).fontSize(12).fontWeight(this.weekValues().includes(option.value) ? FontWeight.Bold : FontWeight.Normal)
              .fontColor(this.weekValues().includes(option.value) ? $r('app.color.on_primary_container') : $r('app.color.text_primary'))
              .textAlign(TextAlign.Center).width('100%').height(36).borderRadius(10)
              .backgroundColor(this.weekValues().includes(option.value) ? $r('app.color.primary_container') : $r('app.color.container_lowest'))
              .border({ width: this.weekValues().includes(option.value) ? 1.2 : 0.5,
                color: this.weekValues().includes(option.value) ? $r('app.color.primary') : $r('app.color.outline') })
              .onClick(() => this.toggleWeek(option.value))
          }.id('editor-week-' + option.value).selected(this.weekValues().includes(option.value))
        }, (option: SelectOption) => option.value)
      }.columnsTemplate('1fr 1fr 1fr 1fr 1fr').columnsGap(6).rowsGap(6).height(Math.ceil(this.currentField('weeks').options.length / 5) * 42 - 6)
        .id('editor-choice-weeks')
    }.padding(14).width('100%').borderRadius(16).backgroundColor($r('app.color.card')).border({ width: 0.5, color: $r('app.color.outline') })
  }

  @Builder
  private toggle(key: string, title: string, subtitle: string = '') {
    Row({ space: 12 }) {
      Column({ space: 4 }) {
        Text(title).fontSize(16).fontColor($r('app.color.text_primary'))
        if (subtitle.length > 0) { Text(subtitle).fontSize(12).fontColor($r('app.color.text_secondary')) }
      }.layoutWeight(1).alignItems(HorizontalAlign.Start)
      Toggle({ type: ToggleType.Switch, isOn: this.value(key) === 'true' }).id('editor-choice-' + key)
        .selectedColor($r('app.color.primary')).switchPointColor(this.value(key) === 'true' ? $r('app.color.on_primary') : $r('app.color.text_secondary'))
        .enabled(!this.saving && !(key === 'allDay' && this.value('countdown') === 'true'))
        .onChange((value: boolean) => this.setValue(key, value.toString()))
    }.padding({ left: 16, right: 16, top: 10, bottom: 10 }).width('100%')
  }

  @Builder
  private dateRow(dateKey: string, timeKey: string) {
    Row({ space: 8 }) {
      Text(dateKey === 'endDate' ? '结束时间' : this.value('countdown') === 'true' ? '目标日期' : '开始时间')
        .fontSize(14).fontColor($r('app.color.text_secondary')).layoutWeight(1)
      Text(this.value(dateKey).replace(/^(\d{4})-(\d{2})-(\d{2})$/, '$1年$2月$3日'))
        .fontSize(14).fontWeight(FontWeight.Medium).fontColor($r('app.color.primary'))
        .padding({ left: 10, right: 10, top: 6, bottom: 6 }).backgroundColor($r('app.color.container_highest')).borderRadius(10)
        .id('editor-choice-' + dateKey).onClick(() => this.choose(dateKey))
      if (this.value('allDay') !== 'true' && this.value('countdown') !== 'true') {
        Text(this.value(timeKey)).fontSize(14).fontWeight(FontWeight.Medium).fontColor($r('app.color.primary'))
          .padding({ left: 10, right: 10, top: 6, bottom: 6 }).backgroundColor($r('app.color.container_highest')).borderRadius(10)
          .id('editor-choice-' + timeKey).onClick(() => this.choose(timeKey))
      }
    }.width('100%').padding({ left: 16, right: 16, top: 12, bottom: 12 })
  }

  @Builder
  private thingFields() {
    this.input('title')
    Column() {
      this.toggle('countdown', '存储为倒计时', '在今日课程页置顶显示剩余天数')
      Divider().color($r('app.color.outline')).opacity(0.4)
      this.toggle('allDay', '全天事项')
      Divider().color($r('app.color.outline')).opacity(0.4)
      this.dateRow('startDate', 'startTime')
      if (this.value('countdown') !== 'true') {
        Divider().color($r('app.color.outline')).opacity(0.4)
        this.dateRow('endDate', 'endTime')
      }
    }.width('100%').borderRadius(16).backgroundColor($r('app.color.card')).border({ width: 0.5, color: $r('app.color.outline') })
    Column({ space: 10 }) {
      Row() {
        Text('标签颜色').fontSize(14).fontWeight(600).fontColor($r('app.color.text_primary')).layoutWeight(1)
        Button('更多颜色').fontSize(14).fontColor($r('app.color.primary')).backgroundColor(Color.Transparent).onClick(() => this.moreColors())
      }.width('100%')
      Scroll() {
        Row({ space: 10 }) {
          ForEach(FEATURE_COLORS.slice(0, 10), (color: string) => {
            Stack() {
              Circle().width(34).height(34).fill(color)
                .stroke(this.value('color').slice(-6).toUpperCase() === color.slice(-6) ? $r('app.color.text_primary') : Color.Transparent).strokeWidth(2)
              if (this.value('color').slice(-6).toUpperCase() === color.slice(-6)) {
                Image($rawfile('shared/ic_action_done.svg')).width(16).height(16).fillColor(readableCourseText(color))
              }
            }.width(34).height(34).accessibilityText(color).onClick(() => this.setValue('color', color))
          }, (color: string) => color)
        }
      }.scrollable(ScrollDirection.Horizontal).scrollBar(BarState.Off).width('100%')
    }.padding(14).width('100%').borderRadius(16).backgroundColor($r('app.color.card')).border({ width: 0.5, color: $r('app.color.outline') })
    this.input('location')
    this.input('remark')
  }

  @Builder
  private editorToolbar() {
    Row() {
      Button({ type: ButtonType.Circle }) { FeatureIcon({ name: 'ic_action_close' }) }
        .width(48).height(48).backgroundColor(Color.Transparent).enabled(!this.saving).id('editor-close').accessibilityText('关闭').onClick(() => this.close())
      Text(this.title).fontSize(16).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary')).layoutWeight(1)
      if (this.request.id.length > 0 && !this.saving) {
        Button({ type: ButtonType.Circle }) { FeatureIcon({ name: 'delete', tint: $r('app.color.danger') }) }
          .width(48).height(48).backgroundColor(Color.Transparent).enabled(!this.loading).id('editor-delete').accessibilityText('删除').onClick(() => this.confirmDelete())
      }
      Button(this.saving ? '保存中' : '保存').fontSize(14).height(40).borderRadius(12).padding({ left: 24, right: 24 }).id('editor-save')
        .backgroundColor($r('app.color.primary')).fontColor($r('app.color.on_primary'))
        .enabled(!this.saving && !this.loading && this.fields.length > 0).onClick(() => this.save())
    }.width('100%')
  }

  build() {
    SheetSurface() {
      Scroll() {
        Column({ space: 14 }) {
          this.editorToolbar()
          if (this.loading) {
            LoadingProgress().width(40).height(40).color($r('app.color.primary'))
          } else {
            if (this.error.length > 0) { FeatureError({ message: this.error, onRetry: () => { if (this.failedOperation === 'load') this.load(); else if (this.failedOperation === 'delete') this.confirmDelete(); else this.save(); } }) }
            if (this.fields.length > 0) {
              Column({ space: 14 }) {
                if (this.request.kind === 'customCourses') { this.courseFields() }
                else { this.thingFields() }
              }.width('100%').enabled(!this.saving)
            }
          }
        }.padding({ left: 20, right: 20, bottom: 32 }).width('100%')
      }.width('100%').flexShrink(1).align(Alignment.Top).scrollBar(BarState.Auto).id('editor-scroll')
    }
  }
}
