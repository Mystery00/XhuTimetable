import { DisplayItem, WeekCell, WeekOverview, WeekPage } from '../model/NativeModels';
import { isNonCurrentCourseColor, NativeEmpty, readableCourseText, weekCellContainer, withOpacity } from './AppComponents';
import { HomeIcon } from './HomeWidgets';

function twoDigits(value: number): string {
  return value < 10 ? '0' + value : value.toString();
}

function weekDate(value: string): Date {
  const parts = value.split('-');
  return new Date(Number(parts[0]), Number(parts[1]) - 1, Number(parts[2]), 12);
}

@Component
struct HomeWeekGrid {
  @Prop page: WeekPage = { week: 1, date: '', cells: [] };
  @Prop cellHeight: number = 70;
  @Prop cellRadius: number = 4;
  @Prop textSize: number = 10;
  @Prop cellOpacity: number = 0.8;
  @StorageProp('harmonyDark') private dark: boolean = false;
  onSelect: (items: DisplayItem[]) => void = () => {};
  private days: number[] = [1, 2, 3, 4, 5, 6, 7];
  private periods: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11];
  private names: string[] = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
  private startTimes: string[] = ['08:30', '09:20', '10:15', '11:05', '14:00', '14:50', '15:45', '16:35', '19:00', '19:50', '20:40'];

  private date(day: number): Date {
    const date = weekDate(this.page.date);
    date.setDate(date.getDate() + day - 1);
    return date;
  }

  private dateLabel(day: number): string {
    const date = this.date(day);
    return date.getDate() === 1 ? twoDigits(date.getMonth() + 1) + '月' : twoDigits(date.getDate());
  }

  private today(day: number): boolean {
    return this.date(day).toDateString() === new Date().toDateString();
  }

  private dayCells(day: number): WeekCell[] {
    return this.page.cells.filter((cell: WeekCell) => cell.day === day && cell.courses.length > 0);
  }

  private cellContainerColor(cell: WeekCell): ResourceColor {
    return isNonCurrentCourseColor(cell.color) ? withOpacity($r('app.color.container_highest'), 0.82) :
      weekCellContainer(cell.color, this.dark, this.cellOpacity);
  }

  private foreground(cell: WeekCell): ResourceColor {
    return isNonCurrentCourseColor(cell.color) ? $r('app.color.text_secondary') : readableCourseText(this.cellContainerColor(cell));
  }

  @Builder
  private courseCell(cell: WeekCell) {
    Column() {
      Stack({ alignContent: Alignment.BottomEnd }) {
        Column() {
          Text(cell.title).fontSize(this.textSize).lineHeight(this.textSize * 1.25)
            .fontWeight(isNonCurrentCourseColor(cell.color) ? FontWeight.Normal : FontWeight.Medium)
            .fontColor(this.foreground(cell)).width('100%').textAlign(TextAlign.Center)
            .maxLines(Math.max(1, Math.floor((this.cellHeight * cell.span - 11 - (cell.courses.length > 1 && cell.span === 1 ? 12 : 0)) / (this.textSize * 1.25))))
            .textOverflow({ overflow: TextOverflow.Ellipsis })
        }.width('100%').height('100%')
        if (cell.courses.length > 1) {
          Text('+' + (cell.courses.length - 1)).fontSize(8).lineHeight(10).fontWeight(FontWeight.Bold)
            .fontColor(Color.White).padding({ left: 3, right: 3, top: 1, bottom: 1 }).borderRadius(4)
            .backgroundColor('#73000000')
        }
      }.width('100%').height('100%').padding({ left: 3, right: 3, top: 4, bottom: 4 })
        .backgroundColor(this.cellContainerColor(cell)).borderRadius(Math.max(0, Math.min(24, this.cellRadius))).clip(true)
        .onClick(() => { this.onSelect(cell.courses); })
        .accessibilityGroup(true).accessibilityText(cell.title + (cell.courses.length > 1 ? '，另有' + (cell.courses.length - 1) + '门冲突课程' : ''))
    }.width('100%').height(this.cellHeight * cell.span).padding(1.5)
      .position({ x: 0, y: (cell.start - 1) * this.cellHeight })
  }

  build() {
    Column() {
      Row() {
        Column() {
          Text(twoDigits(weekDate(this.page.date).getMonth() + 1)).fontSize(12).lineHeight(15)
            .fontWeight(FontWeight.Bold).fontColor($r('app.color.primary'))
          Text('月').fontSize(9).lineHeight(13.75).fontColor($r('app.color.text_secondary'))
        }.layoutWeight(0.42)
        ForEach(this.days, (day: number) => {
          Column({ space: 2 }) {
            Text(this.names[this.date(day).getDay()]).fontSize(10).lineHeight(13.75)
              .fontWeight(this.today(day) ? FontWeight.Bold : FontWeight.Normal)
              .fontColor(this.today(day) ? $r('app.color.primary') : $r('app.color.text_secondary'))
            Text(this.dateLabel(day)).fontSize(10).lineHeight(13.75).maxLines(1)
              .fontWeight(this.today(day) ? FontWeight.Bold : FontWeight.Medium)
              .fontColor(this.today(day) ? $r('app.color.on_primary') : $r('app.color.text_primary'))
              .padding({ left: 6, right: 6, top: 1, bottom: 1 }).borderRadius(10)
              .backgroundColor(this.today(day) ? $r('app.color.primary') : Color.Transparent)
          }.layoutWeight(1)
        }, (day: number) => day.toString())
      }.width('100%').padding({ top: 6, bottom: 6 })
        .backgroundColor(withOpacity($r('app.color.surface'), this.dark ? 0.72 : 0.84))
      if (this.page.cells.some((cell: WeekCell) => cell.courses.length > 0)) {
        Scroll() {
          Row() {
            Column() {
              ForEach(this.periods, (period: number) => {
                Column() {
                  Text(period.toString()).fontSize(11).lineHeight(13.75).fontWeight(600)
                    .fontColor($r('app.color.text_primary'))
                  if (this.cellHeight >= 48) {
                    Text(this.startTimes[period - 1]).fontSize(8).lineHeight(9).maxLines(1)
                      .fontColor(withOpacity($r('app.color.text_secondary'), 0.8))
                  }
                }.height(this.cellHeight).width('100%').justifyContent(FlexAlign.Center)
              }, (period: number) => period.toString())
            }.layoutWeight(0.42).backgroundColor(withOpacity($r('app.color.surface'), this.dark ? 0.52 : 0.65))
            ForEach(this.days, (day: number) => {
              Stack({ alignContent: Alignment.TopStart }) {
                ForEach(this.dayCells(day), (cell: WeekCell) => {
                  this.courseCell(cell)
                }, (cell: WeekCell) => cell.day + ':' + cell.start + ':' + cell.span + ':' + JSON.stringify(cell))
              }.layoutWeight(1).height(this.cellHeight * 11)
            }, (day: number) => day.toString())
          }.width('100%').padding({ bottom: 12 }).alignItems(VerticalAlign.Top)
        }.layoutWeight(1).width('100%').align(Alignment.TopStart).scrollBar(BarState.Off)
      } else {
        Column() {
          NativeEmpty({ title: '本周没有课程哦~', subtitle: '左右滑动可查看其他周次课表', image: 'state_no_course_data.png' })
        }.layoutWeight(1).width('100%').justifyContent(FlexAlign.Center)
      }
    }.width('100%').height('100%').backgroundColor(Color.Transparent)
  }
}

@Component
export struct HomeWeek {
  @Prop pages: WeekPage[] = [];
  @Prop selectedWeek: number = 1;
  @Prop cellHeight: number = 70;
  @Prop cellRadius: number = 4;
  @Prop textSize: number = 10;
  @Prop cellOpacity: number = 0.8;
  onWeek: (week: number) => void = () => {};
  onSelect: (items: DisplayItem[]) => void = () => {};
  onRefresh: () => void = () => {};

  private selectedIndex(): number {
    return Math.max(0, this.pages.findIndex((page: WeekPage) => page.week === this.selectedWeek));
  }

  build() {
    Column() {
      if (this.pages.length > 0) {
        Swiper() {
          ForEach(this.pages, (page: WeekPage) => {
            HomeWeekGrid({ page: page, cellHeight: this.cellHeight, cellRadius: this.cellRadius,
              textSize: this.textSize, cellOpacity: this.cellOpacity, onSelect: this.onSelect })
          }, (page: WeekPage) => page.week + ':' + JSON.stringify(page))
        }.index(this.selectedIndex()).loop(false).indicator(false).cachedCount(1).duration(240)
          .width('100%').height('100%').onChange((index: number) => {
            const page = this.pages[index];
            if (page !== undefined && page.week !== this.selectedWeek) this.onWeek(page.week);
          })
      } else {
        NativeEmpty({ title: '还没有课表数据', subtitle: '联网刷新后，课表也可以离线查看',
          image: 'state_no_course_data.png', action: '刷新课表', onAction: this.onRefresh })
      }
    }.width('100%').height('100%').justifyContent(FlexAlign.Center).backgroundColor(Color.Transparent)
  }
}

@Component
export struct HomeWeekPicker {
  @Prop overview: WeekOverview[] = [];
  @Prop selectedWeek: number = 1;
  @Prop actualWeek: number = 1;
  onSelect: (week: number) => void = () => {};
  private rows: number[] = [0, 1, 2, 3, 4];
  private columns: number[] = [0, 1, 2, 3];
  private dots: number[] = [0, 1, 2, 3, 4];

  private current(week: number): boolean {
    return week === this.actualWeek || (this.overview.find((item: WeekOverview) => item.week === week)?.current ?? false);
  }

  private active(week: number, slot: number, day: number): boolean {
    return this.overview.find((item: WeekOverview) => item.week === week)?.cells[slot]?.[day] ?? false;
  }

  private weekTileContainerColor(week: number): ResourceColor {
    if (week === this.selectedWeek) return $r('app.color.primary_container');
    return this.current(week) ? withOpacity($r('app.color.secondary_container'), 0.55) : $r('app.color.card');
  }

  private weekTileBorderColor(week: number): ResourceColor {
    if (week === this.selectedWeek) return $r('app.color.primary');
    return this.current(week) ? withOpacity($r('app.color.primary'), 0.5) : withOpacity($r('app.color.outline'), 0.4);
  }

  @Builder
  private weekTile(week: number) {
    Stack({ alignContent: Alignment.TopEnd }) {
      Column({ space: 4 }) {
        Column() {
          ForEach(this.dots, (day: number) => {
            Row() {
              ForEach(this.dots, (slot: number) => {
                Stack() {
                  Circle().width(3.6).height(3.6)
                    .fill(this.active(week, slot, day) ? $r('app.color.primary') : withOpacity($r('app.color.outline'), 0.5))
                }.width(6).height(6)
              }, (slot: number) => slot.toString())
            }.height(6)
          }, (day: number) => day.toString())
        }.width(30).height(30).accessibilityLevel('no')
        Text('第' + week + '周').fontSize(12).lineHeight(15).maxLines(1)
          .fontWeight(week === this.selectedWeek || this.current(week) ? FontWeight.Bold : FontWeight.Medium)
          .fontColor(week === this.selectedWeek ? $r('app.color.on_primary_container') : $r('app.color.text_primary'))
      }.width('100%').height('100%').justifyContent(FlexAlign.Center)
      if (this.current(week)) {
        Text('今').fontSize(8).lineHeight(10).fontWeight(FontWeight.Bold).fontColor($r('app.color.on_primary'))
          .padding({ left: 4, right: 4, top: 1, bottom: 1 }).borderRadius(6).backgroundColor($r('app.color.primary'))
      }
    }.layoutWeight(1).height(74).padding({ left: 4, right: 4, top: 6, bottom: 6 }).borderRadius(14)
      .backgroundColor(this.weekTileContainerColor(week))
      .border({ width: week === this.selectedWeek ? 1.5 : this.current(week) ? 1 : 0.5, color: this.weekTileBorderColor(week) })
      .onClick(() => { this.onSelect(week); }).accessibilityGroup(true)
      .id('home-week-' + week).accessibilitySelected(week === this.selectedWeek)
      .accessibilityText('第' + week + '周' + (this.current(week) ? '，本周' : '') + (week === this.selectedWeek ? '，已选中' : ''))
  }

  build() {
    Scroll() {
      Column({ space: 8 }) {
        Row({ space: 4 }) {
          Column() {
            Text('切换教学周').fontSize(16).lineHeight(21.12).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
            Text('点阵表示周一至周五各时段排课密度').fontSize(12).lineHeight(16.56).fontColor($r('app.color.text_secondary'))
          }.layoutWeight(1).alignItems(HorizontalAlign.Start)
          if (this.actualWeek >= 1 && this.actualWeek <= 20) {
            Button({ type: ButtonType.Normal }) {
              Row({ space: 4 }) {
                HomeIcon({ name: 'today', iconSize:16, tint: $r('app.color.primary') })
                Text('回到第' + this.actualWeek + '周').fontSize(14).lineHeight(17.92).fontColor($r('app.color.primary'))
              }
            }.backgroundColor(Color.Transparent).padding({ left: 4, right: 4 }).height(40)
              .onClick(() => { this.onSelect(this.actualWeek); })
          }
        }.width('100%').padding(4)
        Column({ space: 6 }) {
          ForEach(this.rows, (row: number) => {
            Row({ space: 6 }) {
              ForEach(this.columns, (column: number) => {
                this.weekTile(row * 4 + column + 1)
              }, (column: number) => column.toString())
            }.width('100%')
          }, (row: number) => row.toString())
        }.width('100%')
      }.width('100%').padding({ left: 16, right: 16, bottom: 24 })
    }.width('100%').scrollBar(BarState.Off)
  }
}
