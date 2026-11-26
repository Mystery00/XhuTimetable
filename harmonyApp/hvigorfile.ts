import { execFileSync } from 'child_process';
import fs from 'fs';
import path from 'path';
import { HvigorNode, HvigorPlugin } from '@ohos/hvigor';
import { appTasks, OhosAppContext, OhosPluginId } from '@ohos/hvigor-ohos-plugin';

function resolveMainModuleVersion(repoRoot: string, buildMode: string): {
  versionCode: number;
  versionName: string;
} {
  const libsTomlPath = path.join(repoRoot, 'gradle', 'libs.versions.toml');
  const libsToml = fs.readFileSync(libsTomlPath, 'utf-8');
  const versionMatch = libsToml.match(/^\s*app-version\s*=\s*"([^"]+)"/m);
  if (!versionMatch) {
    throw new Error(`未在 ${libsTomlPath} 中找到 app-version 配置`);
  }
  const appVersion = versionMatch[1].trim();
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
  const gitShortHash = execFileSync('git', ['rev-parse', '--short=8', 'HEAD'], {
    cwd: repoRoot,
    encoding: 'utf-8',
  }).trim();
  const normalizedMode = buildMode.toLowerCase();
  const isNightly = (process.env.NIGHTLY ?? '').toLowerCase() === 'true';
  const versionName = normalizedMode === 'release'
    ? (isNightly ? `${appVersion}.n${gitVersionCode}.nightly` : `${appVersion}.r${gitVersionCode}.${gitShortHash}`)
    : `${appVersion}.d${gitVersionCode}.${gitShortHash}`;
  return {
    versionCode: gitVersionCode,
    versionName,
  };
}

// 构建时从主模块 gradle/libs.versions.toml 与 git 状态动态同步版本号与版本名称，保持与 Android 端一致。
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
        const { versionCode, versionName } = resolveMainModuleVersion(repoRoot, appContext.getBuildMode());
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
