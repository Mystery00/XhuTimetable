import { Configuration, ConfigurationConstant, UIAbility } from '@kit.AbilityKit';
import { window } from '@kit.ArkUI';

export default class EntryAbility extends UIAbility {
  onCreate(): void {
    AppStorage.setOrCreate('harmonyDark', this.context.config.colorMode === ConfigurationConstant.ColorMode.COLOR_MODE_DARK);
    this.context.getApplicationContext().setColorMode(ConfigurationConstant.ColorMode.COLOR_MODE_NOT_SET);
  }

  onConfigurationUpdate(configuration: Configuration): void {
    AppStorage.setOrCreate('harmonyDark', configuration.colorMode === ConfigurationConstant.ColorMode.COLOR_MODE_DARK);
  }

  onWindowStageCreate(windowStage: window.WindowStage): void {
    windowStage.loadContent('pages/Index');
  }
}
