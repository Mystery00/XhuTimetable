import { DisplayItem, HomeState } from '../model/NativeModels';
import { courseAccent, courseSoftContainer, createEmptyHomeState, emptyDisplayItem, NativeEmpty, ScheduleCard, withOpacity } from './AppComponents';
import { HomeAccountBadge, HomeIcon } from './HomeWidgets';

@Component
struct HomeTodayThing {
  @Prop item: DisplayItem = emptyDisplayItem();
  @Prop alpha: number = 1;
  @Prop multiAccount: boolean = false;
  @StorageProp('harmonyDark') private dark: boolean = false;
  @State private expanded: boolean = false;
  @State private contentHeight: number = 0;

  private accent(): ResourceColor {
    return courseAccent(this.item.color || $r('app.color.primary'), this.dark);
  }

  private remainingDays(): number {
    return Number(this.item.value);
  }

  build() {
    Stack({ alignContent: Alignment.TopStart }) {
      Row({ space: 12 }) {
        Column({ space: 4 }) {
          Row({ space: 6 }) {
            Text(this.item.title).fontSize(16).lineHeight(21.12).fontWeight(600)
              .fontColor($r('app.color.text_primary')).layoutWeight(1).maxLines(2)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
            if (this.multiAccount && this.item.badge.length > 0) {
              HomeAccountBadge({ text: this.item.badge })
            }
          }.width('100%')
          Row({ space: 5 }) {
            HomeIcon({ name: 'clock', iconSize:14 })
            Text(this.item.time).fontSize(12).lineHeight(16.56).fontColor($r('app.color.text_secondary')).layoutWeight(1)
          }.width('100%')
          if (this.item.location.length > 0) {
            Row({ space: 5 }) {
              HomeIcon({ name: 'location', iconSize:14 })
              Text(this.item.location).fontSize(12).lineHeight(16.56).fontColor($r('app.color.text_secondary'))
                .layoutWeight(1).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
            }.width('100%')
          }
          if (this.item.subtitle.length > 0) {
            Text(this.item.subtitle).fontSize(12).lineHeight(16.56)
              .fontColor(withOpacity($r('app.color.text_secondary'), 0.85)).width('100%')
              .maxLines(this.expanded ? 2147483647 : 2).textOverflow({ overflow: TextOverflow.Ellipsis })
              .margin({ top: 2 })
          }
        }.layoutWeight(1).alignItems(HorizontalAlign.Start)
        if (this.item.value.length > 0 && Number.isFinite(this.remainingDays())) {
          Column() {
            if (this.remainingDays() <= 0) {
              Text('今天').fontSize(16).lineHeight(21.12).fontWeight(FontWeight.Bold).fontColor(this.accent())
            } else {
              Text('还剩').fontSize(11).lineHeight(13.75).fontColor($r('app.color.text_secondary'))
              Text(this.remainingDays() > 730 ? (this.remainingDays() / 365).toFixed(1) : this.item.value)
                .fontSize(22).lineHeight(29.04).fontWeight(FontWeight.Bold).fontColor(this.accent())
              Text(this.remainingDays() > 730 ? '年' : '天').fontSize(11).lineHeight(13.75)
                .fontColor($r('app.color.text_secondary'))
            }
          }.padding({ left: 12, right: 12, top: 8, bottom: 8 }).borderRadius(14)
            .backgroundColor(withOpacity(this.accent(), 0.12))
            .border({ width: 0.5, color: withOpacity(this.accent(), 0.3) })
        }
      }.width('100%').padding({ left: 28, right: 14, top: 14, bottom: 14 })
        .onAreaChange((_oldArea: Area, area: Area) => { this.contentHeight = Number(area.height); })
      Column() {}.width(4).height(Math.max(0, this.contentHeight - 28)).borderRadius(4)
        .backgroundColor(this.accent()).position({ x: 14, y: 14 }).hitTestBehavior(HitTestMode.None)
    }.width('100%').borderRadius(18).backgroundColor(withOpacity($r('app.color.card'), this.alpha))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) })
      .onClick(() => {
        if (this.item.subtitle.length > 0) {
          this.getUIContext().animateTo({ duration: 260, curve: Curve.FastOutSlowIn }, () => { this.expanded = !this.expanded; });
        }
      })
  }
}

@Component
export struct HomeToday {
  @Prop @Watch('onHomeChanged') home: HomeState = createEmptyHomeState();
  @StorageProp('harmonyDark') private dark: boolean = false;
  @State private refreshing: boolean = false;
  onRefresh: () => void = () => {};
  onCourse: (items: DisplayItem[]) => void = () => {};
  onPoem: () => void = () => {};

  aboutToAppear(): void {
    this.onHomeChanged();
  }

  private onHomeChanged(): void {
    this.refreshing = this.home.loading;
  }

  private summary(): string {
    const parts: string[] = [];
    if (this.home.today.length > 0) parts.push(this.home.today.length + ' 门课程');
    if (this.home.things.length > 0) parts.push(this.home.things.length + ' 项日程');
    return parts.join(' · ');
  }

  private empty(): boolean {
    return this.home.today.length === 0 && this.home.things.length === 0 && this.home.poem.length === 0 && this.home.holiday.length === 0;
  }

  private holidayAccent(): ResourceColor {
    return courseAccent(this.home.holidayColor || $r('app.color.primary'), this.dark);
  }

  @Builder
  private summaryStrip() {
    Row({ space: 8 }) {
      HomeIcon({ name: 'clock', iconSize:16, tint: $r('app.color.primary') })
      Text(this.summary()).fontSize(14).lineHeight(17.92).fontWeight(FontWeight.Medium)
        .fontColor($r('app.color.text_primary')).maxLines(1)
    }.padding({ left: 14, right: 14, top: 8, bottom: 8 }).borderRadius(14)
      .backgroundColor(withOpacity($r('app.color.container_high'), Math.max(0.6, Math.min(0.95, this.home.todayOpacity * 0.92))))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) })
  }

  @Builder
  private poemCard() {
    Row({ space: 10 }) {
      Stack() {
        HomeIcon({ name: 'sparkles', iconSize:16, tint: $r('app.color.on_tertiary_container') })
      }.width(32).height(32).borderRadius(16).backgroundColor(withOpacity($r('app.color.tertiary_container'), 0.7))
      Column({ space: 3 }) {
        Text(this.home.poem).fontSize(14).lineHeight(19.32).fontWeight(FontWeight.Medium)
          .fontColor($r('app.color.text_primary')).width('100%')
        if (this.home.poemDetails !== undefined) {
          Text('—— ' + this.home.poemDetails.author + '《' + this.home.poemDetails.title + '》')
            .fontSize(11).lineHeight(13.75).fontColor($r('app.color.text_secondary')).width('100%').textAlign(TextAlign.End)
        }
      }.layoutWeight(1).alignItems(HorizontalAlign.Start)
    }.width('100%').padding({ left: 14, right: 14, top: 12, bottom: 12 }).borderRadius(18)
      .backgroundColor(withOpacity($r('app.color.card'), this.home.todayOpacity))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.35) }).onClick(this.onPoem)
      .accessibilityText('今日诗词，' + this.home.poem + '，查看完整诗词')
  }

  build() {
    Refresh({ refreshing: $$this.refreshing }) {
      Scroll() {
        if (this.empty()) {
          Column() {
            NativeEmpty({ title: this.home.showTomorrow ? '明天没有课哦~' : '今天没有课哦~',
              subtitle: '可以好好放松一下，或者安排些自己的学习计划', image: 'state_no_course_data.png',
              action: '刷新课表', onAction: this.onRefresh })
          }.width('100%').constraintSize({ minHeight: '100%' }).justifyContent(FlexAlign.Center)
        } else {
          Column({ space: 10 }) {
            if (this.summary().length > 0) {
              this.summaryStrip()
            }
            if (this.home.poem.length > 0) {
              this.poemCard()
            }
            if (this.home.holiday.length > 0) {
              Row({ space: 10 }) {
                HomeIcon({ name: 'watermelon', iconSize:20, tint: this.holidayAccent() })
                Text(this.home.holiday).fontSize(14).lineHeight(18.48).fontWeight(600)
                  .fontColor(this.holidayAccent()).layoutWeight(1)
              }.width('100%').padding({ left: 14, right: 14, top: 12, bottom: 12 }).borderRadius(16)
                .backgroundColor(courseSoftContainer(this.home.holidayColor || $r('app.color.primary'), this.dark, this.home.todayOpacity))
                .border({ width: 0.8, color: withOpacity(this.holidayAccent(), 0.35) })
            }
            ForEach(this.home.things, (item: DisplayItem) => {
              HomeTodayThing({ item: item, alpha: this.home.todayOpacity, multiAccount: this.home.multiAccount })
            }, (item: DisplayItem, index: number) => item.id + ':' + index + ':' + JSON.stringify(item))
            if (this.home.today.length > 0) {
              ForEach(this.home.today, (item: DisplayItem) => {
                ScheduleCard({ item: item, cardOpacity: this.home.todayOpacity, multiAccount: this.home.multiAccount,
                  showStatus: this.home.showStatus ?? true, onSelect: () => { this.onCourse([item]); } })
              }, (item: DisplayItem, index: number) => item.id + ':' + index + ':' + JSON.stringify(item))
            } else if (this.home.things.length > 0 || this.home.poem.length > 0) {
              Column({ space: 6 }) {
                Text(this.home.showTomorrow ? '明日暂无课程安排' : '今日暂无课程安排').fontSize(14).lineHeight(18.48)
                  .fontWeight(600).fontColor($r('app.color.text_primary'))
                Text('下拉可从云端同步最新课表').fontSize(12).lineHeight(16.56).fontColor($r('app.color.text_secondary'))
              }.width('100%').padding({ left: 16, right: 16, top: 28, bottom: 28 }).borderRadius(18)
                .backgroundColor(withOpacity($r('app.color.card'), this.home.todayOpacity))
            }
            Column() {}.height(12)
          }.width('100%').padding({ left: 16, right: 16, top: 12, bottom: 12 }).alignItems(HorizontalAlign.Start)
        }
      }.width('100%').height('100%').align(Alignment.TopStart).scrollBar(BarState.Off)
    }.width('100%').height('100%').backgroundColor(Color.Transparent).onRefreshing(this.onRefresh)
  }
}
