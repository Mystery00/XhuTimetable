import { DetailField, DisplayItem, PoemDetails } from '../model/NativeModels';
import { courseAccent, courseSoftContainer, withOpacity } from './AppComponents';
import { HomeAccountBadge, HomeDetailRow, HomeIcon, HomeStatusBadge } from './HomeWidgets';
import { HomeCalendarCard } from './HomeCalendar';

@Component
export struct HomeAddSheet {
  onNavigate: (route: string) => void = () => {};

  @Builder
  private action(icon: string, title: string, subtitle: string, route: string) {
    Row({ space: 14 }) {
      Stack() {
        HomeIcon({ name: icon, iconSize:22, tint: $r('app.color.on_primary_container') })
      }.width(44).height(44).borderRadius(12).backgroundColor($r('app.color.primary_container'))
      Column() {
        Text(title).fontSize(14).lineHeight(18.48).fontWeight(600).fontColor($r('app.color.text_primary'))
        Text(subtitle).fontSize(12).lineHeight(16.56).fontColor($r('app.color.text_secondary'))
      }.layoutWeight(1).alignItems(HorizontalAlign.Start)
    }.width('100%').padding(16).borderRadius(16).backgroundColor($r('app.color.card'))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.4) })
      .onClick(() => { this.onNavigate(route); }).accessibilityGroup(true).accessibilityText(title + '，' + subtitle)
  }

  build() {
    Column({ space: 10 }) {
      Text('添加日程数据').fontSize(16).lineHeight(21.12).fontWeight(FontWeight.Bold)
        .fontColor($r('app.color.text_primary')).padding({ left: 4, bottom: 4 })
      this.action('book', '自定义课程', '手动添加选修、旁听或蹭课安排到课表', 'customCourses')
      this.action('note', '自定义事项 / 倒计时', '记录考试、作业截止或重要纪念日倒计时', 'customThings')
    }.width('100%').padding({ left: 20, right: 20, bottom: 32 }).alignItems(HorizontalAlign.Start)
  }
}

@Component
export struct HomePoemSheet {
  @Prop poem: string = '';
  @Prop details: PoemDetails = { title: '', author: '', dynasty: '', lines: [], translation: [] };

  build() {
    Scroll() {
      Column() {
        if (this.details.title.length > 0) {
          Text('《' + this.details.title + '》').fontSize(22).lineHeight(29.04).fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary')).textAlign(TextAlign.Center).copyOption(CopyOptions.LocalDevice)
          Text((this.details.dynasty.length > 0 ? '[' + this.details.dynasty + '] ' : '') + this.details.author)
            .fontSize(14).lineHeight(17.92).fontColor($r('app.color.primary')).textAlign(TextAlign.Center)
            .margin({ top: 6 }).copyOption(CopyOptions.LocalDevice)
        }
        Text(this.details.lines.length > 0 ? this.details.lines.join('\n') : this.poem)
          .fontSize(16).lineHeight(22.08).fontColor($r('app.color.text_primary')).textAlign(TextAlign.Center)
          .width('100%').padding(16).borderRadius(16).backgroundColor($r('app.color.card'))
          .margin({ top: 16 }).copyOption(CopyOptions.LocalDevice)
        if (this.details.translation.length > 0) {
          Column({ space: 4 }) {
            Text('诗词大意').fontSize(12).lineHeight(15).fontWeight(600).fontColor($r('app.color.primary'))
            Text(this.details.translation.join('')).fontSize(14).lineHeight(19.32)
              .fontColor($r('app.color.text_secondary')).copyOption(CopyOptions.LocalDevice)
          }.width('100%').padding(14).borderRadius(16).backgroundColor($r('app.color.surface'))
            .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.4) })
            .margin({ top: 16 }).alignItems(HorizontalAlign.Start)
        }
      }.width('100%').padding({ left: 24, right: 24, bottom: 36 })
    }.width('100%').align(Alignment.TopStart).scrollBar(BarState.Off)
  }
}

@Component
export struct HomePracticalSheet {
  @Prop items: DisplayItem[] = [];

  @Builder
  private title() {
    Text('本学期实践课程').fontSize(16).lineHeight(21.12).fontWeight(FontWeight.Bold)
      .fontColor($r('app.color.text_primary')).padding({ left: 4, right: 4 })
  }

  build() {
    if (this.items.length === 0) {
      Column({ space: 10 }) {
        this.title()
        Text('本学期暂无实践课程安排').fontSize(14).lineHeight(19.32).fontColor($r('app.color.text_secondary'))
          .textAlign(TextAlign.Center).width('100%').padding({ top: 32, bottom: 32 })
          .borderRadius(16).backgroundColor($r('app.color.card'))
      }.width('100%').padding({ left: 16, right: 16, bottom: 32 }).alignItems(HorizontalAlign.Start)
    } else {
      Column({ space: 10 }) {
        this.title()
        Column({ space: 8 }) {
          ForEach(this.items, (item: DisplayItem) => {
            HomeCalendarCard({ item: item, practical: true })
          }, (item: DisplayItem, index: number) => item.id + ':' + index + ':' + JSON.stringify(item))
        }.width('100%')
      }.width('100%').padding({ left: 16, right: 16, bottom: 32 }).alignItems(HorizontalAlign.Start)
    }
  }
}

@Component
export struct HomeCourseSheet {
  @Prop @Watch('resetPage') items: DisplayItem[] = [];
  @Prop mode: string = 'today';
  @Prop multiAccount: boolean = false;
  @StorageProp('harmonyDark') private dark: boolean = false;
  @State private selectedIndex: number = 0;
  onCopy: (text: string) => void = () => {};
  private pager: SwiperController = new SwiperController();
  private chips: Scroller = new Scroller();

  aboutToAppear(): void {
    this.resetPage();
  }

  private resetPage(): void {
    this.selectedIndex = Math.max(0, this.items.findIndex((item: DisplayItem) => item.status !== '非本周'));
  }

  private accent(item: DisplayItem): ResourceColor {
    return courseAccent(item.color || $r('app.color.primary'), this.dark);
  }

  private fields(item: DisplayItem): DetailField[] {
    // fields 承载周次、节次、附加信息；教师、地点和独立起止时间仍来自各自字段。
    // 不拆分 detail/time 等展示字符串，也不让扩展字段覆盖掉基础详情。
    const supplied = item.fields ?? [];
    const fields: DetailField[] = [];
    const teacher: DetailField = supplied.find((field: DetailField) => field.key === 'teacher') ??
      { key: 'teacher', label: '任课教师', value: item.subtitle };
    const location: DetailField = supplied.find((field: DetailField) => field.key === 'location') ??
      { key: 'location', label: '上课地点', value: item.location };
    const periods = supplied.find((field: DetailField) => field.key === 'periods');
    const clock = item.startTime && item.endTime ? item.startTime + ' - ' + item.endTime : '';
    const timeValue = this.mode === 'today' ? item.time + (clock.length > 0 ? '  (' + clock + ')' : '') :
      (periods !== undefined ? periods.value + '  (' + item.time + ')' : item.time || clock);
    const time: DetailField = supplied.find((field: DetailField) => field.key === 'time') ??
      { key: 'time', label: '上课时间', value: timeValue };
    if (this.mode === 'today') {
      fields.push(time, location, teacher);
    } else {
      fields.push(teacher, location);
      supplied.filter((field: DetailField) => field.key === 'week' || field.key === 'weeks')
        .forEach((field: DetailField) => { fields.push(field); });
      fields.push(time);
    }
    supplied.filter((field: DetailField) => !['teacher', 'location', 'time', 'periods', 'week', 'weeks'].includes(field.key))
      .forEach((field: DetailField) => { fields.push(field); });
    return fields.filter((field: DetailField) => field.value.length > 0);
  }

  private fieldIcon(field: DetailField): string {
    if (field.key === 'teacher') return 'person';
    if (field.key === 'location') return 'location';
    if (field.key === 'time') return 'clock';
    if (field.key === 'week' || field.key === 'weeks' || field.key === 'date') return 'today';
    return 'info';
  }

  private copyable(field: DetailField): boolean {
    return field.key === 'location' || field.key.startsWith('extra') || field.label === '上课地点' || field.label === '附加信息';
  }

  @Builder
  private todayHeader(item: DisplayItem) {
    Row({ space: 12 }) {
      Column() {}.width(5).height(40).borderRadius(4).backgroundColor(this.accent(item))
      Column() {
        Text(item.title).fontSize(22).lineHeight(29.04).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
        if (this.multiAccount && item.badge.length > 0) {
          Row() {
            HomeAccountBadge({ text: item.badge })
          }.margin({ top: 4 })
        }
      }.layoutWeight(1).alignItems(HorizontalAlign.Start)
      if (item.status.length > 0) {
        HomeStatusBadge({ status: item.status })
      }
    }.width('100%')
    Divider().color(withOpacity($r('app.color.outline'), 0.45))
  }

  @Builder
  private weekHeader(item: DisplayItem) {
    Column({ space: 8 }) {
      Row({ space: 8 }) {
        Text(item.status === '非本周' ? '非本周' : '本周课程').fontSize(11).lineHeight(13.75)
          .fontWeight(600).fontColor(item.status === '非本周' ? $r('app.color.text_secondary') : Color.White)
          .padding({ left: 8, right: 8, top: 3, bottom: 3 }).borderRadius(8)
          .backgroundColor(item.status === '非本周' ? $r('app.color.container_highest') : this.accent(item))
        if (this.multiAccount && item.badge.length > 0) {
          Text(item.badge).fontSize(11).lineHeight(13.75).fontColor($r('app.color.on_secondary_container'))
            .padding({ left: 8, right: 8, top: 3, bottom: 3 }).borderRadius(8).backgroundColor($r('app.color.secondary_container'))
        }
      }.width('100%')
      Text(item.title).fontSize(22).lineHeight(29.04).fontWeight(FontWeight.Bold)
        .fontColor($r('app.color.text_primary')).width('100%')
    }.width('100%').padding(16).borderRadius(18).alignItems(HorizontalAlign.Start)
      .backgroundColor(courseSoftContainer(item.color || $r('app.color.primary'), this.dark, 0.96))
      .border({ width: 1, color: withOpacity(this.accent(item), 0.35) })
  }

  @Builder
  private coursePage(item: DisplayItem) {
    Column({ space: this.mode === 'today' ? 14 : 12 }) {
      if (this.mode === 'today') {
        this.todayHeader(item)
      } else {
        this.weekHeader(item)
      }
      Column({ space: this.mode === 'today' ? 14 : 8 }) {
        ForEach(this.fields(item), (field: DetailField, index: number) => {
          if ((field.key.startsWith('extra') || field.label === '附加信息') &&
            (index === 0 || !(this.fields(item)[index - 1].key.startsWith('extra') || this.fields(item)[index - 1].label === '附加信息'))) {
            Divider().color(withOpacity($r('app.color.outline'), 0.45))
          }
          HomeDetailRow({ icon: this.fieldIcon(field), label: field.label, value: field.value,
            copyable: this.copyable(field), onCopy: this.onCopy })
        }, (field: DetailField, index: number) => field.key + ':' + index)
      }.width('100%')
    }.width('100%').padding({ left: 20, right: 20, bottom: this.mode === 'today' ? 32 : 28 })
  }

  build() {
    Column({ space: 6 }) {
      if (this.items.length > 1) {
        List({ space: 8, scroller: this.chips }) {
          ForEach(this.items, (item: DisplayItem, index: number) => {
            ListItem() {
              Button({ type: ButtonType.Normal }) {
                Text((index + 1) + '. ' + item.title).fontSize(14).lineHeight(17.92).maxLines(1)
                  .textOverflow({ overflow: TextOverflow.Ellipsis })
                  .fontColor(index === this.selectedIndex ? $r('app.color.on_secondary_container') : $r('app.color.text_secondary'))
              }.height(32).constraintSize({ maxWidth: 240 }).padding({ left: 12, right: 12 }).borderRadius(8)
                .backgroundColor(index === this.selectedIndex ? $r('app.color.secondary_container') : Color.Transparent)
                .border({ width: index === this.selectedIndex ? 0 : 1, color: $r('app.color.outline') })
                .onClick(() => { this.pager.changeIndex(index, true); })
                .accessibilityText(item.title + (index === this.selectedIndex ? '，已选中' : ''))
            }
          }, (item: DisplayItem, index: number) => item.id + ':' + index + ':' + JSON.stringify(item))
        }.listDirection(Axis.Horizontal).height(40).width('100%').padding({ left: 20, right: 20 })
          .scrollBar(BarState.Off)
        Swiper(this.pager) {
          ForEach(this.items, (item: DisplayItem) => {
            this.coursePage(item)
          }, (item: DisplayItem, index: number) => item.id + ':' + index + ':' + JSON.stringify(item))
        }.index(this.selectedIndex).indicator(false).loop(false).duration(240).width('100%')
          .onChange((index: number) => {
            this.selectedIndex = index;
            this.chips.scrollToIndex(index, true, ScrollAlign.CENTER);
          })
      } else if (this.items.length === 1) {
        this.coursePage(this.items[0])
      }
    }.width('100%')
  }
}
