import { AcademicOptions, BootstrapState, CalendarDay, CalendarWeek, DisplayItem, EditorState, HomeState,
  ListResult, SelectOption, SettingItem, SettingsState, UserSummary, WeekCell, WeekOverview, WeekPage } from '../../../main/ets/model/NativeModels';

// 本文件所有账号、课程、成绩与日程均为离线合成数据，仅用于 UI 验证，不代表真实学生记录。
export function fixtureUsers(): UserSummary[] {
  return [
    { id: '20990001', name: '测试同学甲', title: '测试同学甲 · 计算机科学', college: '测试学院', major: '计算机科学',
      campus: '测试校区', main: true, color: '#FF2E86DE', className: '测试计算机一班', gender: '男', grade: 2026, direction: '测试系统方向' },
    { id: '20990002', name: '测试同学乙', title: '测试同学乙 · 软件工程', college: '测试学院', major: '软件工程',
      campus: '测试校区', main: false, color: '#FF16A085', className: '测试软件二班', gender: '女', grade: 2025, direction: '测试软件方向' }
  ];
}

export function fixtureBootstrap(users: UserSummary[]): BootstrapState {
  return { privacy: true, loggedIn: users.length > 0, version: 'UI 测试', nightMode: 'OFF', loginLabel: '仅用于离线 UI 验证',
    helpUrl: '', privacyUrl: '', avatar: 'builtin:boy1', customAvatar: 'builtin:boy1', users: users,
    multiAccount: false, todayAccountTemplate: '{studentNo}({name})', weekAccountTemplate: '{studentNo}({name})',
    menuGroups: [
      [
        { key: 'QUERY_EXAM', title: '考试安排', icon: 'ic_exam', route: 'exams', link: '', hint: '', action: 'route', unread: false },
        { key: 'QUERY_SCORE', title: '成绩查询', icon: 'ic_score', route: 'scores', link: '', hint: '', action: 'route', unread: false },
        { key: 'EXP_SCORE', title: '实验成绩', icon: 'ic_exp_score', route: 'expScores', link: '', hint: '', action: 'route', unread: false },
        { key: 'QUERY_FREE_ROOM', title: '空闲教室', icon: 'ic_classroom', route: 'rooms', link: '', hint: '', action: 'route', unread: false },
        { key: 'QUERY_TIMETABLE', title: '全校课表', icon: 'ic_school_timetable', route: 'timetable', link: '', hint: '', action: 'route', unread: false }
      ],
      [
        { key: 'ACCOUNT_MANAGE', title: '账号管理', icon: 'ic_account_settings', route: 'accounts', link: '', hint: '', action: 'route', unread: false },
        { key: 'CLASS_SETTING', title: '课程设置', icon: 'ic_class_settings', route: 'classSettings', link: '', hint: '', action: 'route', unread: false },
        { key: 'SETTINGS', title: '软件设置', icon: 'ic_settings', route: 'settings', link: '', hint: '', action: 'route', unread: false }
      ]
    ] };
}

export function fixtureItem(id: string, title: string, color: string = '#2E86DE'): DisplayItem {
  return { id: id, title: title, subtitle: '', detail: '', time: '', location: '', color: color,
    status: '', badge: '', value: '', image: '', url: '' };
}

function fixtureCourse(id: string, title: string, date: string, teacher: string, location: string,
  start: string, end: string, periods: string, color: string): DisplayItem {
  const item = fixtureItem(id, title, color);
  item.subtitle = teacher;
  item.location = location;
  item.time = periods;
  item.startTime = start;
  item.endTime = end;
  item.date = date;
  item.status = '未开始';
  item.fields = [{ key: 'weeks', label: '上课周次', value: '第1-2周' },
    { key: 'periods', label: '上课节次', value: periods },
    { key: 'extra', label: '附加信息', value: '仅用于 UI 验证' }];
  return item;
}

function fixtureWeekCourse(item: DisplayItem): DisplayItem {
  const course = JSON.parse(JSON.stringify(item)) as DisplayItem;
  course.time = item.startTime + ' - ' + item.endTime;
  course.status = '';
  return course;
}

function calendarItem(id: string, title: string, time: string, location: string): DisplayItem {
  const item = fixtureItem(id, title);
  item.detail = time + '，' + location;
  return item;
}

export function fixtureHome(scenario: string = 'today'): HomeState {
  const empty = scenario === 'empty' || scenario === 'calendar-empty';
  const afterTerm = scenario === 'calendar-after-term';
  const mathematics = fixtureCourse('math', '测试高等数学', '2026-11-25', '测试教师甲', '测试楼 A101',
    '08:30', '10:05', '第1-2节', '#2E86DE');
  const physics = fixtureCourse('physics', '测试大学物理', '2026-11-25', '测试教师乙', '测试楼 B202',
    '08:30', '10:05', '第1-2节', '#8E55B8');
  const programming = fixtureCourse('programming', '测试程序设计', '2026-12-01', '测试教师丙', '测试楼 C303',
    '08:30', '10:05', '第1-2节', '#16A085');
  const english = fixtureCourse('english', '测试大学英语', '2026-11-25', '测试教师丁', '测试楼 D404',
    '14:00', '15:35', '第5-6节', '#E59A23');
  english.status = '开课中';
  const practical: DisplayItem[] = [];
  if (scenario === 'week-practical') {
    const electronics = fixtureItem('practical-electronics', '测试电子实习');
    electronics.subtitle = '测试教师丙';
    electronics.time = '第3周';
    const workshop = fixtureItem('practical-workshop', '测试金工实习');
    workshop.subtitle = '测试教师丁';
    workshop.time = '第4周';
    practical.push(electronics, workshop);
  }
  const weeks: WeekPage[] = [];
  const overview: WeekOverview[] = [];
  for (let week = 1; week <= 20; week++) {
    const date = new Date(2026, 10, 23 + (week - 1) * 7, 12);
    const dateText = date.getFullYear().toString() + '-' + (date.getMonth() + 1).toString().padStart(2, '0') +
      '-' + date.getDate().toString().padStart(2, '0');
    const cells: WeekCell[] = [];
    if (!empty && week === 1) {
      cells.push({ title: '测试高等数学', day: 3, start: 1, span: 2, color: '#2E86DE', textColor: '#FFFFFF',
        courses: [fixtureWeekCourse(mathematics), fixtureWeekCourse(physics)] });
      cells.push({ title: '测试大学英语', day: 3, start: 5, span: 2, color: '#E59A23', textColor: '#FFFFFF', courses: [fixtureWeekCourse(english)] });
    }
    if (!empty && week === 2) {
      cells.push({ title: '测试程序设计', day: 2, start: 1, span: 2, color: '#16A085', textColor: '#FFFFFF', courses: [fixtureWeekCourse(programming)] });
    }
    weeks.push({ week: week, date: dateText, cells: cells });
    const dots: boolean[][] = [];
    for (let slot = 0; slot < 5; slot++) {
      const row: boolean[] = [];
      for (let day = 1; day <= 5; day++) row.push(cells.some((cell: WeekCell) => cell.day === day && Math.floor((cell.start - 1) / 2) === slot));
      dots.push(row);
    }
    overview.push({ week: week, current: !afterTerm && week === 1, cells: dots });
  }
  // 两个完整周及跨月标题，足以滚出今天；不把日程展示字符串拆成业务字段。
  const calendar: CalendarWeek[] = [];
  for (let week = 1; week <= 2; week++) {
    const days: CalendarDay[] = [];
    for (let day = 0; day < 7; day++) {
      const date = new Date(2026, 10, 23 + (week - 1) * 7 + day, 12);
      const dateText = date.getFullYear().toString() + '-' + (date.getMonth() + 1).toString().padStart(2, '0') +
        '-' + date.getDate().toString().padStart(2, '0');
      if (date.getDate() === 1) {
        days.push({ date: dateText, title: date.getFullYear() + '年' + (date.getMonth() + 1) + '月',
          today: false, week: week, items: [], kind: 'MONTH_HEADER' });
      }
      const items: DisplayItem[] = [
        calendarItem('calendar-' + dateText + '-am', '测试日程 ' + dateText, '08:30-10:05', '测试楼 A101'),
        calendarItem('calendar-' + dateText + '-pm', '测试自习 ' + dateText, '19:00-20:30', '测试自习室')
      ];
      if (dateText === '2026-11-25') items[0] = calendarItem('calendar-math', '测试高等数学', '08:30-10:05', '测试楼 A101');
      if (dateText === '2026-11-26') items[0] = calendarItem('calendar-sports', '测试体育', '16:00-17:30', '测试操场');
      if (dateText === '2026-12-01') items[0] = calendarItem('calendar-programming', '测试程序设计', '08:30-10:05', '测试楼 C303');
      days.push({ date: dateText, title: ['一', '二', '三', '四', '五', '六', '日'][day] + '\n' + date.getDate(),
        today: !afterTerm && dateText === '2026-11-25', week: week, items: items, kind: 'DAY_ITEM' });
    }
    calendar.push({ week: week, title: week === 1 ? '第1周  11月23日至29日' : '第2周  11月30日至12月6日',
      cursor: !afterTerm, days: days });
  }
  return { title: afterTerm ? '4月12日 星期一' : '11月25日 星期三', week: 1, actualWeek: afterTerm ? 21 : 1,
    calendarEnabled: true, multiAccount: false, showStatus: scenario !== 'today-hide-status',
    loading: false, error: '', showTomorrow: false, poem: '', holiday: '', background: '', backgroundBlur: 0, hideBackgroundInDark: false,
    cellHeight: 70, cellRadius: 8, textSize: 11, cellOpacity: 0.9, todayOpacity: 1,
    today: empty ? [] : [mathematics, english], things: [], weeks: weeks, calendar: empty ? [] : calendar,
    weekOverview: overview, practical: practical };
}

export function fixtureOptions(users: UserSummary[]): AcademicOptions {
  return { year: 2026, term: 1, years: [{ value: '2026', label: '2026–2027学年' }, { value: '2025', label: '2025–2026学年' }],
    users: users, selectedUser: users.find((user: UserSummary) => user.main)?.id ?? users[0]?.id ?? '',
    campus: '', campuses: [{ value: 'test', label: '测试校区' }], buildings: [], colleges: [], majors: [],
    week: 1, day: 3, periods: [1, 2] };
}

export function fixtureList(kind: string, term: number, refreshed: boolean = false): ListResult {
  const items: DisplayItem[] = [];
  if (kind === 'scores') {
    const score = fixtureItem(term === 2 ? 'discrete' : 'math', term === 2 ? '测试离散数学' : '测试高等数学');
    score.value = term === 2 ? '96' : refreshed ? '91' : '88';
    score.fields = [
      { key: 'courseType', label: '课程性质', value: '必修' }, { key: 'scoreType', label: '成绩类型', value: '正常考试' },
      { key: 'credit', label: '学分', value: '4.0' },
      { key: 'gpa', label: '绩点', value: term === 2 ? '4.6' : refreshed ? '4.1' : '3.8' },
      { key: 'creditGpa', label: '学分绩点', value: term === 2 ? '18.4' : refreshed ? '16.4' : '15.2' },
      { key: 'scoreDescription', label: '成绩说明', value: refreshed ? '复核后成绩 91' : '平时成绩 90 · 期末成绩 ' + score.value }
    ];
    items.push(score);
  } else if (!['customCourses', 'customThings', 'exams', 'expScores', 'rooms', 'timetable', 'feedback'].includes(kind)) {
    throw new Error('没有定义离线查询夹具：' + kind);
  }
  return { title: kind, summary: kind === 'scores' ? '仅用于 UI 验证 · 共 1 门课程' : '', items: items, page: 1, hasMore: false };
}

export function fixtureSettings(kind: string, enabled: boolean, nightMode: string): SettingsState {
  if (kind === 'classSettings') return { items: [
    { key: 'showStatus', title: '显示今日课程状态', summary: '标注课程的未开始、开课中、已结束状态', value: enabled.toString(),
      type: 'toggle', options: [], group: '显示设置', icon: 'ic_action_show_status' }
  ] };
  return { items: [
    { key: 'nightMode', title: '深色与主题模式', summary: '当前：' + (nightMode === 'ON' ? '始终开启' : nightMode === 'AUTO' ? '自动' : '始终关闭'),
      value: nightMode, type: 'select', options: [{ value: 'AUTO', label: '自动' }, { value: 'ON', label: '始终开启' },
        { value: 'OFF', label: '始终关闭' }], group: '外观与界面', icon: 'ic_action_night_mode' },
    { key: 'customUi', title: '自定义课表界面', summary: '调整格子高度、圆角、字号、透明度与模板', value: '',
      type: 'navigate', options: [], group: '外观与界面', icon: 'ic_action_custom_ui', route: 'customUi' }
  ] };
}

export function fixtureAppearance(): Record<string, string> {
  const result: Record<string, string> = {};
  result['todayBackgroundAlpha'] = '1';
  result['weekItemHeight'] = '72';
  result['weekBackgroundAlpha'] = '0.8';
  result['weekItemCorner'] = '4';
  result['weekTitleTemplate'] = '{courseName}\n@{location}';
  result['weekNotTitleTemplate'] = '[非本周]\n{courseName}\n@{location}';
  result['weekTitleTextSize'] = '10';
  result['backgroundImageBlur'] = '0';
  return result;
}

export function fixtureAppearanceSettings(values: Record<string, string>): SettingsState {
  return { items: Object.keys(values).map((key: string): SettingItem => ({ key: key, title: key, summary: '仅用于 UI 验证',
    value: values[key], type: key.includes('Template') ? 'text' : 'number', options: [], group: '外观与界面' })) };
}

export function fixtureEditor(kind: string, creating: boolean = false): EditorState {
  if (kind === 'customCourses') {
    const weeks: SelectOption[] = [];
    const periods: SelectOption[] = [];
    for (let value = 1; value <= 20; value++) weeks.push({ value: value.toString(), label: '第' + value + '周' });
    for (let value = 1; value <= 11; value++) periods.push({ value: value.toString(), label: '第' + value + '节' });
    return { title: creating ? '新建自定义课程' : '编辑自定义课程', fields: [
      { key: 'courseName', title: '课程名称', value: creating ? '' : '测试旁听课程', type: 'text', options: [] },
      { key: 'teacher', title: '任课教师', value: creating ? '' : '测试教师甲', type: 'text', options: [] },
      { key: 'location', title: '上课地点', value: creating ? '' : '测试楼 E505', type: 'text', options: [] },
      { key: 'weeks', title: '上课周次', value: creating ? '' : '1', type: 'weeks', options: weeks },
      { key: 'day', title: '星期', value: '1', type: 'select', options: ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
        .map((label: string, index: number): SelectOption => ({ value: (index + 1).toString(), label: label })) },
      { key: 'startPeriod', title: '开始节次', value: '1', type: 'select', options: periods },
      { key: 'endPeriod', title: '结束节次', value: '2', type: 'select', options: periods }
    ] };
  }
  if (kind !== 'customThings') throw new Error('没有定义离线编辑夹具：' + kind);
  return { title: creating ? '新建自定义事项' : '编辑自定义事项', fields: [
    { key: 'title', title: '事项标题', value: creating ? '' : '测试图书归还', type: 'text', options: [] },
    { key: 'location', title: '地点', value: creating ? '' : '测试图书馆', type: 'text', options: [] },
    { key: 'allDay', title: '全天事项', value: 'false', type: 'toggle', options: [] },
    { key: 'countdown', title: '保存为倒计时', value: 'false', type: 'toggle', options: [] },
    { key: 'startDate', title: '开始日期', value: '2026-11-26', type: 'date', options: [] },
    { key: 'startTime', title: '开始时间', value: '18:00', type: 'time', options: [] },
    { key: 'endDate', title: '结束日期', value: '2026-11-26', type: 'date', options: [] },
    { key: 'endTime', title: '结束时间', value: '19:00', type: 'time', options: [] },
    { key: 'color', title: '颜色', value: '#E59A23', type: 'text', options: [] },
    { key: 'remark', title: '备注', value: '仅用于 UI 验证', type: 'multiline', options: [] }
  ] };
}

export function fixtureEntryItem(kind: string, id: string, values: Record<string, string>): DisplayItem {
  const course = kind === 'customCourses';
  const item = fixtureItem(id, values[course ? 'courseName' : 'title'], course ? '#2E86DE' : values['color']);
  item.location = values['location'];
  item.fields = [{ key: 'createTime', label: '添加时间', value: '2026-11-24 12:00' }];
  if (course) {
    item.fields.push({ key: 'day', label: '星期', value: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'][Number(values['day']) - 1] },
      { key: 'periods', label: '节次', value: values['startPeriod'] + '-' + values['endPeriod'] + '节' },
      { key: 'weeks', label: '周次', value: values['weeks'].split(',').join('、') + '周' },
      { key: 'teacher', label: '教师', value: values['teacher'] });
  } else {
    item.time = values['startDate'] + (values['allDay'] === 'true' ? ' 全天' : ' ' + values['startTime'] + ' - ' + values['endTime']);
    item.badge = values['countdown'] === 'true' ? '倒计时' : values['allDay'] === 'true' ? '全天' : '日程';
    item.fields.push({ key: 'startDate', label: '开始日期', value: values['startDate'] },
      { key: 'allDay', label: '全天事项', value: values['allDay'] }, { key: 'countdown', label: '倒计时', value: values['countdown'] },
      { key: 'remark', label: '备注', value: values['remark'] });
  }
  return item;
}
