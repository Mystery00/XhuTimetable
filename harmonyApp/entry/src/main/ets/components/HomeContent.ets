import { DisplayItem, HomeState } from '../model/NativeModels';
import { createEmptyHomeState } from './AppComponents';
import { HomeToday } from './HomeToday';
import { HomeWeek, HomeWeekPicker } from './HomeWeek';
import { HomeCalendar } from './HomeCalendar';
import { bottomSheetOptions, SheetSurface } from './SheetSurface';
import { HomeAddSheet, HomeCourseSheet, HomePoemSheet, HomePracticalSheet } from './HomeSheets';

export function emptyHomeState(): HomeState {
  return createEmptyHomeState();
}

@Component
export struct HomeContent {
  @Prop home: HomeState = emptyHomeState();
  @Prop displayedWeek: number | undefined = undefined;
  @Prop tab: string = 'today';
  onNavigate: (route: string) => void = () => {};
  onRefresh: () => void = () => {};
  onDate: (date: string) => void = () => {};
  onWeek: (week: number) => void = () => {};
  registerBackHandler: (handler: (() => boolean) | undefined) => void = () => {};
  registerActions: (handler: ((action: string) => void) | undefined) => void = () => {};
  onCalendarTitle: (title: string) => void = () => {};
  onCopy: (text: string) => void = () => {};
  @State private todayRequest: number = 0;
  @State private sheetVisible: boolean = false;
  @State private sheetKind: string = 'course';
  @State private detailItems: DisplayItem[] = [];
  @State private detailMode: string = 'today';

  aboutToAppear(): void {
    this.registerActions((action: string) => { this.handleAction(action); });
    this.registerBackHandler(() => {
      if (!this.sheetVisible) return false;
      this.sheetVisible = false;
      return true;
    });
  }

  aboutToDisappear(): void {
    this.registerActions(undefined);
    this.registerBackHandler(undefined);
  }

  private handleAction(action: string): void {
    if (action === 'today') {
      if (this.tab === 'week') {
        if (this.home.actualWeek >= 1 && this.home.actualWeek <= 20) this.onWeek(this.home.actualWeek);
      } else {
        this.todayRequest++;
      }
    } else if (action === 'add' || action === 'week' || action === 'practical') {
      this.sheetKind = action;
      this.sheetVisible = true;
    }
  }

  private showCourses(items: DisplayItem[], mode: string): void {
    if (items.length === 0) return;
    this.detailItems = items;
    this.detailMode = mode;
    this.sheetKind = 'course';
    this.sheetVisible = true;
  }

  @Builder
  private sheetContent() {
    SheetSurface() { this.sheetBody() }
  }

  @Builder
  private sheetBody() {
    if (this.sheetKind === 'add') {
      HomeAddSheet({ onNavigate: (route: string) => {
        this.sheetVisible = false;
        this.onNavigate(route);
      } })
    } else if (this.sheetKind === 'week') {
      HomeWeekPicker({ overview: this.home.weekOverview ?? [], selectedWeek: this.displayedWeek ?? this.home.week,
        actualWeek: this.home.actualWeek, onSelect: (week: number) => {
          this.sheetVisible = false;
          this.onWeek(week);
        } })
    } else if (this.sheetKind === 'practical') {
      HomePracticalSheet({ items: this.home.practical })
    } else if (this.sheetKind === 'poem') {
      HomePoemSheet({ poem: this.home.poem,
        details: this.home.poemDetails ?? { title: '', author: '', dynasty: '', lines: [], translation: [] } })
    } else {
      HomeCourseSheet({ items: this.detailItems, mode: this.detailMode, multiAccount: this.home.multiAccount,
        onCopy: this.onCopy })
    }
  }

  build() {
    Stack() {
      if (this.tab === 'today') {
        HomeToday({ home: this.home, onRefresh: this.onRefresh, onDate: this.onDate,
          onCourse: (items: DisplayItem[]) => { this.showCourses(items, 'today'); },
          onPoem: () => { this.sheetKind = 'poem'; this.sheetVisible = true; } })
      } else if (this.tab === 'week') {
        HomeWeek({ pages: this.home.weeks, selectedWeek: this.displayedWeek ?? this.home.week,
          classTimeSlots: this.home.classTimeSlots ?? [], cellHeight: this.home.cellHeight, cellRadius: this.home.cellRadius,
          textSize: this.home.textSize, cellOpacity: this.home.cellOpacity,
          onWeek: (week: number) => { this.onWeek(week); }, onRefresh: this.onRefresh,
          onSelect: (items: DisplayItem[]) => { this.showCourses(items, 'week'); } })
      } else {
        HomeCalendar({ weeks: this.home.calendar, todayRequest: this.todayRequest,
          onTitle: this.onCalendarTitle, onSelect: (item: DisplayItem) => {
            this.showCourses(item.children ?? [], item.value === 'CUSTOM_THING' ? 'calendarThing' : 'week');
          } })
      }
    }.width('100%').height('100%').backgroundColor(Color.Transparent)
      .bindSheet($$this.sheetVisible, this.sheetContent(), bottomSheetOptions())
  }
}
