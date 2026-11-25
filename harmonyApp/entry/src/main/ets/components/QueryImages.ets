import { DisplayItem } from '../model/NativeModels';
import { localImage, withOpacity } from './AppComponents';
import { FeatureError } from './FeatureControls';

@Component
export struct BackgroundGallery {
  @Prop items: DisplayItem[] = [];
  @Prop busy: boolean = false;
  onSelect: (item: DisplayItem) => void = () => {};

  build() {
    Grid() {
      ForEach(this.items, (item: DisplayItem) => {
        GridItem() {
          Stack() {
            Image(localImage(item.image || item.url)).width('100%').height('100%').objectFit(ImageFit.Cover)
            if (item.selected) {
              Stack() {
                Circle().width(36).height(36).fill($r('app.color.primary'))
                Image($rawfile('shared/ic_action_done.svg')).width(20).height(20).fillColor($r('app.color.on_primary'))
              }.width('100%').height('100%').backgroundColor('#40000000')
            }
          }.width('100%').aspectRatio(9 / 16).borderRadius(16).clip(true)
            .border({ width: item.selected ? 3 : 0.5, color: item.selected ? $r('app.color.primary') : withOpacity($r('app.color.outline'), 0.4) })
            .accessibilityText(item.title + (item.selected ? '，已选中' : '')).enabled(!this.busy)
            .onClick(() => this.onSelect(item))
        }
      }, (item: DisplayItem) => item.id + ':' + item.image + ':' + (item.selected ? 'selected' : 'normal'))
      // 末行间距 10vp 加上占位 78vp，给浮动按钮留出随内容滚动的 88vp。
      GridItem() { Row().height(78) }.columnStart(0).columnEnd(2).accessibilityLevel('no')
    }.columnsTemplate('1fr 1fr 1fr').columnsGap(10).rowsGap(10).width('100%').height('100%')
      .padding({ left: 12, right: 12, top: 8 }).scrollBar(BarState.Auto)
  }
}

export enum CalendarImageState { Loading, Ready, Failed }

@Component
export struct SchoolCalendarCanvas {
  @Prop image: string = '';
  @Prop imageState: CalendarImageState = CalendarImageState.Loading;
  onResult: (state: CalendarImageState) => void = () => {};
  onRetry: () => void = () => {};
  @State private zoom: number = 1;
  @State private x: number = 0;
  @State private y: number = 0;
  private baseZoom: number = 1;
  private baseX: number = 0;
  private baseY: number = 0;
  private viewportWidth: number = 0;
  private viewportHeight: number = 0;
  private imageRatio: number = 1;

  private clampPosition(): void {
    const fitWidth = Math.min(this.viewportWidth, this.viewportHeight * this.imageRatio);
    const fitHeight = Math.min(this.viewportHeight, this.viewportWidth / this.imageRatio);
    const maxX = Math.max(0, (fitWidth * this.zoom - this.viewportWidth) / 2);
    const maxY = Math.max(0, (fitHeight * this.zoom - this.viewportHeight) / 2);
    this.x = Math.max(-maxX, Math.min(maxX, this.x));
    this.y = Math.max(-maxY, Math.min(maxY, this.y));
  }

  build() {
    Stack() {
      if (this.imageState === CalendarImageState.Failed) {
        FeatureError({ message: '校历图片加载失败', onRetry: () => this.onRetry() })
      } else {
        Stack() {
          Image(this.image).width('100%').height('100%').objectFit(ImageFit.Contain).draggable(false)
            .scale({ x: this.zoom, y: this.zoom }).translate({ x: this.x, y: this.y }).accessibilityText('校历图片，可双指缩放和拖动')
            .onComplete((event) => {
              if (event?.loadingStatus !== 1) return;
              this.imageRatio = event.width / Math.max(1, event.height);
              this.clampPosition();
              this.onResult(CalendarImageState.Ready);
            }).onError(() => this.onResult(CalendarImageState.Failed))
          if (this.imageState === CalendarImageState.Loading) { LoadingProgress().width(40).height(40).color($r('app.color.primary')) }
        }.width('100%').height('100%')
          .gesture(GestureGroup(GestureMode.Parallel,
            PinchGesture({ fingers: 2 }).onActionStart(() => { this.baseZoom = this.zoom; })
              .onActionUpdate((event: GestureEvent) => { this.zoom = Math.max(1, Math.min(6, this.baseZoom * event.scale)); this.clampPosition(); }),
            PanGesture().onActionStart(() => { this.baseX = this.x; this.baseY = this.y; })
              .onActionUpdate((event: GestureEvent) => { this.x = this.baseX + event.offsetX; this.y = this.baseY + event.offsetY; this.clampPosition(); }),
            TapGesture({ count: 2 }).onAction(() => { this.zoom = this.zoom > 1 ? 1 : 2; this.x = 0; this.y = 0; })
          ))
      }
    }.width('100%').height('100%').clip(true)
      .onAreaChange((_old: Area, area: Area) => { this.viewportWidth = Number(area.width); this.viewportHeight = Number(area.height); this.clampPosition(); })
  }
}
