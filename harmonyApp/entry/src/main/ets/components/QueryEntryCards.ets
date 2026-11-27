import { iconTint } from './IconTint';
import { DisplayItem, ItemAction } from '../model/NativeModels';
import { detailValue } from './FeatureContracts';
import { courseAccent, emptyDisplayItem, withOpacity } from './AppComponents';

@Component
export struct EntryMeta {
  @Prop icon: string = '';
  @Prop text: string = '';
  build() {
    Row({ space: 8 }) {
      Image($rawfile('shared/' + this.icon + '.svg')).width(15).height(15).colorFilter(iconTint($r('app.color.primary')))
      Text(this.text).fontSize(12).fontColor($r('app.color.text_primary')).layoutWeight(1)
        .maxLines(3).textOverflow({ overflow: TextOverflow.Ellipsis })
    }.width('100%')
  }
}

@Component
export struct CustomEntryCard {
  @Prop item: DisplayItem = emptyDisplayItem();
  @Prop course: boolean = true;
  onEdit: () => void = () => {};
  @State private contentHeight: number = 0;

  private badge(): string {
    return this.course ? '每' + detailValue(this.item, 'day') + ' · ' + detailValue(this.item, 'periods') : this.item.badge;
  }

  private countdownDays(): number {
    const target = new Date(detailValue(this.item, 'startDate') + 'T00:00:00');
    const now = new Date(); now.setHours(0, 0, 0, 0);
    return Math.round((target.getTime() - now.getTime()) / 86400000);
  }

  private badgeContainer(): ResourceColor {
    if (this.course) return $r('app.color.primary_container');
    if (detailValue(this.item, 'countdown') === 'true') {
      if (this.countdownDays() > 0) return $r('app.color.tertiary_container');
      if (this.countdownDays() === 0) return $r('app.color.primary_container');
      return $r('app.color.container_highest');
    }
    return detailValue(this.item, 'allDay') === 'true' ? $r('app.color.secondary_container') : $r('app.color.container_highest');
  }

  private badgeColor(): ResourceColor {
    if (this.course) return $r('app.color.on_primary_container');
    if (detailValue(this.item, 'countdown') === 'true') {
      if (this.countdownDays() > 0) return $r('app.color.on_tertiary_container');
      if (this.countdownDays() === 0) return $r('app.color.on_primary_container');
      return $r('app.color.text_secondary');
    }
    return detailValue(this.item, 'allDay') === 'true' ? $r('app.color.on_secondary_container') : $r('app.color.text_secondary');
  }

  build() {
    Row({ space: 12 }) {
      Column().width(4).height(this.contentHeight).backgroundColor(this.item.color || $r('app.color.primary')).borderRadius(2)
      Column({ space: 10 }) {
        Row({ space: 8 }) {
          Text(this.item.title).fontSize(16).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
            .maxLines(2).textOverflow({ overflow: TextOverflow.Ellipsis }).layoutWeight(1)
          Text(this.badge()).fontSize(12).fontWeight(600).fontColor(this.badgeColor())
            .padding({ left: 9, right: 9, top: 4, bottom: 4 }).backgroundColor(this.badgeContainer()).borderRadius(10)
        }.width('100%')
        Column({ space: 6 }) {
          if (this.course) {
            EntryMeta({ icon: 'ic_calendar', text: '第' + detailValue(this.item, 'weeks') })
          } else {
            EntryMeta({ icon: 'ic_action_notify_time', text: this.item.time })
          }
          if (this.item.location.length > 0) { EntryMeta({ icon: 'ic_classroom', text: this.item.location }) }
          if (this.course && detailValue(this.item, 'teacher').length > 0) {
            EntryMeta({ icon: 'ic_profile', text: detailValue(this.item, 'teacher') })
          }
          if (!this.course && detailValue(this.item, 'remark').length > 0) {
            EntryMeta({ icon: 'ic_notice', text: detailValue(this.item, 'remark') })
          }
        }.padding({ left: 12, right: 12, top: 9, bottom: 9 }).width('100%')
          .backgroundColor($r('app.color.card')).borderRadius(12)
        Row() {
          Text('添加于 ' + detailValue(this.item, 'createTime')).fontSize(11).fontColor($r('app.color.text_secondary')).opacity(0.72).layoutWeight(1)
          Text('编辑 ›').fontSize(11).fontColor($r('app.color.primary'))
        }.width('100%')
      }.layoutWeight(1).onAreaChange((_old: Area, area: Area) => { this.contentHeight = Number(area.height); })
    }.width('100%').alignItems(VerticalAlign.Center)
      .padding({ left: 12, right: 16, top: 14, bottom: 14 }).borderRadius(20).backgroundColor($r('app.color.card'))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.45) }).margin({ top: 6, bottom: 6 }).onClick(() => this.onEdit())
  }
}

@Component
export struct SchoolCourseCard {
  @Prop item: DisplayItem = emptyDisplayItem();
  @Prop busy: boolean = false;
  onImport: () => void = () => {};
  @State private contentHeight: number = 0;

  build() {
    Row({ space: 12 }) {
      Column().width(4).height(this.contentHeight).backgroundColor(this.item.color || $r('app.color.primary')).borderRadius(2)
      Column({ space: 10 }) {
        Row({ space: 8 }) {
          Text(this.item.title).fontSize(16).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
            .maxLines(2).textOverflow({ overflow: TextOverflow.Ellipsis }).layoutWeight(1)
          Text(detailValue(this.item, 'teacher') || '未知教师').fontSize(12).fontWeight(FontWeight.Medium)
            .fontColor($r('app.color.on_secondary_container')).padding({ left: 9, right: 9, top: 4, bottom: 4 })
            .backgroundColor($r('app.color.secondary_container')).borderRadius(10).constraintSize({ maxWidth: 120 })
            .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
        }.width('100%')
        Column({ space: 6 }) {
          EntryMeta({ icon: 'ic_action_notify_time', text: detailValue(this.item, 'time', this.item.time) || '暂无时间数据' })
          EntryMeta({ icon: 'ic_classroom', text: detailValue(this.item, 'location', this.item.location) || '暂无地点数据' })
        }.padding({ left: 12, right: 12, top: 9, bottom: 9 }).width('100%').backgroundColor($r('app.color.card')).borderRadius(12)
        if (Number(detailValue(this.item, 'slotCount', '0')) > 0) {
          Row({ space: 8 }) {
            Text('共 ' + detailValue(this.item, 'slotCount') + ' 个上课时段').fontSize(11).fontColor($r('app.color.text_secondary')).layoutWeight(1)
            Button('＋ 蹭课 · 加入课表').fontSize(12).fontWeight(600).height(34).borderRadius(12)
              .fontColor($r('app.color.on_secondary_container')).backgroundColor($r('app.color.secondary_container'))
              .enabled(!this.busy).onClick(() => this.onImport())
          }.width('100%')
        }
      }.layoutWeight(1).onAreaChange((_old: Area, area: Area) => { this.contentHeight = Number(area.height); })
    }.width('100%').padding({ left: 12, right: 16, top: 14, bottom: 14 })
      .borderRadius(20).backgroundColor($r('app.color.card')).border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.45) })
      .margin({ top: 6, bottom: 6 })
  }
}

@Component
export struct ClassroomCard {
  @Prop item: DisplayItem = emptyDisplayItem();
  @StorageProp('harmonyDark') private dark: boolean = false;
  private accent(): ResourceColor { return courseAccent(this.item.color || $r('app.color.primary'), this.dark); }
  build() {
    Column({ space: 10 }) {
      Row({ space: 8 }) {
        Text(this.item.title || detailValue(this.item, 'roomNo')).fontSize(16).fontWeight(600).fontColor($r('app.color.text_primary'))
          .layoutWeight(1).maxLines(2).textOverflow({ overflow: TextOverflow.Ellipsis })
        Text(detailValue(this.item, 'roomNo')).fontSize(12).fontWeight(600).fontColor(this.accent())
          .padding({ left: 8, right: 8, top: 4, bottom: 4 }).backgroundColor(withOpacity(this.accent(), 0.14)).borderRadius(10)
      }.width('100%')
      Divider().color($r('app.color.outline')).opacity(0.45)
      if (detailValue(this.item, 'campus').length > 0) { EntryMeta({ icon: 'ic_user_campus', text: '校区：' + detailValue(this.item, 'campus') }) }
      if (detailValue(this.item, 'roomType').length > 0) { EntryMeta({ icon: 'ic_classroom', text: '场地类型：' + detailValue(this.item, 'roomType') }) }
      if (detailValue(this.item, 'roomRemark').length > 0) {
        Text('备注：' + detailValue(this.item, 'roomRemark')).fontSize(12).fontColor($r('app.color.text_secondary'))
          .padding(10).width('100%').backgroundColor($r('app.color.container_highest')).borderRadius(12)
      }
    }.width('100%').padding(16).borderRadius(18).backgroundColor($r('app.color.card'))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.45) }).margin({ top: 6, bottom: 6 })
  }
}

interface NoticeToken {
  text: string;
  style: string;
}

@Component
export struct NoticeCard {
  @Prop item: DisplayItem = emptyDisplayItem();
  onAction: (action: ItemAction) => void = () => {};

  private tokens(): NoticeToken[] {
    const parts = this.item.detail.split(/(`[^`]+`|\*.+?\*|_.+?_|~.+?~)/g);
    return parts.map((part: string): NoticeToken => {
      const first = part.charAt(0);
      const styled = ['`', '*', '_', '~'].includes(first) && part.length > 1 && part.endsWith(first);
      return { text: styled ? part.slice(1, -1) : part, style: styled ? first : '' };
    });
  }

  build() {
    Column({ space: 10 }) {
      Column({ space: 4 }) {
        Text(this.item.title).fontSize(16).fontWeight(600).fontColor($r('app.color.text_primary')).width('100%')
        Text('发布于 ' + this.item.time).fontSize(11).fontColor($r('app.color.text_secondary')).width('100%')
      }.width('100%')
      Divider().color($r('app.color.outline')).opacity(0.45)
      Text() {
        ForEach(this.tokens(), (token: NoticeToken) => {
          Span(token.text).fontWeight(token.style === '*' ? FontWeight.Bold : FontWeight.Normal)
            .fontStyle(token.style === '_' ? FontStyle.Italic : FontStyle.Normal)
            .decoration({ type: token.style === '~' ? TextDecorationType.LineThrough : TextDecorationType.None })
            .fontFamily(token.style === '`' ? 'monospace' : 'sans-serif')
            .fontColor(token.style === '`' ? $r('app.color.on_primary_container') : $r('app.color.text_primary'))
        })
      }.fontSize(14).width('100%').copyOption(CopyOptions.LocalDevice)
      if ((this.item.actions || []).length > 0) {
        Divider().color($r('app.color.outline')).opacity(0.45)
        Column({ space: 8 }) {
          ForEach(this.item.actions || [], (action: ItemAction) => {
            Row({ space: 8 }) {
              Text(action.label).fontSize(14).fontWeight(FontWeight.Medium).fontColor($r('app.color.primary')).layoutWeight(1)
              Button({ type: ButtonType.Normal }) {
                Row({ space: 4 }) {
                  if (action.type === 'COPY') {
                    Image($rawfile('home_copy.svg')).width(16).height(16).colorFilter(iconTint($r('app.color.on_secondary_container')))
                  } else {
                    Shape() {
                      Path().commands('M19 4H5c-1.1 0-2 .9-2 2v12c0 1.1.9 2 2 2h4v-2H5V8h14v10h-4v2h4c1.1 0 2-.9 2-2V6c0-1.1-.9-2-2-2zM12 10l-4 4h3v6h2v-6h3z').fill($r('app.color.on_secondary_container'))
                    }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(16).height(16)
                  }
                  Text(action.type === 'COPY' ? '复制' : '访问').fontSize(14).fontColor($r('app.color.on_secondary_container'))
                }
              }.height(40).padding({ left: 16, right: 16 }).borderRadius(10).backgroundColor($r('app.color.secondary_container'))
                .onClick(() => this.onAction(action))
            }.width('100%')
          }, (action: ItemAction, index: number) => action.type + action.value + ':' + index)
        }.width('100%')
      }
    }.padding(16).width('100%').borderRadius(18).backgroundColor($r('app.color.card'))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.45) }).margin({ top: 6, bottom: 6 })
  }
}
