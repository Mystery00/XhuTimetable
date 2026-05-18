# AGENTS.md

本文档面向在本仓库中工作的自动化编码代理。请优先遵循这里的约定；如与用户的明确指令冲突，以用户指令为准。

## 项目概览

- 项目名称：西瓜课表-新装版。
- 技术栈：Kotlin Multiplatform、Compose Multiplatform、Android Application、iOS Framework。
- 主模块：`composeApp`。
- 包名：`vip.mystery0.xhu.timetable`。
- 构建系统：Gradle Kotlin DSL，使用版本目录 `gradle/libs.versions.toml`。
- Java/Kotlin JVM 目标：JDK 21。

## 目录结构

- `composeApp/src/commonMain`：跨平台共享代码、Compose UI、ViewModel、Repository、数据库、网络接口与资源。
- `composeApp/src/androidMain`：Android 专属代码、资源、JNI/CMake 配置、WorkManager/Glance/MMKV 等平台实现。
- `composeApp/src/iosMain`：iOS 专属代码与平台实现。
- `composeApp/src/iosSimulatorArm64Main`：iOS 模拟器相关实现。
- `composeApp/schemas`：Room schema 输出目录。
- `iosApp`：iOS 宿主工程与 Xcode 配置。
- `.github/workflows`：CI、夜间构建、发布流程。

## 开发原则

- 优先把业务逻辑、UI 状态和可复用 UI 放在 `commonMain`，只有平台能力确实不同才放入 `androidMain` 或 `iosMain`。
- 先查找现有实现和工具函数，再新增抽象；保持与当前包结构、命名和 Compose 写法一致。
- 不要随意升级依赖。尤其注意 `gradle/libs.versions.toml` 中 Room 版本旁的注释，升级可能导致 iOS 编译失败。
- 不要提交本地签名文件、密钥、`local.properties`、构建产物或 IDE 临时状态。
- 不要改动无关文件；如果工作区已有用户变更，保留并围绕它们继续工作。
- UI 改动应遵循现有 Material 3 / Compose Multiplatform 风格，避免引入新的设计体系。

## 构建与验证

常用命令：

```shell
./gradlew composeApp:assembleDebug
./gradlew composeApp:compileKotlinAndroid
./gradlew composeApp:exportLibraryDefinitions
./gradlew composeApp:updateAppleBuildVersion
./gradlew assembleRelease
```

验证建议：

- 普通 Kotlin/共享逻辑改动：至少运行 `./gradlew composeApp:compileKotlinAndroid`。
- Android UI、资源、Manifest、BuildConfig 或依赖改动：运行 `./gradlew composeApp:assembleDebug`。
- 影响发布、许可证、iOS 版本号或 KMP framework 的改动：参考 CI 顺序运行 `composeApp:exportLibraryDefinitions`、`composeApp:updateAppleBuildVersion`，必要时再运行 `assembleRelease`。
- 数据库实体、DAO 或 Room 配置改动：确认 KSP 与 schema 生成结果，检查 `composeApp/schemas` 是否需要更新。

注意：

- Release 构建需要签名环境变量和签名文件。缺少签名信息时，优先使用 debug/compile 任务验证。
- 私有 Maven 仓库 `vip.mystery0.sheets-compose-dialogs` 需要 `GITHUB_USERNAME` 和 `GITHUB_PASSWORD` 环境变量。依赖解析失败时先检查凭据，而不是修改依赖声明。
- iOS 构建需要本机具备对应 Xcode/Kotlin Native 环境。

## 代码约定

- Kotlin 代码使用项目现有风格，保持包路径在 `vip.mystery0.xhu.timetable` 下。
- Compose 函数、ViewModel、Repository、Model、DB、API 分层应沿用现有目录：
  - `ui`
  - `viewmodel`
  - `repository`
  - `model`
  - `db`
  - `api`
  - `module`
  - `utils`
  - `config`
  - `feature`
  - `base`
- 网络层优先复用 Ktor/Ktorfit 和 kotlinx.serialization。
- 依赖注入优先复用 Koin 模块。
- 图片加载优先复用 Coil 3。
- 数据持久化涉及结构化数据时优先复用 Room；平台设置项按现有 multiplatform-settings/MMKV 用法处理。
- 日志优先使用 Kermit。
- 不要用阻塞式实现替代已有协程、Flow、Paging 或 Compose state 模式。

## 资源与本地化

- Android 资源位于 `composeApp/src/androidMain/res`。
- Compose Multiplatform 资源位于 `composeApp/src/commonMain/composeResources`。
- 当前 Android locale filter 仅包含 `zh-rCN`；新增文案时保持中文语境一致。
- 修改应用名称、版本显示、图标背景色等发布相关资源时，检查 `composeApp/build.gradle.kts` 中 debug/release/product flavor 的差异。

## CI 与发布相关

- CI 使用 JDK 21。
- Android nightly 构建会执行：
  - `./gradlew composeApp:exportLibraryDefinitions`
  - `./gradlew composeApp:updateAppleBuildVersion`
  - `./gradlew assembleRelease`
- `NIGHTLY=true` 会影响 release 版本名、图标背景色和混淆配置。
- `standard` flavor 启用更新检查，`store` flavor 禁用更新检查。
- `assembleRelease` 输出目录在 `composeApp/build/outputs/apk/standard/release`。

## 修改前检查清单

1. 明确改动属于共享层还是平台层。
2. 搜索现有同类实现，复用已有 API、组件和模块。
3. 确认是否会影响签名、版本号、Room schema、KSP、iOS framework 或 CI。
4. 选择最小可行验证命令，并在最终回复中说明是否已运行。

## 禁止事项

- 不要把凭据、token、签名文件、`agconnect-services.json` 或本机路径硬编码进源码。
- 不要为了让本地构建通过而删除 release 签名、私有仓库或 CI 步骤。
- 不要提交 `build/`、`.gradle/`、`.kotlin/metadata/` 等生成目录。
- 不要在没有需求的情况下大规模重排目录、重命名包或统一格式化全仓库。
- 不要绕过现有 Gradle 版本目录直接硬编码依赖版本。
