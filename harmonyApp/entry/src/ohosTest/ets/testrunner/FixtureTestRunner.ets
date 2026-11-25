import abilityDelegatorRegistry from '@ohos.app.ability.abilityDelegatorRegistry';
import TestRunner from '@ohos.application.testRunner';
import { Hypium } from '@ohos/hypium';

export default class FixtureTestRunner implements TestRunner {
  onPrepare(): void {}

  onRun(): void {
    const delegator = abilityDelegatorRegistry.getAbilityDelegator();
    const args = abilityDelegatorRegistry.getArguments();
    // UITest 必须在 AbilityDelegator 建立后加载，才能自动连接对应测试进程。
    import('../test/List.test').then((suite) => {
      Hypium.hypiumTest(delegator, args, suite.default);
    }).catch(() => {
      delegator.finishTest('交互测试模块加载失败', -1);
    });
  }
}
