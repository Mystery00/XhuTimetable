import { common } from '@kit.AbilityKit';
import { abilityDelegatorRegistry } from '@kit.TestKit';
import { describe, expect, it } from '@ohos/hypium';
import { OhosServices } from '../../../main/ets/platform/OhosServices';

export default function ohosServicesTest(): void {
  describe('OhosServices', () => {
    it('平台消息保留原文并在解绑后停止交付', 0, async () => {
      const context = abilityDelegatorRegistry.getAbilityDelegator().getAppContext() as common.UIAbilityContext;
      const services = new OhosServices(context);
      const messages: string[] = [];
      services.setMessageHandler((message: string) => messages.push(message));
      const payload = '{"id":"1","data":{"title":"课程"},"error":""}';
      await services.nativeMessage(payload);
      services.setMessageHandler(undefined);
      await services.nativeMessage('{"id":"2"}');
      expect(messages.length).assertEqual(1);
      expect(messages[0]).assertEqual(payload);
      services.dispose();
    });

    it('释放后忽略晚到消息且不能重新绑定', 0, async () => {
      const context = abilityDelegatorRegistry.getAbilityDelegator().getAppContext() as common.UIAbilityContext;
      const services = new OhosServices(context);
      let count = 0;
      services.setMessageHandler(() => count++);
      services.dispose();
      await services.nativeMessage('{"id":"1"}');
      services.setMessageHandler(() => count++);
      await services.nativeMessage('{"id":"2"}');
      expect(count).assertEqual(0);
    });

    it('拒绝脚本文件及不完整链接而不拉起外部应用', 0, async () => {
      const context = abilityDelegatorRegistry.getAbilityDelegator().getAppContext() as common.UIAbilityContext;
      const services = new OhosServices(context);
      for (const link of ['javascript:alert(1)', 'data:text/html,test', 'file:///data/test', 'intent://test',
        'https://', 'https://user:password@example.com/', 'https://example.com/\nwrong']) {
        await expect(services.openUrl(link)).assertPromiseIsRejected();
      }
      services.dispose();
      await expect(services.openUrl('https://example.com/')).assertPromiseIsRejected();
      await expect(services.pickImage('')).assertPromiseIsRejected();
    });
  });
}
