import { NativeClient } from '../platform/NativeClient';
import { FeatureAbout } from './FeatureAbout';
import { FeatureAccounts } from './FeatureAccounts';
import { CourseColorContent } from './CourseColorContent';
import { FeatureEditorRequest } from './FeatureContracts';
import { FeatureEditor } from './FeatureEditor';
import { bottomSheetOptions } from './SheetSurface';
import { FeatureList } from './FeatureList';
import { FeatureSettings } from './FeatureSettings';
import { CustomUiContent } from './CustomUiContent';
import { FeedbackContent } from './FeedbackContent';
import { FeatureError, FeatureHeader } from './FeatureControls';

@Component
export struct FeaturePage {
  client: NativeClient | undefined = undefined;
  @Prop @Watch('routeChanged') route: string = '';
  onBack: () => void = () => {};
  onNavigate: (route: string) => void = () => {};
  onChanged: () => void = () => {};
  registerBackHandler: (handler: (() => boolean) | undefined) => void = () => {};
  @State private editing: boolean = false;
  @State private editorBusy: boolean = false;
  @State private refreshTrigger: number = 0;
  @State private editorRequest: FeatureEditorRequest = { kind: '', id: '', user: '', year: 0, term: 0 };

  private childBackHandler?: () => boolean;

  aboutToAppear(): void {
    this.registerBackHandler(() => {
      if (!this.editing) return this.childBackHandler?.() || false;
      if (this.editorBusy) return true;
      this.editing = false;
      return true;
    });
  }

  aboutToDisappear(): void {
    this.registerBackHandler(undefined);
  }

  private routeChanged(): void {
    this.editing = false;
    this.editorBusy = false;
    this.childBackHandler = undefined;
  }

  private queryRoute(): boolean {
    return ['scores', 'exams', 'expScores', 'rooms', 'notices', 'customCourses', 'customThings',
      'schoolCalendar', 'timetable', 'backgrounds', 'feedback', 'urge', 'licenses'].includes(this.route);
  }

  private openEditor(request: FeatureEditorRequest): void {
    this.editorRequest = request;
    this.editing = true;
  }

  private saved(): void {
    this.editing = false;
    this.editorBusy = false;
    this.refreshTrigger++;
    this.onChanged();
  }

  @Builder
  private editorSheet() {
    FeatureEditor({ client: this.client, request: this.editorRequest,
      onBack: () => { if (!this.editorBusy) this.editing = false; }, onSaved: () => this.saved(),
      onBusyChange: (busy: boolean) => { this.editorBusy = busy; } })
  }

  build() {
    Column() {
      if (this.route === 'customUi') {
        CustomUiContent({ client: this.client, onBack: () => this.onBack(), onChanged: () => this.onChanged(),
          registerBackHandler: (handler: (() => boolean) | undefined) => { this.childBackHandler = handler; } })
      } else if (this.route === 'settings' || this.route === 'classSettings') {
        FeatureSettings({ client: this.client, route: this.route, onNavigate: (route: string) => this.onNavigate(route),
          onBack: () => this.onBack(), onChanged: () => this.onChanged() })
      } else if (this.route === 'courseColors') {
        CourseColorContent({ client: this.client, onBack: () => this.onBack(),
          registerBackHandler: (handler: (() => boolean) | undefined) => { this.childBackHandler = handler; } })
      } else if (this.route === 'feedback') {
        FeedbackContent({ client: this.client, onBack: () => this.onBack() })
      } else if (this.route === 'accounts') {
        FeatureAccounts({ client: this.client, onBack: () => this.onBack(),
          onNavigate: (route: string) => this.onNavigate(route), onChanged: () => this.onChanged() })
      } else if (this.route === 'about') {
        FeatureAbout({ client: this.client, onBack: () => this.onBack(), onNavigate: (route: string) => this.onNavigate(route) })
      } else if (this.queryRoute()) {
        FeatureList({ client: this.client, route: this.route, refreshTrigger: this.refreshTrigger,
          onBack: () => this.onBack(), onNavigate: (route: string) => this.onNavigate(route),
          onChanged: () => this.onChanged(), onEdit: (request: FeatureEditorRequest) => this.openEditor(request) })
      } else {
        FeatureHeader({ title: '页面', onBack: () => this.onBack() })
        Column() {
          FeatureError({ message: '页面暂时无法打开，请返回后重试', onRetry: () => this.onBack() })
        }.padding(20).layoutWeight(1).width('100%')
      }
    }.width('100%').height('100%').backgroundColor($r('app.color.background'))
      .bindSheet($$this.editing, this.editorSheet(), bottomSheetOptions((action: DismissSheetAction) => {
        if (!this.editorBusy) action.dismiss();
      }))
  }
}
