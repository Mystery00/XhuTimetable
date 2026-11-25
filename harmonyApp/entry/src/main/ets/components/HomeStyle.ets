import { ColorMetrics } from '@kit.ArkUI';
import { HomeState } from '../model/NativeModels';

export function withOpacity(color: ResourceColor, opacity: number): string {
  const value = ColorMetrics.resourceColor(color);
  return ColorMetrics.rgba(value.red, value.green, value.blue, Math.max(0, Math.min(1, opacity))).color;
}

export function createEmptyHomeState(): HomeState {
  return { title: '', week: 1, actualWeek: 1, calendarEnabled: false, multiAccount: false, loading: false,
    error: '', showTomorrow: false, poem: '', holiday: '', holidayColor: '', background: '', backgroundBlur: 0,
    cellHeight: 70, cellRadius: 4, textSize: 10, cellOpacity: 0.8, todayOpacity: 1, hideBackgroundInDark: true,
    today: [], things: [], weeks: [], weekOverview: [], calendar: [], practical: [] };
}

function linearChannel(value: number): number {
  const channel = value / 255;
  return channel <= 0.04045 ? channel / 12.92 : Math.pow((channel + 0.055) / 1.055, 2.4);
}

function srgbChannel(value: number): number {
  const channel = value <= 0.0031308 ? value * 12.92 : 1.055 * Math.pow(value, 1 / 2.4) - 0.055;
  return Math.round(Math.max(0, Math.min(1, channel)) * 255);
}

function colorLuminance(color: ResourceColor): number {
  const value = ColorMetrics.resourceColor(color);
  return 0.2126 * linearChannel(value.red) + 0.7152 * linearChannel(value.green) + 0.0722 * linearChannel(value.blue);
}

// Compose Color.lerp 在 Oklab 空间插值，不能用 sRGB 通道直接平均代替。
function toOklab(color: ResourceColor): number[] {
  const value = ColorMetrics.resourceColor(color);
  const r = linearChannel(value.red);
  const g = linearChannel(value.green);
  const b = linearChannel(value.blue);
  const l = Math.cbrt(0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b);
  const m = Math.cbrt(0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b);
  const s = Math.cbrt(0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b);
  return [0.2104542553 * l + 0.793617785 * m - 0.0040720468 * s,
    1.9779984951 * l - 2.428592205 * m + 0.4505937099 * s,
    0.0259040371 * l + 0.7827717662 * m - 0.808675766 * s];
}

function lerpColor(start: ResourceColor, end: ResourceColor, fraction: number): string {
  const a = toOklab(start);
  const b = toOklab(end);
  const light = a[0] + (b[0] - a[0]) * fraction;
  const greenRed = a[1] + (b[1] - a[1]) * fraction;
  const blueYellow = a[2] + (b[2] - a[2]) * fraction;
  const l = Math.pow(light + 0.3963377774 * greenRed + 0.2158037573 * blueYellow, 3);
  const m = Math.pow(light - 0.1055613458 * greenRed - 0.0638541728 * blueYellow, 3);
  const s = Math.pow(light - 0.0894841775 * greenRed - 1.291485548 * blueYellow, 3);
  return ColorMetrics.rgba(
    srgbChannel(4.0767416621 * l - 3.3077115913 * m + 0.2309699292 * s),
    srgbChannel(-1.2684380046 * l + 2.6097574011 * m - 0.3413193965 * s),
    srgbChannel(-0.0041960863 * l - 0.7034186147 * m + 1.707614701 * s), 1).color;
}

// 与 ui/theme/Color.kt 的 CourseColorHelper 使用相同混色、阈值与透明度。
export function courseAccent(raw: ResourceColor, dark: boolean): ResourceColor {
  const luminance = colorLuminance(raw);
  if (dark) return luminance < 0.35 ? lerpColor(raw, '#FFFFFF', 0.48) : raw;
  return luminance > 0.55 ? lerpColor(raw, '#000000', 0.35) : raw;
}

export function courseSoftContainer(raw: ResourceColor, dark: boolean, alpha: number = 0.94): string {
  return withOpacity(dark ? lerpColor('#1E2228', raw, 0.22) : lerpColor('#FFFFFF', raw, 0.14), alpha);
}

export function weekCellContainer(raw: ResourceColor, dark: boolean, alpha: number): string {
  return withOpacity(dark ? lerpColor(raw, '#1A1E24', 0.24) : lerpColor(raw, '#FFFFFF', 0.06),
    Math.max(0.25, Math.min(1, alpha)));
}

export function readableCourseText(background: ResourceColor): string {
  return colorLuminance(background) > 0.52 ? '#1C2024' : '#FFFFFF';
}

export function isNonCurrentCourseColor(raw: ResourceColor): boolean {
  const value = ColorMetrics.resourceColor(raw);
  return value.red === 229 && value.green === 229 && value.blue === 229;
}
