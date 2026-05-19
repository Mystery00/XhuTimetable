# AGENTS.md

本文档面向在本仓库中工作的自动化编码代理。请优先遵循这里的约定；如与用户的明确指令冲突，以用户指令为准。

## 语言要求

- 与用户对话、代码注释和项目文档默认使用中文。
- 技术术语、命令、包名、类名、函数名、变量名等代码标识符保持原有形式。

## 项目概览

- 项目名称：西瓜课表-新装版。
- 项目定位：面向西华大学学生的课程表应用。
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
- 不要用阻塞式实现替代已有协程、Flow、Paging 或 Compose state 模式。

## 架构概览

### 多平台模块划分

```text
composeApp/src/
├── commonMain/              # 共享业务逻辑与 UI
├── androidMain/             # Android 平台实现
├── iosMain/                 # iOS 平台实现
├── iosSimulatorArm64Main/   # iOS 模拟器相关实现
```

### 共享层核心目录

| 目录               | 说明                                                        |
|------------------|-----------------------------------------------------------|
| `api/`           | Ktorfit 接口定义，每个功能对应一个接口文件                                 |
| `base/`          | 基类：`ComposeViewModel`、`BaseDataRepo`、分页基类等                |
| `config/store/`  | 持久化状态管理：`ConfigStore`、`UserStore`、`CacheStore`、`EventBus` |
| `db/`            | Room 数据库定义、DAO 与相关配置                                      |
| `feature/`       | 自研 FeatureHub 功能开关系统                                      |
| `model/`         | 数据模型：entity、request、response、transfer、ws、event 等          |
| `module/`        | Koin DI 模块注册：network、database、repository、viewModel 等      |
| `repository/`    | 数据仓库层，实现 `BaseDataRepo` 接口                                |
| `ui/navigation/` | 类型安全路由，Route 通常为 `@Serializable` 数据类或对象                   |
| `ui/screen/`     | Composable 页面                                             |
| `ui/theme/`      | 主题、深色模式                                                   |
| `viewmodel/`     | ViewModel 层                                               |

### Android 特有目录

| 目录             | 说明                                       |
|----------------|------------------------------------------|
| `ui/activity/` | Activity：`StartActivity`、`NavActivity` 等 |
| `ui/widget/`   | Glance 桌面小组件：今日课程、本周课程等                  |
| `work/`        | WorkManager 后台任务：下载 APK/补丁/启动图、通知推送等     |
| `config/mmkv/` | MMKV 存储的 Android 实现                      |

### 数据流

```text
Screen (Composable) <-> ViewModel (extends ComposeViewModel)
                            |
                     Repository (implements BaseDataRepo)
                       /          \
              API (Ktorfit)     Database (Room / DAO)
                                     |
                              ConfigStore / UserStore
                              (MMKV / Settings)
```

### expect/actual 模式

以下平台能力通过 expect/actual 分别实现：

- `Store.getValue/setValue/removeValue`：Android 用 MMKV，iOS 用 NSUserDefaults/Settings。
- `httpClientEngine()`：Android 用 OkHttp，iOS 用 Darwin。
- `killCurrentProcess()`、`platform()`、`sdkKey()` 等平台相关应用信息。

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
- 依赖注入优先复用 Koin 模块，在 `module/Module.kt` 的 `moduleList()` 统一注册模块。
- ViewModel 通过 Koin 注入，Repository 和 API 通常为单例。
- 图片加载优先复用 Coil 3。
- 数据持久化涉及结构化数据时优先复用 Room；平台设置项按现有 multiplatform-settings/MMKV 用法处理。
- 日志优先使用 Kermit。

## 状态管理与导航

- `ConfigStore`：应用配置，如开学时间、主题、多账号模式等，通过 `getConfigStore { }` /
  `setConfigStore { }` 访问。
- `UserStore`：已登录用户列表与主账号管理。
- `CacheStore`：临时缓存数据。
- `EventBus`：单次消费事件（`SingleEvent<EventType>`），用于跨组件通信，如隐私政策同意、登录状态变化。
- `ui/navigation/Nav.kt` 定义路由，`Navs` 为 `NavGraphBuilder` 扩展函数，在 `App.kt` 中注册到
  `NavHost`。
- 路由跳转通过 `LocalNavController` 获取 `NavController`。

## 数据库

- Room 数据库 schema 文件存放在 `composeApp/schemas/`。
- 修改数据库实体、DAO 或 Room 配置后，必须确认 KSP 与 schema 生成结果。
- 修改数据库结构时需要编写 Migration，并检查 `composeApp/schemas` 是否需要更新。

## 网络层

- API Base URL：`https://xgkb.api.mystery0.vip/`。
- `NetworkModule.kt` 中注册多种 Ktor `HttpClient` 实例：
  - `HTTP_CLIENT`：主 API 客户端，包含 `ServerApiPlugin` 鉴权拦截与 20s 超时。
  - `HTTP_CLIENT_POEMS`：今日诗词客户端，包含 `PoemsPlugin`。
  - `HTTP_CLIENT_WS`：WebSocket 客户端，用于意见反馈。
  - `HTTP_CLIENT_FILE`：文件下载客户端。

## 资源与本地化

- Android 资源位于 `composeApp/src/androidMain/res` 和 `composeApp/src/main`。
- Compose Multiplatform 资源位于 `composeApp/src/commonMain/composeResources`。
- 当前 Android locale filter 仅包含 `zh-rCN`；新增文案时保持中文语境一致。
- 修改应用名称、版本显示、图标背景色等发布相关资源时，检查 `composeApp/build.gradle.kts` 中 debug/release/product flavor 的差异。

## 产品渠道与版本号

| 渠道         | `ENABLE_UPDATE_CHECK` | 用途     |
|------------|-----------------------|--------|
| `standard` | `true`                | 官网直接分发 |
| `store`    | `false`               | 应用商店分发 |

- `versionCode`：由 `git rev-list HEAD --count` 自动生成。
- `versionName`：格式为 `{app-version}.{r|d|n}{versionCode}.{gitShortHash}`，分别对应
  release/debug/nightly。
- `NIGHTLY=true` 会影响 release 版本名、图标背景色和混淆配置。

## 构建与验证

常用命令：

```shell
./gradlew composeApp:assembleDebug
./gradlew composeApp:compileKotlinAndroid
./gradlew composeApp:exportLibraryDefinitions
./gradlew composeApp:updateAppleBuildVersion
./gradlew assembleRelease
./gradlew assembleStandardRelease
./gradlew assembleStoreRelease
```

验证建议：

- 普通 Kotlin/共享逻辑改动：至少运行 `./gradlew composeApp:compileKotlinAndroid`。
- Android UI、资源、Manifest、BuildConfig 或依赖改动：运行 `./gradlew composeApp:assembleDebug`。
- 影响发布、许可证、iOS 版本号或 KMP framework 的改动：参考 CI 顺序运行
  `composeApp:exportLibraryDefinitions`、`composeApp:updateAppleBuildVersion`，必要时再运行
  `assembleRelease`。
- 数据库实体、DAO 或 Room 配置改动：确认 KSP 与 schema 生成结果，检查 `composeApp/schemas` 是否需要更新。

注意：

- Release 构建需要签名环境变量和签名文件。缺少签名信息时，优先使用 debug/compile 任务验证。
- 发布构建可能需要在 `local.properties` 中配置签名相关变量，具体变量名参见 `signing.gradle`。
- 私有 Maven 仓库 `vip.mystery0.sheets-compose-dialogs` 需要 `GITHUB_USERNAME` 和 `GITHUB_PASSWORD`
  环境变量。依赖解析失败时先检查凭据，而不是修改依赖声明。
- iOS 构建需要本机具备对应 Xcode/Kotlin Native 环境。

## CI 与发布相关

- CI 使用 JDK 21。
- Android nightly 构建会执行：
  - `./gradlew composeApp:exportLibraryDefinitions`
  - `./gradlew composeApp:updateAppleBuildVersion`
  - `./gradlew assembleRelease`
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
