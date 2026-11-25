import { DIALOG_CARD_WIDTH } from './DialogLayout';

// 个人、账号及设置页面专用控件，尺寸沿用 Android Material 3。
@Component
export struct ProfileUiIcon {
  @Prop kind: string = 'next';
  @Prop tint: ResourceColor = $r('app.color.text_secondary');
  @Prop iconSize: number = 24;

  private commands(): string {
    switch (this.kind) {
      case 'back': return 'M19 12H5M12 5L5 12L12 19';
      case 'down': return 'M6 9L12 15L18 9';
      case 'up': return 'M6 15L12 9L18 15';
      case 'close': return 'M6 6L18 18M18 6L6 18';
      case 'check': return 'M5 12L10 17L20 7';
      case 'add': return 'M12 5V19M5 12H19';
      case 'refresh': return 'M20 7V3M20 7H16M20 7A8 8 0 1 0 20 17';
      case 'logout': return 'M10 4H4V20H10M9 12H21M17 8L21 12L17 16';
      case 'lock': return 'M7 10V7A5 5 0 0 1 17 7V10M5 10H19V21H5ZM12 14V17';
      case 'account': return 'M22 12A10 10 0 1 1 2 12A10 10 0 1 1 22 12M15 8A3 3 0 1 1 9 8A3 3 0 1 1 15 8M5 19C5 12 19 12 19 19';
      case 'eye': return 'M2 12C6 3 18 3 22 12C18 21 6 21 2 12M15 12A3 3 0 1 1 9 12A3 3 0 1 1 15 12';
      case 'eyeOff': return 'M3 3L21 21M2 12C4 8 7 6 10 6M14 6C18 7 20 9 22 12L19 16M15 18C9 20 5 17 2 12';
      case 'help': return 'M22 12A10 10 0 1 1 2 12A10 10 0 1 1 22 12M9 8A3 3 0 1 1 14 10C12 11 12 12 12 13M12 16V17';
      case 'error': return 'M22 12A10 10 0 1 1 2 12A10 10 0 1 1 22 12M12 6V13M12 16V18';
      default: return 'M9 6L15 12L9 18';
    }
  }

  build() {
    Shape() {
      Path().commands(this.commands()).fill(Color.Transparent).stroke(this.tint).strokeWidth('1.8px')
        .strokeLineCap(LineCapStyle.Round).strokeLineJoin(LineJoinStyle.Round)
    }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(this.iconSize).height(this.iconSize)
      .accessibilityLevel('no')
  }
}

@Component
export struct ProfilePageHeader {
  @Prop title: string = '';
  @Prop busy: boolean = false;
  onBack: () => void = () => {};

  build() {
    Stack() {
      Text(this.title).fontSize(16).lineHeight(21.12).fontWeight(600).fontColor($r('app.color.text_primary'))
        .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis }).margin({ left: 56, right: 56 })
      Row() {
        Button({ type: ButtonType.Circle }) { ProfileUiIcon({ kind: 'back', tint: $r('app.color.text_primary') }) }
          .width(48).height(48).backgroundColor(Color.Transparent).id('feature-back')
          .accessibilityText('返回').onClick(this.onBack)
        Blank()
        if (this.busy) { LoadingProgress().width(24).height(24).color($r('app.color.primary')).margin({ right: 12 }) }
      }.width('100%')
    }.height(64).width('100%').padding({ left: 4, right: 4 }).backgroundColor($r('app.color.background'))
  }
}

export interface ProfileTemplateToken {
  label: string;
  value: string;
}

@CustomDialog
export struct ProfileTemplateDialog {
  controller?: CustomDialogController;
  title: string = '';
  initialValue: string = '';
  resetValue: string = '';
  tokens: ProfileTemplateToken[] = [];
  onConfirm: (value: string) => void = () => {};
  @State private value: string = '';
  aboutToAppear(): void { this.value = this.initialValue; }

  build() {
    Column({ space: 16 }) {
      Text(this.title).fontSize(22).fontWeight(FontWeight.Medium).fontColor($r('app.color.text_primary')).width('100%')
      TextArea({ text: this.value, placeholder: '请输入内容' }).height(110).fontSize(16).borderRadius(14)
        .fontColor($r('app.color.text_primary')).backgroundColor(Color.Transparent)
        .border({ width: 1, color: $r('app.color.outline') }).onChange((value: string) => { this.value = value; })
      Text('点击插入变量').fontSize(12).fontColor($r('app.color.text_secondary')).width('100%')
      Flex({ wrap: FlexWrap.Wrap }) {
        ForEach(this.tokens, (token: ProfileTemplateToken) => {
          Button(token.label, { type: ButtonType.Normal }).fontSize(12).height(32).margin({ right: 8, bottom: 4 })
            .backgroundColor(Color.Transparent).fontColor($r('app.color.text_primary')).borderRadius(8)
            .border({ width: 1, color: $r('app.color.outline') }).onClick(() => { this.value += token.value; })
        }, (token: ProfileTemplateToken) => token.value)
      }.width('100%')
      Row() {
        Button('重置默认').fontSize(14).backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
          .onClick(() => { this.onConfirm(this.resetValue); this.controller?.close(); })
        Blank()
        Button('取消').fontSize(14).backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
          .onClick(() => this.controller?.close())
        Button('保存').fontSize(14).backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
          .onClick(() => { this.onConfirm(this.value); this.controller?.close(); })
      }.width('100%')
    }.padding(24).width(DIALOG_CARD_WIDTH).backgroundColor($r('app.color.container_high')).borderRadius(28)
  }
}
