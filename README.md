# 西瓜课表-新装版

面向西华大学学生的跨平台课程表应用，基于 **Kotlin Multiplatform** 与 **Compose Multiplatform** 构建，覆盖 **Android、iOS 与 HarmonyOS** 三端。

- 官方网站：[西瓜课表官网](https://xgkb.mystery0.vip)

[![Android Nightly](https://github.com/Mystery00/XhuTimetable/actions/workflows/build_android_nightly.yaml/badge.svg?branch=master)](https://github.com/Mystery00/XhuTimetable/actions/workflows/build_android_nightly.yaml)
[![iOS Nightly](https://github.com/Mystery00/XhuTimetable/actions/workflows/build_ios_nightly.yml/badge.svg?branch=master)](https://github.com/Mystery00/XhuTimetable/actions/workflows/build_ios_nightly.yml)
[![HarmonyOS Release](https://github.com/Mystery00/XhuTimetable/actions/workflows/build_harmony_release.yml/badge.svg?branch=master)](https://github.com/Mystery00/XhuTimetable/actions/workflows/build_harmony_release.yml)

---

## 功能特性

- **多视图课表**：提供今日课程、本周课表、月历视图，支持多账号切换与聚合展示、自定义课程、自定义事项、自定义课表背景与配色。
- **教务查询与实用工具**：支持考试安排、成绩查询与绩点统计、空闲教室查询、学业进度、校历查看、课程表导出（ICS）等功能。
- **三端桌面小组件 / 服务卡片**：
  - **Android**：基于 Jetpack Glance 提供「下一节课」「今日课程」「本周课表」小组件。
  - **iOS**：基于 WidgetKit 提供对应桌面小组件（`iosApp/TimetableWidgets`）。
  - **HarmonyOS**：基于 FormKit 提供「下一节课（2×2）」「今日课程（2×4 / 4×4）」「本周课表（4×4）」三款动态服务卡片，支持点击刷新与跳转主应用。
- **体验与个性化**：支持浅色 / 深色模式与 Material 3 主题、今日诗词、通知提醒、在线意见反馈与赞助鸣谢页。

## 和以前有什么不一样？

旧版西瓜课表基于传统 Android View 开发，依赖了大量自研自定义 View 组件，维护和升级成本越来越高。因此新装版使用 **Jetpack Compose** 完成了重写，并进一步演进为 **Kotlin Multiplatform** 多端架构：

- **Android 与 iOS** 共享 `composeApp` 中的业务逻辑与 Compose Multiplatform UI。
- **HarmonyOS** 在 `harmonyApp/entry` 中采用 **ArkTS / ArkUI 原生页面**，通过 N-API 调用由 `harmonyApp/kotlin` 编译的共享 Kotlin 业务逻辑，既保证跨平台业务一致，又贴合鸿蒙原生交互与服务卡片能力。

旧版已进入维护状态，新功能与持续迭代均在新装版中进行。

---

## 项目结构

```text
XhuTimetable/
├── composeApp/        # KMP 共享业务与 Compose Multiplatform UI（commonMain / androidMain / iosMain）
├── androidApp/        # Android 应用宿主（渠道、签名、Manifest、资源与 APK 打包）
├── iosApp/            # iOS Xcode 宿主工程与 WidgetKit 桌面小组件
├── harmonyApp/        # HarmonyOS 宿主工程（ArkTS/ArkUI、FormKit 卡片、C++ N-API）
│   ├── entry/         # 鸿蒙主模块与服务卡片
│   ├── kotlin/        # 独立鸿蒙 Kotlin/Native 适配工具链（从 composeApp 同步共享业务源码）
│   └── scripts/       # AppGallery Connect 自动上传脚本等工具
├── gradle/            # Gradle Wrapper 与版本目录（libs.versions.toml）
└── .github/workflows/ # Android / iOS / HarmonyOS CI 与发布流水线
```

### 核心技术栈

- **语言与框架**：Kotlin Multiplatform、Compose Multiplatform、ArkTS / ArkUI、Swift / WidgetKit
- **网络与序列化**：Ktor、Ktorfit、`kotlinx.serialization`
- **持久化与分页**：Room（含 KSP 与 Schema 导出）、MMKV / Multiplatform Settings、Paging 3
- **依赖注入与工具**：Koin、Coil 3、Kermit、AboutLibraries

---

## 如何自行编译？

### 1. 环境准备与凭据配置

- **JDK**：请使用 **JDK 21**。
- **私有 Maven 仓库凭据**：项目依赖私有仓库 `vip.mystery0.sheets-compose-dialogs`，`GITHUB_USERNAME` 与 `GITHUB_PASSWORD` 优先从根目录 `local.properties` 读取，缺失或为空白时回退至环境变量。请勿将任何凭据或签名文件提交到仓库。
- **Android 签名配置**：Release 签名参数读取自根目录 `local.properties` 或环境变量（`SIGN_KEY_STORE_FILE`、`SIGN_KEY_STORE_PASSWORD`、`SIGN_KEY_ALIAS`、`SIGN_KEY_PASSWORD`），其中相对签名路径以 `composeApp` 目录为基准，详见 [signing.gradle](signing.gradle)。
- **华为 AGConnect 配置（Android）**：正式环境配置放在 `androidApp/agconnect-services.json`，调试配置放在 `androidApp/src/debug/agconnect-services.json`（均不提交到仓库）。

> **AGP 兼容说明**：共享模块已启用 Android KMP 插件；由于当前华为 AGConnect 插件仍依赖旧 Variant API，[gradle.properties](gradle.properties) 通过 `android.newDsl.optOut=:androidApp` 仅为应用模块保留旧 DSL。待华为插件适配新 API 后应移除该项（该配置不适用于 AGP 10）。

### 2. Android 构建

在 Android Studio 中运行配置请选择 `androidApp`。

```shell
# 编译共享 Android 代码
./gradlew composeApp:compileAndroidMain

# 构建调试包（含 standard / store 两个渠道）
./gradlew androidApp:assembleDebug

# 导出开源许可定义并构建正式签名包
./gradlew composeApp:exportLibraryDefinitions
./gradlew assembleRelease
```

| 渠道 | `ENABLE_UPDATE_CHECK` | Release APK 输出路径 |
| --- | --- | --- |
| `standard`（官网分发） | `true` | `androidApp/build/outputs/apk/standard/release` |
| `store`（应用商店分发） | `false` | `androidApp/build/outputs/apk/store/release` |

### 3. iOS 构建

需要 macOS 与 Xcode 环境：

1. 参考 `iosApp/Configuration/Config.xcconfig.template` 准备本机 `Config.xcconfig`。
2. 同步版本号并生成苹果构建配置：
   ```shell
   ./gradlew composeApp:exportLibraryDefinitions
   ./gradlew composeApp:updateAppleBuildVersion
   ```
3. 使用 Xcode 打开 `iosApp/iosApp.xcodeproj` 进行编译或归档。

### 4. HarmonyOS 构建

- 目标 SDK：`HarmonyOS 26.0.0`，兼容最低版本：`5.0.5(17)`，支持设备类型：`phone`。
- Windows 本地可使用 DevEco Studio 与内置脚本一键构建（`$DevEcoHome` 与 `$HarmonyCache` 替换为本机路径，脚本会自动选择兼容 Gradle 8.9 的 JDK 17~23）：

```powershell
# 构建模拟器包（x64）
.\harmonyApp\build-preview.ps1 -DevEcoHome $DevEcoHome -CacheDirectory $HarmonyCache -Architecture x64

# 构建真机包（arm64）
.\harmonyApp\build-preview.ps1 -DevEcoHome $DevEcoHome -CacheDirectory $HarmonyCache -Architecture arm64

# 仅改动 ArkTS 时跳过 Kotlin 原生编译，加快构建
.\harmonyApp\build-preview.ps1 -DevEcoHome $DevEcoHome -CacheDirectory $HarmonyCache -Architecture x64 -SkipKotlin
```

- **CI 正式包构建**：仓库已内置 [build_harmony_release.yml](.github/workflows/build_harmony_release.yml)，支持手动触发编译 `ohosArm64` Release 原生库、通过 `hvigorw assembleApp` 打包 `.app`、调用 HarmonyOS SDK `hap-sign-tool.jar` 完成正式签名与验签，并可选发布至 GitHub Release 或华为 AppGallery Connect。

---

## 参与贡献与反馈

欢迎提交 Pull Request 参与共建！

如果你在使用过程中遇到问题，或对新功能有任何建议：
- 欢迎在本仓库提交 [Issue](https://github.com/Mystery00/XhuTimetable/issues)。
- 也可以直接通过应用内的「意见反馈」功能联系开发者。
