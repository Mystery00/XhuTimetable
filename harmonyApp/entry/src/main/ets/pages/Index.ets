import { common, ConfigurationConstant } from '@kit.AbilityKit';
import { ColorMetrics, KeyboardAvoidMode, window } from '@kit.ArkUI';
import { inputMethod } from '@kit.IMEKit';
import { BootstrapState, HomeState, ProfileMenu } from '../model/NativeModels';
import { NativeClient } from '../platform/NativeClient';
import { HomeContent, emptyHomeState } from '../components/HomeContent';
import { LoginContent } from '../components/LoginContent';
import { StartupContent } from '../components/StartupContent';
import { ProfileContent } from '../components/ProfileContent';
import { NativeEmpty, localImage, withOpacity } from '../components/AppComponents';
import { HomeIcon } from '../components/HomeWidgets';
import { FeaturePage } from '../components/FeaturePage';
import { dialogWidth } from '../components/DialogLayout';
import { FeatureUrlRequest, FeatureShareRequest } from '../components/FeatureContracts';

interface PrivacyDecisionRequest {
  accepted: boolean;
}

interface TermStartDecisionRequest {
  sync: boolean;
}

interface WeekRequest {
  week: number;
}

interface HomeMessageRequest {
  text: string;
}

interface MainTab {
  id: string;
  label: string;
  icon: string;
  selectedIcon: string;
}

interface TermStartAlert {
  oldDate: string;
  newDate: string;
  customDate: string;
  message: string;
}

interface ApplicationState extends BootstrapState {
  termAlert?: TermStartAlert;
}

function initialBootstrap(): ApplicationState {
  return { privacy: false, loggedIn: false, version: '', nightMode: 'AUTO', loginLabel: '', helpUrl: '',
    privacyUrl: '', avatar: '', users: [] };
}

function hexColor(resource: Resource): string {
  const color = ColorMetrics.resourceColor(resource);
  return '#' + color.red.toString(16).padStart(2, '0') + color.green.toString(16).padStart(2, '0') +
    color.blue.toString(16).padStart(2, '0');
}

@Entry
@Component
struct Index {
  private client: NativeClient | undefined = undefined;
  private hostWindow: window.Window | undefined = undefined;
  @State private app: ApplicationState = initialBootstrap();
  @State private home: HomeState = emptyHomeState();
  @State private ready: boolean = false;
  @State private starting: boolean = true;
  @State private error: string = '';
  @State private selectedTab: string = 'today';
  @State private routes: string[] = [];
  @State private acceptingPrivacy: boolean = false;
  @StorageProp('harmonyDark') @Watch('onThemeConfigurationChanged') private dark: boolean = false;
  private disposed: boolean = false;
  private reloading: boolean = false;
  private reloadAgain: boolean = false;
  private reloadTimer: number = -1;
  private generation: number = 0;
  private termDialogOpen: boolean = false;
  private termDecisionKey: string = '';
  private appliedNightMode: string = '';
  private appliedBarColor: string = '';
  private featureBackHandler: (() => boolean) | undefined = undefined;
  private homeBackHandler: (() => boolean) | undefined = undefined;
  private homeActions: ((action: string) => void) | undefined = undefined;
  @State private calendarTitle: string = '';
  @State private displayedWeek: number = 1;
  private lastHomeWeek: number = -1;
  @State private systemTop: number = 0;
  @StorageLink('harmonyNavigationInset') private systemBottom: number = 0;
  private onAvoidAreaChanged = (_event: window.AvoidAreaOptions): void => { this.updateInsets(); };

  private updateInsets(): void {
    const hostWindow = this.hostWindow;
    if (hostWindow === undefined || this.disposed) return;
    const system = hostWindow.getWindowAvoidArea(window.AvoidAreaType.TYPE_SYSTEM);
    const navigation = hostWindow.getWindowAvoidArea(window.AvoidAreaType.TYPE_NAVIGATION_INDICATOR);
    const keyboard = hostWindow.getWindowAvoidArea(window.AvoidAreaType.TYPE_KEYBOARD);
    this.systemTop = this.getUIContext().px2vp(system.topRect.height);
    this.systemBottom = keyboard.bottomRect.height > 0 ? 0 :
      this.getUIContext().px2vp(Math.max(system.bottomRect.height, navigation.bottomRect.height));
  }

  private showingLogin(): boolean {
    return this.ready && this.app.privacy && (!this.app.loggedIn || this.route() === 'addAccount');
  }

  private statusBackground(): ResourceColor {
    return this.hasMainBackground() ? withOpacity($r('app.color.surface'), this.dark ? 0.78 : 0.86) : $r('app.color.background');
  }

  private navigationBackground(): ResourceColor {
    if (!this.app.loggedIn || this.routes.length > 0) return $r('app.color.background');
    return this.hasMainBackground() ? withOpacity($r('app.color.container'), this.dark ? 0.88 : 0.92) : $r('app.color.container');
  }

  aboutToAppear(): void {
    this.disposed = false;
    this.getUIContext().setKeyboardAvoidMode(KeyboardAvoidMode.RESIZE);
    const context = this.getUIContext().getHostContext() as common.UIAbilityContext;
    window.getLastWindow(context).then((value: window.Window) => {
      if (this.disposed) return;
      this.hostWindow = value;
      value.on('avoidAreaChange', this.onAvoidAreaChanged);
      this.updateInsets();
      value.setWindowLayoutFullScreen(true).then(() => { this.updateInsets(); this.updateSystemBars(); });
      this.updateSystemBars();
    }).catch(() => {});
    this.start();
  }

  aboutToDisappear(): void {
    this.disposed = true;
    this.generation++;
    if (this.reloadTimer >= 0) clearTimeout(this.reloadTimer);
    this.reloadTimer = -1;
    if (this.client !== undefined) {
      this.client.onStateChanged = undefined;
      this.client.dispose();
      this.client = undefined;
    }
    this.hostWindow?.off('avoidAreaChange', this.onAvoidAreaChanged);
    this.hostWindow = undefined;
  }

  onDidBuild(): void {
    this.updateSystemBars();
  }

  onPageShow(): void {
    this.appliedBarColor = '';
    this.updateSystemBars();
    if (this.ready && this.app.loggedIn && this.client) {
      this.client.request('resumeHome').finally(() => this.scheduleReload()).catch(() => {});
    } else if (this.ready) this.scheduleReload();
  }

  private onThemeConfigurationChanged(): void {
    this.appliedBarColor = '';
    this.updateSystemBars();
  }

  onBackPress(): boolean {
    if (this.ready && !this.app.privacy) return true;
    if (this.hideKeyboard()) return true;
    if (this.featureBackHandler?.() === true || this.homeBackHandler?.() === true) return true;
    if (this.routes.length > 0) {
      this.goBack();
      return true;
    }
    if (this.ready && this.app.loggedIn && this.selectedTab !== 'today') {
      this.selectedTab = 'today';
      return true;
    }
    return false;
  }

  private hideKeyboard(): boolean {
    if (this.hostWindow === undefined ||
      this.hostWindow.getWindowAvoidArea(window.AvoidAreaType.TYPE_KEYBOARD).bottomRect.height <= 0) return false;
    inputMethod.getController().hideTextInput().catch(() => {});
    return true;
  }

  private updateSystemBars(): void {
    const hostWindow = this.hostWindow;
    if (hostWindow === undefined || this.disposed) return;
    this.getUIContext().runScopedTask(() => {
      const base = hexColor($r('app.color.background'));
      const foreground = hexColor($r('app.color.text_primary'));
      const key = base + foreground;
      if (key === this.appliedBarColor) return;
      this.appliedBarColor = key;
      hostWindow.setWindowBackgroundColor(base);
      hostWindow.setWindowSystemBarProperties({
        statusBarColor: '#00000000', navigationBarColor: '#00000000',
        statusBarContentColor: foreground, navigationBarContentColor: foreground
      }).catch(() => { this.appliedBarColor = ''; });
    });
  }

  private applyNightMode(value: string): void {
    if (value === this.appliedNightMode) return;
    this.appliedNightMode = value;
    const context = this.getUIContext().getHostContext() as common.UIAbilityContext;
    let mode = ConfigurationConstant.ColorMode.COLOR_MODE_NOT_SET;
    if (value === 'ON') mode = ConfigurationConstant.ColorMode.COLOR_MODE_DARK;
    else if (value === 'OFF') mode = ConfigurationConstant.ColorMode.COLOR_MODE_LIGHT;
    context.getApplicationContext().setColorMode(mode);
    this.appliedBarColor = '';
  }

  private start(): void {
    if (this.disposed) return;
    this.starting = true;
    this.error = '';
    this.generation++;
    this.client?.dispose();
    this.client = undefined;
    this.reloading = false;
    this.reloadAgain = false;
    try {
      const context = this.getUIContext().getHostContext() as common.UIAbilityContext;
      const client = new NativeClient(context);
      this.client = client;
      client.onStateChanged = () => { this.scheduleReload(); };
      client.start();
      this.reloadState();
    } catch (error) {
      this.error = (error as Error).message;
      this.starting = false;
    }
  }

  private scheduleReload(): void {
    if (this.disposed) return;
    if (this.reloading) {
      this.reloadAgain = true;
      return;
    }
    if (this.reloadTimer >= 0) return;
    this.reloadTimer = setTimeout(() => {
      this.reloadTimer = -1;
      this.reloadState();
    }, 80);
  }

  private async reloadState(): Promise<void> {
    const client = this.client;
    if (client === undefined || this.disposed) return;
    if (this.reloading) {
      this.reloadAgain = true;
      return;
    }
    this.reloading = true;
    const generation = this.generation;
    try {
      const app = JSON.parse(await client.request('bootstrap')) as ApplicationState;
      if (this.disposed || generation !== this.generation) return;
      this.applyNightMode(app.nightMode);
      this.app = app;
      if (app.privacy && app.loggedIn) {
        const home = JSON.parse(await client.request('home')) as HomeState;
        if (this.disposed || generation !== this.generation) return;
        this.home = home;
        if (this.lastHomeWeek !== home.week) {
          this.displayedWeek = home.week;
          this.lastHomeWeek = home.week;
        }
        if (!home.calendarEnabled && this.selectedTab === 'calendar') this.selectedTab = 'today';
      } else {
        this.home = emptyHomeState();
        this.routes = [];
      }
      this.ready = true;
      this.error = '';
      if (this.home.error.length > 0) {
        this.getUIContext().getPromptAction().showToast({ message: this.home.error, duration: 2500 });
        const request: HomeMessageRequest = { text: this.home.error };
        await client.request('clearHomeMessage', request);
        if (this.disposed || generation !== this.generation) return;
      }
      if (app.privacy) this.showTermAlert(app.termAlert);
    } catch (error) {
      if (!this.disposed && generation === this.generation) this.error = (error as Error).message;
    } finally {
      if (!this.disposed && generation === this.generation) {
        this.starting = false;
        this.reloading = false;
        if (this.reloadAgain) {
          this.reloadAgain = false;
          this.scheduleReload();
        }
      }
    }
  }

  private async showTermAlert(alert: TermStartAlert | undefined): Promise<void> {
    if (alert === undefined || this.termDialogOpen || this.disposed || this.client === undefined) return;
    const key = alert.oldDate + ':' + alert.newDate + ':' + alert.customDate;
    if (key === this.termDecisionKey) return;
    this.termDialogOpen = true;
    try {
      const sync = await new Promise<boolean | undefined>((resolve) => {
        const options: AlertDialogParamWithButtons = {
          width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
          title: '开学日期有更新',
          message: alert.message || '云端开学日期已更新为 ' + alert.newDate + '，是否同步到课表？',
          autoCancel: false,
          primaryButton: {
            value: '保留自定义', fontColor: $r('app.color.text_secondary'), action: () => { resolve(false); }
          },
          secondaryButton: {
            value: '同步云端', fontColor: $r('app.color.primary'), action: () => { resolve(true); }
          },
          cancel: () => { resolve(undefined); }
        };
        this.getUIContext().showAlertDialog(options);
      });
      if (this.disposed || sync === undefined) return;
      const request: TermStartDecisionRequest = { sync: sync };
      await this.client.request('termStartDecision', request);
      this.termDecisionKey = key;
      this.scheduleReload();
    } catch (error) {
      if (!this.disposed) this.error = (error as Error).message;
    } finally {
      this.termDialogOpen = false;
    }
  }

  private async acceptPrivacy(): Promise<void> {
    if (this.acceptingPrivacy || this.client === undefined) return;
    this.acceptingPrivacy = true;
    this.error = '';
    try {
      const request: PrivacyDecisionRequest = { accepted: true };
      const response = await this.client.request('privacy', request);
      if (this.disposed) return;
      this.app = JSON.parse(response) as ApplicationState;
      this.scheduleReload();
    } catch (error) {
      if (!this.disposed) this.error = (error as Error).message;
    } finally {
      if (!this.disposed) this.acceptingPrivacy = false;
    }
  }

  private async openPrivacy(): Promise<void> {
    if (this.client === undefined || this.app.privacyUrl.length === 0) return;
    try {
      const request: FeatureUrlRequest = { url: this.app.privacyUrl };
      await this.client.request('openUrl', request);
    } catch (error) {
      this.error = (error as Error).message;
    }
  }

  private declinePrivacy(): void {
    const context = this.getUIContext().getHostContext() as common.UIAbilityContext;
    context.terminateSelf().catch(() => { this.error = '暂时无法退出，请使用系统返回键'; });
  }

  private async refresh(): Promise<void> {
    if (this.home.loading || this.client === undefined) return;
    this.error = '';
    try {
      await this.client.request('refresh');
      this.scheduleReload();
    } catch (error) {
      if (!this.disposed) this.error = (error as Error).message;
    }
  }

  private async changeWeek(week: number): Promise<void> {
    this.displayedWeek = week;
    if (this.client === undefined) return;
    try {
      const request: WeekRequest = { week: week };
      await this.client.request('week', request);
      this.scheduleReload();
    } catch (error) {
      if (!this.disposed) this.error = (error as Error).message;
    }
  }

  private navigate(route: string): void {
    this.hideKeyboard();
    this.routes = this.routes.concat([route]);
  }

  private route(): string {
    return this.routes[this.routes.length - 1] ?? '';
  }

  private goBack(): void {
    this.routes = this.routes.slice(0, -1);
    this.scheduleReload();
  }

  private loggedIn(): void {
    if (this.route() === 'addAccount') this.routes = this.routes.slice(0, -1);
    this.scheduleReload();
  }

  private tabs(): MainTab[] {
    const tabs: MainTab[] = [
      { id: 'today', label: this.home.showTomorrow ? '明日' : '今日', icon: 'ic_today_course_unchecked', selectedIcon: 'ic_today_course' },
      { id: 'week', label: '本周', icon: 'ic_week_course_unchecked', selectedIcon: 'ic_week_course' }
    ];
    if (this.home.calendarEnabled) {
      tabs.push({ id: 'calendar', label: '月历', icon: 'ic_calendar_unchecked', selectedIcon: 'ic_calendar' });
    }
    tabs.push({ id: 'profile', label: '我的', icon: 'ic_profile_unchecked', selectedIcon: 'ic_profile' });
    return tabs;
  }

  private title(): string {
    if (this.selectedTab === 'today') return this.home.title || '今日课程';
    if (this.selectedTab === 'week') return this.displayedWeek <= 0 ? '尚未开学' : this.displayedWeek > 20 ? '学期已结束' : '第' + this.displayedWeek + '周';
    if (this.selectedTab === 'calendar') return this.calendarTitle || (this.home.week <= 0 ? '月历日程' : this.home.week > 20 ? '学期已结束' : '第' + this.home.week + '周');
    return '我的';
  }

  private hasMainBackground(): boolean {
    return this.ready && this.app.loggedIn && this.routes.length === 0 && this.selectedTab !== 'profile' &&
      this.home.background.length > 0 && !(this.dark && this.home.hideBackgroundInDark);
  }

  private async copyText(text: string): Promise<void> {
    if (this.client === undefined) return;
    const request: FeatureShareRequest = { text: text };
    try {
      await this.client.request('copyText', request);
      this.getUIContext().getPromptAction().showToast({ message: '已复制' });
    } catch (error) {
      this.getUIContext().getPromptAction().showToast({ message: (error as Error).message });
    }
  }

  private async openMenu(menu: ProfileMenu): Promise<void> {
    if (menu.action === 'route') { this.navigate(menu.route); return; }
    if (this.client === undefined) return;
    try {
      if (menu.action === 'link') {
        const request: FeatureUrlRequest = { url: menu.link };
        await this.client.request('openUrl', request);
      } else if (menu.action === 'share') {
        const request: FeatureShareRequest = { text: '查课查成绩，我就用西瓜课表~ 下载链接：https://xgkb.mystery0.vip' };
        await this.client.request('share', request);
      } else {
        this.getUIContext().getPromptAction().showToast({ message: menu.hint || '当前版本暂不支持该功能，请更新到最新版本' });
      }
    } catch (error) {
      this.getUIContext().getPromptAction().showToast({ message: (error as Error).message });
    }
  }

  @Builder
  private errorBanner() {
    if (this.error.length > 0) {
      Row({ space: 8 }) {
        Text(this.error).fontSize(12).fontColor($r('app.color.danger')).layoutWeight(1).maxLines(3)
        Button('重试', { type: ButtonType.Normal }).fontSize(12).fontColor($r('app.color.primary'))
          .backgroundColor(Color.Transparent).height(32).onClick(() => { this.scheduleReload(); })
      }.width('100%').padding({ left: 16, right: 8, top: 6, bottom: 6 }).backgroundColor($r('app.color.surface_variant'))
    }
  }

  @Builder
  private privacyPage() {
    StartupContent({ showPrivacy: !this.acceptingPrivacy, error: this.error,
      onOpenPrivacy: () => { this.openPrivacy(); },
      onDecline: () => { if (!this.acceptingPrivacy) this.declinePrivacy(); },
      onAccept: () => { this.acceptPrivacy(); } })
  }

  @Builder
  private mainTitle() {
    if (this.selectedTab === 'week') {
      Column({ space: 0 }) {
        Row({ space: 2 }) {
          Text(this.title()).fontSize(16).lineHeight(21.12).fontWeight(600)
            .fontColor($r('app.color.text_primary'))
          Image($rawfile('toolbar_chevron.svg')).width(18).height(18)
            .fillColor($r('app.color.text_secondary')).accessibilityLevel('no')
        }
        if (this.home.actualWeek >= 1 && this.home.actualWeek <= 20 && this.displayedWeek !== this.home.actualWeek) {
          Text('非本周 · 当前第' + this.home.actualWeek + '周').fontSize(11).lineHeight(13.75)
            .fontColor($r('app.color.primary'))
        }
      }.padding({ left: 12, right: 12, top: 5, bottom: 5 }).borderRadius(14)
        .backgroundColor(withOpacity($r('app.color.container_high'), 0.72)).id('main-week-picker')
        .onClick(() => this.homeActions?.('week')).accessibilityText('切换周次')
    } else {
      Column() {
        Text(this.title()).fontSize(16).lineHeight(21.12).fontWeight(600)
          .fontColor($r('app.color.text_primary')).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
        if (this.selectedTab === 'today' && this.home.showTomorrow) {
          Text('已自动切换至明日课表').fontSize(11).lineHeight(13.75).fontColor($r('app.color.primary'))
        }
      }.constraintSize({ maxWidth: '65%' })
    }
  }

  @Builder
  private mainToolbar() {
    Stack() {
      this.mainTitle()
      Row() {
        Blank()
        if (this.selectedTab === 'today') {
          if (this.home.loading) {
            Row() { LoadingProgress().width(24).height(24).color($r('app.color.text_primary')) }
              .width(48).height(64).justifyContent(FlexAlign.Center)
          }
          Button({ type: ButtonType.Circle }) {
            Image($rawfile('toolbar_add.svg')).width(24).height(24).fillColor($r('app.color.text_secondary'))
          }.width(48).height(48).backgroundColor(Color.Transparent).id('main-add')
            .accessibilityText('添加自定义课程或事项').onClick(() => this.homeActions?.('add'))
        } else if (this.selectedTab === 'week' || this.selectedTab === 'calendar') {
          if (this.selectedTab === 'week' && this.home.actualWeek >= 1 && this.home.actualWeek <= 20 && this.displayedWeek !== this.home.actualWeek) {
            Button({ type: ButtonType.Circle }) {
              HomeIcon({ name: 'today', iconSize: 24, tint: $r('app.color.primary') })
            }.width(48).height(48).backgroundColor(Color.Transparent).id('main-current-week')
              .accessibilityText('回到本周').onClick(() => this.homeActions?.('today'))
          }
          if (this.selectedTab === 'calendar') {
            Button({ type: ButtonType.Circle }) {
              Stack({ alignContent: Alignment.TopEnd }) {
                Image($rawfile('shared/ic_practical_course.svg')).width(24).height(24).fillColor($r('app.color.text_primary'))
                if (this.home.practical.length > 0) {
                  Text(this.home.practical.length.toString()).fontSize(11).fontColor($r('app.color.on_secondary_container'))
                    .backgroundColor($r('app.color.secondary_container')).padding({ left: 4, right: 4 })
                    .constraintSize({ minWidth: 16, minHeight: 16 }).borderRadius(8).translate({ x: 9, y: -5 })
                }
              }.width(24).height(24)
            }.width(48).height(48).backgroundColor(Color.Transparent).id('main-practical')
              .accessibilityText('查看实践课程').onClick(() => this.homeActions?.('practical'))
          }
          Button({ type: ButtonType.Circle }) {
            if (this.home.loading) {
              LoadingProgress().width(24).height(24).color($r('app.color.text_primary'))
            } else {
              Image($rawfile('shared/ic_sync.svg')).width(24).height(24).fillColor($r('app.color.text_primary'))
            }
          }.width(48).height(48).backgroundColor(Color.Transparent).enabled(!this.home.loading)
            .id('main-refresh').accessibilityText('刷新课表').onClick(() => this.refresh())
        }
      }.width('100%').height(64).padding({ right: 4 }).hitTestBehavior(HitTestMode.Transparent)
    }.width('100%').height(64)
      .backgroundColor(this.hasMainBackground() ? withOpacity($r('app.color.surface'), this.dark ? 0.78 : 0.86) : $r('app.color.surface'))
  }

  @Builder
  private mainPage() {
    Column() {
      this.mainToolbar()
      this.errorBanner()
      if (this.selectedTab === 'profile') {
        ProfileContent({ users: this.app.users, avatar: this.app.avatar, version: this.app.version,
          menuGroups: this.app.menuGroups ?? [], onMenu: (menu: ProfileMenu) => this.openMenu(menu),
          onNavigate: (route: string) => this.navigate(route) }).layoutWeight(1)
      } else {
        HomeContent({ home: this.home, tab: this.selectedTab,
          onNavigate: (route: string) => this.navigate(route), onRefresh: () => this.refresh(),
          onWeek: (week: number) => this.changeWeek(week), onCopy: (text: string) => this.copyText(text),
          onCalendarTitle: (title: string) => { this.calendarTitle = title; },
          registerActions: (handler: ((action: string) => void) | undefined) => { this.homeActions = handler; },
          registerBackHandler: (handler: (() => boolean) | undefined) => { this.homeBackHandler = handler; }
        }).layoutWeight(1)
      }
      Row() {
        ForEach(this.tabs(), (tab: MainTab) => {
          Column({ space: 4 }) {
            Stack() {
              Image($rawfile('shared/' + (this.selectedTab === tab.id ? tab.selectedIcon : tab.icon) + (this.dark ? '_night' : '') + '.svg'))
                .width(24).height(24).objectFit(ImageFit.Contain).accessibilityLevel('no')
            }.width(64).height(32).borderRadius(16)
              .backgroundColor(this.selectedTab === tab.id ? $r('app.color.secondary_container') : Color.Transparent)
            Text(tab.label).fontSize(12).lineHeight(15.36)
              .fontWeight(this.selectedTab === tab.id ? 600 : FontWeight.Normal)
              .fontColor(this.selectedTab === tab.id ? $r('app.color.primary') : $r('app.color.text_secondary'))
          }.layoutWeight(1).height(80).justifyContent(FlexAlign.Center).id('main-tab-' + tab.id)
            .onClick(() => { this.selectedTab = tab.id; }).accessibilityGroup(true)
            .accessibilityText(tab.label + (this.selectedTab === tab.id ? '，已选中' : ''))
        }, (tab: MainTab) => tab.id)
      }.width('100%').height(80)
        .backgroundColor(this.hasMainBackground() ? withOpacity($r('app.color.container'), this.dark ? 0.88 : 0.92) : $r('app.color.container'))
    }.width('100%').height('100%')
  }

  @Builder
  private currentPage() {
    Stack() {
      if (!this.ready) {
        if (this.starting) {
          StartupContent()
        } else {
          NativeEmpty({ title: '暂时无法启动', subtitle: this.error || '请稍后重试', image: 'state_no_data.png',
            action: '重新尝试', onAction: () => { this.start(); } })
        }
      } else if (!this.app.privacy) {
        this.privacyPage()
      } else if (!this.app.loggedIn || this.route() === 'addAccount') {
        LoginContent({ client: this.client, loginLabel: this.app.loginLabel, privacyUrl: this.app.privacyUrl,
          helpUrl: this.app.helpUrl, fromAccountManager: this.route() === 'addAccount', systemTop: this.systemTop,
          onLoggedIn: () => { this.loggedIn(); }, onBack: () => { this.goBack(); } })
      } else if (this.routes.length > 0 && this.client !== undefined) {
        FeaturePage({ client: this.client, route: this.route(), onBack: () => { this.goBack(); },
          onNavigate: (route: string) => { this.navigate(route); }, onChanged: () => { this.scheduleReload(); },
          registerBackHandler: (handler: (() => boolean) | undefined) => { this.featureBackHandler = handler; } })
      } else {
        this.mainPage()
      }
    }.width('100%').layoutWeight(1)
  }

  build() {
    Stack() {
      if (this.hasMainBackground()) {
        Image(localImage(this.home.background)).width('100%').height('100%').objectFit(ImageFit.Cover)
          .blur(this.home.backgroundBlur).accessibilityLevel('no').hitTestBehavior(HitTestMode.None)
        Column() {}.width('100%').height('100%').backgroundColor(withOpacity(this.dark ? Color.Black : Color.White, this.dark ? 0.28 : 0.08))
          .hitTestBehavior(HitTestMode.None)
      }
      Column() {
        if (!this.showingLogin()) {
          Column() {}.width('100%').height(this.systemTop).backgroundColor(this.statusBackground())
        }
        this.currentPage()
        Column() {}.width('100%').height(this.systemBottom).backgroundColor(this.navigationBackground())
      }.width('100%').height('100%')
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
  }
}
