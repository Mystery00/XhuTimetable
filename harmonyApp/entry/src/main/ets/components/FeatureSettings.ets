import { SelectOption, SettingItem, SettingsState } from '../model/NativeModels';
import { NativeClient } from '../platform/NativeClient';
import { FeatureKindRequest, FeatureSettingRequest, FeatureUrlRequest } from './FeatureContracts';
import { FeatureChoiceDialog, FeatureError, FeatureTextDialog, featureError } from './FeatureControls';
import { SettingDateDialog, SettingTimeDialog } from './DateTimeDialogs';
import { ProfilePageHeader } from './ProfileUiControls';
import { dialogWidth } from './DialogLayout';
import { withOpacity } from './AppComponents';

interface SettingGroup {
  title: string;
  items: SettingItem[];
}

@Component
export struct FeatureSettings {
  client: NativeClient | undefined = undefined;
  @Prop @Watch('routeChanged') route: string = 'settings';
  onBack: () => void = () => {};
  onChanged: () => void = () => {};
  onNavigate: (route: string) => void = () => {};
  @State private items: SettingItem[] = [];
  @State private loading: boolean = false;
  @State private busyKey: string = '';
  @State private switchRevision: number = 0;
  @State private error: string = '';
  private active: boolean = false;
  private epoch: number = 0;
  private dialog?: CustomDialogController;

  aboutToAppear(): void { this.active = true; this.load(); }
  aboutToDisappear(): void { this.active = false; this.epoch++; this.dialog?.close(); }

  private routeChanged(): void {
    this.dialog?.close();
    this.busyKey = '';
    if (this.active) { this.items = []; this.load(); }
  }

  private async load(): Promise<void> {
    const current = ++this.epoch;
    this.loading = true;
    this.error = '';
    const request: FeatureKindRequest = { kind: this.route };
    try {
      const result = JSON.parse(await this.client!.request('settings', request)) as SettingsState;
      if (this.active && current === this.epoch) this.items = result.items;
    } catch (error) {
      if (this.active && current === this.epoch) this.error = featureError(error as Error);
    } finally {
      if (this.active && current === this.epoch) this.loading = false;
    }
  }

  private async update(key: string, value: string, successMessage: string = ''): Promise<void> {
    if (this.busyKey.length > 0 || !this.active) return;
    this.busyKey = key;
    this.error = '';
    const current = this.epoch;
    const request: FeatureSettingRequest = { key: key, value: value };
    try {
      await this.client!.request('setSetting', request);
      if (!this.active || current !== this.epoch) return;
      if (successMessage) this.getUIContext().getPromptAction().showToast({ message: successMessage });
      this.onChanged();
      this.busyKey = '';
      await this.load();
    } catch (error) {
      if (this.active && current === this.epoch) {
        this.error = featureError(error as Error);
        this.busyKey = '';
        this.items = this.items.slice();
      }
    }
  }

  private groups(): SettingGroup[] {
    const groups: SettingGroup[] = [];
    this.items.forEach((item: SettingItem) => {
      const previous = groups[groups.length - 1];
      if (previous !== undefined && previous.title === item.group) previous.items.push(item);
      else groups.push({ title: item.group, items: [item] });
    });
    return groups;
  }

  private summary(item: SettingItem): string {
    if (item.summary.length > 0) return item.summary;
    if (item.type === 'select') return item.options.find((option: SelectOption) => option.value === item.value)?.label || item.value;
    if (item.type === 'date' || item.type === 'time' || item.type === 'text' || item.type === 'number') return item.value;
    return '';
  }

  private async navigate(item: SettingItem): Promise<void> {
    const route = item.route ?? '';
    if (!route) return;
    if (/^https?:\/\//.test(route)) {
      const request: FeatureUrlRequest = { url: route };
      try { await this.client!.request('openUrl', request); }
      catch (error) { if (this.active) this.error = featureError(error as Error); }
    } else this.onNavigate(route);
  }

  private edit(item: SettingItem): void {
    if (this.busyKey.length > 0 || this.loading || item.enabled === false) return;
    if (item.type === 'navigate' || item.type === 'switchNavigation') { this.navigate(item); return; }
    if (item.type === 'toggle') { this.update(item.key, (item.value !== 'true').toString()); return; }
    if (item.type === 'action') {
      if (item.key === 'resetBackground' || item.key === 'resetPoemsToken') {
        this.update(item.key, item.value, item.key === 'resetBackground' ? '已重置启动图与背景设置' : '重置成功，重启应用后生效');
        return;
      }
      this.getUIContext().showAlertDialog({
        width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
        title: item.title, message: item.summary || '确定执行此操作吗？',
        primaryButton: { value: '取消', action: () => {} },
        secondaryButton: { value: '确定', action: () => this.update(item.key, item.value) }
      });
      return;
    }
    this.dialog?.close();
    if (item.type === 'time') {
      this.dialog = new CustomDialogController({ builder: SettingTimeDialog({ title: '自动切换明日课表时间',
        initialValue: item.value, onConfirm: (value: string) => this.update(item.key, value) }),
        alignment: DialogAlignment.Center, customStyle: true, autoCancel: true });
    } else if (item.type === 'date') {
      this.dialog = new CustomDialogController({ builder: SettingDateDialog({ title: item.title,
        initialValue: item.value, automatic: item.key === 'termStartDate',
        onAutomatic: () => this.update('resetTermStartDate', ''), onConfirm: (value: string) => this.update(item.key, value) }),
        alignment: DialogAlignment.Center, customStyle: true, autoCancel: true });
    } else if (item.type === 'select') {
      this.dialog = new CustomDialogController({ builder: FeatureChoiceDialog({ title: item.title,
        options: item.options, selected: [item.value], onConfirm: (values: string[]) => this.update(item.key, values[0]) }),
        customStyle: true, autoCancel: true });
    } else {
      this.dialog = new CustomDialogController({ builder: FeatureTextDialog({ title: item.title, message: item.summary,
        initialValue: item.value, numeric: item.type === 'number', onConfirm: (value: string) => this.update(item.key, value) }),
        customStyle: true, autoCancel: true });
    }
    this.dialog.open();
  }

  @Builder
  private settingRow(item: SettingItem) {
    Row({ space: 16 }) {
      Row({ space: 16 }) {
        Image($rawfile('shared/' + (item.icon || 'ic_settings') + '.svg')).width(24).height(24)
          .fillColor($r('app.color.text_primary')).accessibilityLevel('no')
        Column({ space: 4 }) {
          Text(item.title).fontSize(16).fontColor($r('app.color.text_primary')).width('100%')
          if (this.summary(item).length > 0) {
            Text(this.summary(item)).fontSize(12).fontColor($r('app.color.text_secondary')).width('100%')
          }
        }.layoutWeight(1).alignItems(HorizontalAlign.Start)
      }.layoutWeight(1).padding({ top: 16, bottom: 16 }).onClick(() => this.edit(item))
      if (item.type === 'switchNavigation' || item.type === 'time') {
        Divider().vertical(true).height(32).color($r('app.color.outline'))
      }
      if (this.busyKey === item.key) {
        LoadingProgress().width(28).height(28).color($r('app.color.primary'))
      } else if (item.type === 'toggle' || item.type === 'switchNavigation' || item.type === 'time') {
        Toggle({ type: ToggleType.Switch, isOn: item.type === 'time' ? item.value.length > 0 : item.value === 'true' })
          .id('setting-toggle-' + item.key).selectedColor($r('app.color.primary'))
          .enabled(this.busyKey.length === 0 && !this.loading && item.enabled !== false)
          .onChange((value: boolean) => {
            if (item.type === 'time') {
              if (value && item.value.length === 0) this.edit(item);
              else if (!value && item.value.length > 0) this.update(item.key, '');
              // 时间弹窗取消时，开关以持久化值重新渲染。
              this.switchRevision++;
            } else if (value !== (item.value === 'true')) this.update(item.key, value.toString());
          })
      }
    }.width('100%').padding({ left: 16, right: 16 }).opacity(item.enabled === false ? 0.45 : 1)
      .id('setting-' + item.key)
  }

  build() {
    Column() {
      ProfilePageHeader({ title: this.route === 'classSettings' ? '课程设置' : '软件设置', busy: this.loading,
        onBack: () => this.onBack() })
      List() {
        if (this.error.length > 0) { ListItem() { FeatureError({ message: this.error, onRetry: () => this.load() }) } }
        ForEach(this.groups(), (group: SettingGroup) => {
          ListItem() {
            Column() {
              if (group.title.length > 0) {
                Text(group.title).fontSize(14).fontWeight(600).fontColor($r('app.color.primary')).width('100%')
                  .padding({ left: 8, right: 8, top: 8, bottom: 6 })
              }
              Column() {
                ForEach(group.items, (item: SettingItem) => { this.settingRow(item) },
                  (item: SettingItem) => item.key + ':' + JSON.stringify(item) + ':' + this.switchRevision)
              }.width('100%').backgroundColor($r('app.color.card')).borderRadius(20).clip(true)
                .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) })
            }.width('100%').padding({ left: 16, right: 16, top: 6, bottom: 6 })
          }
        }, (group: SettingGroup) => group.title + ':' + JSON.stringify(group.items))
        if (this.loading && this.items.length === 0) {
          ListItem() { Row() { LoadingProgress().width(40).height(40).color($r('app.color.primary')) }
            .height(180).width('100%').justifyContent(FlexAlign.Center) }
        }
      }.layoutWeight(1).width('100%').contentEndOffset(24).scrollBar(BarState.Auto)
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
  }
}
