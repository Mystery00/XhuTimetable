import { DetailField, DisplayItem } from '../model/NativeModels';

export function detailValue(item: DisplayItem, key: string, fallback: string = ''): string {
  return item.fields?.find((field: DetailField) => field.key === key)?.value ?? fallback;
}

export interface FeedbackStatus {
  state: string;
  message: string;
  adminOnline: boolean;
}

export interface FeatureKindRequest {
  kind: string;
}

export interface FeatureQueryRequest {
  kind: string;
  page: number;
  year: number;
  term: number;
  user: string;
  area: string;
  weeks: number[];
  days: number[];
  periods: number[];
  campus: string;
  college: string;
  major: string;
  courseName: string;
  teacherName: string;
}

export interface FeatureEditorRequest {
  kind: string;
  id: string;
  user: string;
  year: number;
  term: number;
}

export interface FeatureSaveRequest {
  kind: string;
  id: string;
  user: string;
  year: number;
  term: number;
  values: Record<string, string>;
}

export interface FeatureSettingRequest {
  key: string;
  value: string;
}

export interface FeatureAccountRequest {
  operation: string;
  id: string;
}

export interface FeatureAvatarRequest {
  mode: string;
  value: string;
}

export interface FeatureTextRequest {
  content: string;
}

export interface FeatureUrlRequest {
  url: string;
}

export interface FeatureShareRequest {
  text: string;
}
