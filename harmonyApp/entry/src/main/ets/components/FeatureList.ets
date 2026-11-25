import { AcademicOptions, DetailField, DisplayItem, ListResult, SelectOption, UserSummary, ItemAction } from '../model/NativeModels';
import { NativeClient } from '../platform/NativeClient';
import { FeatureEditorRequest, FeatureKindRequest, FeatureQueryRequest, FeatureSaveRequest,
  FeatureSettingRequest, FeatureShareRequest, FeatureUrlRequest } from './FeatureContracts';
import { FeatureChoiceDialog, FeatureEmpty, FeatureError, FeatureFilterChip, FeatureHeader, FeatureIcon, FeatureImageDialog,
  FeatureSummaryChip, featureError, featureTitle } from './FeatureControls';
import { withOpacity } from './AppComponents';
import { ScoreListHeader, ScoreReminderDialog } from './ScoreControls';
import { ScoreSummary, ScoreCard, ExamCard, ExperimentCard } from './QueryCards';
import { dialogWidth } from './DialogLayout';
import { ClassroomCard, CustomEntryCard, SchoolCourseCard, NoticeCard } from './QueryEntryCards';
import { BackgroundGallery, CalendarImageState, SchoolCalendarCanvas } from './QueryImages';
import { bottomSheetOptions, SheetSurface } from './SheetSurface';

const CAMPUS_ICON = 'M15 11V5l-3-3-3 3v2H3v14h18V11h-6zM7 19H5v-2h2zm0-4H5v-2h2zm0-4H5V9h2zm6 8h-2v-2h2zm0-4h-2v-2h2zm0-4h-2V9h2zm0-4h-2V5h2zm6 12h-2v-2h2zm0-4h-2v-2h2z';
const COLLEGE_ICON = 'M12 2L2 7v2h20V7zM4 11v7h3v-7zm6.5 0v7h3v-7zm6.5 0v7h3v-7zM2 20v2h20v-2z';
const MAJOR_ICON = 'M12 3L1 9l11 6 9-4.91V17h2V9zM5 13.18v4L12 21l7-3.82v-4L12 17z';

interface TimetableOptionsRequest { kind: string; college: string; }
interface UrgeRequest { id: string; }

@Component
export struct FeatureList {
  client: NativeClient | undefined = undefined;
  @Prop @Watch('reloadRoute') route: string = '';
  @Prop @Watch('refreshAfterEdit') refreshTrigger: number = 0;
  onBack: () => void = () => {};
  onNavigate: (route: string) => void = () => {};
  onEdit: (request: FeatureEditorRequest) => void = () => {};
  onChanged: () => void = () => {};
  @State private items: DisplayItem[] = [];
  @State private metrics: DetailField[] = [];
  @State private summary: string = '';
  @State private loading: boolean = false;
  @State private refreshing: boolean = false;
  @State private error: string = '';
  @State private hasMore: boolean = false;
  @State private page: number = 0;
  @State private optionsReady: boolean = false;
  @State private year: number = 0;
  @State private term: number = 0;
  @State private user: string = '';
  @State private years: SelectOption[] = [];
  @State private users: UserSummary[] = [];
  @State private area: string = '';
  @State private weeks: number[] = [];
  @State private days: number[] = [];
  @State private periods: number[] = [];
  @State private campus: string = '';
  @State private campuses: SelectOption[] = [];
  @State private buildings: SelectOption[] = [];
  @State private college: string = '';
  @State private colleges: SelectOption[] = [];
  @State private major: string = '';
  @State private majors: SelectOption[] = [];
  @State private courseName: string = '';
  @State private teacherName: string = '';
  @State private searched: boolean = false;
  @State private actionBusy: boolean = false;
  @State private filterSheet: boolean = false;
  @State private expandScores: boolean = true;
  @State private customFabVisible: boolean = true;
  private listScroller: Scroller = new Scroller();
  private terms: SelectOption[] = [{ value: '1', label: '第1学期' }, { value: '2', label: '第2学期' }];
  @State private calendarIndex: number = 0;
  @State private calendarAttempt: number = 0;
  @State private calendarImageState: CalendarImageState = CalendarImageState.Loading;
  private active: boolean = false;
  private epoch: number = 0;
  private dialog?: CustomDialogController;
  private lastQuery?: FeatureQueryRequest;
  private failedQuery?: FeatureQueryRequest;
  private refreshQueued: boolean = false;
  private quickPeriods: number[] = [1, 3, 5, 7, 9];

  aboutToAppear(): void { this.active = true; this.initialize(); }
  aboutToDisappear(): void { this.active = false; this.epoch++; this.dialog?.close(); }

  private reloadRoute(): void {
    if (!this.active) return;
    this.dialog?.close();
    this.filterSheet = false;
    this.epoch++;
    this.items = [];
    this.metrics = [];
    this.summary = '';
    this.page = 0;
    this.hasMore = false;
    this.loading = false;
    this.actionBusy = false;
    this.optionsReady = false;
    this.searched = false;
    this.area = ''; this.college = ''; this.major = ''; this.courseName = ''; this.teacherName = '';
    this.calendarIndex = 0;
    this.calendarAttempt++;
    this.calendarImageState = CalendarImageState.Loading;
    this.expandScores = true;
    this.customFabVisible = true;
    this.lastQuery = undefined;
    this.failedQuery = undefined;
    this.refreshQueued = false;
    this.initialize();
  }

  private refreshAfterEdit(): void { if (this.active) this.load(true); }
  private academic(): boolean { return ['scores', 'exams', 'expScores', 'customCourses'].includes(this.route); }
  private searchPage(): boolean { return this.route === 'rooms' || this.route === 'timetable'; }
  private customPage(): boolean { return this.route === 'customCourses' || this.route === 'customThings'; }
  private needsOptions(): boolean { return this.academic() || this.searchPage() || this.route === 'customThings'; }

  private async initialize(): Promise<void> {
    const current = ++this.epoch;
    this.error = '';
    this.loading = true;
    try {
      if (this.needsOptions()) {
        const request: FeatureKindRequest = { kind: this.route };
        const options = JSON.parse(await this.client!.request('options', request)) as AcademicOptions;
        if (!this.active || current !== this.epoch) return;
        this.year = options.year; this.term = options.term; this.years = options.years;
        this.users = options.users; this.user = options.selectedUser;
        this.campus = options.campus; this.campuses = options.campuses; this.buildings = options.buildings;
        this.colleges = options.colleges; this.majors = options.majors;
        this.weeks = [options.week]; this.days = [options.day]; this.periods = options.periods;
      }
      if (!this.active || current !== this.epoch) return;
      this.optionsReady = true;
      this.refreshing = false;
      this.loading = false;
      if (!this.searchPage()) await this.load(true);
    } catch (error) {
      if (this.active && current === this.epoch) { this.error = featureError(error as Error); this.loading = false; this.refreshing = false; }
    }
  }

  private async load(reset: boolean, retry?: FeatureQueryRequest): Promise<void> {
    if (!this.active || this.actionBusy) { this.refreshing = false; return; }
    if (this.loading) { if (reset) this.refreshQueued = true; this.refreshing = false; return; }
    if (this.route === 'rooms' && (this.weeks.length === 0 || this.days.length === 0 || this.periods.length === 0)) {
      this.error = '请选择查询周次、星期和节次';
      this.refreshing = false;
      this.filterSheet = true;
      return;
    }
    if (reset) this.epoch++;
    const current = this.epoch;
    this.loading = true;
    this.error = '';
    this.searched = true;
    const request: FeatureQueryRequest = retry ?? (reset || !this.lastQuery ? {
      kind: this.route, page: 1, year: this.year, term: this.term, user: this.route === 'timetable' ? '' : this.user,
      area: this.area, weeks: this.weeks.slice(), days: this.days.slice(), periods: this.periods.slice(), campus: this.campus,
      college: this.college, major: this.major, courseName: this.courseName.trim(), teacherName: this.teacherName.trim()
    } : {
      kind: this.lastQuery.kind, page: this.page + 1, year: this.lastQuery.year, term: this.lastQuery.term, user: this.lastQuery.user,
      area: this.lastQuery.area, weeks: this.lastQuery.weeks, days: this.lastQuery.days, periods: this.lastQuery.periods,
      campus: this.lastQuery.campus, college: this.lastQuery.college, major: this.lastQuery.major,
      courseName: this.lastQuery.courseName, teacherName: this.lastQuery.teacherName
    });
    this.failedQuery = undefined;
    try {
      const result = JSON.parse(await this.client!.request('query', request)) as ListResult;
      if (!this.active || current !== this.epoch) return;
      this.items = reset ? result.items : this.items.concat(result.items);
      if (reset) { this.metrics = result.metrics || []; this.lastQuery = request; }
      if (reset || result.summary.length > 0) this.summary = result.summary;
      this.page = result.page;
      this.hasMore = result.hasMore;
      if (this.route === 'schoolCalendar') {
        if (this.calendarIndex >= this.items.length) this.calendarIndex = 0;
        this.startCalendarImage();
      }
    } catch (error) {
      if (this.active && current === this.epoch) {
        this.failedQuery = request;
        this.error = featureError(error as Error);
      }
    } finally {
      if (this.active && current === this.epoch) {
        this.loading = false; this.refreshing = false;
        if (reset && this.customPage()) this.customFabVisible = true;
        if (this.refreshQueued) { this.refreshQueued = false; this.load(true); }
      }
    }
  }

  private filterChanged(): void {
    if (this.searchPage() && this.filterSheet) return;
    this.epoch++;
    this.loading = false;
    this.refreshing = false;
    this.refreshQueued = false;
    this.items = []; this.metrics = []; this.summary = ''; this.error = ''; this.hasMore = false; this.page = 0;
    if (!this.searchPage() || this.searched) this.load(true);
  }

  private choose(title: string, options: SelectOption[], selected: string[], multiple: boolean,
    changed: (values: string[]) => void, quickActions: boolean = false): void {
    this.dialog?.close();
    this.dialog = new CustomDialogController({
      builder: FeatureChoiceDialog({ title: title, options: options, selected: selected, multiple: multiple,
        allowEmpty: !multiple, columns: multiple ? 4 : 1, quickActions: quickActions,
        onConfirm: (values: string[]) => { changed(values); this.filterChanged(); } }),
      customStyle: true, autoCancel: true
    });
    this.dialog.open();
  }

  private numbers(count: number, prefix: string, suffix: string): SelectOption[] {
    const result: SelectOption[] = [];
    for (let value = 1; value <= count; value++) result.push({ value: value.toString(), label: prefix + value + suffix });
    return result;
  }

  private userLabel(): string { return this.users.find((item: UserSummary) => item.id === this.user)?.name ?? '加载中'; }
  private optionLabel(options: SelectOption[], value: string, fallback: string): string {
    if (value.length === 0) return fallback;
    return options.find((item: SelectOption) => item.value === value)?.label || fallback;
  }
  private allOptions(options: SelectOption[], label: string): SelectOption[] {
    const result: SelectOption[] = [{ value: '', label: label }];
    return result.concat(options.filter((option: SelectOption) => option.value.length > 0));
  }
  private weekLabel(): string { return this.weeks.length > 0 ? '第' + this.weeks.join('、') + '周' : '周次 *'; }
  private dayLabel(): string { return this.days.length > 0 ? this.days.map((day: number) => ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][day - 1]).join('、') : '星期 *'; }
  private periodLabel(): string { return this.periods.length > 0 ? '第' + this.periods.join('、') + '节' : '节次 *'; }

  private chooseUser(): void {
    this.choose('选择学生账号', this.users.map((user: UserSummary): SelectOption => ({ value: user.id, label: user.name + ' · ' + user.id })),
      [this.user], false, (values: string[]) => { this.user = values[0]; });
  }
  private chooseWeeks(): void {
    this.choose('选择查询周次', this.numbers(20, '第', '周'), this.weeks.map((v: number) => v.toString()), true,
      (values: string[]) => { this.weeks = values.map((v: string) => Number(v)).sort((a: number, b: number) => a - b); }, true);
  }
  private chooseDays(): void {
    this.choose('选择查询星期', ['星期一', '星期二', '星期三', '星期四', '星期五', '星期六', '星期日'].map((label: string, index: number): SelectOption => ({ value: (index + 1).toString(), label: label })),
      this.days.map((v: number) => v.toString()), true, (values: string[]) => { this.days = values.map((v: string) => Number(v)).sort((a: number, b: number) => a - b); });
  }
  private choosePeriods(): void {
    this.choose('选择查询节次', this.numbers(11, '第', '节'), this.periods.map((v: number) => v.toString()), true,
      (values: string[]) => { this.periods = values.map((v: string) => Number(v)).sort((a: number, b: number) => a - b); });
  }

  private async currentTime(): Promise<void> {
    const current = this.epoch;
    try {
      const request: FeatureKindRequest = { kind: 'rooms' };
      const options = JSON.parse(await this.client!.request('options', request)) as AcademicOptions;
      if (this.active && this.route === 'rooms' && current === this.epoch) {
        this.weeks = [options.week]; this.days = [options.day]; this.periods = options.periods;
      }
    } catch (error) { if (this.active && current === this.epoch) this.error = featureError(error as Error); }
  }

  private async changeCollege(value: string): Promise<void> {
    this.college = value; this.major = ''; this.majors = [];
    const current = this.epoch;
    const request: TimetableOptionsRequest = { kind: 'timetable', college: value };
    try {
      const result = JSON.parse(await this.client!.request('options', request)) as AcademicOptions;
      if (this.active && current === this.epoch && this.college === value) this.majors = result.majors;
    } catch (error) { if (this.active && current === this.epoch) this.error = featureError(error as Error); }
  }

  private currentItem(id: string): DisplayItem { return this.items.find((item: DisplayItem) => item.id === id)!; }

  private edit(item?: DisplayItem): void {
    const request: FeatureEditorRequest = { kind: this.route, id: item?.id || '', user: this.user, year: this.year, term: this.term };
    this.onEdit(request);
  }

  private async openItem(item: DisplayItem): Promise<void> {
    if (item.image.length > 0) {
      this.dialog?.close();
      this.dialog = new CustomDialogController({ builder: FeatureImageDialog({ image: item.image, title: item.title }), customStyle: true, autoCancel: true });
      this.dialog.open();
      return;
    }
    const detail = [item.subtitle, item.time, item.location, item.detail, item.status].filter((line: string) => line.length > 0).join('\n\n');
    this.getUIContext().showAlertDialog({
      width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
      title: item.title, message: detail || item.title,
      primaryButton: { value: '关闭', action: () => {} },
      secondaryButton: { value: item.url.length > 0 ? '打开链接' : '分享', action: () => {
        if (item.url.length > 0) this.openUrl(item.url); else this.share(item.title + '\n' + detail);
      } }
    });
  }

  private async importCourse(item: DisplayItem): Promise<void> {
    if (this.actionBusy || this.loading) return;
    this.actionBusy = true;
    const current = this.epoch;
    const request: FeatureSaveRequest = { kind: 'timetable', id: item.id, user: '',
      year: this.year, term: this.term, values: {} };
    try {
      await this.client!.request('save', request);
      if (!this.active || current !== this.epoch) return;
      this.onChanged();
      this.getUIContext().getPromptAction().showToast({ message: '已添加到课表' });
    } catch (error) { if (this.active && current === this.epoch) this.error = featureError(error as Error); }
    finally { if (this.active && current === this.epoch) this.actionBusy = false; }
  }

  private async perform(action: string, payload: Object): Promise<void> {
    if (this.actionBusy) return;
    this.actionBusy = true;
    const current = this.epoch;
    let changed = false;
    try {
      await this.client!.request(action, payload);
      changed = true;
      if (this.active && current === this.epoch) this.onChanged();
    } catch (error) { if (this.active && current === this.epoch) this.error = featureError(error as Error); }
    finally {
      if (this.active && current === this.epoch) { this.actionBusy = false; if (changed) this.load(true); }
    }
  }

  private async openUrl(url: string): Promise<void> {
    const request: FeatureUrlRequest = { url: url };
    try { await this.client!.request('openUrl', request); }
    catch (error) { if (this.active) this.error = featureError(error as Error); }
  }
  private async share(text: string): Promise<void> {
    const request: FeatureShareRequest = { text: text };
    try { await this.client!.request('share', request); }
    catch (error) { if (this.active) this.error = featureError(error as Error); }
  }
  private async copy(text: string): Promise<void> {
    const request: FeatureShareRequest = { text: text };
    try {
      await this.client!.request('copyText', request);
      if (this.active) this.getUIContext().getPromptAction().showToast({ message: this.route === 'exams' ? '已复制考试信息' : '已复制' });
    } catch (error) { if (this.active) this.error = featureError(error as Error); }
  }
  private itemAction(action: ItemAction): void {
    if (action.type === 'OPEN_URI') this.openUrl(action.value);
    else if (action.type === 'COPY') this.copy(action.value);
  }

  private selectBackground(item: DisplayItem): void {
    if (item.selected || item.id === 'local' || item.url.length === 0) return;
    const request: FeatureSettingRequest = { key: 'backgroundImage', value: item.id === 'default' ? 'default' : item.url };
    this.perform('setSetting', request);
  }
  private resetBackground(): void {
    this.getUIContext().showAlertDialog({
      width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
      title: '恢复默认背景图', message: '确认清除当前自定义背景并恢复为默认背景图吗？',
      primaryButton: { value: '取消', action: () => {} }, secondaryButton: { value: '恢复默认', action: () => {
        const request: FeatureSettingRequest = { key: 'backgroundImage', value: 'default' }; this.perform('setSetting', request);
      } }
    });
  }
  private async pickBackground(): Promise<void> { await this.perform('pickBackground', new Object()); }
  private async vote(item: DisplayItem): Promise<void> { const request: UrgeRequest = { id: item.id }; await this.perform('urge', request); }

  private calendarTitle(): string {
    const area = this.items[this.calendarIndex]?.title || '';
    if (!area.trim()) return '校历';
    return this.items.length > 1 ? '校历 · ' + area : '校历（' + area + '）';
  }

  private calendarImage(): string {
    const item = this.items[this.calendarIndex];
    return item?.image || item?.url || '';
  }

  private startCalendarImage(): void {
    this.calendarAttempt++;
    this.calendarImageState = this.calendarImage() ? CalendarImageState.Loading : CalendarImageState.Failed;
  }

  private retryCalendarImage(attempt: number): void {
    if (!this.active || this.route !== 'schoolCalendar' || attempt !== this.calendarAttempt) return;
    if (this.calendarImage()) this.startCalendarImage();
    else this.load(true);
  }

  private calendarResult(attempt: number, state: CalendarImageState): void {
    if (this.active && this.route === 'schoolCalendar' && attempt === this.calendarAttempt &&
      this.calendarImageState === CalendarImageState.Loading) this.calendarImageState = state;
  }

  private chooseCalendar(): void {
    this.dialog?.close();
    this.dialog = new CustomDialogController({
      builder: FeatureChoiceDialog({ title: '选择查看校历的校区',
        options: this.items.map((item: DisplayItem, index: number): SelectOption => ({ value: index.toString(), label: item.title })),
        selected: [this.calendarIndex.toString()], onConfirm: (values: string[]) => {
          const index = Number(values[0]);
          if (index !== this.calendarIndex && this.items[index] !== undefined) {
            this.calendarIndex = index;
            this.startCalendarImage();
          }
        } }),
      customStyle: true, autoCancel: true
    });
    this.dialog.open();
  }
  private async shareCalendar(): Promise<void> {
    if (!this.active || this.route !== 'schoolCalendar' || this.actionBusy || this.calendarImageState !== CalendarImageState.Ready) return;
    this.actionBusy = true;
    const current = this.epoch;
    const attempt = this.calendarAttempt;
    const request: FeatureUrlRequest = { url: this.items[this.calendarIndex].url || this.calendarImage() };
    try { await this.client!.request('shareSchoolCalendar', request); }
    catch (error) { if (this.active && current === this.epoch && attempt === this.calendarAttempt) this.error = featureError(error as Error); }
    finally { if (this.active && current === this.epoch) this.actionBusy = false; }
  }

  private headerIcon(): string {
    if (this.searchPage()) return 'filter';
    if (this.route === 'scores') return 'notifications-active';
    if (this.route === 'exams') return 'info';
    if (this.route === 'backgrounds') return 'restore';
    if (this.route === 'schoolCalendar') return 'ic_action_send';
    return '';
  }
  private headerAction(): string {
    if (this.searchPage()) return this.route === 'rooms' ? '筛选条件' : '搜索与筛选';
    if (this.route === 'scores') return '成绩变更提醒';
    if (this.route === 'exams') return '补考查询提示';
    if (this.route === 'backgrounds') return '恢复默认背景';
    if (this.route === 'schoolCalendar') return '分享校历';
    return '';
  }
  private headerClick(): void {
    if (this.searchPage()) this.filterSheet = true;
    else if (this.route === 'scores') {
      this.dialog?.close();
      this.dialog = new CustomDialogController({ builder: ScoreReminderDialog({ client: this.client, multipleAccounts: this.users.length > 1 }),
        alignment: DialogAlignment.Center, customStyle: true, autoCancel: true });
      this.dialog.open();
    } else if (this.route === 'backgrounds') this.resetBackground();
    else if (this.route === 'schoolCalendar') this.shareCalendar();
    else if (this.route === 'exams') this.getUIContext().showAlertDialog({
      width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
      title: '补考查询提示', message: '教务系统的补考安排通常归属在上一学期中。如需查询补考信息，请在顶部筛选栏将学年与学期切换至上一个学期即可。',
      confirm: { value: '知道了', action: () => {} }
    });
  }

  @Builder
  private filters() {
    Row({ space: 6 }) {
      Scroll() {
        Row({ space: this.searchPage() ? 8 : 6 }) {
          if (!this.searchPage()) {
            FeatureFilterChip({ label: this.userLabel(), onSelect: () => this.chooseUser() })
            if (this.academic()) {
              FeatureFilterChip({ label: this.optionLabel(this.years, this.year.toString(), '学年'), onSelect: () =>
                this.choose('选择学年', this.years, [this.year.toString()], false, (values: string[]) => { this.year = Number(values[0]); }) })
              FeatureFilterChip({ label: this.optionLabel(this.terms, this.term.toString(), '学期'), onSelect: () =>
                this.choose('选择学期', this.terms, [this.term.toString()], false,
                  (values: string[]) => { this.term = Number(values[0]); }) })
            }
          } else if (this.route === 'rooms') {
            FeatureSummaryChip({ label: this.optionLabel(this.buildings, this.area, '全部区域'), selected: this.area.length > 0,
              onSelect: () => { this.filterSheet = true; } })
            FeatureSummaryChip({ label: this.weekLabel(), selected: this.weeks.length > 0, onSelect: () => this.chooseWeeks() })
            FeatureSummaryChip({ label: this.dayLabel(), selected: this.days.length > 0, onSelect: () => this.chooseDays() })
            FeatureSummaryChip({ label: this.periodLabel(), selected: this.periods.length > 0, onSelect: () => this.choosePeriods() })
          } else {
            FeatureSummaryChip({ label: this.optionLabel(this.campuses, this.campus, '校区'), selected: this.campus.length > 0,
              onSelect: () => { this.filterSheet = true; } })
            FeatureSummaryChip({ label: this.optionLabel(this.colleges, this.college, '学院'), selected: this.college.length > 0,
              onSelect: () => { this.filterSheet = true; } })
            FeatureSummaryChip({ label: this.optionLabel(this.majors, this.major, '专业'), selected: this.major.length > 0,
              onSelect: () => { this.filterSheet = true; } })
            if (this.courseName.trim().length > 0) {
              FeatureSummaryChip({ label: '课程：' + this.courseName, selected: true, onSelect: () => { this.filterSheet = true; } })
            }
            if (this.teacherName.trim().length > 0) {
              FeatureSummaryChip({ label: '教师：' + this.teacherName, selected: true, onSelect: () => { this.filterSheet = true; } })
            }
          }
        }
      }.scrollable(ScrollDirection.Horizontal).scrollBar(BarState.Off).layoutWeight(1)
        .align(Alignment.Start)
      if (!this.searchPage()) {
        Button({ type: ButtonType.Circle }) {
          if (this.loading) { LoadingProgress().width(20).height(20).color($r('app.color.text_secondary')) }
          else { FeatureIcon({ name: 'refresh', tint: $r('app.color.text_secondary') }).scale({ x: 0.84, y: 0.84 }) }
        }.width(36).height(36).backgroundColor(Color.Transparent).enabled(!this.loading && !this.actionBusy)
          .id('query-refresh').accessibilityText('刷新').onClick(() => this.load(true))
      }
    }.width('100%').padding({ left: this.searchPage() ? 16 : 12, right: this.searchPage() ? 16 : 12, top: 8, bottom: 8 })
      // 页面底色与 surface 相同，叠加主色还原 Android 1dp tonalElevation。
      .backgroundColor(withOpacity($r('app.color.primary'), 0.051))
      .enabled(this.optionsReady && !this.actionBusy)
  }

  @Builder
  private selector(label: string, value: () => string, action: () => void, enabled: () => boolean = () => true,
    icon: string = '', highlighted: () => boolean = () => true) {
    Row({ space: 12 }) {
      if (icon.length > 0) {
        Shape() {
          Path().commands(icon).fill(highlighted() ? $r('app.color.primary') : $r('app.color.text_secondary'))
        }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(20).height(20).accessibilityLevel('no')
      }
      Text(label).fontSize(14).lineHeight(20).fontColor($r('app.color.text_primary'))
      Text(value()).fontSize(14).fontWeight(highlighted() ? FontWeight.Medium : FontWeight.Normal)
        .fontColor(highlighted() ? $r('app.color.primary') : $r('app.color.text_secondary')).textAlign(TextAlign.End)
        .layoutWeight(1).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
      Text('›').fontSize(20).lineHeight(20).textAlign(TextAlign.Center).width(18).fontColor($r('app.color.text_secondary'))
    }.padding(14).width('100%').enabled(enabled()).onClick(action)
  }

  @Builder
  private preset(label: string, selected: () => boolean, action: () => void) {
    Button(label, { type: ButtonType.Normal }).fontSize(14).height(34).borderRadius(10).margin({ right: 8, bottom: 4 })
      .fontColor(selected() ? $r('app.color.on_secondary_container') : $r('app.color.text_secondary'))
      .backgroundColor(selected() ? $r('app.color.secondary_container') : Color.Transparent)
      .border({ width: 0.5, color: $r('app.color.outline') }).onClick(action)
  }

  @Builder
  private searchSheet() {
    SheetSurface() {
      this.searchForm()
    }
  }

  @Builder
  private searchForm() {
    Scroll() {
      Column({ space: 12 }) {
        Row() {
          Text(this.route === 'rooms' ? '空闲教室筛选' : '全校课表搜索').fontSize(16).fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary')).layoutWeight(1)
          if (this.route === 'rooms') {
            Button('设为当前时段').fontSize(14).fontColor($r('app.color.primary')).backgroundColor(Color.Transparent).onClick(() => this.currentTime())
          }
        }.width('100%')
        if (this.route === 'rooms') {
          Text('教学区域（可选）').fontSize(14).fontColor($r('app.color.text_secondary')).width('100%')
          Flex({ wrap: FlexWrap.Wrap }) {
            ForEach(this.buildings, (option: SelectOption) => {
              this.preset(option.label, () => this.area === option.value, () => { this.area = option.value; })
            }, (option: SelectOption) => option.value)
          }.width('100%')
          Text('常用节次快选').fontSize(14).fontColor($r('app.color.text_secondary')).width('100%')
          Flex({ wrap: FlexWrap.Wrap }) {
            ForEach(this.quickPeriods, (start: number) => {
              this.preset(start + '-' + (start + 1) + '节', () => this.periods.length === 2 && this.periods[0] === start && this.periods[1] === start + 1,
                () => { this.periods = [start, start + 1]; })
            }, (start: number) => start.toString())
          }.width('100%')
          Column() {
            this.selector('查询周次 *', () => this.weekLabel(), () => this.chooseWeeks())
            Divider().color($r('app.color.outline')).opacity(0.4)
            this.selector('查询星期 *', () => this.dayLabel(), () => this.chooseDays())
            Divider().color($r('app.color.outline')).opacity(0.4)
            this.selector('查询节次 *', () => this.periodLabel(), () => this.choosePeriods())
          }.width('100%').backgroundColor($r('app.color.card')).borderRadius(18).border({ width: 0.5, color: $r('app.color.outline') })
        } else {
          Column() {
            this.selector('查询校区', () => this.optionLabel(this.campuses, this.campus, '全部校区'), () =>
              this.choose('选择校区', this.allOptions(this.campuses, '全部校区'), [this.campus], false, (values: string[]) => { this.campus = values[0]; }),
              () => true, CAMPUS_ICON, () => this.campus.length > 0)
            Divider().margin({ left: 52 }).color($r('app.color.outline')).opacity(0.4)
            this.selector('查询学院', () => this.optionLabel(this.colleges, this.college, '全部学院'), () =>
              this.choose('选择学院', this.allOptions(this.colleges, '全部学院'), [this.college], false, (values: string[]) => { this.changeCollege(values[0]); }),
              () => true, COLLEGE_ICON, () => this.college.length > 0)
            Divider().margin({ left: 52 }).color($r('app.color.outline')).opacity(0.4)
            this.selector('查询专业', () => this.optionLabel(this.majors, this.major, this.majors.length === 0 ? '请先选学院' : '全部专业'), () =>
              this.choose('选择专业', this.allOptions(this.majors, '全部专业'), [this.major], false, (values: string[]) => { this.major = values[0]; }),
              () => this.majors.length > 0, MAJOR_ICON, () => this.major.length > 0)
          }.width('100%').backgroundColor($r('app.color.card')).borderRadius(18).border({ width: 0.5, color: $r('app.color.outline') })
          TextInput({ text: this.courseName, placeholder: '课程名称（选填）' }).height(56).margin({ top: 8 }).id('timetable-course-name')
            .fontSize(16).placeholderColor($r('app.color.text_secondary')).fontColor($r('app.color.text_primary'))
            .backgroundColor(Color.Transparent).borderRadius(14).border({ width: 1, color: withOpacity($r('app.color.text_secondary'), 0.72) })
            .onChange((value: string) => { this.courseName = value; })
          TextInput({ text: this.teacherName, placeholder: '教师姓名（选填）' }).height(56).margin({ top: 8 }).id('timetable-teacher-name')
            .fontSize(16).placeholderColor($r('app.color.text_secondary')).fontColor($r('app.color.text_primary'))
            .backgroundColor(Color.Transparent).borderRadius(14).border({ width: 1, color: withOpacity($r('app.color.text_secondary'), 0.72) })
            .onChange((value: string) => { this.teacherName = value; })
        }
        if (this.error.length > 0) { Text(this.error).fontSize(12).fontColor($r('app.color.danger')).width('100%') }
        Button({ type: ButtonType.Normal }) {
          Row({ space: 6 }) {
            Image($rawfile('shared/ic_action_search.svg')).width(18).height(18).fillColor($r('app.color.on_primary')).accessibilityLevel('no')
            Text(this.loading ? '查询中…' : (this.route === 'rooms' ? '开始查询' : '查询课程'))
              .fontSize(14).fontWeight(600).fontColor($r('app.color.on_primary'))
          }
        }.width('100%').height(48).borderRadius(14).backgroundColor($r('app.color.primary'))
          .enabled(this.optionsReady && !this.actionBusy && (this.route !== 'rooms' || (this.weeks.length > 0 && this.days.length > 0 && this.periods.length > 0)))
          .onClick(() => { this.epoch++; this.loading = false; this.filterSheet = false; this.load(true); })
      }.padding({ left: 20, right: 20, bottom: 32 }).width('100%')
    }.width('100%').flexShrink(1).align(Alignment.Top).scrollBar(BarState.Auto)
  }

  @Builder
  private experimentHeader(item: DisplayItem) {
    Row({ space: 8 }) {
      Text(item.title).fontSize(14).fontWeight(600).fontColor($r('app.color.text_primary')).layoutWeight(1)
      Text('总分 ' + item.value).fontSize(12).fontWeight(FontWeight.Bold).fontColor($r('app.color.on_primary_container'))
        .padding({ left: 10, right: 10, top: 4, bottom: 4 }).backgroundColor($r('app.color.primary_container')).borderRadius(10)
    }.width('100%').padding({ left: 18, right: 18, top: 10, bottom: 10 }).backgroundColor($r('app.color.container'))
  }

  @Builder
  private otherCard(item: DisplayItem) {
    Column({ space: 10 }) {
      Row({ space: 12 }) {
        Column({ space: 5 }) {
          Text(item.title).fontSize(16).fontWeight(600).fontColor($r('app.color.text_primary')).width('100%')
          if (item.subtitle.length > 0) { Text(item.subtitle).fontSize(13).fontColor($r('app.color.text_secondary')).width('100%') }
        }.layoutWeight(1)
        if (item.value.length > 0) { Text(item.value).fontSize(18).fontWeight(FontWeight.Bold).fontColor($r('app.color.primary')) }
      }.width('100%').onClick(() => this.openItem(item))
      if (item.detail.length > 0) { Text(item.detail).fontSize(14).fontColor($r('app.color.text_secondary')).width('100%').copyOption(CopyOptions.LocalDevice) }
      if (item.badge.length > 0 || item.status.length > 0) { Text([item.status, item.badge].filter((s: string) => s.length > 0).join(' · ')).fontSize(12).fontColor($r('app.color.primary')).width('100%') }
      if (item.url.length > 0) { Button('打开链接').fontColor($r('app.color.primary')).backgroundColor(Color.Transparent).onClick(() => this.openUrl(item.url)) }
      if (this.route === 'urge') {
        Button('支持这个功能').fontSize(14).fontColor($r('app.color.on_secondary_container')).backgroundColor($r('app.color.secondary_container'))
          .enabled(!this.actionBusy).onClick(() => this.vote(item))
      }
    }.width('100%').padding(16).backgroundColor($r('app.color.card')).borderRadius(18)
      .border({ width: 0.5, color: $r('app.color.outline') }).margin({ top: 6, bottom: 6 })
  }

  private emptyTitle(): string {
    if (this.route === 'rooms') return this.searched ? '该时段暂无空闲教室' : '设置条件查询空闲教室';
    if (this.route === 'timetable') return this.searched ? '未找到匹配课程' : '按条件搜索全校课程';
    if (this.route === 'scores') return '暂无成绩数据';
    if (this.route === 'exams') return '当前学期暂无考试安排';
    if (this.route === 'expScores') return '暂无实验成绩数据';
    if (this.route === 'customCourses') return '暂无自定义课程';
    if (this.route === 'customThings') return '暂无自定义事项';
    if (this.route === 'notices') return '暂无通知公告';
    return '暂时没有内容';
  }
  private emptyAction(): string {
    switch (this.route) {
      case 'rooms': return this.searched ? '修改查询条件' : '开始查询';
      case 'timetable': return this.searched ? '修改搜索条件' : '开始搜索';
      case 'notices': return '重新加载';
      case 'exams': return '重新查询';
      default: return '再查一次';
    }
  }

  private emptyDescription(): string {
    if (this.route === 'rooms') return this.searched ? '可以尝试调整教学楼区域、周次或节次条件' : '已为你预选当前周次、今天星期与当前时段节次，可直接查询或调整条件';
    if (this.route === 'timetable' && !this.searched) return '支持按校区、学院、专业、课程名或教师名查找，并可一键蹭课加入课表';
    if (this.route === 'exams') return '如需查询补考信息，可点击顶部学年/学期切换至上一学期';
    if (this.route === 'customCourses') return '点击右下角 + 按钮，手动添加旁听、选修或其他课程';
    if (this.route === 'customThings') return '点击右下角 + 按钮，添加作业提醒、日程或倒计时';
    return '';
  }

  @Builder
  private listContent() {
    Refresh({ refreshing: $$this.refreshing }) {
      List({ scroller: this.listScroller }) {
        if (this.route === 'scores' && this.metrics.length > 0) {
          ListItem() { ScoreSummary({ metrics: this.metrics }) }.padding({ left: 16, right: 16 })
        } else if (this.summary.length > 0) {
          ListItem() { Text(this.summary).fontSize(14).fontColor($r('app.color.text_secondary')).width('100%').padding(16) }
        }
        if (this.error.length > 0) {
          ListItem() { FeatureError({ message: this.error, onRetry: () => {
            if (!this.optionsReady) this.initialize();
            else this.load((this.failedQuery?.page ?? 1) === 1, this.failedQuery);
          } }) }.padding({ left: 16, right: 16 })
        }
        if (this.loading && this.items.length === 0) {
          ListItem() { Row() { LoadingProgress().width(40).height(40).color($r('app.color.primary')) }.width('100%').height(160).justifyContent(FlexAlign.Center) }
        } else if (this.items.length === 0 && this.error.length === 0) {
          ListItem() {
            Column() {
              FeatureEmpty({ route: this.route, title: this.emptyTitle(), description: this.emptyDescription() })
              Button(this.emptyAction(), { type: ButtonType.Normal })
                .height(40).borderRadius(14).fontSize(14).fontWeight(600).padding({ left: 24, right: 24 })
                .fontColor($r('app.color.on_secondary_container')).backgroundColor($r('app.color.secondary_container')).onClick(() => {
                  if (this.route === 'timetable' || (this.route === 'rooms' && this.searched)) this.filterSheet = true;
                  else this.load(true);
                })
            }.width('100%')
          }
        }
        if (this.route === 'scores' && this.items.length > 0) {
          ListItem() {
            ScoreListHeader({ expanded: this.expandScores, onToggle: () => { this.expandScores = !this.expandScores; } })
          }
        }
        if (this.route === 'expScores') {
          ForEach(this.items, (item: DisplayItem) => {
            ListItemGroup({ header: this.experimentHeader(this.currentItem(item.id)) }) {
              ForEach(this.currentItem(item.id).children || [], (child: DisplayItem) => {
                ListItem() { ExperimentCard({ item: this.currentItem(item.id).children!.find((entry: DisplayItem) => entry.id === child.id)! }) }.padding({ left: 16, right: 16 })
              }, (child: DisplayItem) => child.id)
            }
          }, (item: DisplayItem) => item.id)
        } else {
          ForEach(this.items, (item: DisplayItem) => {
            ListItem() {
              if (this.route === 'scores') { ScoreCard({ item: this.currentItem(item.id), expandAll: this.expandScores }) }
              else if (this.route === 'exams') { ExamCard({ item: this.currentItem(item.id), onCopy: (text: string) => this.copy(text) }) }
              else if (this.customPage()) { CustomEntryCard({ item: this.currentItem(item.id), course: this.route === 'customCourses', onEdit: () => this.edit(this.currentItem(item.id)) }) }
              else if (this.route === 'timetable') { SchoolCourseCard({ item: this.currentItem(item.id), busy: this.actionBusy || this.loading, onImport: () => this.importCourse(this.currentItem(item.id)) }) }
              else if (this.route === 'rooms') { ClassroomCard({ item: this.currentItem(item.id) }) }
              else if (this.route === 'notices') { NoticeCard({ item: this.currentItem(item.id), onAction: (action: ItemAction) => this.itemAction(action) }) }
              else { this.otherCard(this.currentItem(item.id)) }
            }.padding({ left: 16, right: 16 })
          }, (item: DisplayItem) => item.id)
        }
        if (this.items.length > 0 && this.route !== 'expScores' && !this.error && (this.loading || !this.hasMore)) {
          ListItem() {
            Text(this.loading ? '正在加载更多…' : '已经到底啦')
              .fontSize(12).fontColor($r('app.color.text_secondary')).opacity(0.7).padding(20).width('100%').textAlign(TextAlign.Center)
          }
        }
      }.width('100%').height('100%').sticky(StickyStyle.Header).contentEndOffset(this.customPage() ? 88 : 24)
        .onDidScroll((offset: number, state: ScrollState) => {
          if (this.customPage() && state !== ScrollState.Idle && offset !== 0) {
            this.customFabVisible = offset < 0 || this.listScroller.currentOffset().yOffset <= 0;
          }
        })
        .onReachStart(() => { if (this.customPage()) this.customFabVisible = true; })
        .scrollBar(BarState.Auto).onReachEnd(() => { if (this.hasMore && !this.loading && this.error.length === 0) this.load(false); })
    }.width('100%').height('100%').onRefreshing(() => { if (!this.optionsReady) this.initialize(); else this.load(true); })
  }

  build() {
    Column() {
      FeatureHeader({ title: this.route === 'schoolCalendar' ? this.calendarTitle() : featureTitle(this.route),
        action: this.headerAction(), actionIcon: this.headerIcon(), busy: this.actionBusy && !this.searchPage(),
        actionEnabled: !this.actionBusy && (this.route !== 'schoolCalendar' || this.calendarImageState === CalendarImageState.Ready),
        selectableTitle: this.route === 'schoolCalendar' && this.items.length > 1,
        onTitle: () => this.chooseCalendar(), onBack: () => this.onBack(), onAction: () => this.headerClick() })
      if (this.needsOptions()) { this.filters() }
      Stack({ alignContent: Alignment.BottomEnd }) {
        if (this.route === 'backgrounds' || this.route === 'schoolCalendar') {
          Column() {
            if (this.error.length > 0) { FeatureError({ message: this.error, onRetry: () => this.load(true) }) }
            if (this.loading && this.items.length === 0) {
              Column() { LoadingProgress().width(40).height(40).color($r('app.color.primary')) }.layoutWeight(1).justifyContent(FlexAlign.Center)
            } else if (this.items.length === 0 && this.error.length === 0) {
              FeatureEmpty({ route: this.route, title: this.route === 'schoolCalendar' ? '暂无校历数据' : '暂无背景图片', description: '' })
              Button('重新加载').fontColor($r('app.color.primary')).backgroundColor(Color.Transparent).onClick(() => this.load(true))
            } else if (this.items.length > 0) {
              if (this.route === 'backgrounds') {
                BackgroundGallery({ items: this.items, busy: this.actionBusy, onSelect: (item: DisplayItem) => this.selectBackground(item) }).layoutWeight(1)
              } else {
                // 换校区或重试时重建图片节点；旧节点的回调始终携带原始请求序号。
                ForEach([this.calendarAttempt], (attempt: number) => {
                  SchoolCalendarCanvas({ image: this.calendarImage(), imageState: this.calendarImageState,
                    onResult: (state: CalendarImageState) => this.calendarResult(attempt, state),
                    onRetry: () => this.retryCalendarImage(attempt) }).layoutWeight(1)
                }, (attempt: number) => attempt.toString())
              }
            }
          }.width('100%').height('100%')
        } else { this.listContent() }
        if (this.customPage() && this.customFabVisible) {
          Button({ type: ButtonType.Normal }) { FeatureIcon({ name: 'ic_add', tint: $r('app.color.on_primary_container') }) }
            .width(56).height(56).borderRadius(16).backgroundColor($r('app.color.primary_container')).margin(20)
            .id('custom-entry-add').accessibilityText(this.route === 'customCourses' ? '添加自定义课程' : '添加自定义事项')
            .enabled(this.optionsReady && !this.actionBusy).onClick(() => this.edit())
            .transition(TransitionEffect.OPACITY.combine(TransitionEffect.scale({ x: 0, y: 0 })).animation({ duration: 180 }))
        } else if (this.route === 'backgrounds') {
          Button({ type: ButtonType.Normal }) {
            Row({ space: 12 }) {
              Shape() {
                Path().commands('M1 6a1 1 0 0 0-1 1v13c0 1.1.9 2 2 2h17a1 1 0 0 0 0-2H2V7a1 1 0 0 0-1-1zM22 4h-8l-2-2H6c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zM7 15l3.5-4.5 2.5 3.01L16.5 9l4.5 6H7z')
                  .fill($r('app.color.on_primary_container'))
              }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(24).height(24).accessibilityLevel('no')
              Text('相册选取').fontSize(14).fontWeight(FontWeight.Medium).fontColor($r('app.color.on_primary_container'))
            }
          }.height(56).padding({ left: 16, right: 20 }).backgroundColor($r('app.color.primary_container')).borderRadius(16)
            .shadow({ radius: 6, color: '#26000000', offsetY: 3 }).margin(16).accessibilityText('相册选取')
            .enabled(!this.actionBusy).onClick(() => this.pickBackground())
        }
      }.layoutWeight(1).width('100%')
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
      .bindSheet($$this.filterSheet, this.searchSheet(), bottomSheetOptions())
  }
}
