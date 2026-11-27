import { execFileSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { HvigorNode, HvigorPlugin } from '@ohos/hvigor';
import { appTasks, OhosAppContext, OhosPluginId } from '@ohos/hvigor-ohos-plugin';

function resolveMainModuleVersion(repoRoot: string): {
  versionCode: number;
  versionName: string;
} {
  const libsTomlPath = path.join(repoRoot, 'gradle', 'libs.versions.toml');
  const libsToml = fs.readFileSync(libsTomlPath, 'utf-8');
  const versionMatch = libsToml.match(/^\s*app-version\s*=\s*"([^"]+)"/m);
  if (!versionMatch) {
    throw new Error(`未在 ${libsTomlPath} 中找到 app-version 配置`);
  }
  // 与 iOS 保持一致，使用标准 x.y.z 版本号，满足 AppGallery Connect 审核校验要求。
  const versionName = versionMatch[1].trim();
  const gitVersionCode = Number.parseInt(
    execFileSync('git', ['rev-list', 'HEAD', '--count'], {
      cwd: repoRoot,
      encoding: 'utf-8',
    }).trim(),
    10,
  );
  if (!Number.isFinite(gitVersionCode) || gitVersionCode <= 0) {
    throw new Error(`无法解析有效的 git 提交计数作为 versionCode: ${gitVersionCode}`);
  }
  return {
    versionCode: gitVersionCode,
    versionName,
  };
}

// 构建时从主模块 gradle/libs.versions.toml 与 git 同步版本号。
// 发布签名在 CI 中通过 HarmonyOS SDK 官方 hap-sign-tool.jar 对构建出的 .app 直接签名，
// 本地真机调试仍沿用 DevEco Studio 的签名配置。
function syncMainModuleVersionPlugin(): HvigorPlugin {
  return {
    pluginId: 'syncMainModuleVersionPlugin',
    apply(node: HvigorNode): void {
      node.afterNodeEvaluate((currentNode: HvigorNode) => {
        const appContext = currentNode.getContext(OhosPluginId.OHOS_APP_PLUGIN) as OhosAppContext | undefined;
        if (!appContext) {
          return;
        }
        const repoRoot = path.resolve(appContext.getProjectPath(), '..');
        const { versionCode, versionName } = resolveMainModuleVersion(repoRoot);
        const appJsonOpt = appContext.getAppJsonOpt();
        appJsonOpt.app.versionCode = versionCode;
        appJsonOpt.app.versionName = versionName;
        appContext.setAppJsonOpt(appJsonOpt);
      });
    },
  };
}

export default {
  system: appTasks,
  plugins: [syncMainModuleVersionPlugin()],
};
