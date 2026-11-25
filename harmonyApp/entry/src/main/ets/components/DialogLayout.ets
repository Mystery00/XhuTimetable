import { common } from '@kit.AbilityKit';

const DIALOG_HORIZONTAL_INSETS = 48;
export const DIALOG_CARD_WIDTH = `calc(100% - ${DIALOG_HORIZONTAL_INSETS}vp)`;

export function dialogWidth(context: UIContext): number | undefined {
  try {
    const host = context.getHostContext() as common.UIAbilityContext;
    const width = host.windowStage.getMainWindowSync().getWindowProperties().windowRect.width;
    return width > 0 ? Math.max(1, context.px2vp(width) - DIALOG_HORIZONTAL_INSETS) : undefined;
  } catch {
    return undefined;
  }
}
