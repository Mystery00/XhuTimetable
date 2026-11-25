export interface UserSummary {
  id: string;
  name: string;
  title: string;
  college: string;
  major: string;
  campus: string;
  main: boolean;
  color: string;
  className?: string;
  gender?: string;
  grade?: number;
  direction?: string;
}

export interface ProfileMenu {
  key: string;
  title: string;
  icon: string;
  route: string;
  link: string;
  hint: string;
  action: string;
  unread: boolean;
}

export interface TermAlert {
  oldDate: string;
  newDate: string;
  customDate: string;
  message: string;
}

export interface BootstrapState {
  privacy: boolean;
  loggedIn: boolean;
  version: string;
  nightMode: string;
  loginLabel: string;
  helpUrl: string;
  privacyUrl: string;
  avatar: string;
  customAvatar?: string;
  users: UserSummary[];
  termAlert?: TermAlert;
  menuGroups?: ProfileMenu[][];
  multiAccount?: boolean;
  todayAccountTemplate?: string;
  weekAccountTemplate?: string;
}

export interface DetailField {
  key: string;
  label: string;
  value: string;
}

export interface ItemAction {
  type: string;
  label: string;
  value: string;
}

export interface DisplayItem {
  id: string;
  title: string;
  subtitle: string;
  detail: string;
  time: string;
  location: string;
  color: string;
  status: string;
  badge: string;
  value: string;
  image: string;
  url: string;
  actions?: ItemAction[];
  startTime?: string;
  endTime?: string;
  date?: string;
  fields?: DetailField[];
  children?: DisplayItem[];
  selected?: boolean;
}

export interface WeekCell {
  title: string;
  day: number;
  start: number;
  span: number;
  color: string;
  textColor: string;
  courses: DisplayItem[];
}

export interface WeekPage {
  week: number;
  date: string;
  cells: WeekCell[];
}

export interface CalendarDay {
  date: string;
  title: string;
  today: boolean;
  week: number;
  items: DisplayItem[];
  kind?: string;
}

export interface CalendarWeek {
  week: number;
  title: string;
  cursor: boolean;
  days: CalendarDay[];
}

export interface PoemDetails {
  title: string;
  author: string;
  dynasty: string;
  lines: string[];
  translation: string[];
}

export interface WeekOverview {
  week: number;
  current: boolean;
  cells: boolean[][];
}

export interface HomeState {
  title: string;
  week: number;
  actualWeek: number;
  calendarEnabled: boolean;
  multiAccount: boolean;
  loading: boolean;
  error: string;
  showTomorrow: boolean;
  showStatus?: boolean;
  poem: string;
  poemDetails?: PoemDetails;
  holiday: string;
  holidayColor?: string;
  background: string;
  backgroundBlur: number;
  hideBackgroundInDark: boolean;
  cellHeight: number;
  cellRadius: number;
  textSize: number;
  cellOpacity: number;
  todayOpacity: number;
  today: DisplayItem[];
  things: DisplayItem[];
  weeks: WeekPage[];
  calendar: CalendarWeek[];
  weekOverview?: WeekOverview[];
  practical: DisplayItem[];
}

export interface SelectOption {
  value: string;
  label: string;
}

export interface AcademicOptions {
  year: number;
  term: number;
  years: SelectOption[];
  users: UserSummary[];
  selectedUser: string;
  campus: string;
  campuses: SelectOption[];
  buildings: SelectOption[];
  colleges: SelectOption[];
  majors: SelectOption[];
  week: number;
  day: number;
  periods: number[];
}

export interface ListResult {
  title: string;
  summary: string;
  items: DisplayItem[];
  page: number;
  hasMore: boolean;
  metrics?: DetailField[];
}

export interface SettingItem {
  key: string;
  title: string;
  summary: string;
  value: string;
  type: string;
  options: SelectOption[];
  group: string;
  icon?: string;
  route?: string;
  enabled?: boolean;
}

export interface SettingsState {
  items: SettingItem[];
}

export interface LoginResult {
  success: boolean;
  error: string;
  showHelp: boolean;
}

export interface EditorField {
  key: string;
  title: string;
  value: string;
  type: string;
  options: SelectOption[];
}

export interface EditorState {
  title: string;
  fields: EditorField[];
}
