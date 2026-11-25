import { BootstrapState, UserSummary } from '../model/NativeModels';
import { NativeClient } from '../platform/NativeClient';
import { FeatureAccountRequest, FeatureAvatarRequest, FeatureSettingRequest } from './FeatureContracts';
import { FeatureEmpty, FeatureError, featureError } from './FeatureControls';
import { dialogWidth } from './DialogLayout';
import { ProfilePageHeader, ProfileTemplateDialog, ProfileTemplateToken, ProfileUiIcon } from './ProfileUiControls';
import { courseAccent, localImage, withOpacity } from './AppComponents';
import { bottomSheetOptions, SheetSurface } from './SheetSurface';

interface AvatarChoice {
  key: string;
  label: string;
}

@Component
export struct FeatureAccounts {
  client: NativeClient | undefined = undefined;
  onBack: () => void = () => {};
  onNavigate: (route: string) => void = () => {};
  onChanged: () => void = () => {};
  @State private users: UserSummary[] = [];
  @State private avatar: string = '';
  @State private customAvatar: string = '';
  @State private multiAccount: boolean = false;
  @State private todayTemplate: string = '';
  @State private weekTemplate: string = '';
  @State private loading: boolean = false;
  @State private busy: boolean = false;
  @State private error: string = '';
  @State private avatarSheet: boolean = false;
  @StorageProp('harmonyDark') private dark: boolean = false;
  private active: boolean = false;
  private epoch: number = 0;
  private dialog?: CustomDialogController;
  private avatars: AvatarChoice[] = [
    { key: 'boy1', label: '清爽学长' }, { key: 'boy2', label: '阳光运动' },
    { key: 'boy3', label: '文艺书生' }, { key: 'boy4', label: '活力少年' },
    { key: 'girl1', label: '温柔学姐' }, { key: 'girl2', label: '元气少女' },
    { key: 'girl3', label: '知性清新' }, { key: 'girl4', label: '软萌可人' }
  ];
  private accountTokens: ProfileTemplateToken[] = [
    { label: '学号', value: '{studentNo}' }, { label: '姓名', value: '{name}' }, { label: '昵称', value: '{nickName}' }
  ];

  aboutToAppear(): void { this.active = true; this.load(); }
  aboutToDisappear(): void { this.active = false; this.epoch++; this.avatarSheet = false; this.dialog?.close(); }

  private async load(): Promise<void> {
    const current = ++this.epoch;
    this.loading = true;
    this.error = '';
    try {
      const state = JSON.parse(await this.client!.request('bootstrap')) as BootstrapState;
      if (!this.active || current !== this.epoch) return;
      this.users = state.users;
      this.avatar = state.avatar;
      this.customAvatar = state.customAvatar ?? state.avatar;
      this.multiAccount = state.multiAccount ?? false;
      this.todayTemplate = state.todayAccountTemplate ?? '{studentNo}({name})';
      this.weekTemplate = state.weekAccountTemplate ?? '{studentNo}({name})';
    } catch (error) {
      if (this.active && current === this.epoch) this.error = featureError(error as Error);
    } finally {
      if (this.active && current === this.epoch) this.loading = false;
    }
  }

  private async changeAvatar(mode: string, value: string): Promise<void> {
    if (this.busy || !this.active) return;
    this.avatarSheet = false;
    this.busy = true;
    this.error = '';
    const request: FeatureAvatarRequest = { mode: mode, value: value };
    try {
      await this.client!.request('avatar', request);
      if (this.active) { this.onChanged(); await this.load(); }
    } catch (error) {
      if (this.active) this.error = featureError(error as Error);
    } finally {
      if (this.active) this.busy = false;
    }
  }

  private async account(operation: string, user: UserSummary): Promise<void> {
    if (this.busy || !this.active) return;
    this.busy = true;
    this.error = '';
    const request: FeatureAccountRequest = { operation: operation, id: user.id };
    try {
      await this.client!.request('account', request);
      if (this.active) { this.onChanged(); await this.load(); }
    } catch (error) {
      if (this.active) this.error = featureError(error as Error);
    } finally {
      if (this.active) this.busy = false;
    }
  }

  private async setting(key: string, value: string): Promise<void> {
    if (this.busy || !this.active) return;
    this.busy = true;
    this.error = '';
    const previousMultiAccount = this.multiAccount;
    if (key === 'multiAccountMode') this.multiAccount = value === 'true';
    const request: FeatureSettingRequest = { key: key, value: value };
    try {
      await this.client!.request('setSetting', request);
      if (this.active) { this.onChanged(); await this.load(); }
    } catch (error) {
      if (this.active) {
        this.error = featureError(error as Error);
        if (key === 'multiAccountMode') this.multiAccount = previousMultiAccount;
      }
    } finally {
      if (this.active) this.busy = false;
    }
  }

  private editTemplate(today: boolean): void {
    if (this.busy) return;
    this.dialog?.close();
    this.dialog = new CustomDialogController({
      builder: ProfileTemplateDialog({ title: today ? '今日课程账号模板' : '本周课程账号模板',
        initialValue: today ? this.todayTemplate : this.weekTemplate, resetValue: '{studentNo}({name})',
        tokens: this.accountTokens, onConfirm: (value: string) => this.setting(today ? 'todayTemplate' : 'weekTemplate', value) }),
      customStyle: true, autoCancel: true
    });
    this.dialog.open();
  }

  private confirmLogout(user: UserSummary): void {
    if (this.busy) return;
    this.getUIContext().showAlertDialog({
      width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
      title: '退出登录', message: '确认退出账号「' + user.name + ' (' + user.id + ')」吗？退出后将清除该账号的本地登录态。',
      primaryButton: { value: '取消', action: () => {} },
      secondaryButton: { value: '确认退出', fontColor: $r('app.color.danger'), action: () => this.account('logout', user) }
    });
  }

  private avatarStatus(): string {
    if (this.customAvatar.startsWith('builtin:')) {
      return '当前：内置头像 · ' + (this.avatars.find((item: AvatarChoice) => 'builtin:' + item.key === this.customAvatar)?.label ?? '已选择');
    }
    if (this.customAvatar.length > 0) return '当前：相册自定义头像';
    return '当前：默认头像（按账号自动匹配）';
  }

  private initial(user: UserSummary): string {
    const name = user.name.substring(user.name.lastIndexOf('-') + 1).trim();
    return name.length > 0 ? name.substring(0, 1).toUpperCase() : (user.id.slice(-2) || '学');
  }

  private badgeColor(user: UserSummary): string {
    return /^#(?:[0-9a-fA-F]{2})?[0-9a-fA-F]{6}$/.test(user.color) ? user.color : '#2E628C';
  }

  private metadata(user: UserSummary): string {
    return [user.grade ? user.grade.toString() + '级' : '', user.college, user.major, user.direction ?? '']
      .filter((value: string) => value.length > 0).join(' · ');
  }

  @Builder
  private groupTitle(title: string) {
    Text(title).fontSize(14).fontWeight(600).fontColor($r('app.color.primary')).width('100%')
      .padding({ left: 8, top: 8, bottom: 6 })
  }

  @Builder
  private avatarOption(avatar: AvatarChoice) {
    Column({ space: 6 }) {
      Stack({ alignContent: Alignment.BottomEnd }) {
        Image($rawfile('shared/img_' + avatar.key + '.jpg')).width('100%').aspectRatio(1)
          .objectFit(ImageFit.Cover).borderRadius(16)
        if (this.customAvatar === 'builtin:' + avatar.key) {
          Stack() { ProfileUiIcon({ kind: 'check', iconSize:14, tint: $r('app.color.on_primary') }) }
            .width(22).height(22).backgroundColor($r('app.color.primary')).borderRadius(11).margin(4)
        }
      }.width('100%').aspectRatio(1).borderRadius(16).clip(true)
        .border({ width: this.customAvatar === 'builtin:' + avatar.key ? 2.5 : 0.8,
          color: this.customAvatar === 'builtin:' + avatar.key ? $r('app.color.primary') : withOpacity($r('app.color.outline'), 0.5) })
      Text(avatar.label).fontSize(11).lineHeight(13.75).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
        .fontColor(this.customAvatar === 'builtin:' + avatar.key ? $r('app.color.primary') : $r('app.color.text_secondary'))
    }.layoutWeight(1).id('account-avatar-' + avatar.key).accessibilityText(avatar.label +
      (this.customAvatar === 'builtin:' + avatar.key ? '，已选中' : ''))
      .onClick(() => this.changeAvatar('builtin', avatar.key))
  }

  @Builder
  private avatarPicker() {
    SheetSurface() {
      Scroll() {
        this.avatarContent()
      }.width('100%').flexShrink(1).align(Alignment.Top).scrollBar(BarState.Auto)
    }
  }

  @Builder
  private avatarContent() {
    Column({ space: 16 }) {
      Column({ space: 4 }) {
        Text('选择本地头像').fontSize(22).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
        Text('仅保存在本地设备，不上传服务端，切换账号后保持不变').fontSize(12).fontColor($r('app.color.text_secondary'))
      }.width('100%').alignItems(HorizontalAlign.Start)
      Column({ space: 12 }) {
        ForEach([0, 1], (row: number) => {
          Row({ space: 10 }) {
            ForEach(this.avatars.slice(row * 4, row * 4 + 4), (avatar: AvatarChoice) => {
              this.avatarOption(avatar)
            }, (avatar: AvatarChoice) => avatar.key)
          }.width('100%')
        }, (row: number) => row.toString())
      }.width('100%')
      Row({ space: 10 }) {
        Button('从相册选择', { type: ButtonType.Normal }).layoutWeight(1).height(48).borderRadius(14)
          .fontSize(14).fontColor($r('app.color.on_primary')).backgroundColor($r('app.color.primary'))
          .id('account-avatar-pick').onClick(() => this.changeAvatar('pick', ''))
        if (this.customAvatar.length > 0) {
          Button('恢复默认', { type: ButtonType.Normal }).height(48).borderRadius(14).fontSize(14)
            .backgroundColor(Color.Transparent).fontColor($r('app.color.primary')).border({ width: 1, color: $r('app.color.outline') })
            .id('account-avatar-reset').onClick(() => this.changeAvatar('reset', ''))
        }
      }.width('100%')
    }.width('100%').padding({ left: 20, right: 20, bottom: 28 })
  }

  @Builder
  private accountCard(user: UserSummary) {
    Column({ space: 12 }) {
      Row({ space: 12 }) {
        Row({ space: 12 }) {
          Text(this.initial(user)).fontSize(16).fontWeight(FontWeight.Bold)
            .fontColor(courseAccent(this.badgeColor(user), this.dark))
            .width(46).height(46).textAlign(TextAlign.Center).backgroundColor(withOpacity(this.badgeColor(user), 0.16))
            .border({ width: 1, color: withOpacity(this.badgeColor(user), 0.32) }).borderRadius(14)
          Column({ space: 3 }) {
            Flex({ wrap: FlexWrap.Wrap, alignItems: ItemAlign.Center }) {
              Text(user.name).fontSize(16).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
                .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis }).margin({ right: 8 })
              if (user.main) {
                Text('当前主账号').fontSize(11).fontWeight(600).fontColor($r('app.color.on_primary_container'))
                  .backgroundColor($r('app.color.primary_container')).padding({ left: 7, right: 7, top: 2, bottom: 2 }).borderRadius(8)
              }
            }.width('100%')
            Text(user.id).fontSize(12).fontColor($r('app.color.text_secondary'))
          }.layoutWeight(1).alignItems(HorizontalAlign.Start)
        }.layoutWeight(1).id('account-switch-' + user.id)
          .onClick(() => { if (!user.main) this.account('main', user); })
        Button({ type: ButtonType.Circle }) { ProfileUiIcon({ kind: 'refresh', iconSize:18, tint: $r('app.color.on_secondary_container') }) }
          .width(40).height(40).backgroundColor($r('app.color.secondary_container')).enabled(!this.busy)
          .id('account-refresh-' + user.id).accessibilityText('强制刷新用户信息')
          .onClick(() => this.account('refresh', user))
        Button({ type: ButtonType.Circle }) { ProfileUiIcon({ kind: 'logout', iconSize:18, tint: $r('app.color.on_error_container') }) }
          .width(40).height(40).backgroundColor($r('app.color.error_container')).enabled(!this.busy)
          .id('account-logout-' + user.id).accessibilityText('退出登录')
          .onClick(() => this.confirmLogout(user))
      }.width('100%')
      if (this.metadata(user).length > 0) {
        Text(this.metadata(user)).fontSize(12).fontColor($r('app.color.text_secondary')).width('100%')
          .onClick(() => { if (!user.main) this.account('main', user); })
      }
    }.width('100%').padding(16).backgroundColor($r('app.color.card')).borderRadius(20)
      .border({ width: user.main ? 1.5 : 0.5,
        color: user.main ? $r('app.color.primary') : withOpacity($r('app.color.outline'), 0.45) })
      .id('account-card-' + user.id)
  }

  @Builder
  private templateRow(today: boolean) {
    Row({ space: 16 }) {
      ProfileUiIcon({ kind: 'account' })
      Column({ space: 4 }) {
        Text(today ? '今日课程账号信息模板' : '本周课程账号信息模板').fontSize(16).fontColor($r('app.color.text_primary'))
        Text('当前：' + (today ? this.todayTemplate : this.weekTemplate)).fontSize(12).fontColor($r('app.color.text_secondary'))
      }.layoutWeight(1).alignItems(HorizontalAlign.Start)
    }.width('100%').padding(16).id(today ? 'account-today-template' : 'account-week-template')
      .onClick(() => this.editTemplate(today))
  }

  build() {
    Column() {
      ProfilePageHeader({ title: '账号管理', busy: this.loading || this.busy, onBack: () => this.onBack() })
      Stack({ alignContent: Alignment.Bottom }) {
        List({ space: 12 }) {
          if (this.error.length > 0) { ListItem() { FeatureError({ message: this.error, onRetry: () => this.load() }) } }
          ListItem() {
            Column() {
              this.groupTitle('个性头像')
              Row({ space: 14 }) {
                Image(localImage(this.avatar)).width(56).height(56).objectFit(ImageFit.Cover).borderRadius(16)
                  .border({ width: 1, color: $r('app.color.outline') }).accessibilityText('当前头像')
                Column({ space: 3 }) {
                  Text('本地头像').fontSize(14).fontWeight(600).fontColor($r('app.color.text_primary'))
                  Text(this.avatarStatus()).fontSize(12).fontColor($r('app.color.text_secondary'))
                    .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
                  Text('仅本地生效，切换账号保持不变').fontSize(11).fontColor($r('app.color.text_secondary'))
                }.layoutWeight(1).alignItems(HorizontalAlign.Start)
                Button('更换', { type: ButtonType.Normal }).fontSize(12).fontWeight(600).height(36).borderRadius(12)
                  .backgroundColor($r('app.color.secondary_container')).fontColor($r('app.color.on_secondary_container'))
                  .enabled(!this.busy).onClick(() => { this.avatarSheet = true; })
              }.width('100%').padding({ left: 16, right: 16, top: 14, bottom: 14 })
                .backgroundColor($r('app.color.card')).borderRadius(20)
                .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) }).id('account-avatar')
                .onClick(() => { if (!this.busy) this.avatarSheet = true; })
            }.width('100%')
          }
          ListItem() {
            Column() {
              this.groupTitle('多账号显示')
              Column() {
                Row({ space: 16 }) {
                  Image($rawfile('shared/ic_action_multi_user.svg')).width(24).height(24).fillColor($r('app.color.text_primary'))
                  Column({ space: 4 }) {
                    Text('多账号模式').fontSize(16).fontColor($r('app.color.text_primary'))
                    Text('同时展示所有已登录账号的课程安排').fontSize(12).fontColor($r('app.color.text_secondary'))
                  }.layoutWeight(1).alignItems(HorizontalAlign.Start)
                  Toggle({ type: ToggleType.Switch, isOn: this.multiAccount }).selectedColor($r('app.color.primary'))
                    .enabled(!this.busy && !this.loading).id('setting-toggle-multiAccountMode')
                    .onChange((value: boolean) => { if (value !== this.multiAccount) this.setting('multiAccountMode', value.toString()); })
                }.width('100%').padding(16)
                if (this.multiAccount) {
                  Divider().margin({ left: 16, right: 16 }).color($r('app.color.outline')).opacity(0.35)
                  this.templateRow(true)
                  Divider().margin({ left: 16, right: 16 }).color($r('app.color.outline')).opacity(0.35)
                  this.templateRow(false)
                }
              }.width('100%').backgroundColor($r('app.color.card')).borderRadius(20).clip(true)
                .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) })
            }.width('100%')
          }
          ListItem() {
            Row() {
              Text('已登录账号 (' + this.users.length + ')').fontSize(14).fontWeight(600).fontColor($r('app.color.primary'))
              Blank()
              Text('点击卡片切换主账号').fontSize(11).fontColor($r('app.color.text_secondary'))
            }.width('100%').padding({ left: 8, right: 8, top: 4 })
          }
          ForEach(this.users, (user: UserSummary) => { ListItem() { this.accountCard(user) } },
            (user: UserSummary) => user.id + ':' + JSON.stringify(user))
          if (this.users.length === 0 && !this.loading && this.error.length === 0) {
            ListItem() { FeatureEmpty({ title: '还没有登录账号', description: '登录教务账号后同步你的课程' }) }
          }
        }.width('100%').height('100%').padding({ left: 16, right: 16 }).contentStartOffset(6).contentEndOffset(88)
        Button({ type: ButtonType.Normal }) {
          Row({ space: 12 }) {
            ProfileUiIcon({ kind: 'add', tint: $r('app.color.on_primary_container') })
            Text('登录其他账号').fontSize(14).fontWeight(600).fontColor($r('app.color.on_primary_container'))
          }.padding({ left: 20, right: 20 })
        }.height(56).borderRadius(16).backgroundColor($r('app.color.primary_container')).margin({ bottom: 16 })
          .shadow({ radius: 6, color: '#18000000', offsetY: 3 }).enabled(!this.busy).id('account-add')
          .onClick(() => this.onNavigate('addAccount'))
      }.layoutWeight(1).width('100%')
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
      .bindSheet($$this.avatarSheet, this.avatarPicker(), bottomSheetOptions())
  }
}
