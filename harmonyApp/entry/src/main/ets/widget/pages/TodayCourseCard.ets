interface TodayCardRow {
  id: string;
  title: string;
  subtitle: string;
  timeText: string;
  accentColor: string;
  ongoing: boolean;
  isThing: boolean;
}

const todayCourseStorage = new LocalStorage();

@Entry(todayCourseStorage)
@Component
struct TodayCourseCard {
  @LocalStorageProp('weekBadge') weekBadge: string = '未登录';
  @LocalStorageProp('dateLabel') dateLabel: string = '';
  @LocalStorageProp('emptyText') emptyText: string = '打开西瓜课表同步课程';
  @LocalStorageProp('items') items: TodayCardRow[] = [];
  @LocalStorageProp('isLarge') isLarge: boolean = false;

  private openApp(): void {
    postCardAction(this, {
      action: 'router',
      abilityName: 'EntryAbility',
      params: { 'source': 'todayCourseWidget' }
    });
  }

  private refreshCard(): void {
    postCardAction(this, {
      action: 'message',
      params: { 'action': 'refresh' }
    });
  }

  @Builder
  private courseRow(item: TodayCardRow) {
    Row({ space: 9 }) {
      Column() {}
        .width(4)
        .height(28)
        .borderRadius(2)
        .backgroundColor(item.accentColor)

      Column({ space: 2 }) {
        Text(item.title)
          .fontSize(13)
          .fontWeight(FontWeight.Bold)
          .fontColor($r('app.color.text_primary'))
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
          .width('100%')
        Text(item.subtitle)
          .fontSize(11)
          .fontColor(item.ongoing ? item.accentColor : $r('app.color.text_secondary'))
          .maxLines(1)
          .textOverflow({ overflow: TextOverflow.Ellipsis })
          .width('100%')
      }
      .layoutWeight(1)
      .alignItems(HorizontalAlign.Start)

      Text(item.timeText)
        .fontSize(11)
        .fontWeight(item.isThing || item.ongoing ? FontWeight.Medium : FontWeight.Normal)
        .fontColor(item.isThing ? item.accentColor : $r('app.color.text_secondary'))
        .textAlign(TextAlign.End)
        .maxLines(1)
    }
    .width('100%')
    .padding({ left: 10, right: 10, top: 7, bottom: 7 })
    .borderRadius(12)
    .backgroundColor($r('app.color.container'))
    .alignItems(VerticalAlign.Center)
    .onClick(() => this.openApp())
  }

  build() {
    Column() {
      // 顶部标题栏：左击刷新，右击打开主应用
      Row() {
        Column({ space: 2 }) {
          Row({ space: 6 }) {
            Text('今日课程')
              .fontSize(15)
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
          if (this.dateLabel.length > 0) {
            Text(this.dateLabel)
              .fontSize(11)
              .fontColor($r('app.color.text_secondary'))
          }
        }
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Start)
        .onClick(() => this.refreshCard())

        Text('打开 ›')
          .fontSize(12)
          .fontWeight(FontWeight.Medium)
          .fontColor($r('app.color.primary'))
          .padding({ left: 6, right: 2, top: 4, bottom: 4 })
          .onClick(() => this.openApp())
      }
      .width('100%')
      .alignItems(VerticalAlign.Center)

      if (this.items.length === 0) {
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
        Column({ space: 6 }) {
          ForEach(this.items, (item: TodayCardRow) => {
            this.courseRow(item)
          }, (item: TodayCardRow) => item.id)
        }
        .width('100%')
        .layoutWeight(1)
        .margin({ top: 8 })
        .justifyContent(FlexAlign.Start)
      }
    }
    .width('100%')
    .height('100%')
    .padding(14)
    .backgroundColor($r('app.color.surface'))
  }
}
