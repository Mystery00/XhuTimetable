import { AbilityConstant, UIAbility, Want } from '@kit.AbilityKit';
import { window } from '@kit.ArkUI';

export default class TestFixtureAbility extends UIAbility {
  onCreate(want: Want, launchParam: AbilityConstant.LaunchParam): void {
    this.selectFixture(want);
  }

  onNewWant(want: Want, launchParam: AbilityConstant.LaunchParam): void {
    this.selectFixture(want);
  }

  private selectFixture(want: Want): void {
    const value = want.parameters?.['route'];
    const route = typeof value === 'string' ? value : 'today';
    AppStorage.setOrCreate('fixtureRoute', route);
    AppStorage.setOrCreate('fixtureGeneration', (AppStorage.get<number>('fixtureGeneration') ?? 0) + 1);
  }

  onWindowStageCreate(stage: window.WindowStage): void {
    stage.loadContent('pages/TestFixture');
  }
}
