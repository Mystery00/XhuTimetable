import { drawing } from '@kit.ArkGraphics2D';
import { ColorMetrics } from '@kit.ArkUI';

// 与 Compose Icon 的 SrcIn 一致：先渲染 SVG，再只对已有像素着色，保留描边和透明区域。
export function iconTint(tint: ResourceColor): drawing.ColorFilter {
  const color = ColorMetrics.resourceColor(tint);
  return drawing.ColorFilter.createBlendModeColorFilter({ alpha: color.alpha, red: color.red,
    green: color.green, blue: color.blue }, drawing.BlendMode.SRC_IN);
}
