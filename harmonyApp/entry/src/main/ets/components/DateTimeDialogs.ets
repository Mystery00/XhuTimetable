import { DIALOG_CARD_WIDTH } from './DialogLayout';
import { dateText } from './FeatureControls';
import { HomeIcon } from './HomeWidgets';

const FIRST_YEAR = 2015;
const LAST_YEAR = 2040;
const FIRST_MONTH = new Date(FIRST_YEAR, 0, 1, 12).getTime();
const LAST_MONTH = new Date(LAST_YEAR, 11, 1, 12).getTime();
const WEEKDAYS = ['一', '二', '三', '四', '五', '六', '日'];

function sequence(first: number, count: number): number[] {
  const values: number[] = [];
  for (let i = 0; i < count; i++) values.push(first + i);
  return values;
}

const YEARS = sequence(FIRST_YEAR, LAST_YEAR - FIRST_YEAR + 1);
const HOURS = sequence(0, 24);
const MINUTES = sequence(0, 12).map((tick: number) => tick * 5);

function twoDigits(value: number): string {
  return value.toString().padStart(2, '0');
}

// 共享层 LocalDate.now() 和 LocalTime.now() 都使用北京时间。
function chinaNow(): Date {
  return new Date(Date.now() + 8 * 60 * 60 * 1000);
}

@Component
struct DateTimeCard {
  title: string = '';
  date: boolean = false;
  @Prop extra: string = '';
  @Prop confirmEnabled: boolean = true;
  onExtra: () => void = () => {};
  onCancel: () => void = () => {};
  onConfirm: () => void = () => {};
  @BuilderParam content: () => void;

  build() {
    Column() {
      Scroll() {
        Column({ space: 12 }) {
          if (this.date) { HomeIcon({ name: 'today', iconSize: 28, tint: $r('app.color.primary') }) }
          Text(this.title).fontSize(24).fontWeight(600).fontColor($r('app.color.text_primary')).width('100%')
          this.content()
        }.width('100%').alignItems(HorizontalAlign.Start).padding({ left: 20, right: 20, top: 20, bottom: 8 })
      }.width('100%').flexShrink(1).scrollBar(BarState.Auto).id(this.date ? 'setting-date-scroll' : 'setting-time-scroll')
      Row({ space: 4 }) {
        if (this.extra.length > 0) {
          Button(this.extra).fontSize(14).height(40).padding({ left: 10, right: 10 })
            .backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
            .id(this.date ? 'setting-date-auto' : 'setting-time-disable').onClick(() => this.onExtra())
        }
        Blank()
        Button('取消').fontSize(14).height(40).padding({ left: 12, right: 12 })
          .backgroundColor(Color.Transparent).fontColor($r('app.color.primary'))
          .id(this.date ? 'setting-date-cancel' : 'setting-time-cancel').onClick(() => this.onCancel())
        Button('确定').fontSize(14).height(40).padding({ left: 16, right: 16 }).enabled(this.confirmEnabled)
          .backgroundColor($r('app.color.primary')).fontColor($r('app.color.on_primary'))
          .id(this.date ? 'setting-date-confirm' : 'setting-time-confirm').onClick(() => this.onConfirm())
      }.width('100%').padding({ left: 12, right: 16, top: 4, bottom: 12 })
    }.width(DIALOG_CARD_WIDTH).constraintSize({ maxHeight: '92%' })
      .backgroundColor($r('app.color.container_high')).borderRadius(28).clip(true)
  }
}

@CustomDialog
export struct SettingDateDialog {
  controller?: CustomDialogController;
  title: string = '';
  initialValue: string = '';
  automatic: boolean = false;
  onConfirm: (value: string) => void = () => {};
  onAutomatic: () => void = () => {};
  @State private selected: Date = new Date();
  @State private month: Date = new Date();
  @State private pickingYear: boolean = false;
  private today: Date = new Date();

  aboutToAppear(): void {
    const now = chinaNow();
    this.today = new Date(now.getUTCFullYear(), now.getUTCMonth(), now.getUTCDate(), 12);
    const parsed = new Date(this.initialValue + 'T12:00:00');
    this.selectDate(!Number.isNaN(parsed.getTime()) && dateText(parsed) === this.initialValue ? parsed : this.today);
  }

  private selectDate(date: Date): void {
    this.selected = date;
    this.month = new Date(date.getFullYear(), date.getMonth(), 1, 12);
    this.pickingYear = false;
  }

  private monthLength(year: number): number {
    return new Date(year, this.month.getMonth() + 1, 0, 12).getDate();
  }

  private selectYear(year: number): void {
    const day = Math.min(this.selected.getDate(), this.monthLength(year));
    this.selectDate(new Date(year, this.month.getMonth(), day, 12));
  }

  private shiftMonth(delta: number): void {
    const next = new Date(this.month.getFullYear(), this.month.getMonth() + delta, 1, 12);
    this.month = new Date(Math.max(FIRST_MONTH, Math.min(LAST_MONTH, next.getTime())));
  }

  @Builder
  private preview() {
    Row({ space: 8 }) {
      Column({ space: 2 }) {
        Text(this.selected.getFullYear() + '年').fontSize(12).fontColor($r('app.color.on_primary_container'))
        Text((this.selected.getMonth() + 1) + '月' + this.selected.getDate() + '日  周' + WEEKDAYS[(this.selected.getDay() + 6) % 7])
          .fontSize(16).fontWeight(FontWeight.Bold).fontColor($r('app.color.on_primary_container')).id('setting-date-selected')
      }.layoutWeight(1).alignItems(HorizontalAlign.Start)
      Button('今天').fontSize(13).height(32).padding({ left: 10, right: 10 }).borderRadius(10)
        .backgroundColor($r('app.color.surface')).fontColor($r('app.color.primary')).id('setting-date-today')
        .enabled(this.today.getFullYear() >= FIRST_YEAR && this.today.getFullYear() <= LAST_YEAR)
        .onClick(() => this.selectDate(this.today))
    }.width('100%').padding({ left: 14, right: 14, top: 10, bottom: 10 })
      .backgroundColor($r('app.color.primary_container')).borderRadius(16)
  }

  @Builder
  private monthArrow(previous: boolean) {
    Button(previous ? '‹' : '›').width(34).height(34).padding(0).fontSize(26)
      .backgroundColor(Color.Transparent).fontColor($r('app.color.text_secondary'))
      .id(previous ? 'setting-date-prev' : 'setting-date-next').accessibilityText(previous ? '上个月' : '下个月')
      .enabled(previous ? this.month.getTime() > FIRST_MONTH : this.month.getTime() < LAST_MONTH)
      .onClick(() => this.shiftMonth(previous ? -1 : 1))
  }

  @Builder
  private yearGrid() {
    List({ space: 8, initialIndex: Math.max(0, Math.min(YEARS.length - 1, this.month.getFullYear() - FIRST_YEAR - 3)) }) {
      ForEach(YEARS, (year: number) => {
        ListItem() {
          Button(year + '年').width('100%').height(38).padding(0).borderRadius(12).fontSize(14)
            .fontColor(year === this.month.getFullYear() ? $r('app.color.on_primary') : $r('app.color.text_primary'))
            .backgroundColor(year === this.month.getFullYear() ? $r('app.color.primary') : $r('app.color.surface'))
            .border({ width: year === this.today.getFullYear() ? 1 : 0, color: $r('app.color.primary') })
            .accessibilitySelected(year === this.month.getFullYear()).id('setting-date-year-' + year)
            .onClick(() => this.selectYear(year))
        }
      }, (year: number) => year.toString())
    }.lanes(3, 8).height(258).width('100%').id('setting-date-years')
      .nestedScroll({ scrollForward: NestedScrollMode.SELF_FIRST, scrollBackward: NestedScrollMode.SELF_FIRST })
  }

  @Builder
  private dayCell(date: Date) {
    Text(date.getDate().toString()).width(36).constraintSize({ maxWidth: '100%' }).height(36).textAlign(TextAlign.Center)
      .fontSize(14).fontWeight(date.getTime() === this.selected.getTime() || date.getTime() === this.today.getTime() ? FontWeight.Bold : FontWeight.Normal)
      .fontColor(date.getTime() === this.selected.getTime() ? $r('app.color.on_primary') :
        date.getTime() === this.today.getTime() ? $r('app.color.primary') : $r('app.color.text_primary'))
      .backgroundColor(date.getTime() === this.selected.getTime() ? $r('app.color.primary') : Color.Transparent).borderRadius(18)
      .border({ width: date.getTime() === this.today.getTime() ? 1.5 : 0, color: $r('app.color.primary') })
      .id('setting-date-day-' + dateText(date)).accessibilityText(dateText(date)).accessibilitySelected(date.getTime() === this.selected.getTime())
      .onClick(() => { this.selected = date; })
  }

  @Builder
  private calendar() {
    Row() {
      ForEach(WEEKDAYS, (label: string, index: number) => {
        Text(label).layoutWeight(1).textAlign(TextAlign.Center).fontSize(12)
          .fontColor(index >= 5 ? $r('app.color.primary') : $r('app.color.text_secondary'))
      }, (label: string) => label)
    }.width('100%')
    Grid() {
      ForEach(sequence(1 - (this.month.getDay() + 6) % 7, 42), (day: number) => {
        GridItem() {
          if (day > 0 && day <= this.monthLength(this.month.getFullYear())) {
            this.dayCell(new Date(this.month.getFullYear(), this.month.getMonth(), day, 12))
          }
        }
      }, (day: number) => dateText(this.month) + '/' + day)
    }.columnsTemplate('1fr 1fr 1fr 1fr 1fr 1fr 1fr').rowsTemplate('1fr 1fr 1fr 1fr 1fr 1fr').width('100%').height(246)
  }

  build() {
    DateTimeCard({ title: this.title, date: true, extra: this.automatic ? '自动获取' : '',
      onExtra: (): void => { this.onAutomatic(); this.controller?.close(); },
      onCancel: (): void => { this.controller?.close(); },
      onConfirm: (): void => { this.onConfirm(dateText(this.selected)); this.controller?.close(); } }) {
      this.preview()
      Row() {
        Button(this.month.getFullYear() + '年 ' + (this.month.getMonth() + 1) + '月 ' + (this.pickingYear ? '▴' : '▾'))
          .height(34).fontSize(14).padding({ left: 10, right: 10 }).borderRadius(10)
          .backgroundColor($r('app.color.surface')).fontColor($r('app.color.text_primary')).id('setting-date-year-month')
          .onClick(() => { this.pickingYear = !this.pickingYear; })
        Blank()
        if (!this.pickingYear) { this.monthArrow(true); this.monthArrow(false) }
      }.width('100%')
      if (this.pickingYear) { this.yearGrid() } else { this.calendar() }
    }
  }
}

function validPart(text: string, max: number): boolean {
  return /^\d{1,2}$/.test(text) && Number(text) <= max;
}

const DIAL_SIZE = 256;
const OUTER_RADIUS = 101 / DIAL_SIZE;
const INNER_RADIUS = 69 / DIAL_SIZE;

@CustomDialog
export struct SettingTimeDialog {
  controller?: CustomDialogController;
  title: string = '';
  initialValue: string = '';
  allowDisable: boolean = true;
  onConfirm: (value: string) => void = () => {};
  @State private hour: number = 21;
  @State private minute: number = 0;
  @State private pickingHour: boolean = true;
  @State private keyboard: boolean = false;
  @State private hourInput: string = '';
  @State private minuteInput: string = '';
  @State private diameter: number = DIAL_SIZE;

  aboutToAppear(): void {
    const time = /^([01]?\d|2[0-3]):([0-5]\d)(?::[0-5]\d(?:\.\d{1,9})?)?$/.exec(this.initialValue);
    if (time) {
      this.hour = Number(time[1]);
      this.minute = Number(time[2]);
    } else if (!this.allowDisable) {
      const now = chinaNow();
      this.hour = now.getUTCHours();
      this.minute = now.getUTCMinutes();
    }
  }

  private validInput(): boolean {
    return validPart(this.hourInput, 23) && validPart(this.minuteInput, 59);
  }

  private switchMode(): void {
    if (this.keyboard) {
      this.getUIContext().getFocusController().clearFocus();
      if (validPart(this.hourInput, 23)) this.hour = Number(this.hourInput);
      if (validPart(this.minuteInput, 59)) this.minute = Number(this.minuteInput);
    } else {
      this.hourInput = twoDigits(this.hour);
      this.minuteInput = twoDigits(this.minute);
    }
    this.keyboard = !this.keyboard;
  }

  private confirm(): void {
    if (this.keyboard && !this.validInput()) return;
    const hour = this.keyboard ? Number(this.hourInput) : this.hour;
    const minute = this.keyboard ? Number(this.minuteInput) : this.minute;
    this.onConfirm(twoDigits(hour) + ':' + twoDigits(minute));
    this.controller?.close();
  }

  // 12 点方向为零，顺时针增加。24 小时制将下午的 12 个数字排在内圈。
  private dialOffset(value: number, horizontal: boolean): number {
    const radius = this.diameter * (this.pickingHour && value >= 12 ? INNER_RADIUS : OUTER_RADIUS);
    const angle = 2 * Math.PI * value / (this.pickingHour ? 12 : 60);
    return radius * (horizontal ? Math.sin(angle) : -Math.cos(angle));
  }

  private pointAt(x: number, y: number, tap: boolean): void {
    const dx = x - this.diameter / 2;
    const dy = y - this.diameter / 2;
    const turn = (Math.atan2(dx, -dy) / (2 * Math.PI) + 1) % 1;
    if (this.pickingHour) {
      const inner = Math.hypot(dx, dy) < this.diameter * (INNER_RADIUS + OUTER_RADIUS) / 2;
      this.hour = Math.round(turn * 12) % 12 + (inner ? 12 : 0);
      if (tap) this.pickingHour = false;
    } else {
      this.minute = tap ? Math.round(turn * 12) % 12 * 5 : Math.round(turn * 60) % 60;
    }
  }

  private drag(event: GestureEvent): void {
    const finger = event.fingerList[0];
    if (finger) this.pointAt(finger.localX, finger.localY, false);
  }

  @Builder
  private clockNumber(value: number) {
    Text(value.toString())
      .width(this.diameter * 0.12).height(this.diameter * 0.12).textAlign(TextAlign.Center).fontSize(this.diameter < 210 ? 12 : 14)
      .fontColor(value === (this.pickingHour ? this.hour : this.minute) ? $r('app.color.on_primary') : $r('app.color.text_primary'))
      .translate({ x: this.dialOffset(value, true), y: this.dialOffset(value, false) })
      .id('setting-time-dial-' + (this.pickingHour ? 'hour-' : 'minute-') + value)
      .accessibilityText(value + (this.pickingHour ? '时' : '分')).accessibilitySelected(value === (this.pickingHour ? this.hour : this.minute))
      .onClick(() => {
        if (this.pickingHour) { this.hour = value; this.pickingHour = false; }
        else this.minute = value;
      })
  }

  private handValue(): number {
    return this.pickingHour ? this.hour : this.minute;
  }

  @Builder
  private dial() {
    Stack() {
      Circle().width('100%').height('100%').fill($r('app.color.container_highest')).accessibilityLevel('no')
        .onClick((event: ClickEvent) => this.pointAt(event.x, event.y, true))
      Line().width('100%').height('100%').startPoint([this.diameter / 2, this.diameter / 2])
        .endPoint([this.diameter / 2 + this.dialOffset(this.handValue(), true), this.diameter / 2 + this.dialOffset(this.handValue(), false)])
        .stroke($r('app.color.primary')).strokeWidth(2).hitTestBehavior(HitTestMode.None)
      Circle().width(8).height(8).fill($r('app.color.primary')).hitTestBehavior(HitTestMode.None)
      Stack() {
        Circle().width('100%').height('100%').fill($r('app.color.primary'))
        if (!this.pickingHour && this.minute % 5 !== 0) { Circle().width(4).height(4).fill($r('app.color.on_primary')) }
      }.width(this.diameter * 0.17).height(this.diameter * 0.17)
        .translate({ x: this.dialOffset(this.handValue(), true), y: this.dialOffset(this.handValue(), false) })
        .hitTestBehavior(HitTestMode.None)
      ForEach(this.pickingHour ? HOURS : MINUTES, (tick: number) => { this.clockNumber(tick) },
        (tick: number) => (this.pickingHour ? 'h' : 'm') + tick)
    }.width('100%').constraintSize({ maxWidth: DIAL_SIZE }).aspectRatio(1).id('setting-time-dial')
      .onAreaChange((_old: Area, area: Area) => { this.diameter = Number(area.width); })
      .priorityGesture(PanGesture({ distance: 4 })
        .onActionStart((event: GestureEvent) => this.drag(event)).onActionUpdate((event: GestureEvent) => this.drag(event))
        .onActionEnd(() => { this.pickingHour = false; }))
  }

  @Builder
  private timeField(hours: boolean) {
    if (this.keyboard) {
      Column({ space: 4 }) {
        TextInput({ text: hours ? this.hourInput : this.minuteInput }).type(InputType.Number)
          .width('100%').height(54).padding(4).fontSize(32).textAlign(TextAlign.Center).borderRadius(10)
          .backgroundColor($r('app.color.surface')).fontColor($r('app.color.text_primary'))
          .border({ width: 1, color: validPart(hours ? this.hourInput : this.minuteInput, hours ? 23 : 59) ? $r('app.color.outline') : $r('app.color.danger') })
          .id(hours ? 'setting-time-input-hour' : 'setting-time-input-minute').accessibilityText(hours ? '小时' : '分钟')
          .onAppear(() => {
            if (this.pickingHour === hours) this.getUIContext().getFocusController().requestFocus(hours ? 'setting-time-input-hour' : 'setting-time-input-minute');
          })
          .onFocus(() => { this.pickingHour = hours; })
          .onChange((text: string) => { if (hours) this.hourInput = text; else this.minuteInput = text; })
        Text(hours ? '小时' : '分钟').fontSize(12).fontColor($r('app.color.text_secondary'))
      }.layoutWeight(1).constraintSize({ maxWidth: 96 })
    } else {
      Button(twoDigits(hours ? this.hour : this.minute)).layoutWeight(1).constraintSize({ maxWidth: 96 }).height(64)
        .fontSize(36).padding(0).borderRadius(12).fontWeight(FontWeight.Medium)
        .backgroundColor(this.pickingHour === hours ? $r('app.color.primary_container') : $r('app.color.container_highest'))
        .fontColor(this.pickingHour === hours ? $r('app.color.on_primary_container') : $r('app.color.text_primary'))
        .id(hours ? 'setting-time-hour' : 'setting-time-minute').accessibilityText((hours ? '小时 ' : '分钟 ') + (hours ? this.hour : this.minute))
        .accessibilitySelected(this.pickingHour === hours).onClick(() => { this.pickingHour = hours; })
    }
  }

  build() {
    DateTimeCard({ title: this.title, extra: this.allowDisable && this.initialValue.length > 0 ? '关闭功能' : '',
      confirmEnabled: !this.keyboard || this.validInput(),
      onCancel: (): void => { this.controller?.close(); },
      onExtra: (): void => { this.onConfirm(''); this.controller?.close(); }, onConfirm: (): void => this.confirm() }) {
      Column({ space: 16 }) {
        Row({ space: 4 }) {
          this.timeField(true)
          Text(':').fontSize(32).fontColor($r('app.color.text_primary')).height(this.keyboard ? 54 : 64).textAlign(TextAlign.Center)
          this.timeField(false)
        }.width('100%').alignItems(VerticalAlign.Top).justifyContent(FlexAlign.Center)
        if (!this.keyboard) { this.dial() }
        else if (!this.validInput()) {
          Text('小时请输入 0–23，分钟请输入 0–59').fontSize(13).fontColor($r('app.color.danger')).id('setting-time-error')
        }
      }.width('100%')
      Row({ space: 8 }) {
        if (this.keyboard) { HomeIcon({ name: 'clock', iconSize: 20 }) }
        else { SymbolGlyph($r('sys.symbol.keyboard')).fontSize(20).fontColor([$r('app.color.text_secondary')]) }
        Text(this.keyboard ? '切换为拨盘选择' : '切换为键盘输入').fontSize(13).fontColor($r('app.color.text_secondary'))
      }.height(40).id('setting-time-mode').onClick(() => this.switchMode())
    }
  }
}
