import { CalendarDay, CalendarWeek, DisplayItem } from '../model/NativeModels';
import { courseAccent, courseSoftContainer, emptyDisplayItem, NativeEmpty, withOpacity } from './AppComponents';
import { HomeIcon } from './HomeWidgets';

interface HomeCalendarRow {
  key: string;
  week: number;
  weekTitle: string;
  header: boolean;
  day: CalendarDay;
}

class HomeCalendarSource implements IDataSource {
  private rows: HomeCalendarRow[] = [];
  private listeners: DataChangeListener[] = [];

  totalCount(): number {
    return this.rows.length;
  }

  getData(index: number): HomeCalendarRow {
    return this.rows[index];
  }

  replace(rows: HomeCalendarRow[]): void {
    this.rows = rows;
    this.listeners.forEach((listener: DataChangeListener) => { listener.onDataReloaded(); });
  }

  registerDataChangeListener(listener: DataChangeListener): void {
    if (!this.listeners.includes(listener)) this.listeners.push(listener);
  }

  unregisterDataChangeListener(listener: DataChangeListener): void {
    const index = this.listeners.indexOf(listener);
    if (index >= 0) this.listeners.splice(index, 1);
  }
}

@Component
export struct HomeCalendarCard {
  @Prop item: DisplayItem = emptyDisplayItem();
  @Prop practical: boolean = false;
  @StorageProp('harmonyDark') private dark: boolean = false;
  @State private contentHeight: number = 0;

  private accent(): ResourceColor {
    return courseAccent(this.item.color || $r('app.color.primary'), this.dark);
  }

  private practicalDescription(): string {
    const parts: string[] = [];
    if (this.item.subtitle.length > 0) parts.push(this.item.subtitle);
    if (this.item.time.length > 0) parts.push(this.item.time);
    return parts.join(' · ');
  }

  build() {
    Stack({ alignContent: Alignment.TopStart }) {
      Column({ space: this.practical ? 3 : 2 }) {
        Text(this.item.title).fontSize(14).lineHeight(18.48).fontWeight(600)
          .fontColor($r('app.color.text_primary')).width('100%')
        if (this.practical) {
          Text(this.practicalDescription()).fontSize(12).lineHeight(16.56)
            .fontColor($r('app.color.text_secondary')).width('100%')
        } else {
          if (this.item.subtitle.length > 0) {
            Text(this.item.subtitle).fontSize(12).lineHeight(16.56).fontColor(this.accent()).width('100%')
          }
          if (this.item.detail.length > 0) {
            Text(this.item.detail).fontSize(12).lineHeight(16.56).fontColor($r('app.color.text_secondary')).width('100%')
          }
        }
      }.width('100%').padding({ left: this.practical ? 26 : 25.5, right: 12,
        top: this.practical ? 12 : 10, bottom: this.practical ? 12 : 10 }).alignItems(HorizontalAlign.Start)
        .onAreaChange((_oldArea: Area, area: Area) => { this.contentHeight = Number(area.height); })
      Column() {}.width(this.practical ? 4 : 3.5).height(Math.max(0, this.contentHeight - (this.practical ? 24 : 20)))
        .borderRadius(4).backgroundColor(this.accent()).position({ x: 12, y: this.practical ? 12 : 10 })
        .hitTestBehavior(HitTestMode.None)
    }.width('100%').borderRadius(14)
      .backgroundColor(courseSoftContainer(this.item.color || $r('app.color.primary'), this.dark, 0.94))
      .border({ width: this.practical ? 0.8 : 0.6, color: withOpacity(this.accent(), this.practical ? 0.35 : 0.3) })
  }
}

@Component
export struct HomeCalendar {
  @Prop @Watch('onWeeksChanged') weeks: CalendarWeek[] = [];
  @Prop @Watch('onTodayRequested') todayRequest: number = 0;
  @StorageProp('harmonyDark') private dark: boolean = false;
  @State private rowCount: number = 0;
  @State private targetIndex: number = 0;
  @State private firstIndex: number = 0;
  onTitle: (title: string) => void = () => {};
  private scroller: Scroller = new Scroller();
  private source: HomeCalendarSource = new HomeCalendarSource();
  private hasAutoScrolled: boolean = false;
  private listReady: boolean = false;
  private scrollTimer: number = -1;
  private publishedTitle: string = '';

  aboutToAppear(): void {
    this.onWeeksChanged();
  }

  aboutToDisappear(): void {
    if (this.scrollTimer >= 0) clearTimeout(this.scrollTimer);
    this.scrollTimer = -1;
    this.listReady = false;
  }

  private onWeeksChanged(): void {
    const rows: HomeCalendarRow[] = [];
    let today = -1;
    let cursor = -1;
    this.weeks.forEach((week: CalendarWeek) => {
      if (week.cursor && cursor < 0) cursor = rows.length;
      const title = week.title || '第' + week.week + '周';
      rows.push({ key: 'week:' + week.week, week: week.week, weekTitle: title, header: true,
        day: { date: '', title: '', today: false, week: week.week, items: [], kind: 'DAY_ITEM' } });
      week.days.forEach((day: CalendarDay, index: number) => {
        if (day.today && day.kind !== 'MONTH_HEADER' && today < 0) today = rows.length;
        rows.push({ key: week.week + ':' + day.kind + ':' + day.date + ':' + index,
          week: week.week, weekTitle: title, header: false, day: day });
      });
    });
    this.targetIndex = today >= 0 ? today : cursor >= 0 ? cursor : 0;
    this.rowCount = rows.length;
    this.source.replace(rows);
    if (rows.length === 0) {
      this.publishedTitle = '';
      this.onTitle('');
    } else {
      this.publishTitle(Math.min(this.firstIndex, rows.length - 1));
    }
    this.scrollInitially();
  }

  private publishTitle(index: number): void {
    if (index < 0 || index >= this.source.totalCount()) return;
    const title = '第' + this.source.getData(index).week + '周';
    if (title !== this.publishedTitle) {
      this.publishedTitle = title;
      this.onTitle(title);
    }
  }

  private scrollInitially(): void {
    if (!this.listReady || this.hasAutoScrolled || this.targetIndex <= 0 || this.scrollTimer >= 0) return;
    // 等待 LazyForEach 应用新数据，避免异步首批数据到达时滚动索引尚未建立。
    this.scrollTimer = setTimeout(() => {
      this.scrollTimer = -1;
      if (!this.listReady || this.targetIndex <= 0) return;
      this.scroller.scrollToIndex(this.targetIndex, false, ScrollAlign.START);
      this.firstIndex = this.targetIndex;
      this.publishTitle(this.targetIndex);
      this.hasAutoScrolled = true;
    }, 0);
  }

  private onTodayRequested(): void {
    if (!this.listReady || this.targetIndex <= 0) return;
    this.scroller.scrollToIndex(this.targetIndex, true, ScrollAlign.START);
  }

  private monthImage(day: CalendarDay): ResourceStr {
    return $rawfile('shared/ic_month' + Number(day.date.substring(5, 7)) + '.jpg');
  }

  @Builder
  private monthHeader(day: CalendarDay) {
    Stack({ alignContent: Alignment.TopStart }) {
      Image(this.monthImage(day)).width('100%').height('100%').objectFit(ImageFit.Cover).accessibilityLevel('no')
      Column() {}.width('100%').height('100%')
        .linearGradient({ angle: 180, colors: [['#6B000000', 0], ['#00000000', 0.32], ['#00000000', 1]] })
      Text(day.title).fontSize(22).lineHeight(29.04).fontWeight(FontWeight.Bold).fontColor(Color.White)
        .padding({ left: 18, top: 14, right: 18 })
    }.width('100%').aspectRatio(16 / 9).borderRadius(20).clip(true)
  }

  @Builder
  private dayRow(day: CalendarDay) {
    Row() {
      Column() {
        Text(day.title).fontSize(11).lineHeight(13.75).fontWeight(FontWeight.Bold).textAlign(TextAlign.Center)
          .fontColor(day.today ? $r('app.color.on_primary') : $r('app.color.text_primary'))
          .width(40).height(40).borderRadius(20)
          .backgroundColor(day.today ? $r('app.color.primary') : Color.Transparent)
      }.width(64)
      Column({ space: 6 }) {
        ForEach(day.items, (item: DisplayItem) => {
          HomeCalendarCard({ item: item })
        }, (item: DisplayItem, index: number) => item.id + ':' + index + ':' + JSON.stringify(item))
      }.layoutWeight(1).padding({ right: 16 })
    }.width('100%').padding({ top: 8, bottom: 8 }).alignItems(VerticalAlign.Top)
      .id('calendar-day-' + day.date)
  }

  build() {
    Stack({ alignContent: Alignment.BottomEnd }) {
      if (this.rowCount > 0) {
        List({ scroller: this.scroller, initialIndex: this.targetIndex }) {
          LazyForEach(this.source, (row: HomeCalendarRow) => {
            ListItem() {
              if (row.header) {
                Text(row.weekTitle).fontSize(14).lineHeight(17.92).fontWeight(600)
                  .fontColor($r('app.color.primary')).width('100%')
                  .padding({ left: 64, right: 16, top: 8, bottom: 8 })
                  .backgroundColor(withOpacity($r('app.color.container'), 0.88)).margin({ top: 6, bottom: 2 })
              } else if (row.day.kind === 'MONTH_HEADER') {
                Column() {
                  this.monthHeader(row.day)
                }.width('100%').padding({ left: 16, right: 16, top: 6, bottom: 6 })
              } else {
                this.dayRow(row.day)
              }
            }.width('100%')
          }, (row: HomeCalendarRow) => row.key + ':' + row.weekTitle + ':' + JSON.stringify(row.day))
        }.width('100%').height('100%').scrollBar(BarState.Off).cachedCount(5)
          .id('home-calendar-list').contentEndOffset(28)
          .onAppear(() => { this.listReady = true; this.scrollInitially(); })
          .onDisAppear(() => { this.listReady = false; })
          .onScrollIndex((first: number) => {
            this.firstIndex = first;
            this.publishTitle(first);
          })
      } else {
        Column() {
          NativeEmpty({ title: '还没有日程数据', subtitle: '刷新课表后即可查看学期日程', image: 'state_no_thing.png' })
        }.width('100%').height('100%').justifyContent(FlexAlign.Center)
      }
      if (this.targetIndex > 0 && Math.abs(this.firstIndex - this.targetIndex) > 3) {
        Button({ type: ButtonType.Normal }) {
          HomeIcon({ name: 'today', iconSize:24, tint: $r('app.color.on_primary_container') })
        }.width(40).height(40).padding(0).borderRadius(12).backgroundColor($r('app.color.primary_container'))
          .shadow({ radius: 3, color: '#26000000', offsetY: 2 }).margin(20)
          .id('calendar-today').accessibilityText('回到今天').onClick(() => { this.onTodayRequested(); })
          .transition(TransitionEffect.OPACITY.combine(TransitionEffect.scale({ x: 0.8, y: 0.8 })))
      }
    }.width('100%').height('100%')
      .backgroundColor(withOpacity($r('app.color.surface'), this.dark ? 0.48 : 0.55))
  }
}
