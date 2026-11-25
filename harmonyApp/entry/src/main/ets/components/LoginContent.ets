import { inputMethod } from '@kit.IMEKit';
import { NativeClient } from '../platform/NativeClient';
import { LoginResult } from '../model/NativeModels';
import { FeatureUrlRequest } from './FeatureContracts';
import { dialogWidth } from './DialogLayout';
import { ProfileUiIcon } from './ProfileUiControls';
import { withOpacity } from './AppComponents';

interface LoginRequest {
  username: string;
  password: string;
}

@Component
export struct LoginContent {
  client: NativeClient | undefined = undefined;
  @Prop loginLabel: string = '';
  @Prop privacyUrl: string = '';
  @Prop helpUrl: string = '';
  @Prop fromAccountManager: boolean = false;
  @Prop systemTop: number = 0;
  onLoggedIn: () => void = () => {};
  onBack: () => void = () => {};
  @State private username: string = '';
  @State private password: string = '';
  @State private usernameError: boolean = false;
  @State private passwordError: boolean = false;
  @State private accepted: boolean = true;
  @State private submitting: boolean = false;
  @State private error: string = '';
  @State private passwordVisible: boolean = false;
  @State private usernameFocused: boolean = false;
  @State private passwordFocused: boolean = false;
  private disposed: boolean = false;

  aboutToAppear(): void { this.disposed = false; }
  aboutToDisappear(): void { this.disposed = true; this.password = ''; }

  private async openUrl(url: string): Promise<void> {
    if (url.length === 0 || this.client === undefined || this.disposed) return;
    try {
      const request: FeatureUrlRequest = { url: url };
      await this.client.request('openUrl', request);
    } catch (error) {
      if (!this.disposed) this.error = (error as Error).message;
    }
  }

  private async help(): Promise<void> {
    if (this.client === undefined || this.disposed) return;
    try {
      const getHelp = await new Promise<boolean>((resolve) => {
        this.getUIContext().showAlertDialog({
          title: '登录遇到困难？',
          message: '已连续尝试登录 3 次，仍未成功。你可以前往帮助与反馈页面，通过交流群、邮件或留言联系我们协助排查。',
          width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
          primaryButton: { value: '继续尝试', fontColor: $r('app.color.text_secondary'), action: () => resolve(false) },
          secondaryButton: { value: '获取帮助', fontColor: $r('app.color.primary'), action: () => resolve(true) },
          cancel: () => resolve(false)
        });
      });
      if (this.disposed) return;
      await this.client.request('loginHelpDismiss');
      if (getHelp && !this.disposed) await this.openUrl(this.helpUrl);
    } catch {
      if (!this.disposed) this.client.request('loginHelpDismiss').catch(() => {});
    }
  }

  private async submit(): Promise<void> {
    const client = this.client;
    if (this.submitting || client === undefined || this.disposed) return;
    this.usernameError = this.username.trim().length === 0;
    this.passwordError = this.password.trim().length === 0;
    if (this.usernameError) {
      this.error = '';
      this.getUIContext().getFocusController().requestFocus('login-username');
      return;
    }
    if (this.passwordError) {
      this.error = '';
      this.getUIContext().getFocusController().requestFocus('login-password');
      return;
    }
    if (!this.accepted) { this.error = '请阅读并同意隐私协议'; return; }
    this.submitting = true;
    this.error = '';
    inputMethod.getController().hideTextInput().catch(() => {});
    try {
      const request: LoginRequest = { username: this.username.trim(), password: this.password };
      const response = await client.request('login', request);
      if (this.disposed) return;
      const result = JSON.parse(response) as LoginResult;
      if (result.success) {
        this.password = '';
        this.onLoggedIn();
      } else {
        this.error = result.error.length > 0 ? result.error : '登录未完成，请重试';
        if (result.showHelp) this.help();
      }
    } catch (error) {
      if (!this.disposed) this.error = (error as Error).message;
    } finally {
      if (!this.disposed) this.submitting = false;
    }
  }

  @Builder
  private usernameField() {
    Column({ space: 4 }) {
      Stack({ alignContent: Alignment.Start }) {
        Row() {
          ProfileUiIcon({ kind: 'account', tint: this.usernameError ? $r('app.color.danger') : $r('app.color.text_secondary') })
          TextInput({ text: this.username, placeholder: this.usernameFocused ? '' : '学号' })
            .id('login-username').type(InputType.Number).enterKeyType(EnterKeyType.Next)
            .layoutWeight(1).height(54).fontSize(16).padding({ left: 12, right: 0 })
            .backgroundColor(Color.Transparent).fontColor($r('app.color.text_primary'))
            .placeholderColor($r('app.color.text_secondary')).enabled(!this.submitting)
            .onFocus(() => { this.usernameFocused = true; }).onBlur(() => { this.usernameFocused = false; })
            .onChange((value: string) => { this.username = value; if (value.trim()) this.usernameError = false; })
            .onSubmit(() => { this.getUIContext().getFocusController().requestFocus('login-password'); })
          if (this.username.length > 0) {
            Button({ type: ButtonType.Circle }) { ProfileUiIcon({ kind: 'close', iconSize:20 }) }
              .width(40).height(48).backgroundColor(Color.Transparent).enabled(!this.submitting)
              .id('login-clear-username').accessibilityText('清除学号').onClick(() => { this.username = ''; })
          }
        }.padding({ left: 12, right: 4 }).height(56).width('100%').borderRadius(14)
          .border({ width: this.usernameFocused || this.usernameError ? 2 : 1,
            color: this.usernameError ? $r('app.color.danger') :
              this.usernameFocused ? $r('app.color.primary') : $r('app.color.outline') })
        if (this.usernameFocused || this.username.length > 0) {
          Text('学号').fontSize(12).fontColor(this.usernameError ? $r('app.color.danger') :
            this.usernameFocused ? $r('app.color.primary') : $r('app.color.text_secondary'))
            .padding({ left: 4, right: 4 }).backgroundColor($r('app.color.card')).position({ x: 12, y: -7 })
        }
      }.width('100%')
      if (this.usernameError) {
        Text('请输入学号').fontSize(12).fontColor($r('app.color.danger')).padding({ left: 16 }).id('login-username-error')
      }
    }.width('100%').alignItems(HorizontalAlign.Start)
  }

  @Builder
  private passwordField() {
    Column({ space: 4 }) {
      Stack({ alignContent: Alignment.Start }) {
        Row() {
          ProfileUiIcon({ kind: 'lock', tint: this.passwordError ? $r('app.color.danger') : $r('app.color.text_secondary') })
          TextInput({ text: this.password, placeholder: this.passwordFocused ? '' : '密码' })
            .id('login-password').type(this.passwordVisible ? InputType.Normal : InputType.Password)
            .showPasswordIcon(false).enterKeyType(EnterKeyType.Done).copyOption(CopyOptions.None)
            .layoutWeight(1).height(54).fontSize(16).padding({ left: 12, right: 0 })
            .backgroundColor(Color.Transparent).fontColor($r('app.color.text_primary'))
            .placeholderColor($r('app.color.text_secondary')).enabled(!this.submitting)
            .onFocus(() => { this.passwordFocused = true; }).onBlur(() => { this.passwordFocused = false; })
            .onChange((value: string) => { this.password = value; if (value.trim()) this.passwordError = false; })
            .onSubmit(() => { this.submit(); })
          if (this.password.length > 0) {
            Button({ type: ButtonType.Circle }) { ProfileUiIcon({ kind: 'close', iconSize:20 }) }
              .width(36).height(48).backgroundColor(Color.Transparent).enabled(!this.submitting)
              .id('login-clear-password').accessibilityText('清除密码').onClick(() => { this.password = ''; })
          }
          Button({ type: ButtonType.Circle }) { ProfileUiIcon({ kind: this.passwordVisible ? 'eyeOff' : 'eye', iconSize:22 }) }
            .width(40).height(48).backgroundColor(Color.Transparent).enabled(!this.submitting)
            .id('login-password-visibility').accessibilityText(this.passwordVisible ? '隐藏密码' : '显示密码')
            .onClick(() => { this.passwordVisible = !this.passwordVisible; })
        }.padding({ left: 12, right: 4 }).height(56).width('100%').borderRadius(14)
          .border({ width: this.passwordFocused || this.passwordError ? 2 : 1,
            color: this.passwordError ? $r('app.color.danger') :
              this.passwordFocused ? $r('app.color.primary') : $r('app.color.outline') })
        if (this.passwordFocused || this.password.length > 0) {
          Text('密码').fontSize(12).fontColor(this.passwordError ? $r('app.color.danger') :
            this.passwordFocused ? $r('app.color.primary') : $r('app.color.text_secondary'))
            .padding({ left: 4, right: 4 }).backgroundColor($r('app.color.card')).position({ x: 12, y: -7 })
        }
      }.width('100%')
      if (this.passwordError || this.loginLabel.length > 0) {
        Text(this.passwordError ? '请输入密码' : this.loginLabel).fontSize(12).lineHeight(16)
          .fontColor(this.passwordError ? $r('app.color.danger') : $r('app.color.text_secondary'))
          .padding({ left: 16, right: 16 }).id('login-password-support')
      }
    }.width('100%').alignItems(HorizontalAlign.Start)
  }

  build() {
    Scroll() {
      Column() {
        Stack({ alignContent: Alignment.Bottom }) {
          Image($rawfile('shared/login_header.jpg')).width('100%').height(236).objectFit(ImageFit.Cover).accessibilityLevel('no')
          Column() {}.width('100%').height(236)
            .linearGradient({ angle: 180, colors: [['#2E000000', 0], [Color.Transparent, 0.33],
              [withOpacity($r('app.color.background'), 0.72), 0.67], [$r('app.color.background'), 1]] })
          Image($rawfile('shared/ic_app_icon.svg')).width(74).height(74).objectFit(ImageFit.Cover)
            .borderRadius(22).backgroundColor('#3DDC84').border({ width: 2, color: $r('app.color.background') })
            .shadow({ radius: 8, color: '#22000000', offsetY: 4 }).accessibilityText('西瓜课表')
          if (this.fromAccountManager) {
            Row() {
              Button({ type: ButtonType.Normal }) {
                Row({ space: 4 }) {
                  ProfileUiIcon({ kind: 'back', iconSize:18, tint: $r('app.color.text_primary') })
                  Text('返回').fontSize(14).fontColor($r('app.color.text_primary'))
                }.padding({ left: 12, right: 12, top: 8, bottom: 8 })
              }.borderRadius(14).backgroundColor($r('app.color.surface')).id('login-back').onClick(this.onBack)
              Blank()
            }.width('100%').padding({ left: 12, right: 12 }).position({ x: 0, y: this.systemTop + 8 })
          }
        }.width('100%').height(236).id('login-header')
        Column() {
          Column({ space: 6 }) {
            Text(this.fromAccountManager ? '登录新账号' : '欢迎使用西瓜课表').fontSize(24).fontWeight(FontWeight.Bold)
              .fontColor($r('app.color.text_primary')).textAlign(TextAlign.Center)
            Row() {
              Text('请使用西华大学').fontSize(14).fontColor($r('app.color.text_secondary'))
              Text('教务系统').fontSize(14).fontColor($r('app.color.primary')).fontWeight(FontWeight.Medium)
                .decoration({ type: TextDecorationType.Underline, color: $r('app.color.primary') })
                .onClick(() => this.openUrl('https://jwc.xhu.edu.cn'))
              Text('账号登录').fontSize(14).fontColor($r('app.color.text_secondary'))
            }.justifyContent(FlexAlign.Center)
          }.width('100%').margin({ top: 12, bottom: 32 })
          Column({ space: 14 }) {
            this.usernameField()
            this.passwordField()
            if (this.error.length > 0 && !this.submitting) {
              Row({ space: 8 }) {
                ProfileUiIcon({ kind: 'error', iconSize:18, tint: $r('app.color.on_error_container') })
                Text(this.error).fontSize(12).fontColor($r('app.color.on_error_container')).lineHeight(16).layoutWeight(1)
              }.width('100%').padding({ left: 12, right: 12, top: 10, bottom: 10 }).borderRadius(12)
                .backgroundColor(withOpacity($r('app.color.error_container'), 0.7)).id('login-error').accessibilityText('登录提示：' + this.error)
            }
            Row({ space: 3 }) {
              Checkbox({ name: 'loginPrivacy' }).select(this.accepted).selectedColor($r('app.color.primary'))
                .width(20).height(20).margin({ left: 8, right: 10 }).enabled(!this.submitting)
                .onChange((value: boolean) => { this.accepted = value; }).accessibilityText('同意隐私协议')
              Text('我已阅读并同意').fontSize(12).fontColor($r('app.color.text_secondary'))
                .onClick(() => { if (!this.submitting) this.accepted = !this.accepted; })
              Text('《隐私协议》').fontSize(12).fontColor($r('app.color.primary'))
                .decoration({ type: TextDecorationType.Underline, color: $r('app.color.primary') })
                .onClick(() => this.openUrl(this.privacyUrl))
            }.width('100%').height(48)
            Button({ type: ButtonType.Normal }) {
              Row({ space: 10 }) {
                if (this.submitting) { LoadingProgress().width(20).height(20).color($r('app.color.on_primary')) }
                Text(this.submitting ? '正在登录…' : '登 录').fontSize(14).fontWeight(600).fontColor($r('app.color.on_primary'))
              }.justifyContent(FlexAlign.Center).width('100%')
            }.width('100%').height(50).borderRadius(16).backgroundColor($r('app.color.primary'))
              .enabled(!this.submitting).id('login-submit').onClick(() => this.submit())
          }.width('100%').padding(20).borderRadius(24).backgroundColor($r('app.color.card'))
            .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.45) })
          Button({ type: ButtonType.Normal }) {
            Row({ space: 4 }) {
              ProfileUiIcon({ kind: 'help', iconSize:16, tint: $r('app.color.primary') })
              Text('登录遇到问题？获取帮助与反馈').fontSize(14).fontColor($r('app.color.primary'))
            }
          }.height(48).backgroundColor(Color.Transparent).margin({ top: 16, bottom: 24 })
            .id('login-help').onClick(() => this.openUrl(this.helpUrl))
        }.width('100%').padding({ left: 24, right: 24 })
      }.width('100%')
    }.width('100%').height('100%').align(Alignment.Top).scrollBar(BarState.Off)
      .backgroundColor($r('app.color.background')).id('login-scroll')
  }
}
