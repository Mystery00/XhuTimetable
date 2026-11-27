@Component
export struct HomeIcon {
  @Prop name: string = 'clock';
  @Prop iconSize: number = 16;
  @Prop tint: ResourceColor = $r('app.color.text_secondary');

  private source(): ResourceStr {
    if (this.name === 'watermelon') return $rawfile('shared/ic_today_course_watermelon.svg');
    if (this.name === 'clock') return $rawfile('shared/ic_action_custom_start_time.svg');
    return $rawfile('home_' + this.name + '.svg');
  }

  build() {
    Image(this.source()).width(this.iconSize).height(this.iconSize).objectFit(ImageFit.Contain)
      .fillColor(this.tint).accessibilityLevel('no')
  }
}

@Component
export struct HomeAccountBadge {
  @Prop text: string = '';

  build() {
    Text(this.text).fontSize(11).lineHeight(13.75).fontColor($r('app.color.on_secondary_container'))
      .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
      .padding({ left: 6, right: 6, top: 2, bottom: 2 }).borderRadius(6)
      .backgroundColor($r('app.color.secondary_container'))
  }
}

export function homeCourseStatus(value: string): string {
  if (value === 'BEFORE') return '未开始';
  if (value === 'IN' || value === '进行中') return '开课中';
  if (value === 'AFTER') return '已结束';
  return value;
}

@Component
export struct HomeStatusBadge {
  @Prop status: string = '';
  @StorageProp('harmonyDark') private dark: boolean = false;

  private foreground(): ResourceColor {
    if (homeCourseStatus(this.status) === '未开始') return this.dark ? '#81C784' : '#1B5E20';
    if (homeCourseStatus(this.status) === '开课中') return this.dark ? '#FFB74D' : '#E65100';
    return $r('app.color.text_secondary');
  }

  private statusContainerColor(): ResourceColor {
    if (homeCourseStatus(this.status) === '未开始') return this.dark ? '#1B3A24' : '#E3F5E8';
    if (homeCourseStatus(this.status) === '开课中') return this.dark ? '#42290A' : '#FFF0DB';
    return $r('app.color.container_highest');
  }

  build() {
    Row({ space: 4 }) {
      if (homeCourseStatus(this.status) === '开课中') {
        Circle().width(6).height(6).fill(this.foreground())
      }
      Text(homeCourseStatus(this.status)).fontSize(11).lineHeight(13.75).fontWeight(600)
        .fontColor(this.foreground()).maxLines(1)
    }.padding({ left: 8, right: 8, top: 3, bottom: 3 }).borderRadius(8).backgroundColor(this.statusContainerColor())
  }
}

@Component
export struct HomeDetailRow {
  @Prop icon: string = 'info';
  @Prop label: string = '';
  @Prop value: string = '';
  @Prop copyable: boolean = false;
  onCopy: (text: string) => void = () => {};

  build() {
    Row({ space: 12 }) {
      Stack() {
        HomeIcon({ name: this.icon, iconSize:18, tint: $r('app.color.primary') })
      }.width(36).height(36).borderRadius(10).backgroundColor($r('app.color.container_highest'))
      Column() {
        Text(this.label).fontSize(11).lineHeight(13.75).fontColor($r('app.color.text_secondary'))
        Text(this.value).fontSize(14).lineHeight(19.32).fontWeight(FontWeight.Medium)
          .fontColor($r('app.color.text_primary')).width('100%')
      }.layoutWeight(1).alignItems(HorizontalAlign.Start)
      if (this.copyable) {
        Button({ type: ButtonType.Normal }) {
          HomeIcon({ name: 'copy', iconSize:15, tint: $r('app.color.on_secondary_container') })
        }.width(32).height(32).padding(0).borderRadius(16).backgroundColor($r('app.color.secondary_container'))
          .id('home-copy-' + this.label).accessibilityText('复制' + this.label).onClick(() => { this.onCopy(this.value); })
      }
    }.width('100%').padding({ top: 4, bottom: 4 }).borderRadius(12)
      .onClick(() => { if (this.copyable) this.onCopy(this.value); })
  }
}
