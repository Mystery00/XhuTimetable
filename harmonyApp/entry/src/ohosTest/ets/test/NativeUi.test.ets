import { Want } from '@kit.AbilityKit';
import { window } from '@kit.ArkUI';
import { image } from '@kit.ImageKit';
import { inputMethod } from '@kit.IMEKit';
import { abilityDelegatorRegistry, Component, Driver, MatchPattern, ON, On, Rect } from '@kit.TestKit';
import { describe, expect, it } from '@ohos/hypium';

// 仅用 ohosTest 中的离线合成数据驱动真实组件，不启动 native 或发送线上写请求。
async function openFixture(route: string): Promise<Driver> {
  const want: Want = {
    bundleName: abilityDelegatorRegistry.getArguments().bundleName,
    moduleName: 'entry_test', abilityName: 'TestFixtureAbility', parameters: { route: route }
  };
  await abilityDelegatorRegistry.getAbilityDelegator().startAbility(want);
  const driver = Driver.create();
  if (driver === null || driver === undefined) throw new Error('UITest Driver 尚未初始化');
  // 首次隐私弹层会遮住夹具页标记，直接等待弹层中的实际操作。
  if (route === 'privacy') {
    await onScreen(driver, ON.text('同意并继续'), '等待首次隐私对话框');
    await driver.delayMs(700);
    return driver;
  }
  const marker = await driver.waitForComponent(ON.text('UI 测试 · ' + route), 5000);
  if (marker === null || marker === undefined) {
    throw new Error('测试页面未就绪：' + route + '; ' + await fixtureTextSnapshot(driver));
  }
  let readyText: string;
  switch (route) {
    case 'starting': readyText = '正在准备课表数据…'; break;
    case 'today':
    case 'today-hide-status': readyText = '测试高等数学'; break;
    case 'empty': readyText = '今天没有课哦~'; break;
    case 'week':
    case 'week-practical': readyText = '测试高等数学'; break;
    case 'calendar':
      // 不用 scrollSearch，防止测试替产品完成自动定位。
      await onScreen(driver, ON.id('calendar-day-2026-11-25'), '月历初始化自动定位今天');
      await driver.delayMs(700);
      await driver.waitForIdle(250, 3000);
      return driver;
    case 'calendar-after-term': readyText = '第1周  11月23日至29日'; break;
    case 'calendar-empty': readyText = '还没有日程数据'; break;
    case 'timetable': readyText = '按条件搜索全校课程'; break;
    case 'feedback': readyText = '已连接 · 留言即可收到回复'; break;
    case 'profile': readyText = '常用查询'; break;
    case 'accounts': readyText = '已登录账号 (2)'; break;
    case 'settings': readyText = '深色与主题模式'; break;
    case 'classSettings': readyText = '显示今日课程状态'; break;
    case 'courseColors': readyText = '测试高等数学'; break;
    case 'courseColors-empty': readyText = '暂无课程数据'; break;
    case 'customUi': readyText = '今日课程页面'; break;
    case 'customCourses': readyText = '测试旁听课程'; break;
    case 'customThings':
    case 'customThings-delete-retry': readyText = '测试图书归还'; break;
    case 'rooms-filter-retry': readyText = '设置条件查询空闲教室'; break;
    case 'scores':
    case 'scores-loading':
    case 'scores-error':
    case 'scores-refresh': readyText = '第1学期'; break;
    case 'login':
      await visible(driver, ON.id('login-username'), '初始化 login：等待学号输入框');
      await driver.delayMs(700);
      return driver;
    default: throw new Error('没有定义测试页面就绪条件：' + route);
  }
  await visible(driver, ON.text(readyText), '初始化 ' + route + '：等待实际页面内容「' + readyText + '」');
  // 模拟器的窗口转场晚于 UI 树就绪，过早注入触摸会被系统丢弃。
  await driver.delayMs(700);
  if (route !== 'scores-loading') await driver.waitForIdle(250, 3000);
  return driver;
}

async function fixtureTextSnapshot(driver: Driver): Promise<string> {
  const texts: string[] = [];
  try {
    const components = await driver.findComponents(ON.type('Text'));
    for (const component of (components ?? []).slice(0, 60)) {
      try {
        const text = await component.getText();
        const bounds = await component.getBounds();
        if (text.length > 0) texts.push(text.substring(0, 100) + ' @' + bounds.top + '..' + bounds.bottom);
      } catch {
        texts.push('<控件在采集时更新>');
      }
    }
  } catch {
    return '当前页面文本无法采集';
  }
  return '当前页面文本=' + JSON.stringify(texts);
}

async function onScreen(driver: Driver, selector: On, stage: string): Promise<Component> {
  const component = await driver.waitForComponent(selector, 3000);
  if (component === null || component === undefined) {
    throw new Error(stage + '：未找到控件（此步骤禁止自动滚动）; ' + await fixtureTextSnapshot(driver));
  }
  return component;
}

async function visible(driver: Driver, selector: On, stage: string = '等待目标控件'): Promise<Component> {
  let component: Component | undefined = undefined;
  try {
    component = await driver.waitForComponent(selector, 3000);
    if (component === null || component === undefined) {
      // Sheet 下方的列表及外观预览也可能可滚动，优先当前表单的内容容器。
      let scroll = await driver.findComponent(ON.id('editor-scroll').scrollable(true));
      if (scroll === null || scroll === undefined) scroll = await driver.findComponent(ON.id('custom-ui-scroll').scrollable(true));
      if (scroll === null || scroll === undefined) scroll = await driver.findComponent(ON.type('List').scrollable(true));
      if (scroll === null || scroll === undefined) scroll = await driver.findComponent(ON.type('Scroll').scrollable(true));
      if (scroll !== null && scroll !== undefined) component = await scroll.scrollSearch(selector);
    }
  } catch (error) {
    throw new Error(stage + '：查询失败：' + (error as Error).message + '; ' + await fixtureTextSnapshot(driver));
  }
  if (component === null || component === undefined) {
    throw new Error(stage + '：等待与滚动查询均未找到目标控件; ' + await fixtureTextSnapshot(driver));
  }
  return component;
}

async function absent(driver: Driver, selector: On, stage: string = '等待旧控件消失'): Promise<void> {
  const deadline = Date.now() + 2000;
  while (true) {
    const component = await driver.findComponent(selector);
    if (component === null || component === undefined) return;
    if (Date.now() >= deadline) {
      throw new Error(stage + '：状态更新后旧控件仍然存在; ' + await fixtureTextSnapshot(driver));
    }
    await driver.delayMs(100);
  }
}

async function clickTarget(driver: Driver, selector: On, stage: string): Promise<void> {
  await driver.waitForIdle(250, 3000);
  try {
    for (let attempt = 0; attempt < 4; attempt++) {
      const target = await visible(driver, selector, stage);
      const before = await target.getBounds();
      await driver.delayMs(180);
      const after = await target.getBounds();
      if (Math.abs(before.left - after.left) > 1 || Math.abs(before.top - after.top) > 1 ||
        Math.abs(before.right - after.right) > 1 || Math.abs(before.bottom - after.bottom) > 1) continue;
      await driver.click(Math.round((after.left + after.right) / 2), Math.round((after.top + after.bottom) / 2));
      await driver.delayMs(300);
      await driver.waitForIdle(400, 3000);
      return;
    }
    throw new Error('控件仍在滚动或调整位置');
  } catch (error) {
    throw new Error(stage + '：点击失败：' + (error as Error).message + '; ' + await fixtureTextSnapshot(driver));
  }
}

async function clickText(driver: Driver, text: string, stage: string = '点击文字'): Promise<void> {
  await clickTarget(driver, ON.text(text), stage + '「' + text + '」');
}

async function back(driver: Driver): Promise<void> {
  await inputMethod.getController().hideTextInput().catch(() => {});
  await driver.delayMs(250);
  await driver.pressBack();
  await driver.delayMs(500);
  await driver.waitForIdle(400, 3000);
}

async function selectedOptions(driver: Driver, labels: string[], stage: string): Promise<void> {
  const deadline = Date.now() + 2000;
  while (true) {
    const checks = await driver.findComponents(ON.type('Text').text('✓')) ?? [];
    const matched = new Set<number>();
    if (checks.length === labels.length) {
      for (const label of labels) {
        const options = await driver.findComponents(ON.type('Text').text(label)) ?? [];
        for (const option of options) {
          let found = false;
          for (let index = 0; index < checks.length; index++) {
            if (matched.has(index)) continue;
            try {
              const optionBounds = await option.getBounds();
              const checkBounds = await checks[index].getBounds();
              const centerDifference = Math.abs((optionBounds.top + optionBounds.bottom - checkBounds.top - checkBounds.bottom) / 2);
              if (optionBounds.bottom > optionBounds.top && checkBounds.bottom > checkBounds.top &&
                centerDifference <= Math.min(optionBounds.bottom - optionBounds.top, checkBounds.bottom - checkBounds.top) / 4 &&
                checkBounds.left >= optionBounds.right - 2) {
                matched.add(index);
                found = true;
                break;
              }
            } catch {}
          }
          if (found) break;
        }
      }
      if (matched.size === labels.length) return;
    }
    if (Date.now() >= deadline) {
      throw new Error(stage + '：选项与同一行勾选不匹配，预期=' + JSON.stringify(labels) + '; ' + await fixtureTextSnapshot(driver));
    }
    await driver.delayMs(100);
  }
}

async function textInCard(driver: Driver, cardId: string, text: string, present: boolean = true): Promise<void> {
  const card = await visible(driver, ON.id(cardId), '定位当前卡片 ' + cardId);
  const bounds = await card.getBounds();
  const matches = await driver.findComponents(ON.type('Text').text(text)) ?? [];
  let found = false;
  for (const match of matches) {
    const child = await match.getBounds();
    if (child.left >= bounds.left - 2 && child.right <= bounds.right + 2 &&
      child.top >= bounds.top - 2 && child.bottom <= bounds.bottom + 2) found = true;
  }
  expect(found).assertEqual(present);
}

async function checkCourseColorRow(driver: Driver, name: string, custom: boolean): Promise<void> {
  const row = ON.id('course-color-row-' + name);
  await onScreen(driver, ON.text(name).within(row), '课程名称 ' + name);
  const reset = ON.id('course-color-reset-' + name);
  if (custom) {
    const button = await (await onScreen(driver, reset, '行上恢复默认 ' + name)).getBounds();
    const bounds = await (await onScreen(driver, row, '定位课程行 ' + name)).getBounds();
    expect(button.top >= bounds.top - 2 && button.bottom <= bounds.bottom + 2).assertTrue();
  } else {
    await absent(driver, reset, '默认色课程没有行上重置 ' + name);
  }
  await textInCard(driver, 'course-color-row-' + name, '已自定义', custom);
}

async function checkRowSwatch(name: string, expectedColor: string): Promise<void> {
  const ability = await abilityDelegatorRegistry.getAbilityDelegator().getCurrentTopAbility();
  const fixtureWindow = await window.getLastWindow(ability.context);
  const snapshot = await fixtureWindow.getUIContext().getComponentSnapshot()
    .get('course-color-row-' + name, { waitUntilRenderFinished: true });
  try {
    const info = await snapshot.getImageInfo();
    const width = info.size.width;
    const buffer = new ArrayBuffer(snapshot.getPixelBytesNumber());
    await snapshot.readPixelsToBuffer(buffer);
    const bgra = info.pixelFormat === image.PixelMapFormat.BGRA_8888;
    expect(bgra || info.pixelFormat === image.PixelMapFormat.RGBA_8888).assertTrue();
    const expected = Number.parseInt(expectedColor.slice(1), 16);
    const bytes = new Uint8Array(buffer, Math.floor(info.size.height / 2) * snapshot.getBytesNumberPerRow(), width * 4);
    // 在卡片中线上查找实心圆内部的连续像素，避开描边和抗锯齿。
    let run = 0;
    for (let i = 0; i < bytes.length; i += 4) {
      const rgb = (bytes[i + (bgra ? 2 : 0)] << 16) | (bytes[i + 1] << 8) | bytes[i + (bgra ? 0 : 2)];
      run = rgb === expected ? run + 1 : 0;
      if (run >= 8) return;
    }
    const x = Math.round(width * 0.085) * 4;
    throw new Error(name + ' 预期色块 ' + expectedColor + '，像素格式 ' + info.pixelFormat + '，原始采样 ' + bytes.slice(x, x + 4).join(','));
  } finally {
    await snapshot.release();
  }
}

async function openCourseColor(driver: Driver, name: string, expectedColor: string): Promise<void> {
  await onScreen(driver, ON.id('course-color-row-' + name), '定位待编辑的课程');
  await checkRowSwatch(name, expectedColor);
  await clickTarget(driver, ON.id('course-color-row-' + name), '编辑课程颜色 ' + name);
  await onScreen(driver, ON.text('精选色板'), '打开选色弹窗');
  await onScreen(driver, ON.type('Text').text(expectedColor), '核对 ' + name + ' 的当前色');
}

async function adjustCourseRgb(driver: Driver, initialColor: string): Promise<string> {
  await clickText(driver, '自定义');
  const labels = ['红 (R)', '绿 (G)', '蓝 (B)'];
  const positions = [0.75, 0.5, 0.25];
  const sliders = await driver.findComponents(ON.type('Slider')) ?? [];
  expect(sliders.length).assertEqual(labels.length);
  const tracks: Rect[] = [];
  for (const slider of sliders) tracks.push(await slider.getBounds());
  tracks.sort((a: Rect, b: Rect) => a.top - b.top);
  let color = initialColor;
  for (let index = 0; index < labels.length; index++) {
    await onScreen(driver, ON.text(labels[index]), '调整 ' + labels[index]);
    const track = tracks[index];
    const x = track.left + (track.right - track.left) * positions[index];
    await driver.click(Math.round(x), Math.round((track.top + track.bottom) / 2));
    await absent(driver, ON.text(color), '通道调整后更新预览');
    const preview = await onScreen(driver, ON.type('Text').text('#', MatchPattern.STARTS_WITH), '读取 RGB 预览');
    const next = await preview.getText();
    expect(/^#[0-9A-F]{6}$/.test(next)).assertTrue();
    const start = 1 + index * 2;
    expect(next.slice(start, start + 2) !== color.slice(start, start + 2)).assertTrue();
    expect(next.slice(0, start)).assertEqual(color.slice(0, start));
    expect(next.slice(start + 2)).assertEqual(color.slice(start + 2));
    color = next;
  }
  return color;
}

async function fixtureWindowBounds(driver: Driver): Promise<Rect> {
  const window = await driver.findWindow({ bundleName: abilityDelegatorRegistry.getArguments().bundleName, focused: true });
  return window.getBounds();
}

async function featureDialogCardBounds(driver: Driver, title: string): Promise<Rect> {
  const dialog = ON.type('Dialog');
  const heading = await (await onScreen(driver, ON.type('Text').text(title).within(dialog), title)).getBounds();
  const cancel = await (await onScreen(driver, ON.type('Button').text('取消').within(dialog), title + '取消按钮')).getBounds();
  // 原生布局中标题与底部操作分属卡片的不同子节点，系统还会在卡片外包一层透明 Column。
  const columns = await driver.findComponents(ON.type('Column').within(dialog)) ?? [];
  let card: Rect | undefined = undefined;
  for (const column of columns) {
    const bounds = await column.getBounds();
    if (bounds.left <= Math.min(heading.left, cancel.left) && bounds.right >= Math.max(heading.right, cancel.right) &&
      bounds.top <= heading.top && bounds.bottom >= cancel.bottom &&
      (!card || (bounds.left >= card.left && bounds.right <= card.right && bounds.top >= card.top && bounds.bottom <= card.bottom))) {
      card = bounds;
    }
  }
  if (!card) throw new Error(title + '：未找到同时包含标题与底部操作的对话框卡片');
  return card;
}

async function checkDialogWidth(driver: Driver, card: Rect, title: string): Promise<void> {
  const viewport = await fixtureWindowBounds(driver);
  const ability = await abilityDelegatorRegistry.getAbilityDelegator().getCurrentTopAbility();
  const ui = (await window.getLastWindow(ability.context)).getUIContext();
  const width = viewport.right - viewport.left;
  const expectedGap = ui.vp2px(24);
  const leftGap = card.left - viewport.left;
  const rightGap = viewport.right - card.right;
  // UITest 的可见边界取整为物理像素，分别核对两侧以捕获等宽但偏移的弹框。
  if (Math.abs(leftGap - expectedGap) > 2 || Math.abs(rightGap - expectedGap) > 2) {
    throw new Error(title + '：窗口宽 ' + width + 'px，预期两侧各 ' + expectedGap + 'px，实际左 ' + leftGap + 'px、右 ' + rightGap + 'px');
  }
}

async function sheetTextBounds(driver: Driver, sheet: Rect, text: string): Promise<Rect> {
  const node = await onScreen(driver, ON.text(text).within(ON.id('timetable-sheet')), '弹层正文「' + text + '」');
  const bounds = await node.getBounds();
  expect(bounds.right > bounds.left && bounds.bottom > bounds.top).assertTrue();
  expect(bounds.left >= sheet.left && bounds.right <= sheet.right &&
    bounds.top >= sheet.top && bounds.bottom <= sheet.bottom).assertTrue();
  return bounds;
}

async function shortSheetBounds(driver: Driver, page: Rect, title: string): Promise<Rect> {
  const sheet = await (await onScreen(driver, ON.id('timetable-sheet'), title)).getBounds();
  const height = (sheet.bottom - sheet.top) / (page.bottom - page.top);
  const bottomGap = Math.abs(page.bottom - sheet.bottom) / (page.bottom - page.top);
  // 以夹具窗口为基准，底部留出系统导航区的余量。
  if (!(height > 0 && height < 0.70 && bottomGap < 0.08)) {
    throw new Error(title + '：短弹层高度占比=' + height + '，距窗口底部占比=' + bottomGap);
  }
  const handle = await (await onScreen(driver, ON.id('timetable-sheet-handle').within(ON.id('timetable-sheet')), '弹层手柄')).getBounds();
  const heading = await sheetTextBounds(driver, sheet, title);
  expect(handle.right > handle.left && handle.bottom > handle.top).assertTrue();
  expect(sheet.top <= handle.top && handle.bottom <= heading.top).assertTrue();
  return sheet;
}

async function dismissByMask(driver: Driver, page: Rect, sheet: Rect): Promise<void> {
  const x = Math.round((sheet.left + sheet.right) / 2);
  const y = Math.round(page.top + (sheet.top - page.top) * 0.75);
  await driver.click(x, y);
  await absent(driver, ON.id('timetable-sheet'), '点击遮罩关闭弹层');
  await absent(driver, ON.type('SheetWrapper'), '等待退场遮罩释放触摸');
  await driver.waitForIdle(500, 3000);
}

async function todayAtListStart(driver: Driver): Promise<void> {
  await driver.waitForIdle(400, 3000);
  const list = await onScreen(driver, ON.id('home-calendar-list'), '日程列表');
  const today = await onScreen(driver, ON.id('calendar-day-2026-11-25'), '今天的日程');
  const listBounds = await list.getBounds();
  const dayBounds = await today.getBounds();
  expect(Math.abs(dayBounds.top - listBounds.top) < 8).assertTrue();
  expect(dayBounds.bottom > listBounds.top && dayBounds.top < listBounds.bottom).assertTrue();
  await onScreen(driver, ON.id('fixture-home-title').text('第1周'), '今天所在周标题');
}

async function scrollToCalendarEnd(driver: Driver, list: Component): Promise<void> {
  const bounds = await list.getBounds();
  const x = Math.round((bounds.left + bounds.right) / 2);
  const distance = bounds.bottom - bounds.top;
  for (let attempt = 0; attempt < 8; attempt++) {
    const last = await driver.findComponent(ON.id('calendar-day-2026-12-06'));
    const title = await driver.findComponent(ON.id('fixture-home-title').text('第2周'));
    if (last && title) {
      const day = await last.getBounds();
      if (day.bottom > day.top && day.bottom <= bounds.bottom && day.top >= bounds.top) return;
    }
    await driver.swipe(x, Math.round(bounds.top + distance * 0.82), x, Math.round(bounds.top + distance * 0.22), 1200);
    await driver.waitForIdle(250, 1500);
  }
  throw new Error('连续滑动后仍未到达第二周末');
}

async function singleEditorToolbar(driver: Driver): Promise<void> {
  await visible(driver, ON.id('editor-save'), 'Sheet 顶栏保存');
  const saves = await driver.findComponents(ON.type('Button').text('保存')) ?? [];
  expect(saves.length).assertEqual(1);
  const saveBounds = await saves[0].getBounds();
  const closeBounds = await (await visible(driver, ON.id('editor-close'))).getBounds();
  expect(Math.abs((saveBounds.top + saveBounds.bottom - closeBounds.top - closeBounds.bottom) / 2) < 8).assertTrue();
  const deletes = await driver.findComponents(ON.id('editor-delete')) ?? [];
  expect(deletes.length).assertEqual(1);
  const deleteBounds = await deletes[0].getBounds();
  expect(Math.abs((deleteBounds.top + deleteBounds.bottom - closeBounds.top - closeBounds.bottom) / 2) < 8).assertTrue();
}

export default function nativeUiTest(): void {
  describe('NativeUiFixture', () => {
    it('首次启动叠加隐私提示，外部点击与返回保留选择，同意失败可重试', 0, async () => {
      let driver = await openFixture('starting');
      const name = await onScreen(driver, ON.text('西瓜课表'), '启动应用名');
      const progress = await onScreen(driver, ON.type('LoadingProgress'), '启动进度');
      const preparing = await onScreen(driver, ON.text('正在准备课表数据…'), '启动进度说明');
      const brand = await onScreen(driver, ON.id('startup-brand'), '底部品牌字标');
      expect((await name.getBounds()).bottom <= (await progress.getBounds()).top).assertTrue();
      expect((await progress.getBounds()).bottom <= (await preparing.getBounds()).top).assertTrue();
      expect((await preparing.getBounds()).bottom < (await brand.getBounds()).top).assertTrue();
      await absent(driver, ON.text('隐私政策提示'));

      driver = await openFixture('privacy');
      const body = await onScreen(driver, ON.id('startup-privacy-body'), '隐私提示正文');
      expect(await body.getText()).assertEqual('本应用尊重并保护所有用户的个人隐私权。为了向你提供课程表同步、成绩与考试查询等服务，本应用会按照隐私政策的规定使用必要信息。请仔细阅读 《隐私政策》。');
      const decline = await onScreen(driver, ON.text('不同意并退出'), '拒绝按钮');
      const accept = await onScreen(driver, ON.text('同意并继续'), '同意按钮');
      const left = await decline.getBounds();
      const right = await accept.getBounds();
      expect(left.right <= right.left && left.top < right.bottom && right.top < left.bottom).assertTrue();
      const card = await onScreen(driver, ON.id('startup-privacy-card'), '隐私对话框');
      const bounds = await card.getBounds();
      await checkDialogWidth(driver, bounds, '启动隐私提示');
      await driver.click(Math.floor((bounds.left + bounds.right) / 2), Math.floor(bounds.top / 2));
      await onScreen(driver, ON.text('隐私政策提示'), '点击遮罩保留弹层');
      await back(driver);
      await onScreen(driver, ON.text('隐私政策提示'), '系统返回保留弹层');

      await clickText(driver, '同意并继续');
      await onScreen(driver, ON.text('正在准备课表数据…'), '保存期间回到启动进度');
      await absent(driver, ON.text('同意并继续'));
      await clickText(driver, '完成离线保存');
      await onScreen(driver, ON.text('离线模拟：同意未保存'), '失败后提示并保留选择');
      await clickText(driver, '同意并继续');
      await clickText(driver, '完成离线保存');
      await onScreen(driver, ON.id('login-username'), '重试成功后进入登录页');
      await absent(driver, ON.text('隐私政策提示'));

      driver = await openFixture('privacy');
      await clickText(driver, '不同意并退出');
      await onScreen(driver, ON.text('已退出首次流程'), '拒绝触发退出动作');
      await absent(driver, ON.id('login-username'));
    });

    it('今日课程详情显示独立起止时间与地点并能复制地点', 0, async () => {
      const driver = await openFixture('today');
      await onScreen(driver, ON.text('未开始'), '开启状态显示时列表有课程徽标');
      await visible(driver, ON.text('08:30'));
      await visible(driver, ON.text('10:05'));
      await clickText(driver, '测试高等数学');
      await visible(driver, ON.text('上课时间'));
      await visible(driver, ON.text('第1-2节  (08:30 - 10:05)'));
      await visible(driver, ON.text('上课地点'));
      await visible(driver, ON.text('测试楼 A101'));
      await visible(driver, ON.text('任课教师'));
      await visible(driver, ON.text('测试教师甲'));
      await clickTarget(driver, ON.id('home-copy-上课地点'), '复制详情地点');
      await back(driver);
      await visible(driver, ON.id('fixture-copied-text').text('测试楼 A101'));
      await absent(driver, ON.text('上课时间'));
    });

    it('关闭今日状态后列表隐藏徽标，详情保留该课程的真实状态', 0, async () => {
      const driver = await openFixture('today-hide-status');
      await onScreen(driver, ON.text('测试高等数学'), '未开始的课程在列表中');
      await absent(driver, ON.text('未开始'), '列表隐藏未开始徽标');
      await visible(driver, ON.text('测试大学英语'), '开课中的课程在列表中');
      await absent(driver, ON.text('开课中'), '列表隐藏开课中徽标');
      await clickText(driver, '测试大学英语');
      const sheet = ON.id('timetable-sheet');
      await onScreen(driver, ON.text('测试大学英语').within(sheet), '打开同一门课程详情');
      await onScreen(driver, ON.text('开课中').within(sheet), '详情仍有开课中徽标');
    });

    it('今日详情与添加日程短弹层贴底，点击遮罩或下拖手柄回到今日', 0, async () => {
      const driver = await openFixture('today');
      const page = await fixtureWindowBounds(driver);
      await clickText(driver, '测试高等数学');
      const detail = await shortSheetBounds(driver, page, '测试高等数学');
      await sheetTextBounds(driver, detail, '上课时间');
      await dismissByMask(driver, page, detail);

      await clickTarget(driver, ON.id('fixture-home-add'), '从今日页打开添加日程');
      const add = await shortSheetBounds(driver, page, '添加日程数据');
      await sheetTextBounds(driver, add, '自定义事项 / 倒计时');
      const handle = await (await onScreen(driver, ON.id('timetable-sheet-handle'), '下拖添加弹层手柄')).getBoundsCenter();
      await driver.swipe(handle.x, handle.y, handle.x, Math.round(add.bottom - (add.bottom - add.top) * 0.1));
      await absent(driver, ON.id('timetable-sheet'), '下拖手柄关闭添加弹层');
      await onScreen(driver, ON.text('测试高等数学'), '回到今日课程列表');
    });

    it('今日空态刷新后展示课程及起止时间', 0, async () => {
      const driver = await openFixture('empty');
      await clickText(driver, '刷新课表');
      await visible(driver, ON.text('测试高等数学'));
      await visible(driver, ON.text('08:30'));
      await visible(driver, ON.text('10:05'));
      await visible(driver, ON.text('测试楼 A101'));
      await absent(driver, ON.text('今天没有课哦~'));
    });

    it('周表冲突课程在详情Sheet切换后显示各自教师地点', 0, async () => {
      const driver = await openFixture('week');
      await visible(driver, ON.text('+1'));
      await clickText(driver, '测试高等数学');
      await visible(driver, ON.text('1. 测试高等数学'));
      await visible(driver, ON.text('2. 测试大学物理'));
      await visible(driver, ON.text('测试教师甲'));
      await clickText(driver, '2. 测试大学物理');
      await visible(driver, ON.text('测试教师乙'));
      await visible(driver, ON.text('测试楼 B202'));
      await visible(driver, ON.text('第1-2周'));
      await clickText(driver, '1. 测试高等数学');
      await visible(driver, ON.text('测试楼 A101'));
    });

    it('工具栏选周Sheet切第二周并按本周语义返回', 0, async () => {
      const driver = await openFixture('week');
      await clickTarget(driver, ON.id('fixture-home-week'), '打开选周Sheet');
      await visible(driver, ON.text('切换教学周'));
      await clickTarget(driver, ON.id('home-week-2'), '选择第二周');
      await visible(driver, ON.id('fixture-home-week').text('第2周'));
      await visible(driver, ON.text('测试程序设计'));
      await absent(driver, ON.text('切换教学周'));
      await clickTarget(driver, ON.id('fixture-home-week'), '再次打开选周Sheet');
      await clickText(driver, '回到第1周');
      await visible(driver, ON.id('fixture-home-week').text('第1周'));
      await visible(driver, ON.text('测试高等数学'));
      await clickTarget(driver, ON.id('fixture-home-week'), '重新切到第二周');
      await clickTarget(driver, ON.id('home-week-2'), '第二周');
      await clickTarget(driver, ON.id('fixture-home-today'), '工具栏回本周');
      await visible(driver, ON.id('fixture-home-week').text('第1周'));
      await visible(driver, ON.text('测试高等数学'));
      await clickTarget(driver, ON.id('fixture-home-practical'), '打开实践课程');
      await visible(driver, ON.text('本学期暂无实践课程安排'));
      await back(driver);
      await absent(driver, ON.text('本学期暂无实践课程安排'));
    });

    it('两条实践课程用短弹层展示，冲突详情可横滑，选周可点到末行', 0, async () => {
      const driver = await openFixture('week-practical');
      const page = await fixtureWindowBounds(driver);
      await clickTarget(driver, ON.id('fixture-home-practical'), '打开两条实践课程');
      const practical = await shortSheetBounds(driver, page, '本学期实践课程');
      const first = await sheetTextBounds(driver, practical, '测试电子实习');
      const second = await sheetTextBounds(driver, practical, '测试金工实习');
      expect(first.bottom <= second.top).assertTrue();
      await dismissByMask(driver, page, practical);

      await clickText(driver, '测试高等数学');
      const conflict = await (await onScreen(driver, ON.id('timetable-sheet'), '冲突课程详情')).getBounds();
      const teacher = await sheetTextBounds(driver, conflict, '测试教师甲');
      const y = Math.round((teacher.top + teacher.bottom) / 2);
      const width = conflict.right - conflict.left;
      await driver.swipe(Math.round(conflict.left + width * 0.8), y, Math.round(conflict.left + width * 0.2), y);
      await driver.waitForIdle(400, 3000);
      await sheetTextBounds(driver, conflict, '测试教师乙');
      await sheetTextBounds(driver, conflict, '测试楼 B202');
      await back(driver);
      await absent(driver, ON.id('timetable-sheet'), '回到冲突课程所在周');

      await clickTarget(driver, ON.id('fixture-home-week'), '打开选周表');
      const picker = ON.id('timetable-sheet');
      const scroll = await driver.findComponent(ON.scrollable(true).within(picker));
      if (scroll) await scroll.scrollToBottom();
      await driver.waitForIdle(400, 3000);
      const last = await onScreen(driver, ON.id('home-week-20').within(picker), '选周表末行的第20周');
      await last.click();
      await absent(driver, picker, '选中末行后关闭周次表');
      await onScreen(driver, ON.id('fixture-home-week').text('第20周'), '底层课表切到第20周');
    });

    it('月历按周纵向浏览自动定位今天跨月滚动后可回到今天', 0, async () => {
      const driver = await openFixture('calendar');
      await todayAtListStart(driver);
      await visible(driver, ON.id('calendar-day-2026-11-26'));
      await textInCard(driver, 'calendar-day-2026-11-26', '测试体育');
      await textInCard(driver, 'calendar-day-2026-11-26', '16:00-17:30，测试操场');
      const list = await visible(driver, ON.id('home-calendar-list'));
      const nextWeek = await list.scrollSearch(ON.text('第2周  11月30日至12月6日'));
      expect(nextWeek !== null && nextWeek !== undefined).assertTrue();
      const december = await list.scrollSearch(ON.text('2026年12月'));
      expect(december !== null && december !== undefined).assertTrue();
      await visible(driver, ON.id('calendar-day-2026-12-01'));
      await textInCard(driver, 'calendar-day-2026-12-01', '测试程序设计');
      await scrollToCalendarEnd(driver, list);
      await driver.waitForIdle(400, 3000);
      await onScreen(driver, ON.id('fixture-home-title').text('第2周'), '滚动后的周标题');
      await onScreen(driver, ON.id('calendar-day-2026-12-06'), '列表确实滚到第二周末');
      await clickTarget(driver, ON.id('calendar-today'), '浮动按钮回今天');
      await todayAtListStart(driver);
      await scrollToCalendarEnd(driver, list);
      await driver.waitForIdle(400, 3000);
      await clickTarget(driver, ON.id('fixture-home-today'), '工具栏回今天');
      await todayAtListStart(driver);
    });

    it('学期结束后跨周滚动无回今天浮钮，空月历正文无刷新课表按钮', 0, async () => {
      const driver = await openFixture('calendar-after-term');
      await onScreen(driver, ON.id('fixture-home-title').text('第1周'), '期外月历从首周开始');
      const list = await onScreen(driver, ON.id('home-calendar-list'), '有历史日程可滚动');
      await scrollToCalendarEnd(driver, list);
      await absent(driver, ON.id('calendar-today'), '滚到第二周末仍无回今天浮钮');

      const empty = await openFixture('calendar-empty');
      const content = ON.id('fixture-home-content');
      await onScreen(empty, ON.text('还没有日程数据').within(content), '确认空月历正文已渲染');
      await absent(empty, ON.text('刷新课表').within(content), '空态不重复提供顶栏的刷新操作');
    });

    it('个人页常用查询保持Android顺序同一行且进入成绩页', 0, async () => {
      const driver = await openFixture('profile');
      const keys = ['QUERY_EXAM', 'QUERY_SCORE', 'EXP_SCORE', 'QUERY_FREE_ROOM', 'QUERY_TIMETABLE'];
      const first = await (await visible(driver, ON.id('profile-quick-' + keys[0]))).getBounds();
      let right = first.left;
      for (const key of keys) {
        const bounds = await (await onScreen(driver, ON.id('profile-quick-' + key), '常用查询同一行')).getBounds();
        expect(Math.abs(bounds.top - first.top) < 3).assertTrue();
        expect(bounds.left >= right - 2).assertTrue();
        right = bounds.right;
      }
      await clickTarget(driver, ON.id('profile-quick-QUERY_SCORE'), '常用查询进入成绩页');
      await visible(driver, ON.text('成绩查询'));
      await visible(driver, ON.text('测试高等数学'));
      await visible(driver, ON.text('88'));
    });

    it('shortProfileContentStartsAtTop', 0, async () => {
      const driver = await openFixture('profile');
      const ability = await abilityDelegatorRegistry.getAbilityDelegator().getCurrentTopAbility();
      const stage = ability.context.windowStage;
      const originalDensity = stage.getMainWindowSync().getUIContext().vp2px(1);
      const viewport = await fixtureWindowBounds(driver);
      try {
        const wideDensity = (viewport.bottom - viewport.top) / 1200;
        stage.setCustomDensity(wideDensity);
        const deadline = Date.now() + 5000;
        let leftInset = 0;
        let topInset = 0;
        do {
          const scroll = await (await onScreen(driver, ON.type('Scroll'), '个人页视口')).getBounds();
          const card = await (await onScreen(driver, ON.id('profile-card'), '首张账号名片')).getBounds();
          const last = await (await onScreen(driver, ON.id('profile-menu-SETTINGS'), '短菜单末项')).getBounds();
          leftInset = card.left - scroll.left;
          topInset = card.top - scroll.top;
          if (Math.abs(leftInset - wideDensity * 16.5) <= 2 && scroll.bottom - last.bottom > 100 * wideDensity &&
            Math.abs(topInset - leftInset * 12 / 16) <= 2) return;
          await driver.delayMs(150);
        } while (Date.now() < deadline);
        throw new Error('短菜单名片边距与实际窗口密度不符，左 ' + leftInset + 'px，顶 ' + topInset + 'px，密度 ' + wideDensity);
      } finally {
        stage.setCustomDensity(originalDensity);
        await driver.waitForIdle(500, 4000);
      }
    });

    it('个人名片展开后管理账号切换主账号并回显完整资料', 0, async () => {
      const driver = await openFixture('profile');
      await absent(driver, ON.id('profile-manage-accounts'));
      await clickTarget(driver, ON.id('profile-card'), '展开账号详情');
      await visible(driver, ON.text('测试计算机一班'));
      await visible(driver, ON.text('男'));
      await visible(driver, ON.text('2026级'));
      await visible(driver, ON.text('测试系统方向'));
      await clickTarget(driver, ON.id('profile-manage-accounts'), '展开后管理账号');
      await visible(driver, ON.text('账号管理'));
      await clickTarget(driver, ON.id('account-card-20990002'), '点击第二个账号切换主账号');
      await textInCard(driver, 'account-card-20990002', '当前主账号');
      await back(driver);
      await visible(driver, ON.text('测试同学乙'));
      await visible(driver, ON.text('测试软件二班'));
      await visible(driver, ON.text('学号 20990002'));
      await clickTarget(driver, ON.id('profile-card'), '展开新主账号资料');
      await visible(driver, ON.text('女'));
      await visible(driver, ON.text('2025级'));
      await visible(driver, ON.text('软件工程'));
      await visible(driver, ON.text('测试软件方向'));
    });

    it('成绩加载结束后选择第二学期更新列表且筛选回显', 0, async () => {
      const driver = await openFixture('scores-loading');
      await visible(driver, ON.type('LoadingProgress').enabled(true));
      await visible(driver, ON.text('测试高等数学'));
      await visible(driver, ON.text('88'));
      await clickText(driver, '第1学期');
      await visible(driver, ON.text('选择学期'));
      await clickTarget(driver, ON.text('第2学期').within(ON.type('Dialog')), '选择第二学期');
      await visible(driver, ON.text('测试离散数学'));
      await visible(driver, ON.text('96'));
      await absent(driver, ON.text('测试高等数学'));
      await clickText(driver, '第2学期');
      await selectedOptions(driver, ['第2学期'], '学期选择回显');
      await clickText(driver, '取消');
    });

    it('课程设置开关双向保存且离开重进保持已保存状态', 0, async () => {
      const driver = await openFixture('classSettings');
      expect(await (await visible(driver, ON.id('setting-toggle-showStatus'))).isChecked()).assertFalse();
      await clickTarget(driver, ON.id('setting-toggle-showStatus'), '开启今日课程状态');
      expect(await (await visible(driver, ON.id('setting-toggle-showStatus'))).isChecked()).assertTrue();
      await back(driver);
      await clickTarget(driver, ON.id('profile-menu-CLASS_SETTING'), '重进课程设置');
      expect(await (await visible(driver, ON.id('setting-toggle-showStatus'))).isChecked()).assertTrue();
      await clickTarget(driver, ON.id('setting-toggle-showStatus'), '关闭今日课程状态');
      expect(await (await visible(driver, ON.id('setting-toggle-showStatus'))).isChecked()).assertFalse();
      await back(driver);
      await clickTarget(driver, ON.id('profile-menu-CLASS_SETTING'), '验证关闭值已保存');
      expect(await (await visible(driver, ON.id('setting-toggle-showStatus'))).isChecked()).assertFalse();
    });

    it('课程颜色按行回显与应用色板和 RGB，取消保留原色，行上和弹窗均能恢复默认', 0, async () => {
      const driver = await openFixture('courseColors');
      const math = '测试高等数学';
      const physics = '测试大学物理';
      await checkCourseColorRow(driver, math, false);
      await checkCourseColorRow(driver, physics, true);

      await openCourseColor(driver, math, '#2E86DE');
      await checkDialogWidth(driver, await featureDialogCardBounds(driver, math), '课程色板');
      await clickTarget(driver, ON.id('color-swatch-#E55353'), '选择红色色板');
      await onScreen(driver, ON.type('Text').text('#E55353'), '预览选中的色板颜色');
      await clickText(driver, '应用');
      await checkCourseColorRow(driver, math, true);
      await openCourseColor(driver, math, '#E55353');
      await clickTarget(driver, ON.id('color-swatch-#8E55B8'), '选择另一颜色后取消');
      await onScreen(driver, ON.type('Text').text('#8E55B8'), '取消前草稿确实已改变');
      await clickText(driver, '取消');

      await openCourseColor(driver, physics, '#3142EA');
      const editedColor = await adjustCourseRgb(driver, '#3142EA');
      await clickText(driver, '应用');
      await checkCourseColorRow(driver, physics, true);
      // 重新查询后核对两个课程，避免只更新弹窗或列表的临时状态也能通过。
      await clickTarget(driver, ON.id('course-color-search'), '重查已保存的课程颜色');
      await (await visible(driver, ON.id('course-color-keywords'))).inputText('物理');
      await absent(driver, ON.id('course-color-row-' + math));
      await back(driver);
      await onScreen(driver, ON.id('course-color-row-' + math), '恢复全部课程后核对保存结果');
      await openCourseColor(driver, physics, editedColor);
      await clickText(driver, '恢复默认');
      await checkCourseColorRow(driver, physics, false);
      await openCourseColor(driver, physics, '#16A085');
      await clickText(driver, '取消');

      await checkCourseColorRow(driver, math, true);
      await openCourseColor(driver, math, '#E55353');
      await clickText(driver, '取消');
      await clickTarget(driver, ON.id('course-color-reset-' + math), '从数学课程行恢复默认');
      await absent(driver, ON.text('精选色板'), '行上重置不会打开弹窗');
      await checkCourseColorRow(driver, math, false);
      await openCourseColor(driver, math, '#2E86DE');
      await clickText(driver, '取消');
    });

    it('课程颜色按课程名原文搜索，清空或返回恢复列表，区分无匹配与无课程', 0, async () => {
      const driver = await openFixture('courseColors');
      const search = ON.id('course-color-search');
      const keywords = ON.id('course-color-keywords');
      const clear = ON.id('course-color-search-clear');
      const math = ON.id('course-color-row-测试高等数学');
      const physics = ON.id('course-color-row-测试大学物理');
      await clickTarget(driver, search, '顶栏展开课程搜索');
      await (await visible(driver, keywords)).inputText('物理');
      await absent(driver, math, '按部分课程名排除数学');
      await onScreen(driver, physics, '保留名称匹配的物理课');
      await (await visible(driver, keywords)).inputText(' 物理 ');
      await onScreen(driver, ON.text('未找到匹配课程'), '保留关键词两侧空格查询');
      await absent(driver, physics);
      await absent(driver, math);
      await clickTarget(driver, clear, '清空关键词');
      await onScreen(driver, keywords, '清空后保留搜索框');
      await onScreen(driver, math, '清空后恢复数学');
      await onScreen(driver, physics, '清空后恢复物理');
      await absent(driver, ON.text('未找到匹配课程'));
      await clickTarget(driver, clear, '无关键词时关闭搜索');
      await absent(driver, keywords);

      await clickTarget(driver, search, '重新打开搜索');
      await (await visible(driver, keywords)).inputText('数学');
      await absent(driver, physics);
      await onScreen(driver, math, '返回前只显示数学');
      await back(driver);
      await absent(driver, keywords, '返回退出搜索');
      await onScreen(driver, search, '仍在课程颜色页');
      await onScreen(driver, physics, '返回恢复全部课程');
      await onScreen(driver, math, '返回保留数学');
      await back(driver);
      await onScreen(driver, ON.id('profile-card'), '再次返回离开课程颜色页');

      const empty = await openFixture('courseColors-empty');
      await onScreen(empty, ON.text('暂无课程数据'), '没有课程的初始空态');
      await absent(empty, ON.text('未找到匹配课程'));
      await absent(empty, math);
      await absent(empty, physics);
    });

    it('事项Sheet取消丢弃草稿保存更新同记录删除需确认', 0, async () => {
      const driver = await openFixture('customThings');
      await clickText(driver, '测试图书归还');
      await visible(driver, ON.text('编辑自定义事项'));
      await singleEditorToolbar(driver);
      await (await visible(driver, ON.id('editor-input-title'))).inputText('测试未保存事项');
      await back(driver);
      await visible(driver, ON.text('测试图书归还'));
      await absent(driver, ON.text('测试未保存事项'));
      await absent(driver, ON.id('editor-input-title'));
      await clickText(driver, '测试图书归还');
      await visible(driver, ON.id('editor-input-title').text('测试图书归还'));
      await (await visible(driver, ON.id('editor-input-title'))).inputText('测试已保存事项');
      await clickTarget(driver, ON.id('editor-save'), '顶栏保存事项');
      await absent(driver, ON.id('editor-input-title'));
      await clickText(driver, '测试已保存事项');
      await visible(driver, ON.id('editor-input-title').text('测试已保存事项'));
      await clickTarget(driver, ON.id('editor-delete'), '顶栏删除事项');
      await visible(driver, ON.text('确认删除'));
      await clickText(driver, '取消');
      await visible(driver, ON.id('editor-input-title').text('测试已保存事项'));
      await clickTarget(driver, ON.id('editor-delete'), '重新删除事项');
      await clickText(driver, '删除');
      await absent(driver, ON.id('editor-save'));
      await visible(driver, ON.text('暂无自定义事项'));
      await absent(driver, ON.text('测试已保存事项'));
    });

    it('有未保存草稿时删除失败，重试仍需确认删除', 0, async () => {
      const driver = await openFixture('customThings-delete-retry');
      await clickText(driver, '测试图书归还');
      const draft = '不应保存的删除草稿';
      await (await visible(driver, ON.id('editor-input-title'))).inputText(draft);
      await visible(driver, ON.id('editor-input-title').text(draft));
      await inputMethod.getController().hideTextInput().catch(() => {});
      await clickTarget(driver, ON.id('editor-delete'), '删除带草稿的事项');
      await clickText(driver, '删除');
      await visible(driver, ON.text('离线模拟：首次删除失败'));
      await clickText(driver, '重试');
      await onScreen(driver, ON.text('确认删除'), '重试必须重新确认');
      await clickText(driver, '取消');
      await visible(driver, ON.id('editor-input-title').text(draft));
      await clickText(driver, '重试');
      await onScreen(driver, ON.text('确认删除'), '取消后再次重试');
      await clickText(driver, '删除');
      await absent(driver, ON.id('editor-save'));
      await visible(driver, ON.text('暂无自定义事项'));
    });

    it('dialogWidthInWideWindow', 0, async () => {
      const driver = await openFixture('settings');
      const ability = await abilityDelegatorRegistry.getAbilityDelegator().getCurrentTopAbility();
      const stage = ability.context.windowStage;
      const ui = stage.getMainWindowSync().getUIContext();
      const original = ui.vp2px(1);
      try {
        const viewport = await fixtureWindowBounds(driver);
        const width = viewport.right - viewport.left;
        stage.setCustomDensity(width / 690);
        await driver.waitForIdle(500, 4000);
        expect(ui.px2vp(width) > 600).assertTrue();
        await clickTarget(driver, ON.id('setting-nightMode'), '宽窗口打开主题模式');
        await checkDialogWidth(driver, await featureDialogCardBounds(driver, '深色与主题模式'), '宽窗口主题模式');
        await clickText(driver, '取消');
      } finally {
        stage.setCustomDensity(original);
        await driver.waitForIdle(500, 4000);
      }
    });

    it('主题模式同键更新回显新值重开选择并能保存回原值', 0, async () => {
      const driver = await openFixture('settings');
      await visible(driver, ON.text('当前：始终关闭'));
      await clickTarget(driver, ON.id('setting-nightMode'), '打开主题模式');
      await checkDialogWidth(driver, await featureDialogCardBounds(driver, '深色与主题模式'), '主题模式');
      await clickText(driver, '始终开启');
      await visible(driver, ON.text('当前：始终开启'));
      await absent(driver, ON.text('当前：始终关闭'));
      await clickTarget(driver, ON.id('setting-nightMode'), '再次打开主题模式');
      await selectedOptions(driver, ['始终开启'], '主题保存回显');
      await clickText(driver, '始终关闭');
      await visible(driver, ON.text('当前：始终关闭'));
      await back(driver);
      await clickTarget(driver, ON.id('profile-menu-SETTINGS'), '重进软件设置');
      await visible(driver, ON.text('当前：始终关闭'));
    });

    it('账号卡同id切换徽标并刷新资料而不残留旧内容', 0, async () => {
      const driver = await openFixture('accounts');
      await textInCard(driver, 'account-card-20990001', '当前主账号');
      await textInCard(driver, 'account-card-20990002', '当前主账号', false);
      await clickTarget(driver, ON.id('account-card-20990002'), '切换主账号');
      await textInCard(driver, 'account-card-20990002', '当前主账号');
      await textInCard(driver, 'account-card-20990001', '当前主账号', false);
      await clickTarget(driver, ON.id('account-refresh-20990002'), '刷新第二个账号资料');
      await textInCard(driver, 'account-card-20990002', '2025级 · 测试学院 · 测试资料已更新 · 测试软件方向');
      await absent(driver, ON.text('2025级 · 测试学院 · 软件工程 · 测试软件方向'));
      await textInCard(driver, 'account-card-20990002', '20990002');
      await textInCard(driver, 'account-card-20990002', '当前主账号');
    });

    it('成绩刷新同id记录替换分数及结构化成绩说明', 0, async () => {
      const driver = await openFixture('scores-refresh');
      await visible(driver, ON.text('测试高等数学'));
      await visible(driver, ON.text('88'));
      await visible(driver, ON.text('成绩说明：平时成绩 90 · 期末成绩 88'));
      await clickTarget(driver, ON.id('query-refresh'), '刷新成绩');
      await visible(driver, ON.text('测试高等数学'));
      await visible(driver, ON.text('91'));
      await visible(driver, ON.text('成绩说明：复核后成绩 91'));
      await absent(driver, ON.text('88'));
      await absent(driver, ON.text('成绩说明：平时成绩 90 · 期末成绩 88'));
    });

    it('课程编辑连续输入保留焦点选择星期与内联五列周次保存回显', 0, async () => {
      const driver = await openFixture('customCourses');
      await clickText(driver, '测试旁听课程');
      await singleEditorToolbar(driver);
      const title = await visible(driver, ON.id('editor-input-courseName'));
      await title.inputText('测试连续输入一');
      expect(await (await visible(driver, ON.id('editor-input-courseName').text('测试连续输入一'))).isFocused()).assertTrue();
      await title.inputText('测试连续输入二');
      expect(await (await visible(driver, ON.id('editor-input-courseName').text('测试连续输入二'))).isFocused()).assertTrue();
      await clickTarget(driver, ON.id('editor-choice-day'), '选择上课星期');
      await clickText(driver, '周二');
      await textInCard(driver, 'editor-choice-day', '周二');
      await clickTarget(driver, ON.id('editor-choice-day'), '验证星期选择状态');
      await selectedOptions(driver, ['周二'], '星期选项同行勾选');
      await clickText(driver, '取消');
      await absent(driver, ON.text('取消'));
      const form = await visible(driver, ON.id('editor-scroll'));
      const sixthWeek = await form.scrollSearch(ON.id('editor-week-6'));
      expect(sixthWeek !== null && sixthWeek !== undefined).assertTrue();
      await driver.waitForIdle(400, 3000);
      // 在同一滚动位置读取两行坐标，不能让查询 helper 在读取中途移动表单。
      const first = await (await onScreen(driver, ON.id('editor-week-1'), '第一周')).getBounds();
      const fifth = await (await onScreen(driver, ON.id('editor-week-5'), '第五周')).getBounds();
      const sixth = await (await onScreen(driver, ON.id('editor-week-6'), '第六周')).getBounds();
      expect(Math.abs(first.top - fifth.top) < 3 && fifth.left > first.right).assertTrue();
      expect(sixth.top >= first.bottom && Math.abs(sixth.left - first.left) < 3).assertTrue();
      await visible(driver, ON.text('已选 1 周'));
      expect(await (await visible(driver, ON.id('editor-week-1'))).isSelected()).assertTrue();
      expect(await (await visible(driver, ON.id('editor-week-2'))).isSelected()).assertFalse();
      await clickTarget(driver, ON.id('editor-week-2'), '内联增选第二周');
      await visible(driver, ON.text('已选 2 周'));
      expect(await (await visible(driver, ON.id('editor-week-2'))).isSelected()).assertTrue();
      await absent(driver, ON.text('确定'), '内联周次不打开二级选择弹窗');
      await clickTarget(driver, ON.id('editor-save'), '保存课程名称星期及周次');
      await absent(driver, ON.id('editor-input-courseName'));
      await visible(driver, ON.text('每周二 · 1-2节'));
      await visible(driver, ON.text('第1、2周'));
      await clickText(driver, '测试连续输入二');
      await visible(driver, ON.id('editor-input-courseName').text('测试连续输入二'));
      await textInCard(driver, 'editor-choice-day', '周二');
      await visible(driver, ON.text('已选 2 周'));
      expect(await (await visible(driver, ON.id('editor-week-1'))).isSelected()).assertTrue();
      expect(await (await visible(driver, ON.id('editor-week-2'))).isSelected()).assertTrue();
      expect(await (await visible(driver, ON.id('editor-week-3'))).isSelected()).assertFalse();
      await clickTarget(driver, ON.id('editor-week-2'), '取消第二周验证已保存选择');
      await visible(driver, ON.text('已选 1 周'));
      await clickTarget(driver, ON.id('editor-week-1'), '取消第一周验证已保存选择');
      await visible(driver, ON.text('未选择'));
      await clickTarget(driver, ON.id('editor-close'), '取消未保存周次草稿');
      await visible(driver, ON.text('第1、2周'));
    });

    it('eventDateAndTimeSelection', 0, async () => {
      const driver = await openFixture('customThings');
      await clickText(driver, '测试图书归还');
      await clickTarget(driver, ON.id('editor-choice-startDate'), '打开事项月历');
      await clickTarget(driver, ON.id('setting-date-next'), '翻到下一月');
      await clickTarget(driver, ON.id('setting-date-day-2026-12-01'), '选择十二月一日');
      await clickTarget(driver, ON.id('setting-date-confirm'), '确认日期');
      await onScreen(driver, ON.id('editor-choice-startDate').text('2026年12月01日'), '表单显示所选日期');
      await clickTarget(driver, ON.id('editor-choice-startTime'), '打开时间拨盘');
      await clickTarget(driver, ON.id('setting-time-dial-hour-17'), '选中下午五时');
      await clickTarget(driver, ON.id('setting-time-dial-minute-15'), '选中十五分');
      await clickTarget(driver, ON.id('setting-time-mode'), '切到键盘输入');
      await onScreen(driver, ON.id('setting-time-input-hour').text('17'), '拨盘小时回填');
      const minutes = await onScreen(driver, ON.id('setting-time-input-minute').text('15'), '拨盘分钟回填');
      expect(await minutes.isFocused()).assertTrue();
      await minutes.inputText('61');
      expect(await (await onScreen(driver, ON.id('setting-time-confirm'), '无效分钟')).isEnabled()).assertFalse();
      await minutes.inputText('23');
      await inputMethod.getController().hideTextInput().catch(() => {});
      await clickTarget(driver, ON.id('setting-time-confirm'), '确认有效时间');
      await onScreen(driver, ON.id('editor-choice-startTime').text('17:23'), '事项草稿回显');
      await clickTarget(driver, ON.id('editor-close'), '放弃本次草稿');
      await clickText(driver, '测试图书归还');
      await onScreen(driver, ON.id('editor-choice-startTime').text('18:00'), '取消编辑保留原时间');
    });

    it('事项倒计时联动全天隐藏时刻关闭倒计时后可恢复时刻', 0, async () => {
      const driver = await openFixture('customThings');
      await clickText(driver, '测试图书归还');
      expect(await (await visible(driver, ON.id('editor-choice-allDay'))).isChecked()).assertFalse();
      await visible(driver, ON.id('editor-choice-startTime').text('18:00'));
      await visible(driver, ON.id('editor-choice-endTime').text('19:00'));
      await clickTarget(driver, ON.id('editor-choice-countdown'), '开启倒计时');
      expect(await (await visible(driver, ON.id('editor-choice-countdown'))).isChecked()).assertTrue();
      const allDay = await visible(driver, ON.id('editor-choice-allDay'));
      expect(await allDay.isChecked()).assertTrue();
      expect(await allDay.isEnabled()).assertFalse();
      await visible(driver, ON.text('目标日期'));
      await absent(driver, ON.id('editor-choice-startTime'));
      await absent(driver, ON.id('editor-choice-endDate'));
      await clickTarget(driver, ON.id('editor-save'), '保存倒计时');
      await absent(driver, ON.id('editor-input-title'));
      await clickText(driver, '测试图书归还');
      expect(await (await visible(driver, ON.id('editor-choice-countdown'))).isChecked()).assertTrue();
      expect(await (await visible(driver, ON.id('editor-choice-allDay'))).isChecked()).assertTrue();
      await clickTarget(driver, ON.id('editor-choice-countdown'), '关闭倒计时');
      expect(await (await visible(driver, ON.id('editor-choice-allDay'))).isEnabled()).assertTrue();
      expect(await (await visible(driver, ON.id('editor-choice-allDay'))).isChecked()).assertTrue();
      await visible(driver, ON.id('editor-choice-endDate'));
      await clickTarget(driver, ON.id('editor-choice-allDay'), '关闭全天');
      expect(await (await visible(driver, ON.id('editor-choice-allDay'))).isChecked()).assertFalse();
      await visible(driver, ON.id('editor-choice-startTime').text('18:00'));
      await visible(driver, ON.id('editor-choice-endTime').text('19:00'));
      await clickTarget(driver, ON.id('editor-save'), '保存普通定时事项');
      await absent(driver, ON.id('editor-input-title'));
      await clickText(driver, '测试图书归还');
      expect(await (await visible(driver, ON.id('editor-choice-countdown'))).isChecked()).assertFalse();
      expect(await (await visible(driver, ON.id('editor-choice-allDay'))).isChecked()).assertFalse();
      await visible(driver, ON.id('editor-choice-startTime').text('18:00'));
    });

    it('首页添加Sheet进入课程管理新建课程后可删除', 0, async () => {
      const driver = await openFixture('today');
      await clickTarget(driver, ON.id('fixture-home-add'), '工具栏添加日程');
      await visible(driver, ON.text('添加日程数据'));
      await clickText(driver, '自定义课程');
      await visible(driver, ON.text('测试旁听课程'));
      await clickTarget(driver, ON.id('custom-entry-add'), '新建自定义课程');
      await visible(driver, ON.text('新建自定义课程'));
      await absent(driver, ON.id('editor-add-note'));
      await absent(driver, ON.id('editor-input-extraData'));
      await absent(driver, ON.id('editor-delete'));
      await (await visible(driver, ON.id('editor-input-courseName'))).inputText('测试新增旁听');
      await inputMethod.getController().hideTextInput().catch(() => {});
      await driver.delayMs(300);
      await clickTarget(driver, ON.id('editor-week-3'), '选择第三周');
      await clickTarget(driver, ON.id('editor-save'), '保存新课程');
      await absent(driver, ON.id('editor-input-courseName'));
      await clickText(driver, '测试新增旁听');
      await visible(driver, ON.text('编辑自定义课程'));
      await absent(driver, ON.id('editor-add-note'));
      await absent(driver, ON.id('editor-input-extraData'));
      await visible(driver, ON.text('已选 1 周'));
      expect(await (await visible(driver, ON.id('editor-week-3'))).isSelected()).assertTrue();
      expect(await (await visible(driver, ON.id('editor-week-1'))).isSelected()).assertFalse();
      await clickTarget(driver, ON.id('editor-delete'), '删除新课程');
      await clickText(driver, '删除');
      await absent(driver, ON.text('测试新增旁听'));
      await visible(driver, ON.text('测试旁听课程'));
    });

    it('界面草稿离开时丢弃不写入保存并退出后重新进入可回显', 0, async () => {
      const driver = await openFixture('customUi');
      const slider = ON.id('custom-ui-slider-todayBackgroundAlpha');
      await visible(driver, ON.text('100%'));
      await clickTarget(driver, slider, '调整卡片不透明度');
      await absent(driver, ON.text('100%'));
      const changed = await visible(driver, ON.type('Text').text('65%'));
      expect(await changed.getText()).assertEqual('65%');
      await clickTarget(driver, ON.id('custom-ui-back'), '携带草稿离开');
      await visible(driver, ON.text('有未保存的更改'));
      await clickText(driver, '直接放弃');
      await visible(driver, ON.id('profile-card'));
      await clickTarget(driver, ON.id('profile-menu-SETTINGS'), '重进软件设置');
      await clickTarget(driver, ON.id('setting-customUi'), '重进界面设置');
      await visible(driver, ON.text('100%'));
      await clickTarget(driver, slider, '再次调整卡片不透明度');
      await visible(driver, ON.text('65%'));
      await clickTarget(driver, ON.id('custom-ui-back'), '选择保存后退出');
      await clickText(driver, '保存并退出');
      await visible(driver, ON.text('软件设置'));
      await clickTarget(driver, ON.id('setting-customUi'), '查看保存后的界面设置');
      await visible(driver, ON.text('65%'));
      await absent(driver, ON.text('100%'));
      await clickTarget(driver, ON.id('custom-ui-back'), '未再次修改直接返回');
      await visible(driver, ON.text('软件设置'));
      await absent(driver, ON.text('有未保存的更改'));
    });

    it('查询失败可重试恢复同一成绩列表', 0, async () => {
      const driver = await openFixture('scores-error');
      await visible(driver, ON.text('测试网络暂不可用'));
      await clickText(driver, '重试');
      await visible(driver, ON.text('测试高等数学'));
      await visible(driver, ON.text('88'));
      await absent(driver, ON.text('测试网络暂不可用'));
    });

    it('多账号全校课表显示未筛选摘要，搜索面板没有加入课表账号', 0, async () => {
      const driver = await openFixture('timetable');
      for (const label of ['校区', '学院', '专业']) {
        await onScreen(driver, ON.text(label), '初态筛选摘要「' + label + '」');
      }
      await onScreen(driver, ON.text('支持按校区、学院、专业、课程名或教师名查找，并可一键蹭课加入课表'), '初次搜索说明');
      await clickText(driver, '开始搜索');
      const sheet = ON.id('timetable-sheet');
      await onScreen(driver, ON.text('全校课表搜索').within(sheet), '打开实际搜索面板');
      for (const value of ['全部校区', '全部学院', '请先选学院']) {
        await visible(driver, ON.text(value).within(sheet), '未选择时的筛选值「' + value + '」');
      }
      const account = ON.text('加入课表账号').within(sheet);
      await absent(driver, account, '筛选表单没有账号选择行');
      const scroll = await driver.findComponent(ON.type('Scroll').scrollable(true).within(sheet));
      if (scroll) {
        const match = await scroll.scrollSearch(account);
        expect(match === null || match === undefined).assertTrue();
      }
    });

    it('反馈草稿插入表情再切回键盘，保留编辑内容且没有发送尝试', 0, async () => {
      const driver = await openFixture('feedback');
      const draft = '离线测试草稿';
      const input = ON.id('feedback-input');
      const toggle = ON.id('feedback-emoji-toggle');
      const panel = ON.id('feedback-emoji-panel');
      const send = ON.id('feedback-send');
      await absent(driver, panel, '初始收起表情面板');
      expect(await (await onScreen(driver, send, '空草稿发送按钮')).isEnabled()).assertFalse();
      await (await onScreen(driver, input, '编辑反馈草稿')).inputText(draft);
      await onScreen(driver, ON.id('feedback-input').text(draft), '草稿已输入');
      expect(await (await onScreen(driver, send, '草稿可发送')).isEnabled()).assertTrue();
      await clickTarget(driver, toggle, '切换至表情面板');
      await clickTarget(driver, ON.id('feedback-emoji-0').within(panel), '选择首个表情');
      const edited = draft + '😀';
      await onScreen(driver, ON.id('feedback-input').text(edited), '表情插入草稿末尾');
      await clickTarget(driver, toggle, '切回键盘');
      await absent(driver, panel, '表情面板已收起');
      await onScreen(driver, ON.id('feedback-input').text(edited), '切换后草稿仍完整');
      await onScreen(driver, ON.text('离线发送尝试 0 次'), '编辑、选择表情和切换均未触发发送');
    });

    it('空闲教室切换节次后首页查询失败，重试替换为新节次的第一页', 0, async () => {
      const driver = await openFixture('rooms-filter-retry');
      await clickText(driver, '开始查询');
      await visible(driver, ON.text('测试教室 A101'));
      await clickText(driver, '全部区域');
      await visible(driver, ON.text('空闲教室筛选'));
      await clickText(driver, '3-4节');
      await onScreen(driver, ON.text('第3、4节').within(ON.id('timetable-sheet')), '筛选面板同步显示新节次');
      await clickText(driver, '开始查询');
      await visible(driver, ON.text('离线模拟：新节次第一页失败'));
      await clickText(driver, '重试');
      await visible(driver, ON.text('测试教室 B202'));
      await absent(driver, ON.text('测试教室 A101'));
      await absent(driver, ON.text('离线模拟：新节次第一页失败'));
    });

    it('loginHeaderAtTopWhenFormFits', 0, async () => {
      const driver = await openFixture('login');
      const ability = await abilityDelegatorRegistry.getAbilityDelegator().getCurrentTopAbility();
      const stage = ability.context.windowStage;
      const original = stage.getMainWindowSync().getUIContext().vp2px(1);
      try {
        const viewport = await fixtureWindowBounds(driver);
        stage.setCustomDensity((viewport.bottom - viewport.top) / 1200);
        await driver.waitForIdle(500, 4000);
        const scroll = await (await onScreen(driver, ON.id('login-scroll'), '登录页面')).getBounds();
        const header = await (await onScreen(driver, ON.id('login-header'), '校园头图')).getBounds();
        const help = await (await onScreen(driver, ON.id('login-help'), '表单底部')).getBounds();
        expect(help.bottom < scroll.bottom).assertTrue();
        expect(Math.abs(header.top - scroll.top) <= 1).assertTrue();
      } finally {
        stage.setCustomDensity(original);
        await driver.waitForIdle(500, 4000);
      }
    });

    it('登录空学号与空密码分别显示必填校验', 0, async () => {
      const driver = await openFixture('login');
      await absent(driver, ON.id('login-username-error'));
      await clickTarget(driver, ON.id('login-submit'), '空表单登录');
      await inputMethod.getController().hideTextInput().catch(() => {});
      await visible(driver, ON.id('login-username-error').text('请输入学号'));
      await (await visible(driver, ON.id('login-username'))).inputText('20990001');
      await inputMethod.getController().hideTextInput().catch(() => {});
      await driver.waitForIdle(400, 3000);
      await clickTarget(driver, ON.id('login-submit'), '只填学号登录');
      await inputMethod.getController().hideTextInput().catch(() => {});
      await absent(driver, ON.id('login-username-error'));
      await visible(driver, ON.id('login-password-support').text('请输入密码'));
    });
  });
}
