import { iconTint } from './IconTint';
import { SelectOption } from '../model/NativeModels';
import { DIALOG_CARD_WIDTH } from './DialogLayout';
import { readableCourseText, withOpacity } from './AppComponents';

export function featureTitle(route: string): string {
  switch (route) {
    case 'scores': return '成绩查询';
    case 'exams': return '考试查询';
    case 'expScores': return '实验成绩查询';
    case 'rooms': return '空闲教室查询';
    case 'notices': return '通知公告';
    case 'customCourses': return '自定义课程';
    case 'customThings': return '自定义事项';
    case 'courseColors': return '自定义课程颜色';
    case 'schoolCalendar': return '校历';
    case 'timetable': return '全校课表查询';
    case 'backgrounds': return '自定义背景图';
    case 'feedback': return '意见反馈';
    case 'urge': return '催更与建议';
    case 'settings': return '设置';
    case 'classSettings': return '课表设置';
    case 'customUi': return '界面设置';
    case 'accounts': return '账号管理';
    case 'about': return '关于西瓜课表';
    case 'licenses': return '开源许可';
    default: return '页面';
  }
}

export function featureError(error: Error): string {
  return error.message || '操作失败，请稍后重试';
}

export function dateText(value: Date): string {
  return value.getFullYear().toString() + '-' + (value.getMonth() + 1).toString().padStart(2, '0') +
    '-' + value.getDate().toString().padStart(2, '0');
}

@Component
export struct FeatureIcon {
  @Prop name: string = '';
  @Prop tint: ResourceColor = $r('app.color.text_primary');

  @Builder
  private vectorPath(commands: string, stroke: number) {
    Shape() {
      Path().commands(commands).fill(Color.Transparent).stroke(this.tint).strokeWidth(stroke.toString() + 'px')
        .strokeLineCap(LineCapStyle.Round).strokeLineJoin(LineJoinStyle.Round)
    }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(24).height(24)
  }

  build() {
    Stack() {
      if (this.name === 'filter') {
        this.vectorPath('M3 6 H21 M6 12 H18 M10 18 H14', 2)
      } else if (this.name === 'refresh' || this.name === 'restore') {
        this.vectorPath('M20 11 A8 8 0 1 0 18 18 M20 4 V11 H13', 2)
      } else if (this.name === 'down') {
        this.vectorPath('M7 10 L12 15 L17 10', 2)
      } else if (this.name === 'notifications-active') {
        Shape() {
          Path().commands('M12 22a2 2 0 0 0 2-2h-4a2 2 0 0 0 2 2z M18 15v-4a6 6 0 0 0-4.5-5.8V4.5a1.5 1.5 0 0 0-3 0v0.7A6 6 0 0 0 6 11v4l-2 2v1h16v-1z M4 4l1.4 1.4A9.7 9.7 0 0 0 2.5 12H0.5A12 12 0 0 1 4 4z M20 4a12 12 0 0 1 3.5 8h-2a9.7 9.7 0 0 0-2.9-6.6z').fill(this.tint)
        }.viewPort({ x: '0px', y: '0px', width: '24px', height: '24px' }).width(22).height(22)
      } else if (this.name === 'info') {
        Circle().width(19).height(19).fill(Color.Transparent).stroke(this.tint).strokeWidth(1.8)
        Text('i').fontSize(15).fontWeight(FontWeight.Bold).fontColor(this.tint)
      } else if (this.name === 'delete') {
        this.vectorPath('M4 6 H20 M9 6 V3 H15 V6 M6 6 L7 21 H17 L18 6 M10 10 V17 M14 10 V17', 1.8)
      } else {
        Image($rawfile('shared/' + this.name + '.svg')).width(22).height(22).colorFilter(iconTint(this.tint))
      }
    }.width(24).height(24)
  }
}

@Component
export struct FeatureHeader {
  @Prop title: string = '';
  @Prop action: string = '';
  @Prop actionIcon: string = '';
  @Prop busy: boolean = false;
  @Prop actionEnabled: boolean = true;
  @Prop selectableTitle: boolean = false;
  @Prop subtitle: string = '';
  @Prop subtitleColor: ResourceColor = $r('app.color.primary');
  @Prop titleActionEnabled: boolean = false;
  onBack: () => void = () => {};
  onAction: () => void = () => {};
  onTitle: () => void = () => {};

  build() {
    Stack() {
      Column({ space: 2 }) {
        Row({ space: 2 }) {
          Text(this.title).fontSize(16).fontWeight(600)
            .fontColor($r('app.color.text_primary')).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
          if (this.selectableTitle) { FeatureIcon({ name: 'down' }) }
        }
        if (this.subtitle.length > 0) {
          Row({ space: 5 }) {
            Circle().width(7).height(7).fill(this.subtitleColor)
            Text(this.subtitle).fontSize(11).fontColor($r('app.color.text_secondary')).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
          }
        }
      }.constraintSize({ maxWidth: '72%' }).padding({ left: 8, right: 8, top: 5, bottom: 5 })
        .backgroundColor(this.selectableTitle ? $r('app.color.container_high') : Color.Transparent).borderRadius(14)
        .onClick(() => { if (this.selectableTitle || this.titleActionEnabled) this.onTitle(); })
      Row() {
        Button({ type: ButtonType.Circle }) {
          FeatureIcon({ name: 'ic_back' })
        }.width(48).height(48).backgroundColor(Color.Transparent)
          .accessibilityText('返回').onClick(() => this.onBack())
        Blank()
        if (this.busy) {
          LoadingProgress().width(24).height(24).color($r('app.color.primary')).margin(12)
        } else if (this.actionIcon.length > 0) {
          Button({ type: ButtonType.Circle }) { FeatureIcon({ name: this.actionIcon }) }
            .width(48).height(48).backgroundColor(Color.Transparent).accessibilityText(this.action)
            .enabled(this.actionEnabled).onClick(() => this.onAction())
        } else if (this.action.length > 0) {
          Button(this.action).fontSize(14).fontColor($r('app.color.primary'))
            .backgroundColor(Color.Transparent).height(40).enabled(this.actionEnabled).onClick(() => this.onAction())
        }
      }.width('100%').hitTestBehavior(HitTestMode.Transparent)
    }.height(64).width('100%').padding({ left: 4, right: 4 }).backgroundColor($r('app.color.surface'))
  }
}

@Component
export struct FeatureFilterChip {
  @Prop label: string = '';
  onSelect: () => void = () => {};

  build() {
    Row({ space: 2 }) {
      Text(this.label).fontSize(12).fontWeight(FontWeight.Medium).fontColor($r('app.color.on_secondary_container'))
        .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis }).constraintSize({ maxWidth: 112 })
      FeatureIcon({ name: 'down', tint: $r('app.color.on_secondary_container') }).scale({ x: 0.67, y: 0.67 }).width(16).height(16)
    }.padding({ left: 10, right: 6, top: 7, bottom: 7 }).borderRadius(10)
      .backgroundColor(withOpacity($r('app.color.secondary_container'), 0.85)).onClick(() => this.onSelect())
  }
}

@Component
export struct FeatureSummaryChip {
  @Prop label: string = '';
  @Prop selected: boolean = false;
  onSelect: () => void = () => {};

  build() {
    Text(this.label).fontSize(14).fontWeight(FontWeight.Medium).maxLines(1)
      .fontColor(this.selected ? $r('app.color.on_secondary_container') : $r('app.color.text_secondary'))
      .height(32).padding({ left: 16, right: 16 }).borderRadius(8)
      .backgroundColor(this.selected ? $r('app.color.secondary_container') : Color.Transparent)
      .border({ width: this.selected ? 0 : 1, color: $r('app.color.outline') })
      .accessibilitySelected(this.selected).onClick(() => this.onSelect())
  }
}

@Component
export struct FeatureEmpty {
  @Prop route: string = '';
  @Prop title: string = '暂时没有内容';
  @Prop description: string = '下拉刷新，稍后再来看看';

  private imageName(): string {
    switch (this.route) {
      case 'scores':
      case 'expScores': return 'state_no_score.png';
      case 'exams': return 'state_no_exam.png';
      case 'rooms': return 'state_no_classroom.png';
      case 'timetable': return 'state_no_search_result.png';
      case 'notices':
      case 'feedback': return 'state_no_message.png';
      case 'customCourses': return 'state_no_course_data.png';
      case 'customThings': return 'state_no_thing.png';
      default: return 'state_no_data.png';
    }
  }

  build() {
    Column() {
      Image($rawfile('shared/' + this.imageName())).width('100%').height(176).objectFit(ImageFit.Contain)
      Text(this.title).fontSize(16).fontWeight(600).fontColor($r('app.color.text_primary')).textAlign(TextAlign.Center).margin({ top: 20 })
      if (this.description.length > 0) {
        Text(this.description).fontSize(14).fontColor($r('app.color.text_secondary')).textAlign(TextAlign.Center).margin({ top: 8 })
      }
    }.width('100%').padding({ top: 40, bottom: 20, left: 44, right: 44 })
  }
}

@Component
export struct FeatureError {
  @Prop message: string = '';
  onRetry: () => void = () => {};

  build() {
    Column({ space: 12 }) {
      Text(this.message).fontSize(14).fontColor($r('app.color.danger')).textAlign(TextAlign.Center)
      Button('重试').fontColor($r('app.color.primary')).backgroundColor($r('app.color.primary_container'))
        .onClick(() => this.onRetry())
    }.width('100%').padding(20).backgroundColor($r('app.color.surface')).borderRadius(18)
  }
}

@CustomDialog
export struct FeatureChoiceDialog {
  controller?: CustomDialogController;
  title: string = '';
  options: SelectOption[] = [];
  selected: string[] = [];
  multiple: boolean = false;
  allowEmpty: boolean = true;
  quickActions: boolean = false;
  columns: number = 1;
  onConfirm: (values: string[]) => void = () => {};
  @State private checked: string[] = [];

  aboutToAppear(): void {
    this.checked = this.selected.slice();
  }

  private toggle(value: string): void {
    if (!this.multiple) {
      this.onConfirm([value]);
      this.controller?.close();
      return;
    }
    this.checked = this.checked.includes(value)
      ? this.checked.filter((item: string) => item !== value) : this.checked.concat([value]);
  }

  build() {
    Column({ space: 12 }) {
      Text(this.title).fontSize(21).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
        .width('100%').padding({ top: 8, bottom: 8 })
      if (this.multiple && this.quickActions) {
        Row({ space: 6 }) {
          ForEach(['全选', '单周', '双周', '清空'], (label: string, index: number) => {
            Button(label).fontSize(12).layoutWeight(1).backgroundColor($r('app.color.secondary_container'))
              .fontColor($r('app.color.on_secondary_container')).onClick(() => {
                this.checked = this.options.filter((option: SelectOption) => index === 0 ||
                  (index === 1 && Number(option.value) % 2 === 1) || (index === 2 && Number(option.value) % 2 === 0))
                  .map((option: SelectOption) => option.value);
              })
          })
        }.width('100%')
      }
      Scroll() {
        Flex({ wrap: FlexWrap.Wrap }) {
          ForEach(this.options, (option: SelectOption) => {
            Row({ space: 4 }) {
              Text(option.label).fontSize(this.columns > 1 ? 13 : 16).fontColor($r('app.color.text_primary')).layoutWeight(1)
              if (this.checked.includes(option.value)) {
                Text('✓').fontSize(16).fontColor($r('app.color.primary'))
              }
            }.width((100 / this.columns).toString() + '%').padding({ top: 14, bottom: 14, left: 8, right: 8 })
              .backgroundColor(this.checked.includes(option.value) ? $r('app.color.primary_container') : Color.Transparent)
              .borderRadius(10).onClick(() => this.toggle(option.value))
          }, (option: SelectOption) => option.value)
        }.width('100%')
      }.constraintSize({ maxHeight: 360 }).scrollBar(BarState.Auto).width('100%')
      Row({ space: 12 }) {
        Button('取消').layoutWeight(1).fontColor($r('app.color.text_secondary'))
          .backgroundColor($r('app.color.surface_variant')).onClick(() => this.controller?.close())
        if (this.multiple) {
          Button('确定').layoutWeight(1).backgroundColor($r('app.color.primary')).fontColor($r('app.color.on_primary'))
            .enabled(this.allowEmpty || this.checked.length > 0)
            .onClick(() => { this.onConfirm(this.checked); this.controller?.close(); })
        }
      }.width('100%')
    }.padding(24).width(DIALOG_CARD_WIDTH).backgroundColor($r('app.color.surface')).borderRadius(28)
  }
}

export const FEATURE_COLORS: string[] = ['#E55353', '#D84B84', '#8E55B8', '#6B5CE7', '#4A69BD', '#2E86DE',
  '#1B9CFC', '#00A8B5', '#16A085', '#2ECC71', '#6AB04C', '#B5A624', '#E59A23', '#EB7D2A', '#E66742', '#8D6E63', '#C0396B', '#2D8659'];

@CustomDialog
export struct FeatureColorDialog {
  controller?: CustomDialogController;
  title: string = '选择主题颜色';
  initialValue: string = '#E55353';
  onReset?: () => void;
  onConfirm: (value: string) => void = () => {};
  @State private value: string = '#E55353';
  @State private mode: number = 0;

  aboutToAppear(): void {
    if (/^#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$/.test(this.initialValue)) this.value = '#' + this.initialValue.slice(-6).toUpperCase();
  }

  private channel(index: number): number { return Number.parseInt(this.value.slice(1 + index * 2, 3 + index * 2), 16); }
  private setChannel(index: number, value: number): void {
    const channels = [this.channel(0), this.channel(1), this.channel(2)];
    channels[index] = Math.round(value);
    this.value = '#' + channels.map((channel: number) => channel.toString(16).padStart(2, '0')).join('').toUpperCase();
  }

  @Builder
  private channelSlider(index: number, label: string, color: string) {
    Column() {
      Row() {
        Text(label).fontSize(12).fontColor($r('app.color.text_secondary')).layoutWeight(1)
        Text(this.channel(index).toString()).fontSize(12).fontFamily('monospace').fontColor($r('app.color.text_primary'))
      }.width('100%')
      Slider({ value: this.channel(index), min: 0, max: 255, step: 1 }).selectedColor(color).blockColor(color)
        .onChange((value: number) => this.setChannel(index, value))
    }.width('100%')
  }

  build() {
    Column({ space: 12 }) {
      Text(this.title).fontSize(20).fontWeight(600).fontColor($r('app.color.text_primary')).width('100%')
      Row() {
        Text('预览效果').fontSize(14).fontWeight(600).fontColor(readableCourseText(this.value)).layoutWeight(1)
        Text(this.value).fontSize(14).fontFamily('monospace').fontColor(readableCourseText(this.value))
      }.width('100%').height(48).padding({ left: 16, right: 16 }).backgroundColor(this.value).borderRadius(16)
      Row() {
        ForEach(['精选色板', '自定义'], (label: string, index: number) => {
          Button(label).layoutWeight(1).fontSize(14).height(40).borderRadius(index === 0 ? { topLeft: 20, bottomLeft: 20 } : { topRight: 20, bottomRight: 20 })
            .backgroundColor(this.mode === index ? $r('app.color.secondary_container') : Color.Transparent)
            .fontColor(this.mode === index ? $r('app.color.on_secondary_container') : $r('app.color.text_primary'))
            .border({ width: 0.5, color: $r('app.color.outline') }).onClick(() => { this.mode = index; })
        })
      }.width('100%')
      if (this.mode === 0) {
        Grid() {
          ForEach(FEATURE_COLORS, (color: string) => {
            GridItem() {
              Stack() {
                Circle().width('100%').height('100%').fill(color).stroke(this.value === color ? $r('app.color.text_primary') : $r('app.color.outline'))
                  .strokeWidth(this.value === color ? 2.5 : 1)
                if (this.value === color) { Image($rawfile('shared/ic_action_done.svg')).width(18).height(18).colorFilter(iconTint(readableCourseText(color))) }
              }.width('100%').aspectRatio(1).id('color-swatch-' + color).accessibilityText(color)
                .accessibilitySelected(this.value === color).onClick(() => { this.value = color; })
            }
          }, (color: string) => color)
        }.columnsTemplate('1fr 1fr 1fr 1fr 1fr 1fr').columnsGap(10).rowsGap(10).width('100%').height(160).padding(4)
      } else {
        Column({ space: 4 }) {
          this.channelSlider(0, '红 (R)', '#E53935')
          this.channelSlider(1, '绿 (G)', '#43A047')
          this.channelSlider(2, '蓝 (B)', '#1E88E5')
        }.width('100%')
      }
      Row({ space: 12 }) {
        if (this.onReset) {
          Button('恢复默认').layoutWeight(1).fontSize(14).padding({ left: 4, right: 4 })
            .backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
            .onClick(() => { this.onReset?.(); this.controller?.close(); })
        }
        Button('取消').layoutWeight(1).backgroundColor($r('app.color.container')).fontColor($r('app.color.text_secondary')).onClick(() => this.controller?.close())
        Button('应用').layoutWeight(1).backgroundColor($r('app.color.primary')).fontColor($r('app.color.on_primary'))
          .onClick(() => { this.onConfirm(this.value); this.controller?.close(); })
      }.width('100%')
    }.padding(24).width(DIALOG_CARD_WIDTH).backgroundColor($r('app.color.surface')).borderRadius(28)
  }
}

@CustomDialog
export struct FeatureTextDialog {
  controller?: CustomDialogController;
  title: string = '';
  message: string = '';
  initialValue: string = '';
  numeric: boolean = false;
  multiline: boolean = false;
  onConfirm: (value: string) => void = () => {};
  @State private value: string = '';

  aboutToAppear(): void {
    this.value = this.initialValue;
  }

  build() {
    Column({ space: 16 }) {
      Text(this.title).fontSize(21).fontWeight(FontWeight.Bold).fontColor($r('app.color.text_primary'))
        .width('100%')
      if (this.message.length > 0) {
        Text(this.message).fontSize(14).fontColor($r('app.color.text_secondary')).width('100%')
      }
      if (this.multiline) {
        TextArea({ text: this.value, placeholder: '请输入内容' }).height(140)
          .fontColor($r('app.color.text_primary')).backgroundColor($r('app.color.surface_variant'))
          .onChange((value: string) => { this.value = value; })
      } else {
        TextInput({ text: this.value, placeholder: '请输入' })
          .type(this.numeric ? InputType.Number : InputType.Normal).height(48)
          .fontColor($r('app.color.text_primary')).backgroundColor($r('app.color.surface_variant'))
          .onChange((value: string) => { this.value = value; })
      }
      Row({ space: 12 }) {
        Button('取消').layoutWeight(1).backgroundColor($r('app.color.surface_variant'))
          .fontColor($r('app.color.text_secondary')).onClick(() => this.controller?.close())
        Button('保存').layoutWeight(1).backgroundColor($r('app.color.primary')).fontColor($r('app.color.on_primary'))
          .onClick(() => { this.onConfirm(this.value); this.controller?.close(); })
      }.width('100%')
    }.padding(24).width(DIALOG_CARD_WIDTH).backgroundColor($r('app.color.surface')).borderRadius(28)
  }
}

@CustomDialog
export struct FeatureImageDialog {
  controller?: CustomDialogController;
  image: string = '';
  title: string = '';

  build() {
    Column({ space: 12 }) {
      Row() {
        Text(this.title).fontSize(18).fontWeight(FontWeight.Medium).fontColor($r('app.color.text_primary'))
          .layoutWeight(1).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
        Button('关闭').fontColor($r('app.color.primary')).backgroundColor(Color.Transparent)
          .onClick(() => this.controller?.close())
      }.width('100%')
      Image(this.image).width('100%').height('75%').objectFit(ImageFit.Contain)
        .draggable(false).accessibilityText(this.title)
    }.padding(16).width(DIALOG_CARD_WIDTH).backgroundColor($r('app.color.surface')).borderRadius(24)
  }
}
