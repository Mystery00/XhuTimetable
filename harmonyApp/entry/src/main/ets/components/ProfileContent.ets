import { ProfileMenu, UserSummary } from '../model/NativeModels';
import { localImage, withOpacity } from './AppComponents';
import { ProfileUiIcon } from './ProfileUiControls';

interface ProfileInfo {
  label: string;
  value: string;
}

@Component
export struct ProfileContent {
  @Prop users: UserSummary[] = [];
  @Prop avatar: string = '';
  @Prop menuGroups: ProfileMenu[][] = [];
  version: string = '';
  onNavigate: (route: string) => void = () => {};
  onMenu: (menu: ProfileMenu) => void = () => {};
  @State private expanded: boolean = false;
  @StorageProp('harmonyDark') private dark: boolean = false;
  private quickKeys: string[] = ['QUERY_EXAM', 'QUERY_SCORE', 'EXP_SCORE', 'QUERY_FREE_ROOM', 'QUERY_TIMETABLE'];

  private user(): UserSummary | undefined {
    return this.users.find((item: UserSummary) => item.main) ?? this.users[0];
  }

  private quickMenus(): ProfileMenu[] {
    const menus: ProfileMenu[] = [];
    // MenuStore 已按 group/sort 排好；与 Android 一样只抽取 quick keys，保持原顺序。
    this.menuGroups.forEach((group: ProfileMenu[]) => {
      group.forEach((menu: ProfileMenu) => {
        if (this.quickKeys.includes(menu.key.toUpperCase())) menus.push(menu);
      });
    });
    return menus;
  }

  private remainingGroups(): ProfileMenu[][] {
    return this.menuGroups.map((group: ProfileMenu[]) =>
      group.filter((menu: ProfileMenu) => !this.quickKeys.includes(menu.key.toUpperCase())))
      .filter((group: ProfileMenu[]) => group.length > 0);
  }

  private infoRows(): ProfileInfo[][] {
    const user = this.user();
    if (user === undefined) return [];
    const fields: ProfileInfo[] = [];
    if (user.gender) fields.push({ label: '性别', value: user.gender });
    if (user.grade) fields.push({ label: '年级', value: user.grade.toString() + '级' });
    if (user.college) fields.push({ label: '学院', value: user.college });
    if (user.major) fields.push({ label: '专业', value: user.major });
    if (user.direction) fields.push({ label: '方向', value: user.direction });
    const rows: ProfileInfo[][] = [];
    for (let index = 0; index < fields.length; index += 2) rows.push(fields.slice(index, index + 2));
    return rows;
  }

  @Builder
  private menuIcon(menu: ProfileMenu) {
    if (menu.icon.startsWith('ic_action_')) {
      Image($rawfile('shared/' + menu.icon + '.svg')).width(24).height(24)
        .fillColor($r('app.color.text_primary')).accessibilityLevel('no')
    } else {
      Image($rawfile('shared/' + (menu.icon || 'ic_unknown_menu') + (this.dark ? '_night' : '') + '.svg'))
        .width(24).height(24).objectFit(ImageFit.Contain).accessibilityLevel('no')
    }
  }

  @Builder
  private menuGroup(menus: ProfileMenu[]) {
    Column() {
      ForEach(menus, (menu: ProfileMenu, index: number) => {
        Row({ space: 14 }) {
          this.menuIcon(menu)
          Text(menu.title).fontSize(16).fontColor($r('app.color.text_primary')).layoutWeight(1)
          if (menu.unread) { Circle().width(8).height(8).fill($r('app.color.danger')).accessibilityText('有未读消息') }
          ProfileUiIcon({ kind: 'next', iconSize:20 })
        }.padding({ left: 16, right: 16, top: 14, bottom: 14 }).width('100%')
          .id('profile-menu-' + menu.key).onClick(() => this.onMenu(menu)).accessibilityGroup(true)
        if (index < menus.length - 1) {
          Divider().color($r('app.color.outline')).opacity(0.4).margin({ left: 56, right: 16 })
        }
      }, (menu: ProfileMenu) => menu.key)
    }.width('100%').backgroundColor($r('app.color.card')).borderRadius(20)
      .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.4) }).clip(true)
  }

  build() {
    Scroll() {
      Column({ space: 14 }) {
        Column() {
          Row({ space: 14 }) {
            Image(localImage(this.avatar)).width(58).height(58).objectFit(ImageFit.Cover).borderRadius(16)
              .accessibilityText('个人头像')
            Column({ space: 3 }) {
              Text(this.user()?.name ?? '账号未登录').fontSize(16).fontWeight(FontWeight.Bold)
                .fontColor($r('app.color.text_primary')).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
              if (this.user() !== undefined) {
                Text(this.user()?.className || this.user()?.id || '').fontSize(12).fontColor($r('app.color.text_secondary'))
                  .maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
                Text('学号 ' + (this.user()?.id ?? '')).fontSize(11).fontColor($r('app.color.on_secondary_container'))
                  .padding({ left: 7, right: 7, top: 2, bottom: 2 })
                  .backgroundColor(withOpacity($r('app.color.secondary_container'), 0.7)).borderRadius(6)
              }
            }.layoutWeight(1).alignItems(HorizontalAlign.Start)
            ProfileUiIcon({ kind: this.expanded ? 'up' : 'down' })
          }.width('100%').padding(16).id('profile-card').accessibilityGroup(true)
            .accessibilityText(this.expanded ? '收起账号详情' : '展开账号详情').onClick(() => {
              if (this.user() !== undefined) {
                this.getUIContext().animateTo({ duration: 180 }, () => { this.expanded = !this.expanded; });
              }
            })
          if (this.expanded && this.user() !== undefined) {
            Column({ space: 8 }) {
              Divider().color($r('app.color.outline')).opacity(0.45)
              Column({ space: 6 }) {
                ForEach(this.infoRows(), (row: ProfileInfo[]) => {
                  Row({ space: 12 }) {
                    ForEach(row, (field: ProfileInfo) => {
                      Column() {
                        Text(field.label).fontSize(11).fontColor($r('app.color.text_secondary'))
                        Text(field.value).fontSize(12).fontWeight(FontWeight.Medium).fontColor($r('app.color.text_primary'))
                      }.layoutWeight(1).alignItems(HorizontalAlign.Start)
                    }, (field: ProfileInfo) => field.label)
                    if (row.length === 1) { Blank().layoutWeight(1) }
                  }.width('100%').alignItems(VerticalAlign.Top)
                }, (row: ProfileInfo[]) => JSON.stringify(row))
              }.width('100%')
              Row() {
                Blank()
                Button({ type: ButtonType.Normal }) {
                  Row({ space: 4 }) {
                    Image($rawfile('shared/ic_action_multi_user.svg')).width(16).height(16).fillColor($r('app.color.primary'))
                    Text('切换或管理账号').fontSize(14).fontColor($r('app.color.primary'))
                  }
                }.height(40).backgroundColor(Color.Transparent).id('profile-manage-accounts')
                  .onClick(() => this.onNavigate('accounts'))
              }.width('100%')
            }.padding({ left: 16, right: 16, bottom: 12 }).width('100%')
          }
        }.width('100%').backgroundColor($r('app.color.card')).borderRadius(22)
          .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.4) })
        if (this.quickMenus().length > 0) {
          Column({ space: 10 }) {
            Text('常用查询').fontSize(14).fontWeight(600).fontColor($r('app.color.primary'))
              .width('100%').padding({ left: 6 })
            Row() {
              ForEach(this.quickMenus(), (menu: ProfileMenu) => {
                Column({ space: 6 }) {
                  Stack() { this.menuIcon(menu) }.width(44).height(44)
                    .backgroundColor(withOpacity($r('app.color.primary_container'), 0.55)).borderRadius(14)
                  Text(menu.title).fontSize(12).fontWeight(FontWeight.Medium).fontColor($r('app.color.text_primary'))
                    .textAlign(TextAlign.Center).maxLines(1).textOverflow({ overflow: TextOverflow.Ellipsis })
                }.layoutWeight(1).padding({ top: 8, bottom: 8, left: 2, right: 2 }).borderRadius(14)
                  .id('profile-quick-' + menu.key).onClick(() => this.onMenu(menu)).accessibilityGroup(true)
              }, (menu: ProfileMenu) => JSON.stringify(menu))
            }.width('100%')
          }.padding({ left: 12, right: 12, top: 14, bottom: 14 }).width('100%')
            .backgroundColor($r('app.color.card')).borderRadius(22)
            .border({ width: 0.5, color: withOpacity($r('app.color.outline'), 0.4) })
        }
        ForEach(this.remainingGroups(), (group: ProfileMenu[]) => { this.menuGroup(group) },
          (group: ProfileMenu[]) => JSON.stringify(group))
        Blank().height(8)
      }.width('100%').padding({ left: 16, right: 16, top: 12, bottom: 12 })
    }.width('100%').height('100%').align(Alignment.TopStart).scrollBar(BarState.Off).backgroundColor($r('app.color.background'))
  }
}
