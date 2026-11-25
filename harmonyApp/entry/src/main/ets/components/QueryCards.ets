import { DetailField, DisplayItem } from '../model/NativeModels';
import { detailValue } from './FeatureContracts';
import { dialogWidth } from './DialogLayout';
import { FeatureIcon } from './FeatureControls';
import { courseAccent, emptyDisplayItem, withOpacity } from './AppComponents';

@Component
export struct ScoreSummary {
  @Prop metrics: DetailField[] = [];

  private value(key: string): string {
    return this.metrics.find((field: DetailField) => field.key === key)?.value || '—';
  }

  @Builder
  private metric(label: string, key: string) {
    Column({ space: 2 }) {
      Text(label).fontSize(11).fontColor($r('app.color.on_primary_container')).opacity(0.75)
      Text(this.value(key)).fontSize(16).fontWeight(FontWeight.Bold).fontColor($r('app.color.on_primary_container'))
    }.layoutWeight(1).alignItems(HorizontalAlign.Start)
  }

  build() {
    Column({ space: 14 }) {
      Row() {
        Column() {
          Text('学期平均绩点 (GPA)').fontSize(14).fontColor($r('app.color.on_primary_container')).opacity(0.8)
          Text(this.value('gpa')).fontSize(36).fontWeight(FontWeight.Bold).fontColor($r('app.color.on_primary_container'))
        }.layoutWeight(1).alignItems(HorizontalAlign.Start)
        Button({ type: ButtonType.Circle }) { FeatureIcon({ name: 'info', tint: $r('app.color.on_primary_container') }) }
          .width(40).height(40).backgroundColor(Color.Transparent).accessibilityText('GPA 计算说明')
          .onClick(() => this.getUIContext().showAlertDialog({
            width: dialogWidth(this.getUIContext()), alignment: DialogAlignment.Center,
            title: 'GPA 计算说明',
            message: '当前展示的 GPA 为服务端根据课程学分与绩点自动计算所得，并非教务系统官方直接返回的数值。如发现计算结果与教务系统存在差异，欢迎通过意见反馈告知我们。',
            confirm: { value: '知道了', action: () => {} }
          }))
      }.width('100%')
      Divider().color($r('app.color.on_primary_container')).opacity(0.14)
      Row() {
        this.metric('平均成绩', 'averageScore')
        this.metric('总学分', 'totalCredit')
        this.metric('总成绩', 'totalScore')
      }.width('100%')
    }.padding(18).width('100%').borderRadius(22).backgroundColor(withOpacity($r('app.color.primary_container'), 0.55))
      .border({ width: 0.5, color: withOpacity($r('app.color.primary'), 0.25) }).margin({ top: 8, bottom: 8 })
  }
}

@Component
export struct ScoreBadge {
  @Prop value: string = '';
  @StorageProp('harmonyDark') private dark: boolean = false;

  private foreground(): string {
    const score = Number(this.value);
    if (score < 60) return this.dark ? '#FF8A80' : '#D32F2F';
    if (score >= 90) return this.dark ? '#69F0AE' : '#1B7F46';
    if (score >= 75) return this.dark ? '#80CBC4' : '#00796B';
    return this.dark ? '#FFCC80' : '#E65100';
  }

  private badgeBackground(): string {
    const score = Number(this.value);
    if (score < 60) return this.dark ? '#4A1B1B' : '#FFEBEE';
    if (score >= 90) return this.dark ? '#153826' : '#E3F6EB';
    if (score >= 75) return this.dark ? '#133330' : '#E0F2F1';
    return this.dark ? '#3E2712' : '#FFF3E0';
  }

  build() {
    Text(this.value).fontSize(16).fontWeight(FontWeight.Bold).fontColor(this.foreground())
      .padding({ left: 12, right: 12, top: 6, bottom: 6 }).backgroundColor(this.badgeBackground()).borderRadius(12)
  }
}

@Component
export struct ScoreTag {
  @Prop label: string = '';
  build() {
    Text(this.label).fontSize(12).fontColor($r('app.color.text_secondary'))
      .padding({ left: 9, right: 9, top: 4, bottom: 4 }).backgroundColor(withOpacity($r('app.color.container_highest'), 0.8)).borderRadius(8)
  }
}

@Component
export struct ScoreCard {
  @Prop item: DisplayItem = emptyDisplayItem();
  @Prop @Watch('resetExpanded') expandAll: boolean = true;
  @State private expanded: boolean = true;

  aboutToAppear(): void { this.expanded = this.expandAll; }
  private resetExpanded(): void { this.expanded = this.expandAll; }

  build() {
    Column() {
      Row({ space: 12 }) {
        Column({ space: 4 }) {
          Text(this.item.title).fontSize(16).fontWeight(600).fontColor($r('app.color.text_primary'))
            .maxLines(2).textOverflow({ overflow: TextOverflow.Ellipsis })
          Text([detailValue(this.item, 'courseType'), detailValue(this.item, 'scoreType')].filter((s: string) => s.length > 0).join(' · '))
            .fontSize(12).fontColor($r('app.color.text_secondary'))
        }.layoutWeight(1).alignItems(HorizontalAlign.Start)
        ScoreBadge({ value: this.item.value })
      }.width('100%')
      if (this.expanded) {
        Column({ space: 8 }) {
          Divider().color($r('app.color.outline')).opacity(0.4)
          Flex({ wrap: FlexWrap.Wrap }) {
            ScoreTag({ label: '学分 ' + detailValue(this.item, 'credit', '—') }).margin({ right: 8, bottom: 6 })
            ScoreTag({ label: '绩点 ' + detailValue(this.item, 'gpa', '—') }).margin({ right: 8, bottom: 6 })
            ScoreTag({ label: '学分绩点 ' + detailValue(this.item, 'creditGpa', '—') }).margin({ bottom: 6 })
          }.width('100%')
          if (detailValue(this.item, 'scoreDescription').length > 0) {
            Text('成绩说明：' + detailValue(this.item, 'scoreDescription')).fontSize(12).fontColor($r('app.color.text_secondary')).width('100%')
          }
        }.width('100%').margin({ top: 10 }).transition(TransitionEffect.OPACITY)
      }
    }.padding(16).width('100%').borderRadius(18).backgroundColor($r('app.color.card'))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.4) }).margin({ top: 5, bottom: 5 })
      .onClick(() => this.getUIContext().animateTo({ duration: 240, curve: Curve.FastOutSlowIn }, () => { this.expanded = !this.expanded; }))
  }
}

@Component
export struct ExperimentCard {
  @Prop item: DisplayItem = emptyDisplayItem();
  build() {
    Column({ space: 10 }) {
      Row({ space: 12 }) {
        Text(this.item.title).fontSize(16).fontWeight(600).fontColor($r('app.color.text_primary'))
          .maxLines(2).textOverflow({ overflow: TextOverflow.Ellipsis }).layoutWeight(1)
        ScoreBadge({ value: this.item.value })
      }.width('100%')
      Flex({ wrap: FlexWrap.Wrap }) {
        if (detailValue(this.item, 'mustTest').length > 0) {
          ScoreTag({ label: detailValue(this.item, 'mustTest') }).margin({ right: 8, bottom: 6 })
        }
        ScoreTag({ label: '实验学分 ' + detailValue(this.item, 'credit', '—') }).margin({ bottom: 6 })
      }.width('100%')
      if (detailValue(this.item, 'scoreDescription').length > 0) {
        Text('成绩说明：' + detailValue(this.item, 'scoreDescription')).fontSize(12).fontColor($r('app.color.text_secondary')).width('100%')
      }
    }.padding(16).width('100%').borderRadius(18).backgroundColor($r('app.color.card'))
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.4) }).margin({ top: 5, bottom: 5 })
  }
}

@Component
export struct ExamCard {
  @Prop item: DisplayItem = emptyDisplayItem();
  onCopy: (text: string) => void = () => {};
  @StorageProp('harmonyDark') private dark: boolean = false;
  @State private contentHeight: number = 72;

  private accent(): ResourceColor {
    if (this.finished()) return $r('app.color.text_secondary');
    if (this.today()) return this.dark ? '#FFB74D' : '#E65100';
    return courseAccent(this.item.color || $r('app.color.primary'), this.dark);
  }
  private badgeBackground(): ResourceColor {
    if (this.finished()) return $r('app.color.container_highest');
    if (this.today()) return this.dark ? '#42290A' : '#FFF0DB';
    return withOpacity(this.accent(), 0.13);
  }

  private finished(): boolean { return detailValue(this.item, 'status') === 'AFTER'; }
  private today(): boolean { return detailValue(this.item, 'status') === 'IN'; }

  @Builder
  private meta(icon: string, text: string) {
    Row({ space: 5 }) {
      Image($rawfile('shared/' + icon + '.svg')).width(14).height(14).fillColor($r('app.color.text_secondary'))
      Text(text).fontSize(12).fontColor($r('app.color.text_secondary')).maxLines(2).textOverflow({ overflow: TextOverflow.Ellipsis }).layoutWeight(1)
    }.width('100%')
  }

  build() {
    Row({ space: 12 }) {
      Text(detailValue(this.item, 'showText', this.item.badge)).fontSize(14).fontWeight(FontWeight.Bold)
        .fontColor(this.accent()).textAlign(TextAlign.Center).width(62).height(Math.max(62, this.contentHeight))
        .padding({ left: 4, right: 4, top: 10, bottom: 10 }).borderRadius(14).backgroundColor(this.badgeBackground())
      Column({ space: 5 }) {
        Row({ space: 8 }) {
          Text(this.item.title).fontSize(16).fontWeight(600).fontColor($r('app.color.text_primary'))
            .maxLines(2).textOverflow({ overflow: TextOverflow.Ellipsis }).layoutWeight(1)
          if (detailValue(this.item, 'seatNo').length > 0) {
            Text(detailValue(this.item, 'seatNo') + '座').fontSize(11).fontWeight(FontWeight.Bold)
              .fontColor(this.finished() ? $r('app.color.text_secondary') : $r('app.color.on_primary_container'))
              .padding({ left: 8, right: 8, top: 4, bottom: 4 }).borderRadius(10)
              .backgroundColor(this.finished() ? $r('app.color.container_highest') : $r('app.color.primary_container'))
          }
        }.width('100%').alignItems(VerticalAlign.Top)
        this.meta('ic_action_notify_time', detailValue(this.item, 'dateString') + '  ' + detailValue(this.item, 'time'))
        this.meta('ic_classroom', this.item.location)
        if (detailValue(this.item, 'examName').length > 0) { this.meta('ic_exam', detailValue(this.item, 'examName')) }
      }.layoutWeight(1).alignItems(HorizontalAlign.Start).onAreaChange((_old: Area, area: Area) => { this.contentHeight = Number(area.height); })
    }.padding(14).width('100%').borderRadius(18).backgroundColor($r('app.color.card'))
      .border({ width: this.today() ? 1.2 : 0.5, color: this.today() ? withOpacity(this.accent(), 0.7) : withOpacity($r('app.color.outline'), 0.4) })
      .opacity(this.finished() ? 0.68 : 1).margin({ top: 6, bottom: 6 })
      .onClick(() => this.onCopy(this.item.title + ' | ' + detailValue(this.item, 'dateString') + ' ' +
        detailValue(this.item, 'time') + ' | ' + this.item.location + ' 座位号 ' + detailValue(this.item, 'seatNo')))
  }
}
