import { common } from '@kit.AbilityKit';
import { preferences } from '@kit.ArkData';
import { formBindingData, formProvider } from '@kit.FormKit';
import { hilog } from '@kit.PerformanceAnalysisKit';

const LOG_DOMAIN = 0x0000;
const LOG_TAG = 'XhuWidgetStore';
const STORE_NAME = 'xhu_widget_store';
const KEY_SNAPSHOT = 'snapshot';
const KEY_FORMS = 'forms';

export const FORM_NEXT_COURSE = 'nextCourse';
export const FORM_TODAY_COURSE = 'todayCourse';
export const FORM_WEEK_COURSE = 'weekCourse';

export interface WidgetSnapshotItem {
  title: string;
  location: string;
  startMillis: number;
  endMillis: number;
  startPeriod: number;
  endPeriod: number;
  colorArgb: number;
  timeText: string;
  kind: string;
}

export interface WidgetSnapshotDay {
  date: string;
  week: number;
  items: WidgetSnapshotItem[];
}

export interface WidgetSnapshot {
  version: number;
  generatedAtMillis: number;
  validUntilMillis: number;
  state: string;
  days: WidgetSnapshotDay[];
}

interface RegisteredForm {
  id: string;
  name: string;
  dimension: number;
}

export interface CardCourseRow {
  id: string;
  title: string;
  subtitle: string;
  timeText: string;
  accentColor: string;
  ongoing: boolean;
  isThing: boolean;
}

export interface CardWeekDayHeader {
  weekLabel: string;
  dateLabel: string;
  isToday: boolean;
}

export interface CardWeekSlot {
  id: string;
  title: string;
  step: number;
  empty: boolean;
  bgColor: string;
  conflictText: string;
}

export interface CardWeekColumn {
  dayIndex: number;
  slots: CardWeekSlot[];
}

function getFreshPreferences(context: common.Context): preferences.Preferences {
  try {
    preferences.removePreferencesFromCacheSync(context, STORE_NAME);
  } catch {
    // 忽略尚未缓存的情况
  }
  return preferences.getPreferencesSync(context, { name: STORE_NAME });
}

function argbToHex(argb: number): string {
  const rgb = (argb >>> 0) & 0xFFFFFF;
  return '#' + rgb.toString(16).padStart(6, '0').toUpperCase();
}

function formatIsoDate(date: Date): string {
  const y = date.getFullYear().toString();
  const m = (date.getMonth() + 1).toString().padStart(2, '0');
  const d = date.getDate().toString().padStart(2, '0');
  return `${y}-${m}-${d}`;
}

function formatChineseDateLabel(date: Date): string {
  const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  return `${date.getMonth() + 1}月${date.getDate()}日 ${weekdays[date.getDay()]}`;
}

function formatWeekBadge(week: number): string {
  if (week <= 0) return '未开学';
  if (week > 20) return '已结课';
  return `第${week}周`;
}

function startOfWeekMonday(date: Date): Date {
  const result = new Date(date.getFullYear(), date.getMonth(), date.getDate());
  const day = result.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  result.setDate(result.getDate() + diff);
  return result;
}

function parseSnapshot(raw: string): WidgetSnapshot | undefined {
  if (!raw || raw.length === 0) return undefined;
  try {
    const parsed = JSON.parse(raw) as WidgetSnapshot;
    if (parsed && typeof parsed === 'object' && typeof parsed.state === 'string') {
      return parsed;
    }
  } catch {
    // 忽略损坏快照
  }
  return undefined;
}

function readRegisteredForms(pref: preferences.Preferences): RegisteredForm[] {
  const raw = pref.getSync(KEY_FORMS, '[]') as string;
  try {
    const parsed = JSON.parse(raw) as RegisteredForm[];
    if (Array.isArray(parsed)) {
      return parsed.filter((item: RegisteredForm) => item && typeof item.id === 'string' && item.id.length > 0);
    }
  } catch {
    // 忽略异常数据
  }
  return [];
}

function writeRegisteredForms(pref: preferences.Preferences, forms: RegisteredForm[]): void {
  pref.putSync(KEY_FORMS, JSON.stringify(forms));
  pref.flushSync();
}

function findDay(snapshot: WidgetSnapshot | undefined, dateKey: string): WidgetSnapshotDay | undefined {
  if (!snapshot || !Array.isArray(snapshot.days)) return undefined;
  return snapshot.days.find((day: WidgetSnapshotDay) => day.date === dateKey);
}

function buildNextCourseData(snapshot: WidgetSnapshot | undefined, now: Date): Record<string, Object> {
  const nowMillis = now.getTime();
  const todayKey = formatIsoDate(now);
  const loggedOut = !snapshot || snapshot.state !== 'ready';
  const day = findDay(snapshot, todayKey);
  const weekBadge = loggedOut ? '未登录' : formatWeekBadge(day?.week ?? 0);
  const courses = (day?.items ?? []).filter((item: WidgetSnapshotItem) => item.kind === 'course');
  const next = courses.find((item: WidgetSnapshotItem) => item.endMillis > nowMillis);

  if (loggedOut) {
    return {
      'weekBadge': '未登录',
      'hasNext': false,
      'emptyText': '打开西瓜课表\n同步课程',
      'statusText': '',
      'courseName': '',
      'location': '',
      'timeText': '',
      'accentColor': '#2E628C',
      'ongoing': false,
    };
  }
  if (!next) {
    return {
      'weekBadge': weekBadge,
      'hasNext': false,
      'emptyText': courses.length > 0 ? '今日课程已结束' : '今天没有课程\n享受自由时光',
      'statusText': '',
      'courseName': '',
      'location': '',
      'timeText': '',
      'accentColor': '#2E628C',
      'ongoing': false,
    };
  }
  const ongoing = next.startMillis <= nowMillis && nowMillis < next.endMillis;
  return {
    'weekBadge': weekBadge,
    'hasNext': true,
    'emptyText': '',
    'statusText': ongoing ? '正在上课' : '即将开始',
    'courseName': next.title,
    'location': next.location,
    'timeText': next.timeText,
    'accentColor': argbToHex(next.colorArgb),
    'ongoing': ongoing,
  };
}

function buildTodayCourseData(
  snapshot: WidgetSnapshot | undefined,
  now: Date,
  dimension: number,
): Record<string, Object> {
  const nowMillis = now.getTime();
  const todayKey = formatIsoDate(now);
  const dateLabel = formatChineseDateLabel(now);
  const loggedOut = !snapshot || snapshot.state !== 'ready';
  const day = findDay(snapshot, todayKey);
  const weekBadge = loggedOut ? '未登录' : formatWeekBadge(day?.week ?? 0);

  if (loggedOut) {
    return {
      'weekBadge': '未登录',
      'dateLabel': dateLabel,
      'emptyText': '打开西瓜课表同步课程',
      'items': [] as CardCourseRow[],
      'isLarge': dimension >= 4,
    };
  }

  const allItems = day?.items ?? [];
  const hasCourseToday = allItems.some((item: WidgetSnapshotItem) => item.kind === 'course');
  const remaining = allItems.filter((item: WidgetSnapshotItem) => item.kind === 'thing' || item.endMillis > nowMillis);
  const maxItems = dimension >= 4 ? 6 : 2;
  const rows: CardCourseRow[] = remaining.slice(0, maxItems).map((item: WidgetSnapshotItem, index: number): CardCourseRow => {
    const isThing = item.kind === 'thing';
    const ongoing = !isThing && item.startMillis <= nowMillis && nowMillis < item.endMillis;
    const baseSub = item.location.length > 0 ? item.location : (isThing ? '自定义事项' : '课程');
    const row: CardCourseRow = {
      id: `${todayKey}-${index}-${item.title}`,
      title: item.title,
      subtitle: ongoing ? `正在上课 · ${baseSub}` : baseSub,
      timeText: item.timeText,
      accentColor: argbToHex(item.colorArgb),
      ongoing: ongoing,
      isThing: isThing,
    };
    return row;
  });

  const emptyText = rows.length > 0
    ? ''
    : (hasCourseToday ? '今日课程已全部结束' : '今天没有课程，享受自由时光');

  return {
    'weekBadge': weekBadge,
    'dateLabel': dateLabel,
    'emptyText': emptyText,
    'items': rows,
    'isLarge': dimension >= 4,
  };
}

function buildWeekColumn(dayIndex: number, courses: WidgetSnapshotItem[]): CardWeekColumn {
  const maxPeriod = 10;
  const periodMap: Array<WidgetSnapshotItem[]> = [];
  for (let p = 0; p <= maxPeriod; p++) {
    periodMap.push([]);
  }
  courses.forEach((course: WidgetSnapshotItem) => {
    const start = Math.max(1, Math.min(maxPeriod, course.startPeriod));
    const end = Math.max(start, Math.min(maxPeriod, course.endPeriod));
    for (let p = start; p <= end; p++) {
      periodMap[p].push(course);
    }
  });

  const slots: CardWeekSlot[] = [];
  let p = 1;
  while (p <= maxPeriod) {
    const currentList = periodMap[p];
    if (currentList.length === 0) {
      let emptyEnd = p;
      while (emptyEnd + 1 <= maxPeriod && periodMap[emptyEnd + 1].length === 0) {
        emptyEnd++;
      }
      slots.push({
        id: `d${dayIndex}-e${p}`,
        title: '',
        step: emptyEnd - p + 1,
        empty: true,
        bgColor: 'transparent',
        conflictText: '',
      });
      p = emptyEnd + 1;
      continue;
    }

    const primary = currentList[0];
    const desiredEnd = Math.min(maxPeriod, Math.max(p, primary.endPeriod));
    let spanEnd = p;
    while (spanEnd < desiredEnd && periodMap[spanEnd + 1].some((c: WidgetSnapshotItem) => c.title === primary.title)) {
      spanEnd++;
    }
    const overlapKeys = new Set<string>();
    for (let sp = p; sp <= spanEnd; sp++) {
      periodMap[sp].forEach((c: WidgetSnapshotItem) => {
        overlapKeys.add(`${c.title}|${c.startPeriod}|${c.endPeriod}|${c.location}`);
      });
    }
    const extraCount = Math.max(0, overlapKeys.size - 1);
    const shortTitle = primary.location.length > 0
      ? `${primary.title}\n@${primary.location}`
      : primary.title;
    slots.push({
      id: `d${dayIndex}-c${p}`,
      title: shortTitle,
      step: spanEnd - p + 1,
      empty: false,
      bgColor: argbToHex(primary.colorArgb),
      conflictText: extraCount > 0 ? `+${extraCount}` : '',
    });
    p = spanEnd + 1;
  }

  return { dayIndex: dayIndex, slots: slots };
}

function buildWeekCourseData(snapshot: WidgetSnapshot | undefined, now: Date): Record<string, Object> {
  const todayKey = formatIsoDate(now);
  const monday = startOfWeekMonday(now);
  const weekLabels = ['一', '二', '三', '四', '五', '六', '日'];
  const monthLabel = `${monday.getMonth() + 1}月`;
  const loggedOut = !snapshot || snapshot.state !== 'ready';

  const headers: CardWeekDayHeader[] = [];
  const columns: CardWeekColumn[] = [];
  let currentWeek = 0;
  let totalCourses = 0;

  for (let i = 0; i < 7; i++) {
    const dayDate = new Date(monday.getFullYear(), monday.getMonth(), monday.getDate() + i);
    const key = formatIsoDate(dayDate);
    headers.push({
      weekLabel: weekLabels[i],
      dateLabel: dayDate.getDate() === 1
        ? `${dayDate.getMonth() + 1}月`
        : dayDate.getDate().toString().padStart(2, '0'),
      isToday: key === todayKey,
    });
    const snapDay = findDay(snapshot, key);
    if (snapDay && snapDay.week > 0 && currentWeek === 0) {
      currentWeek = snapDay.week;
    }
    const dayCourses = (snapDay?.items ?? [])
      .filter((item: WidgetSnapshotItem) => item.kind === 'course' && item.startPeriod > 0)
      .sort((a: WidgetSnapshotItem, b: WidgetSnapshotItem) => a.startPeriod - b.startPeriod);
    totalCourses += dayCourses.length;
    columns.push(buildWeekColumn(i, dayCourses));
  }

  const weekBadge = loggedOut ? '未登录' : formatWeekBadge(currentWeek);
  const emptyText = loggedOut
    ? '打开西瓜课表同步课程'
    : (totalCourses === 0 ? '本周暂无课程安排' : '');

  return {
    'weekBadge': weekBadge,
    'monthLabel': monthLabel,
    'headers': headers,
    'columns': columns,
    'emptyText': emptyText,
  };
}

export function buildFormBinding(
  context: common.Context,
  formName: string,
  dimension: number,
): formBindingData.FormBindingData {
  let snapshot: WidgetSnapshot | undefined = undefined;
  try {
    const pref = getFreshPreferences(context);
    snapshot = parseSnapshot(pref.getSync(KEY_SNAPSHOT, '') as string);
  } catch (error) {
    hilog.warn(LOG_DOMAIN, LOG_TAG, 'read snapshot failed: %{public}s', (error as Error).message);
  }
  const now = new Date();
  let data: Record<string, Object>;
  if (formName === FORM_TODAY_COURSE) {
    data = buildTodayCourseData(snapshot, now, dimension);
  } else if (formName === FORM_WEEK_COURSE) {
    data = buildWeekCourseData(snapshot, now);
  } else {
    data = buildNextCourseData(snapshot, now);
  }
  return formBindingData.createFormBindingData(data);
}

export function registerForm(context: common.Context, formId: string, name: string, dimension: number): void {
  if (!formId) return;
  try {
    const pref = getFreshPreferences(context);
    const list = readRegisteredForms(pref).filter((item: RegisteredForm) => item.id !== formId);
    list.push({ id: formId, name: name, dimension: dimension });
    writeRegisteredForms(pref, list);
  } catch (error) {
    hilog.warn(LOG_DOMAIN, LOG_TAG, 'registerForm failed: %{public}s', (error as Error).message);
  }
}

export function unregisterForm(context: common.Context, formId: string): void {
  if (!formId) return;
  try {
    const pref = getFreshPreferences(context);
    const list = readRegisteredForms(pref).filter((item: RegisteredForm) => item.id !== formId);
    writeRegisteredForms(pref, list);
  } catch (error) {
    hilog.warn(LOG_DOMAIN, LOG_TAG, 'unregisterForm failed: %{public}s', (error as Error).message);
  }
}

export async function refreshFormById(context: common.Context, formId: string): Promise<void> {
  if (!formId) return;
  let formName = FORM_NEXT_COURSE;
  let dimension = 2;
  try {
    const pref = getFreshPreferences(context);
    const found = readRegisteredForms(pref).find((item: RegisteredForm) => item.id === formId);
    if (found) {
      formName = found.name;
      dimension = found.dimension;
    }
  } catch (error) {
    hilog.warn(LOG_DOMAIN, LOG_TAG, 'lookup form failed: %{public}s', (error as Error).message);
  }
  const binding = buildFormBinding(context, formName, dimension);
  try {
    await formProvider.updateForm(formId, binding);
  } catch (error) {
    hilog.warn(LOG_DOMAIN, LOG_TAG, 'updateForm failed (%{public}s): %{public}s', formId, (error as Error).message);
  }
}

export async function saveSnapshotAndRefreshAll(context: common.Context, snapshotJson: string): Promise<void> {
  let forms: RegisteredForm[] = [];
  try {
    const pref = getFreshPreferences(context);
    pref.putSync(KEY_SNAPSHOT, snapshotJson);
    pref.flushSync();
    forms = readRegisteredForms(pref);
  } catch (error) {
    hilog.warn(LOG_DOMAIN, LOG_TAG, 'saveSnapshot failed: %{public}s', (error as Error).message);
    return;
  }
  for (const item of forms) {
    const binding = buildFormBinding(context, item.name, item.dimension);
    try {
      await formProvider.updateForm(item.id, binding);
    } catch (error) {
      hilog.warn(LOG_DOMAIN, LOG_TAG, 'push updateForm failed (%{public}s): %{public}s', item.id, (error as Error).message);
    }
  }
}
