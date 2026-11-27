import { Want } from '@kit.AbilityKit';
import { formBindingData, FormExtensionAbility, formInfo } from '@kit.FormKit';
import {
  buildFormBinding,
  FORM_NEXT_COURSE,
  refreshFormById,
  registerForm,
  unregisterForm,
} from '../widget/WidgetStore';

export default class EntryFormAbility extends FormExtensionAbility {
  onAddForm(want: Want): formBindingData.FormBindingData {
    const params = want.parameters;
    const formId = params ? String(params[formInfo.FormParam.IDENTITY_KEY] ?? '') : '';
    const formName = params ? String(params[formInfo.FormParam.NAME_KEY] ?? FORM_NEXT_COURSE) : FORM_NEXT_COURSE;
    const rawDim = params ? Number(params[formInfo.FormParam.DIMENSION_KEY] ?? 2) : 2;
    const dimension = Number.isFinite(rawDim) ? rawDim : 2;
    registerForm(this.context, formId, formName, dimension);
    return buildFormBinding(this.context, formName, dimension);
  }

  onUpdateForm(formId: string): void {
    refreshFormById(this.context, formId).catch(() => {});
  }

  onFormEvent(formId: string, _message: string): void {
    refreshFormById(this.context, formId).catch(() => {});
  }

  onRemoveForm(formId: string): void {
    unregisterForm(this.context, formId);
  }

  onAcquireFormState(_want: Want): formInfo.FormState {
    return formInfo.FormState.READY;
  }
}
