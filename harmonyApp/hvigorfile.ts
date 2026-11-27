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

function applyEnvReleaseSigning(appContext: OhosAppContext): void {
  const storeFile = (process.env.HARMONY_SIGN_STORE_FILE ?? '').trim();
  const certpath = (process.env.HARMONY_SIGN_CERT_PATH ?? '').trim();
  const profile = (process.env.HARMONY_SIGN_PROFILE_PATH ?? '').trim();
  const keyAlias = (process.env.HARMONY_SIGN_KEY_ALIAS ?? '').trim();
  const storePassword = (process.env.HARMONY_SIGN_STORE_PASSWORD ?? '').trim();
  const keyPassword = (process.env.HARMONY_SIGN_KEY_PASSWORD ?? '').trim();
  const signAlg = (process.env.HARMONY_SIGN_ALG ?? 'SHA256withECDSA').trim();

  const provided = [storeFile, certpath, profile, keyAlias, storePassword, keyPassword].filter(Boolean);
  if (provided.length === 0) {
    return;
  }
  if (provided.length !== 6) {
    throw new Error(
      '检测到部分鸿蒙签名环境变量，但未配置完整；需要同时提供 HARMONY_SIGN_STORE_FILE、' +
      'HARMONY_SIGN_CERT_PATH、HARMONY_SIGN_PROFILE_PATH、HARMONY_SIGN_KEY_ALIAS、' +
      'HARMONY_SIGN_STORE_PASSWORD、HARMONY_SIGN_KEY_PASSWORD',
    );
  }

  const buildProfileOpt = appContext.getBuildProfileOpt();
  const signingConfigs = Array.isArray(buildProfileOpt.app.signingConfigs)
    ? buildProfileOpt.app.signingConfigs.filter((item: { name?: string }) => item?.name !== 'release')
    : [];
  signingConfigs.push({
    name: 'release',
    type: 'HarmonyOS',
    material: {
      certpath,
      storePassword,
      keyAlias,
      keyPassword,
      profile,
      signAlg,
      storeFile,
    },
  });
  buildProfileOpt.app.signingConfigs = signingConfigs;

  if (Array.isArray(buildProfileOpt.app.products)) {
    for (const product of buildProfileOpt.app.products) {
      if (product?.name === 'default') {
        product.signingConfig = 'release';
      }
    }
  }
  appContext.setBuildProfileOpt(buildProfileOpt);
}

// 构建时从主模块 gradle/libs.versions.toml 与 git 同步版本号，并在 CI/Release 环境变量存在时动态注入签名。
function syncMainModuleAndSigningPlugin(): HvigorPlugin {
  return {
    pluginId: 'syncMainModuleAndSigningPlugin',
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
        applyEnvReleaseSigning(appContext);
      });
    },
  };
}

export default {
  system: appTasks,
  plugins: [syncMainModuleAndSigningPlugin()],
};
