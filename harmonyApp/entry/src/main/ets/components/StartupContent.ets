import { drawing } from '@kit.ArkGraphics2D';
import { ColorMetrics } from '@kit.ArkUI';
import { DIALOG_CARD_WIDTH } from './DialogLayout';

@CustomDialog
struct StartupPrivacyDialog {
  controller?: CustomDialogController;
  @Prop error: string = '';
  onOpenPrivacy: () => void = () => {};
  onDecline: () => void = () => {};
  onAccept: () => void = () => {};

  build() {
    Stack() {
      Scroll() {
        Column() {
          Column({ space: 10 }) {
            Stack() {
              // Android Icons.Rounded.PrivacyTip 的 24 × 24 路径。
              Shape() {
                Path().commands('M4.19 4.47C3.47 4.79 3 5.51 3 6.3V11c0 5.55 3.84 10.74 9 12c5.16-1.26 9-6.45 9-12V6.3c0-.79-.47-1.51-1.19-1.83l-7-3.11c-.52-.23-1.11-.23-1.62 0Z M12 7a1 1 0 1 1 0 2a1 1 0 1 1 0-2Z M12 11a1 1 0 0 1 1 1v4a1 1 0 0 1-2 0v-4a1 1 0 0 1 1-1Z')
                  .fill($r('app.color.primary'))
              }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(24).height(24)
            }.width(28).height(28).accessibilityLevel('no')
            Text('隐私政策提示').fontSize(24).lineHeight(30.72).fontWeight(600).fontColor($r('app.color.text_primary'))
          }.width('100%').alignItems(HorizontalAlign.Start).padding({ left: 24, right: 24, top: 24, bottom: 12 })
          Column({ space: 8 }) {
            Text() {
              Span('本应用尊重并保护所有用户的个人隐私权。为了向你提供课程表同步、成绩与考试查询等服务，本应用会按照隐私政策的规定使用必要信息。请仔细阅读 ')
              Span('《隐私政策》').fontColor($r('app.color.primary')).fontWeight(600)
                .decoration({ type: TextDecorationType.Underline, color: $r('app.color.primary') })
                .onClick(() => this.onOpenPrivacy())
              Span('。')
            }.width('100%').fontSize(14).lineHeight(19.88).fontColor($r('app.color.text_secondary')).id('startup-privacy-body')
            if (this.error.length > 0) {
              Text(this.error).width('100%').fontSize(12).fontColor($r('app.color.danger'))
            }
          }.width('100%').padding({ left: 20, right: 20, top: 8, bottom: 8 })
          Row({ space: 8 }) {
            Button('不同意并退出').height(40).padding({ left: 12, right: 12 }).fontSize(14).fontWeight(500)
              .fontColor($r('app.color.primary')).backgroundColor(Color.Transparent).onClick(() => this.onDecline())
            Button('同意并继续').height(40).padding({ left: 24, right: 24 }).fontSize(14).fontWeight(500)
              .fontColor($r('app.color.on_primary')).backgroundColor($r('app.color.primary')).onClick(() => this.onAccept())
          }.width('100%').justifyContent(FlexAlign.End).padding({ left: 16, right: 20, top: 8, bottom: 16 })
        }.width('100%')
      }.width('100%').constraintSize({ maxHeight: 640 }).scrollBar(BarState.Auto)
        .backgroundColor($r('app.color.container_high')).borderRadius(28).clip(true)
        .shadow({ radius: 8, color: '#33000000', offsetY: 4 }).id('startup-privacy-card')
    }.width(DIALOG_CARD_WIDTH).padding({ top: 32, bottom: 32 })
  }
}

@Component
export struct StartupContent {
  @Prop @Watch('syncPrivacyDialog') showPrivacy: boolean = false;
  @Prop error: string = '';
  onOpenPrivacy: () => void = () => {};
  onDecline: () => void = () => {};
  onAccept: () => void = () => {};
  private dialog?: CustomDialogController;

  aboutToDisappear(): void {
    this.closePrivacyDialog();
  }

  private closePrivacyDialog(): void {
    this.dialog?.close();
    this.dialog = undefined;
  }

  private syncPrivacyDialog(): void {
    if (!this.showPrivacy) {
      this.closePrivacyDialog();
      return;
    }
    if (this.dialog !== undefined) return;
    this.dialog = new CustomDialogController({
      builder: StartupPrivacyDialog({ error: this.error, onOpenPrivacy: this.onOpenPrivacy,
        onDecline: this.onDecline, onAccept: this.onAccept }),
      alignment: DialogAlignment.Center, width: '100%', customStyle: true, isModal: true,
      maskColor: '#52000000', autoCancel: false, onWillDismiss: () => {}
    });
    this.dialog.open();
  }

  private brandTint(): drawing.ColorFilter {
    const color = ColorMetrics.resourceColor($r('app.color.text_secondary'));
    return drawing.ColorFilter.createBlendModeColorFilter({ alpha: 166, red: color.red, green: color.green,
      blue: color.blue }, drawing.BlendMode.SRC_IN);
  }

  build() {
    Stack({ alignContent: Alignment.Bottom }) {
      Column({ space: 16 }) {
        Image($rawfile('shared/ic_app_icon.svg')).width(104).height(104).objectFit(ImageFit.Cover)
          .backgroundColor('#3DDC84').borderRadius(32).shadow({ radius: 6, color: '#33000000', offsetY: 3 })
          .accessibilityLevel('no')
        Text('西瓜课表').fontSize(22).lineHeight(28.6).fontWeight(FontWeight.Bold)
          .fontColor($r('app.color.text_primary'))
        Column() {}.height(24)
        LoadingProgress().width(48).height(48).color($r('app.color.primary'))
        Text('正在准备课表数据…').fontSize(14).lineHeight(19.88).fontColor($r('app.color.text_secondary'))
      }.height('100%').justifyContent(FlexAlign.Center)
      Image($rawfile('shared/splash_image_bottom.png')).width(156).height(78).objectFit(ImageFit.Contain)
        .colorFilter(this.brandTint()).margin({ bottom: 28 }).accessibilityLevel('no').id('startup-brand')
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
      .onAppear(() => this.syncPrivacyDialog())
  }
}
