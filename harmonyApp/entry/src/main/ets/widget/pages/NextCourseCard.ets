const nextCourseStorage = new LocalStorage();

@Entry(nextCourseStorage)
@Component
struct NextCourseCard {
  @LocalStorageProp('weekBadge') weekBadge: string = '未登录';
  @LocalStorageProp('hasNext') hasNext: boolean = false;
  @LocalStorageProp('emptyText') emptyText: string = '打开西瓜课表\n同步课程';
  @LocalStorageProp('statusText') statusText: string = '';
  @LocalStorageProp('courseName') courseName: string = '';
  @LocalStorageProp('location') location: string = '';
  @LocalStorageProp('timeText') timeText: string = '';
  @LocalStorageProp('accentColor') accentColor: string = '#2E628C';
  @LocalStorageProp('ongoing') ongoing: boolean = false;

  private openApp(): void {
    postCardAction(this, {
      action: 'router',
      abilityName: 'EntryAbility',
      params: { 'source': 'nextCourseWidget' }
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
      // 顶栏：标题 + 周次胶囊（点击刷新）
      Row() {
        Text('下一节课')
          .fontSize(13)
          .fontWeight(FontWeight.Bold)
          .fontColor($r('app.color.text_primary'))
        Blank()
        Text(this.weekBadge)
          .fontSize(10)
          .fontWeight(FontWeight.Medium)
          .fontColor($r('app.color.on_primary_container'))
          .backgroundColor($r('app.color.primary_container'))
          .borderRadius(8)
          .padding({ left: 6, right: 6, top: 2, bottom: 2 })
      }
      .width('100%')
      .alignItems(VerticalAlign.Center)
      .onClick(() => this.refreshCard())

      if (!this.hasNext) {
        Column() {
          Text(this.emptyText)
            .fontSize(12)
            .lineHeight(18)
            .fontColor($r('app.color.text_secondary'))
            .textAlign(TextAlign.Center)
        }
        .width('100%')
        .layoutWeight(1)
        .justifyContent(FlexAlign.Center)
        .alignItems(HorizontalAlign.Center)
        .onClick(() => this.openApp())
      } else {
        Column() {
          Row({ space: 6 }) {
            Column() {}
              .width(4)
              .height(14)
              .borderRadius(2)
              .backgroundColor(this.accentColor)
            Text(this.statusText)
              .fontSize(11)
              .fontWeight(FontWeight.Bold)
              .fontColor(this.accentColor)
          }
          .width('100%')
          .margin({ top: 8 })
          .alignItems(VerticalAlign.Center)

          Text(this.courseName)
            .fontSize(15)
            .fontWeight(FontWeight.Bold)
            .fontColor($r('app.color.text_primary'))
            .maxLines(2)
            .textOverflow({ overflow: TextOverflow.Ellipsis })
            .width('100%')
            .margin({ top: 6 })

          if (this.location.length > 0) {
            Text(this.location)
              .fontSize(11)
              .fontColor($r('app.color.text_secondary'))
              .maxLines(1)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
              .width('100%')
              .margin({ top: 3 })
          }

          Blank()

          Row() {
            Text(this.timeText)
              .fontSize(11)
              .fontWeight(FontWeight.Medium)
              .fontColor($r('app.color.text_primary'))
              .maxLines(1)
              .textOverflow({ overflow: TextOverflow.Ellipsis })
          }
          .width('100%')
          .backgroundColor($r('app.color.container'))
          .borderRadius(10)
          .padding({ left: 8, right: 8, top: 5, bottom: 5 })
        }
        .width('100%')
        .layoutWeight(1)
        .alignItems(HorizontalAlign.Start)
        .onClick(() => this.openApp())
      }
    }
    .width('100%')
    .height('100%')
    .padding(14)
    .backgroundColor($r('app.color.surface'))
  }
}
