import { common } from '@kit.AbilityKit';
import { KeyboardAvoidMode } from '@kit.ArkUI';
import { HomeContent } from '../../../main/ets/components/HomeContent';
import { LoginContent } from '../../../main/ets/components/LoginContent';
import { StartupContent } from '../../../main/ets/components/StartupContent';
import { ProfileContent } from '../../../main/ets/components/ProfileContent';
import { FeaturePage } from '../../../main/ets/components/FeaturePage';
import { HomeState, ProfileMenu, UserSummary } from '../../../main/ets/model/NativeModels';
import { FixtureClient } from '../fixture/FixtureClient';
import { fixtureBootstrap, fixtureHome, fixtureUsers } from '../fixture/FixtureData';

@Entry
@Component
struct TestFixture {
  @StorageLink('fixtureGeneration') @Watch('resetFixture') generation: number = 0;
  @StorageLink('fixtureFeedbackSendAttempts') feedbackSendAttempts: number = 0;
  @State private scenario: string = 'today';
  @State private screen: string = 'today';
  @State private fakeHome: HomeState = fixtureHome();
  @State private users: UserSummary[] = fixtureUsers();
  @State private calendarTitle: string = '';
  @State private copiedText: string = '';
  @State private privacyError: string = '';
  @State private privacySaving: boolean = false;
  private client: FixtureClient | undefined = undefined;
  private backHandler: (() => boolean) | undefined = undefined;
  private homeActions: ((action: string) => void) | undefined = undefined;
  private history: string[] = [];

  aboutToAppear(): void {
    this.getUIContext().setKeyboardAvoidMode(KeyboardAvoidMode.RESIZE);
    this.resetFixture();
  }

  aboutToDisappear(): void {
    this.client?.dispose();
    this.client = undefined;
    this.backHandler = undefined;
    this.homeActions = undefined;
  }

  private resetFixture(): void {
    this.client?.dispose();
    this.scenario = AppStorage.get<string>('fixtureRoute') ?? 'today';
    this.screen = this.scenario === 'empty' ? 'today' : this.scenario.split('-')[0];
    this.fakeHome = fixtureHome(this.scenario);
    this.feedbackSendAttempts = 0;
    this.client = new FixtureClient(this.getUIContext().getHostContext() as common.UIAbilityContext, this.scenario);
    this.users = this.client.getUsers().slice();
    this.backHandler = undefined;
    this.homeActions = undefined;
    this.calendarTitle = '';
    this.copiedText = '';
    this.privacyError = '';
    this.privacySaving = false;
    this.history = [];
  }

  private navigate(route: string): void {
    this.history.push(this.screen);
    this.backHandler = undefined;
    this.screen = route === 'addAccount' ? 'login' : route;
    this.users = this.client?.getUsers().slice() ?? [];
  }

  private back(): void {
    this.backHandler = undefined;
    this.screen = this.history.pop() ?? 'profile';
    this.users = this.client?.getUsers().slice() ?? [];
  }

  private refreshHome(): void {
    this.fakeHome = fixtureHome(this.scenario === 'empty' ? 'today' : this.scenario);
    this.client?.setHome(this.fakeHome);
  }

  onBackPress(): boolean {
    if (this.backHandler?.()) return true;
    this.back();
    return true;
  }

  // 首页工具栏属于 Index；此测试宿主只转发动作，Sheet 与日程仍由真实 HomeContent 渲染。
  @Builder
  private homeToolbar() {
    Row({ space: 4 }) {
      if (this.screen === 'week') {
        Button('第' + this.fakeHome.week + '周').id('fixture-home-week').fontSize(14)
          .onClick(() => this.homeActions?.('week'))
      } else {
        Text(this.screen === 'calendar' ? this.calendarTitle : this.fakeHome.title)
          .id('fixture-home-title').fontSize(14).layoutWeight(1)
      }
      Blank()
      if (this.screen === 'week') {
        Button('实践').id('fixture-home-practical').fontSize(12).onClick(() => this.homeActions?.('practical'))
      }
      if (this.screen === 'week' || this.screen === 'calendar') {
        Button(this.screen === 'week' ? '回本周' : '回到今天').id('fixture-home-today').fontSize(12)
          .onClick(() => this.homeActions?.('today'))
      }
      Button('添加').id('fixture-home-add').fontSize(12).onClick(() => this.homeActions?.('add'))
    }.width('100%').height(52).padding({ left: 12, right: 12 })
  }

  build() {
    Column() {
      Text('UI 测试 · ' + this.scenario).id('fixture-scenario').fontSize(11)
        .fontColor('#FFFFFF').backgroundColor('#355948').textAlign(TextAlign.Center)
        .width('100%').height(24)
      Text(this.scenario === 'feedback' ? '离线发送尝试 ' + this.feedbackSendAttempts + ' 次' : '仅用于 UI 验证 · 离线合成数据')
        .fontSize(10).height(18)
      if (this.copiedText.length > 0) { Text(this.copiedText).id('fixture-copied-text').fontSize(11) }
      ForEach([this.generation], (generation: number) => {
        Column() {
          if (this.screen === 'today' || this.screen === 'week' || this.screen === 'calendar') {
            this.homeToolbar()
            HomeContent({ home: this.fakeHome, tab: this.screen,
              onRefresh: () => this.refreshHome(),
              onNavigate: (route: string) => this.navigate(route),
              onWeek: (week: number) => {
                const next = fixtureHome(this.scenario);
                next.week = week;
                this.fakeHome = next;
                this.client?.setHome(next);
              },
              onCalendarTitle: (title: string) => { this.calendarTitle = title; },
              onCopy: (text: string) => { this.copiedText = text; },
              registerActions: (handler: ((action: string) => void) | undefined) => { this.homeActions = handler; },
              registerBackHandler: (handler: (() => boolean) | undefined) => { this.backHandler = handler; } })
              .layoutWeight(1).id('fixture-home-content')
          } else if (this.screen === 'starting' || this.screen === 'privacy') {
            if (this.privacySaving) {
              Button('完成离线保存').onClick(() => {
                this.privacySaving = false;
                if (this.privacyError.length === 0) this.privacyError = '离线模拟：同意未保存';
                else this.screen = 'login';
              })
            }
            StartupContent({ showPrivacy: this.screen === 'privacy' && !this.privacySaving, error: this.privacyError,
              onOpenPrivacy: () => this.navigate('fixturePolicy'), onDecline: () => { this.screen = 'privacyExit'; },
              onAccept: () => { this.privacySaving = true; } }).layoutWeight(1)
          } else if (this.screen === 'fixturePolicy') {
            Text('离线隐私政策').fontSize(20)
            Button('返回').onClick(() => this.back())
          } else if (this.screen === 'privacyExit') {
            Text('已退出首次流程')
          } else if (this.screen === 'profile') {
            ProfileContent({ users: this.users, avatar: 'builtin:boy1', version: 'UI 测试',
              menuGroups: fixtureBootstrap(this.users).menuGroups ?? [],
              onMenu: (menu: ProfileMenu) => { if (menu.action === 'route') this.navigate(menu.route); },
              onNavigate: (route: string) => this.navigate(route) })
          } else if (this.screen === 'login') {
            LoginContent({ client: this.client, loginLabel: '仅用于 UI 验证，不会向线上发送登录请求',
              privacyUrl: '', helpUrl: '', onLoggedIn: () => this.navigate('today'),
              onBack: () => this.back() })
          } else {
            FeaturePage({ client: this.client, route: this.screen,
              onBack: () => this.back(), onNavigate: (route: string) => this.navigate(route),
              onChanged: () => { this.users = this.client?.getUsers().slice() ?? []; },
              registerBackHandler: (handler: (() => boolean) | undefined) => { this.backHandler = handler; } })
          }
        }.width('100%').layoutWeight(1)
      }, (generation: number) => generation.toString())
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
  }
}
