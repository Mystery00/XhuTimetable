export function bottomSheetOptions(onWillDismiss?: (action: DismissSheetAction) => void): SheetOptions {
  return {
    height: SheetSize.FIT_CONTENT,
    preferType: SheetType.BOTTOM,
    dragBar: false,
    showClose: false,
    radius: { topLeft: 28, topRight: 28, bottomLeft: 0, bottomRight: 0 },
    maskColor: '#52000000',
    backgroundColor: $r('app.color.card'),
    keyboardAvoidMode: SheetKeyboardAvoidMode.TRANSLATE_AND_RESIZE,
    onWillDismiss: onWillDismiss
  };
}

@Component
export struct SheetSurface {
  @BuilderParam content: () => void;
  @StorageProp('harmonyNavigationInset') private navigationInset: number = 0;

  build() {
    Column() {
      Stack() {
        Column() {}.width(32).height(4).borderRadius(2).backgroundColor($r('app.color.text_secondary'))
      }.width('100%').height(48).id('timetable-sheet-handle')
      this.content()
    }.width('100%').padding({ bottom: this.navigationInset }).id('timetable-sheet')
  }
}
