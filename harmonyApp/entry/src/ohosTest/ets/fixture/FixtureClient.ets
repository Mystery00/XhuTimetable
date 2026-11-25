import { common } from '@kit.AbilityKit';
import { NativeClient } from '../../../main/ets/platform/NativeClient';
import { DisplayItem, EditorField, HomeState, ListResult, LoginResult, UserSummary } from '../../../main/ets/model/NativeModels';
import { FeedbackStatus, FeatureAccountRequest, FeatureEditorRequest, FeatureKindRequest, FeatureQueryRequest, FeatureSaveRequest,
  FeatureSettingRequest } from '../../../main/ets/components/FeatureContracts';
import { fixtureAppearance, fixtureAppearanceSettings, fixtureBootstrap, fixtureEditor, fixtureEntryItem, fixtureHome,
  fixtureItem, fixtureList, fixtureOptions, fixtureSettings, fixtureUsers } from './FixtureData';

interface FixtureEntry {
  kind: string;
  id: string;
  user: string;
  year: number;
  term: number;
  values: Record<string, string>;
}

interface FixtureAppearanceRequest {
  values: Record<string, string>;
}

interface FixtureCourseColorQuery extends FeatureKindRequest {
  keywords: string;
}

interface FixtureCourseColor {
  name: string;
  defaultColor: string;
  customColor?: string;
}

// 仅用于 UI 验证：所有写操作仅修改本实例内存；从不启动 native、不转发线上请求、不写持久化配置。
export class FixtureClient extends NativeClient {
  private scenario: string;
  private fixtureActive: boolean = true;
  private fixtureListeners: Set<() => void> = new Set();
  private users: UserSummary[] = fixtureUsers();
  private showStatus: boolean = false;
  private nightMode: string = 'OFF';
  private queryAttempts: number = 0;
  private failedDeleteOnce: boolean = false;
  private failedRoomFilterOnce: boolean = false;
  private nextId: number = 1;
  private entries: FixtureEntry[] = [];
  private courseColors: FixtureCourseColor[] = [];
  private appearance: Record<string, string> = fixtureAppearance();
  private home: HomeState;

  constructor(context: common.UIAbilityContext, scenario: string) {
    super(context);
    this.scenario = scenario;
    if (scenario === 'courseColors') {
      this.courseColors = [
        { name: '测试高等数学', defaultColor: '#2E86DE' },
        { name: '测试大学物理', defaultColor: '#16A085', customColor: '#3142EA' }
      ];
    }
    this.home = fixtureHome(scenario);
    ['customCourses', 'customThings'].forEach((kind: string) => {
      const values: Record<string, string> = {};
      fixtureEditor(kind).fields.forEach((field: EditorField) => { values[field.key] = field.value; });
      this.entries.push({ kind: kind, id: kind === 'customCourses' ? 'course-1' : 'thing-1', user: '20990001', year: 2026, term: 1, values: values });
    });
  }

  start(): void {
    throw new Error('测试夹具禁止连接线上业务');
  }

  getUsers(): UserSummary[] {
    return this.users;
  }

  setHome(home: HomeState): void {
    this.home = home;
  }

  private entry(request: FeatureEditorRequest): FixtureEntry | undefined {
    if (request.kind !== 'customThings' && request.kind !== 'customCourses') throw new Error('测试夹具仅允许编辑离线课程或事项');
    if (!this.users.some((user: UserSummary) => user.id === request.user)) throw new Error('测试账号不存在');
    if (request.id.length === 0) return undefined;
    const entry = this.entries.find((entry: FixtureEntry) => entry.kind === request.kind && entry.id === request.id &&
      entry.user === request.user && (entry.kind === 'customThings' || entry.year === request.year && entry.term === request.term));
    if (entry === undefined) throw new Error('测试记录不存在');
    return entry;
  }

  private requestCourseColors(action: string, payload: Object): string {
    if ((payload as FeatureKindRequest).kind !== 'courseColors') throw new Error('离线课程颜色请求的 kind 错误');
    if (action === 'query') {
      const keywords = (payload as FixtureCourseColorQuery).keywords;
      if (typeof keywords !== 'string') throw new Error('离线课程颜色查询缺少关键词字符串');
      const items = this.courseColors.filter((course: FixtureCourseColor) =>
        keywords.trim().length === 0 || course.name.includes(keywords)).map((course: FixtureCourseColor): DisplayItem => {
        const color = course.customColor ?? course.defaultColor;
        // 共享层的 nativeHex() 返回小写 #aarrggbb，弹窗预览和保存使用 #RRGGBB。
        const item = fixtureItem(course.name, course.name, '#ff' + color.slice(1).toLowerCase());
        item.selected = course.customColor !== undefined;
        return item;
      });
      const result: ListResult = { title: '自定义课程颜色', summary: '', items: items, page: 1, hasMore: false };
      return JSON.stringify(result);
    }
    const id = (payload as FeatureEditorRequest).id;
    const course = this.courseColors.find((item: FixtureCourseColor) => item.name === id);
    if (course === undefined) throw new Error('离线课程颜色请求了未知课程：' + id);
    if (action === 'save') {
      const color = (payload as FeatureSaveRequest).values?.['color'];
      if (typeof color !== 'string' || !/^#[0-9A-Fa-f]{6}$/.test(color)) throw new Error('课程颜色须为 #RRGGBB');
      course.customColor = color;
    } else {
      course.customColor = undefined;
    }
    this.changed();
    return 'null';
  }

  async request(action: string, payload: Object = new Object()): Promise<string> {
    if (!this.fixtureActive) throw new Error('测试夹具已关闭');
    if ((this.scenario === 'courseColors' || this.scenario === 'courseColors-empty') && ['query', 'save', 'delete'].includes(action)) {
      return this.requestCourseColors(action, payload);
    }
    switch (action) {
      case 'bootstrap': {
        const bootstrap = fixtureBootstrap(this.users);
        bootstrap.nightMode = this.nightMode;
        bootstrap.multiAccount = this.scenario === 'timetable';
        return JSON.stringify(bootstrap);
      }
      case 'home': return JSON.stringify(this.home);
      case 'options': return JSON.stringify(fixtureOptions(this.users));
      case 'query': {
        this.queryAttempts++;
        if (this.scenario === 'scores-loading' && this.queryAttempts === 1) {
          await new Promise<void>((resolve) => { setTimeout(resolve, 2500); });
          if (!this.fixtureActive) throw new Error('测试夹具已关闭');
        }
        if (this.scenario === 'scores-error' && this.queryAttempts === 1) throw new Error('测试网络暂不可用');
        const request = payload as FeatureQueryRequest;
        const result = fixtureList(request.kind, request.term, this.scenario === 'scores-refresh' && this.queryAttempts > 1);
        if (this.scenario === 'rooms-filter-retry' && request.kind === 'rooms') {
          const periods = request.periods.join(',');
          if (request.page !== 1) throw new Error('离线教室查询请求了错误页码：' + request.page);
          if (periods === '1,2') result.items = [fixtureItem('room-a101', '测试教室 A101')];
          else if (periods === '3,4') {
            if (!this.failedRoomFilterOnce) {
              this.failedRoomFilterOnce = true;
              throw new Error('离线模拟：新节次第一页失败');
            }
            result.items = [fixtureItem('room-b202', '测试教室 B202')];
          } else throw new Error('离线教室查询请求了错误节次：' + periods);
        }
        if (request.kind === 'customThings' || request.kind === 'customCourses') {
          result.items = this.entries.filter((entry: FixtureEntry) => entry.kind === request.kind && entry.user === request.user &&
            (entry.kind === 'customThings' || entry.year === request.year && entry.term === request.term))
            .map((entry: FixtureEntry) => fixtureEntryItem(entry.kind, entry.id, entry.values));
        }
        return JSON.stringify(result);
      }
      case 'settings': {
        const request = payload as FeatureKindRequest;
        if (request.kind === 'customUi') return JSON.stringify(fixtureAppearanceSettings(this.appearance));
        if (request.kind !== 'settings' && request.kind !== 'classSettings') throw new Error('没有定义离线设置夹具：' + request.kind);
        return JSON.stringify(fixtureSettings(request.kind, this.showStatus, this.nightMode));
      }
      case 'setSetting': {
        const request = payload as FeatureSettingRequest;
        if (request.key === 'showStatus' && ['true', 'false'].includes(request.value)) this.showStatus = request.value === 'true';
        else if (request.key === 'nightMode' && ['AUTO', 'ON', 'OFF'].includes(request.value)) this.nightMode = request.value;
        else if (request.key === 'resetUi') this.appearance = fixtureAppearance();
        else throw new Error('测试夹具不支持此设置：' + request.key);
        this.changed();
        return '{}';
      }
      case 'saveAppearance': {
        const request = payload as FixtureAppearanceRequest;
        const values: Record<string, string> = {};
        const keys = Object.keys(fixtureAppearance());
        if (Object.keys(request.values).length !== keys.length) throw new Error('界面草稿字段不完整');
        keys.forEach((key: string) => {
          if (typeof request.values[key] !== 'string') throw new Error('界面草稿缺少字段：' + key);
          values[key] = request.values[key];
        });
        this.appearance = values;
        this.changed();
        return '{}';
      }
      case 'account': {
        const request = payload as FeatureAccountRequest;
        const user = this.users.find((item: UserSummary) => item.id === request.id);
        if (user === undefined) throw new Error('测试账号不存在');
        if (request.operation === 'main') {
          this.users.forEach((item: UserSummary) => { item.main = item.id === request.id; });
        } else if (request.operation === 'logout') {
          this.users = this.users.filter((item: UserSummary) => item.id !== request.id);
          if (user.main && this.users.length > 0) this.users[0].main = true;
        } else if (request.operation === 'refresh') {
          user.major = '测试资料已更新';
          user.title = user.name + ' · ' + user.major;
        } else throw new Error('测试夹具不支持此账号操作：' + request.operation);
        this.changed();
        return '{}';
      }
      case 'editor': {
        const request = payload as FeatureEditorRequest;
        const entry = this.entry(request);
        const editor = fixtureEditor(request.kind, entry === undefined);
        if (entry !== undefined) editor.fields.forEach((field: EditorField) => { field.value = entry.values[field.key]; });
        return JSON.stringify(editor);
      }
      case 'save': {
        const request = payload as FeatureSaveRequest;
        const identity: FeatureEditorRequest = { kind: request.kind, id: request.id, user: request.user, year: request.year, term: request.term };
        const entry = this.entry(identity);
        const values: Record<string, string> = {};
        fixtureEditor(request.kind).fields.forEach((field: EditorField) => {
          if (typeof request.values[field.key] !== 'string') throw new Error('缺少测试编辑字段：' + field.key);
          values[field.key] = request.values[field.key];
        });
        if (!values[request.kind === 'customCourses' ? 'courseName' : 'title'].trim()) throw new Error('请填写标题');
        if (request.kind === 'customCourses' && !values['weeks']) throw new Error('请选择上课周次');
        if (request.kind === 'customThings' && values['countdown'] === 'true' && values['allDay'] !== 'true') {
          throw new Error('倒计时必须联动为全天事项');
        }
        if (entry !== undefined) entry.values = values;
        else this.entries.push({ kind: request.kind, id: 'fixture-new-' + this.nextId++, user: request.user,
          year: request.year, term: request.term, values: values });
        this.changed();
        return '{}';
      }
      case 'delete': {
        const request = payload as FeatureEditorRequest;
        const entry = this.entry(request);
        if (entry === undefined) throw new Error('测试记录不存在');
        if (this.scenario === 'customThings-delete-retry' && !this.failedDeleteOnce) {
          this.failedDeleteOnce = true;
          throw new Error('离线模拟：首次删除失败');
        }
        this.entries = this.entries.filter((item: FixtureEntry) => item !== entry);
        this.changed();
        return '{}';
      }
      case 'login': {
        const result: LoginResult = { success: false, error: '测试账号无法连接线上服务', showHelp: false };
        return JSON.stringify(result);
      }
      case 'feedbackStatus': {
        const status: FeedbackStatus = { state: 'CONNECTED', message: '', adminOnline: false };
        return JSON.stringify(status);
      }
      case 'feedbackSend':
        AppStorage.setOrCreate('fixtureFeedbackSendAttempts', (AppStorage.get<number>('fixtureFeedbackSendAttempts') ?? 0) + 1);
        throw new Error('测试夹具禁止发送反馈');
      case 'loginHelpDismiss':
      case 'feedbackOpen':
      case 'feedbackClose': return '{}';
      default: throw new Error('测试夹具禁止执行此操作：' + action);
    }
  }

  subscribe(listener: () => void): () => void {
    if (this.fixtureActive) this.fixtureListeners.add(listener);
    return () => { this.fixtureListeners.delete(listener); };
  }

  dispose(): void {
    this.fixtureActive = false;
    this.fixtureListeners.clear();
    super.dispose();
  }

  private changed(): void {
    this.fixtureListeners.forEach((listener: () => void) => listener());
    this.onStateChanged?.();
  }
}
