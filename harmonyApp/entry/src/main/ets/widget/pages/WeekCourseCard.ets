interface WeekHeaderItem {
  weekLabel: string;
  dateLabel: string;
  isToday: boolean;
}

interface WeekGridSlot {
  id: string;
  title: string;
  step: number;
  empty: boolean;
  bgColor: string;
  conflictText: string;
}

interface WeekGridColumn {
  dayIndex: number;
  slots: WeekGridSlot[];
}

const weekCourseStorage = new LocalStorage();
const WEEK_CARD_PERIODS: number[] = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];

@Entry(weekCourseStorage)
@Component
struct WeekCourseCard {
  @LocalStorageProp('weekBadge') weekBadge: string = '未登录';
  @LocalStorageProp('monthLabel') monthLabel: string = '';
  @LocalStorageProp('headers') headers: WeekHeaderItem[] = [];
  @LocalStorageProp('columns') columns: WeekGridColumn[] = [];
  @LocalStorageProp('emptyText') emptyText: string = '打开西瓜课表同步课程';

  private openApp(): void {
    postCardAction(this, {
      action: 'router',
      abilityName: 'EntryAbility',
      params: { 'source': 'weekCourseWidget' }
    });
  }

  private refreshCard(): void {
    postCardAction(this, {
      action: 'message',
      params: { 'action': 'refresh' }
    });
  }

  build() {
    Column() {
      // 顶栏：本周课表 + 周次胶囊（点击刷新） + 打开
      Row() {
        Row({ space: 6 }) {
          Text('本周课表')
            .fontSize(14)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary'))
          Text(this.weekBadge)
            .fontSize(10)
            .fontWeight(FontWeight.Medium)
            .fontColor($r('app.color.on_primary_container'))
            .backgroundColor($r('app.color.primary_container'))
            .borderRadius(8)
            .padding({ left: 6, right: 6, top: 2, bottom: 2 })
        }
        .layoutWeight(1)
        .alignItems(VerticalAlign.Center)
        .onClick(() => this.refreshCard())

        Text('打开 ›')
          .fontSize(12)
          .fontWeight(FontWeight.Medium)
          .fontColor($r('app.color.primary'))
          .padding({ left: 6, right: 2, top: 2, bottom: 2 })
          .onClick(() => this.openApp())
      }
      .width('100%')
      .alignItems(VerticalAlign.Center)

      // 星期与日期栏
      if (this.headers.length === 7) {
        Row() {
          Text(this.monthLabel)
            .width(20)
            .fontSize(9)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.primary'))
            .textAlign(TextAlign.Center)
          ForEach(this.headers, (header: WeekHeaderItem, index: number) => {
            Column({ space: 1 }) {
              Text(header.weekLabel)
                .fontSize(9)
                .fontWeight(header.isToday ? FontWeight.Bold : FontWeight.Normal)
                .fontColor(header.isToday ? $r('app.color.primary') : $r('app.color.text_secondary'))
              Text(header.dateLabel)
                .fontSize(9)
                .fontWeight(header.isToday ? FontWeight.Bold : FontWeight.Medium)
                .fontColor(header.isToday ? $r('app.color.on_primary') : $r('app.color.text_primary'))
                .backgroundColor(header.isToday ? $r('app.color.primary') : Color.Transparent)
                .borderRadius(7)
                .padding({ left: 4, right: 4, top: 1, bottom: 1 })
            }
            .layoutWeight(1)
            .alignItems(HorizontalAlign.Center)
          }, (_header: WeekHeaderItem, index: number) => index.toString())
        }
        .width('100%')
        .margin({ top: 6, bottom: 4 })
        .alignItems(VerticalAlign.Center)
      }

      if (this.emptyText.length > 0) {
        Column() {
          Text(this.emptyText)
            .fontSize(12)
            .fontColor($r('app.color.text_secondary'))
            .textAlign(TextAlign.Center)
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .onClick(() => this.openApp())
      } else {
        Row() {
          // 左侧节次栏（1~10 节自适应填满卡片高度）
          Column() {
            ForEach(WEEK_CARD_PERIODS, (period: number) => {
              Column() {
                Text(period.toString())
                  .fontSize(9)
                  .fontWeight(FontWeight.Medium)
                  .fontColor($r('app.color.text_secondary'))
              }
              .width('100%')
              .layoutWeight(1)
              .justifyContent(FlexAlign.Center)
              .alignItems(HorizontalAlign.Center)
            }, (period: number) => period.toString())
          }
          .width(20)
          .height('100%')

          // 右侧周一至周日课程列
          ForEach(this.columns, (col: WeekGridColumn) => {
            Column() {
              ForEach(col.slots, (slot: WeekGridSlot) => {
                if (slot.empty) {
                  Column() {}
                    .width('100%')
                    .layoutWeight(slot.step)
                } else {
                  Column() {
                    Column() {
                      Text(slot.title)
                        .fontSize(8)
                        .lineHeight(10)
                        .fontWeight(FontWeight.Medium)
                        .fontColor('#FFFFFF')
                        .textAlign(TextAlign.Center)
                        .maxLines(Math.max(2, Math.min(8, slot.step * 2)))
                        .textOverflow({ overflow: TextOverflow.Ellipsis })
                        .layoutWeight(1)
                      if (slot.conflictText.length > 0) {
                        Text(slot.conflictText)
                          .fontSize(7)
                          .fontWeight(FontWeight.Bold)
                          .fontColor('#FFFFFF')
                          .backgroundColor('#38000000')
                          .borderRadius(4)
                          .padding({ left: 3, right: 3, top: 1, bottom: 1 })
                          .margin({ top: 1 })
                      }
                    }
                    .width('100%')
                    .height('100%')
                    .padding({ left: 2, right: 2, top: 3, bottom: 2 })
                    .borderRadius(6)
                    .backgroundColor(slot.bgColor)
                    .justifyContent(FlexAlign.SpaceBetween)
                    .alignItems(HorizontalAlign.Center)
                  }
                  .width('100%')
                  .layoutWeight(slot.step)
                  .padding(1.5)
                }
              }, (slot: WeekGridSlot) => slot.id)
            }
            .layoutWeight(1)
            .height('100%')
          }, (col: WeekGridColumn) => col.dayIndex.toString())
        }
        .width('100%')
        .layoutWeight(1)
        .onClick(() => this.openApp())
      }
    }
    .width('100%')
    .height('100%')
    .padding(12)
    .backgroundColor($r('app.color.surface'))
  }
}
