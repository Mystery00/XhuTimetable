import { DisplayItem } from '../model/NativeModels';
import { courseAccent, withOpacity } from './HomeStyle';
import { HomeAccountBadge, homeCourseStatus, HomeIcon, HomeStatusBadge } from './HomeWidgets';

export { createEmptyHomeState, courseAccent, courseSoftContainer, isNonCurrentCourseColor,
  readableCourseText, weekCellContainer, withOpacity } from './HomeStyle';

export function emptyDisplayItem(): DisplayItem {
  return { id: '', title: '', subtitle: '', detail: '', time: '', location: '', color: '#2E628C', status: '',
    badge: '', value: '', image: '', url: '' };
}

export function localImage(source: string): ResourceStr {
  if (source.startsWith('builtin:')) {
    const key = source.substring(8);
    const keys: string[] = ['boy1', 'boy2', 'boy3', 'boy4', 'girl1', 'girl2', 'girl3', 'girl4'];
    return $rawfile('shared/img_' + (keys.includes(key) ? key : 'boy1') + '.jpg');
  }
  if (source.startsWith('file://')) return source;
  if (source.startsWith('file:')) return 'file://' + source.substring(5);
  if (source.startsWith('/')) return 'file://' + source;
  if (source.startsWith('https://') || source.startsWith('http://')) return source;
  if (source.includes('main_bg')) return $rawfile('shared/main_bg.png');
  return $r('app.media.app_icon');
}

export function statusLabel(value: string): string {
  switch (value) {
    case 'BEFORE': return '未开始';
    case 'IN': return '进行中';
    case 'AFTER': return '已结束';
    default: return value;
  }
}

@Component
export struct NativeEmpty {
  @Prop title: string = '这里还没有内容';
  @Prop subtitle: string = '';
  @Prop image: string = 'state_no_data.png';
  @Prop action: string = '';
  onAction: () => void = () => {};

  build() {
    Column({ space: 12 }) {
      Image($rawfile('shared/' + this.image))
        .width(208).height(176).objectFit(ImageFit.Contain).accessibilityLevel('no')
      Text(this.title).fontSize(19).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
        .textAlign(TextAlign.Center)
      if (this.subtitle.length > 0) {
        Text(this.subtitle).fontSize(14).fontColor($r('app.color.text_secondary'))
          .textAlign(TextAlign.Center).lineHeight(22).constraintSize({ maxWidth: 280 })
      }
      if (this.action.length > 0) {
        Button(this.action, { type: ButtonType.Capsule }).fontSize(14)
          .fontColor($r('app.color.primary')).backgroundColor($r('app.color.primary_container'))
          .padding({ left: 24, right: 24 }).margin({ top: 6 }).onClick(this.onAction)
      }
    }.width('100%').padding({ top: 30, bottom: 32, left: 20, right: 20 })
  }
}

@Component
export struct SectionHeading {
  @Prop title: string = '';
  @Prop action: string = '';
  onAction: () => void = () => {};

  build() {
    Row() {
      Text(this.title).fontSize(14).fontWeight(FontWeight.Medium).fontColor($r('app.color.text_secondary'))
        .layoutWeight(1)
      if (this.action.length > 0) {
        Button(this.action, { type: ButtonType.Normal }).fontSize(13)
          .fontColor($r('app.color.primary')).backgroundColor(Color.Transparent)
          .height(36).onClick(this.onAction)
      }
    }.width('100%').padding({ left: 4, right: 4 }).constraintSize({ minHeight: 32 })
  }
}

// 首页与自定义界面设置的今日预览共用同一张卡片。
@Component
export struct ScheduleCard {
  @Prop item: DisplayItem = emptyDisplayItem();
  @Prop cardOpacity: number = 1;
  @Prop multiAccount: boolean = false;
  @Prop showStatus: boolean = true;
  @StorageProp('harmonyDark') private dark: boolean = false;
  @State private contentHeight: number = 0;
  onSelect: () => void = () => {};

  private accent(): ResourceColor {
    return courseAccent(this.item.color || $r('app.color.primary'), this.dark);
  }

  private cardStatus(): string {
    return this.showStatus ? homeCourseStatus(this.item.status) : '';
  }

  build() {
    Stack({ alignContent: Alignment.TopStart }) {
      Row({ space: 12 }) {
        Column({ space: 3 }) {
          HomeIcon({ name: 'watermelon', iconSize:16, tint: this.accent() })
          Text(this.item.startTime ?? '').fontSize(14).lineHeight(17.92).fontWeight(600)
            .fontColor($r('app.color.text_primary')).maxLines(1)
          Text(this.item.endTime ?? '').fontSize(11).lineHeight(13.75)
            .fontColor($r('app.color.text_secondary')).maxLines(1)
        }.width(46)
        Column() {}.width(4)
        Column({ space: 5 }) {
          Row({ space: 8 }) {
            Text(this.item.title).fontSize(16).lineHeight(21.12).fontWeight(600)
              .fontColor($r('app.color.text_primary')).layoutWeight(1).maxLines(2)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
            if (this.item.time.length > 0) {
              Text(this.item.time).fontSize(11).lineHeight(13.75).fontWeight(FontWeight.Medium)
                .fontColor($r('app.color.text_secondary')).maxLines(1)
                .padding({ left: 7, right: 7, top: 3, bottom: 3 }).borderRadius(8)
                .backgroundColor(withOpacity($r('app.color.container_highest'), 0.85))
            }
          }.width('100%').alignItems(VerticalAlign.Top)
          if (this.item.location.length > 0) {
            Row({ space: 5 }) {
              HomeIcon({ name: 'location', iconSize:14 })
              Text(this.item.location).fontSize(12).lineHeight(16.56).fontColor($r('app.color.text_secondary'))
                .layoutWeight(1).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
            }.width('100%')
          }
          Row({ space: 8 }) {
            if (this.item.subtitle.length > 0) {
              Row({ space: 5 }) {
                HomeIcon({ name: 'person', iconSize:14 })
                Text(this.item.subtitle).fontSize(12).lineHeight(16.56).fontColor($r('app.color.text_secondary'))
                  .layoutWeight(1).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
              }.layoutWeight(1)
            } else {
              Blank().layoutWeight(1)
            }
            if (this.multiAccount && this.item.badge.length > 0) {
              HomeAccountBadge({ text: this.item.badge })
            }
            if (this.cardStatus().length > 0) {
              HomeStatusBadge({ status: this.item.status })
            }
          }.width('100%')
        }.layoutWeight(1).alignItems(HorizontalAlign.Start)
      }.width('100%').padding(14).opacity(this.cardStatus() === '已结束' ? 0.72 : 1)
        .onAreaChange((_oldArea: Area, area: Area) => { this.contentHeight = Number(area.height); })
      // ArkUI 用实际布局高度实现 Compose IntrinsicSize.Min 的随内容色条。
      Column() {}.width(4).height(Math.max(0, this.contentHeight - 28)).borderRadius(4)
        .backgroundColor(this.accent()).position({ x: 72, y: 14 }).hitTestBehavior(HitTestMode.None)
        .opacity(this.cardStatus() === '已结束' ? 0.72 : 1)
    }.width('100%').borderRadius(18).backgroundColor(withOpacity($r('app.color.card'), this.cardOpacity))
      .border({ width: this.cardStatus() === '开课中' ? 1.2 : 0.5,
        color: this.cardStatus() === '开课中' ? withOpacity(this.accent(), 0.65) : withOpacity($r('app.color.outline'), 0.35) })
      .onClick(this.onSelect).accessibilityGroup(true)
      .accessibilityText(this.item.title + '，' + (this.item.startTime ?? '') + '至' + (this.item.endTime ?? '') + '，' + this.item.time + '，' + this.item.location)
  }
}

@Component
export struct CourseDetails {
  @Prop items: DisplayItem[] = [];
  @Prop title: string = '课程详情';

  build() {
    Scroll() {
      Column({ space: 16 }) {
        Text(this.title).fontSize(21).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
          .width('100%').margin({ bottom: 6 })
        ForEach(this.items, (item: DisplayItem, index: number) => {
          Column({ space: 10 }) {
            Row({ space: 10 }) {
              Column() {}.width(4).height(24).borderRadius(2).backgroundColor(item.color || $r('app.color.primary'))
              Text(item.title).fontSize(19).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
                .layoutWeight(1).copyOption(CopyOptions.InApp)
            }.width('100%')
            if (item.subtitle.length > 0) {
              Text(item.subtitle).fontSize(15).fontColor($r('app.color.text_secondary')).copyOption(CopyOptions.InApp)
            }
            if (item.time.length > 0) {
              Text('时间  ' + item.time).fontSize(15).fontColor($r('app.color.text_primary')).copyOption(CopyOptions.InApp)
            }
            if (item.location.length > 0) {
              Text('地点  ' + item.location).fontSize(15).fontColor($r('app.color.text_primary')).copyOption(CopyOptions.InApp)
            }
            if (item.detail.length > 0) {
              Text(item.detail).fontSize(15).fontColor($r('app.color.text_secondary')).lineHeight(24).copyOption(CopyOptions.InApp)
            }
            if (item.badge.length > 0) {
              Text(item.badge).fontSize(13).fontColor($r('app.color.primary'))
            }
            if (index < this.items.length - 1) {
              Divider().color($r('app.color.outline')).margin({ top: 10 })
            }
          }.width('100%').alignItems(HorizontalAlign.Start)
        }, (item: DisplayItem, index: number) => item.id + ':' + index.toString() + ':' + JSON.stringify(item))
      }.padding({ left: 24, right: 24, top: 14, bottom: 36 }).width('100%').alignItems(HorizontalAlign.Start)
    }.width('100%').height('100%').scrollBar(BarState.Off)
  }
}
