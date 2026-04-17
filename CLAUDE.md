# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this
repository.

## 语言要求

与用户对话、代码注释、文档均使用**中文**。技术术语和代码标识符保持原有形式。

## 项目概述

**西瓜课表（新装版）** —— 面向西华大学学生的课程表应用，使用 Kotlin Multiplatform + Compose
Multiplatform 开发，支持 Android 和 iOS 双平台。

## 构建命令

```shell
# 生成开源库清单（首次构建或依赖变更后执行）
./gradlew composeApp:exportLibraryDefinitions

# 构建 Release APK（standard 渠道，含更新检测）
./gradlew assembleStandardRelease

# 构建 store 渠道 APK（无更新检测，适用于应用商店）
./gradlew assembleStoreRelease

# 构建 Debug APK
./gradlew assembleDebug

# 更新 iOS 版本配置文件（iOS 构建前需执行）
./gradlew composeApp:updateAppleBuildVersion
```

### 签名配置

发布构建需要在 `local.properties` 中配置签名相关变量，具体变量名参见 `signing.gradle`。

### GitHub Packages 依赖

`settings.gradle.kts` 中引用了私有 GitHub Packages（`sheets-compose-dialogs`），构建时需设置环境变量：

- `GITHUB_USERNAME`
- `GITHUB_PASSWORD`

### 版本号规则

- `versionCode`：由 `git rev-list HEAD --count` 自动生成
- `versionName`：格式为 `{app-version}.{r|d|n}{versionCode}.{gitShortHash}`（release/debug/nightly）

## 架构概览

### 多平台模块划分

```
composeApp/src/
├── commonMain/   # 共享业务逻辑与 UI（Compose Multiplatform）
├── androidMain/  # Android 平台实现
├── iosMain/      # iOS 平台实现
└── main/         # Android 资源文件
```

### 共享层（commonMain）核心目录

| 目录               | 说明                                                        |
|------------------|-----------------------------------------------------------|
| `api/`           | Ktorfit 接口定义，每个功能对应一个接口文件                                 |
| `base/`          | 基类：`ComposeViewModel`、`BaseDataRepo`、分页基类等                |
| `config/store/`  | 持久化状态管理（ConfigStore、UserStore、CacheStore、EventBus）        |
| `db/`            | Room 数据库定义（`AppDatabase`）及 DAO                            |
| `feature/`       | 自研 FeatureHub 功能开关系统（轮询拉取远端特性标志）                          |
| `model/`         | 数据模型（entity / request / response / transfer / ws / event） |
| `module/`        | Koin DI 模块注册（network、database、repository、viewModel）       |
| `repository/`    | 数据仓库层，实现 `BaseDataRepo` 接口                                |
| `ui/navigation/` | 类型安全路由（所有 Route 为 `@Serializable` 数据类/对象）                 |
| `ui/screen/`     | Composable 页面，每个页面对应一个文件                                  |
| `ui/theme/`      | 主题、深色模式                                                   |
| `viewmodel/`     | ViewModel 层                                               |

### Android 特有目录（androidMain）

| 目录             | 说明                                               |
|----------------|--------------------------------------------------|
| `ui/activity/` | Activity：`StartActivity`（启动）、`NavActivity`（主导航）等 |
| `ui/widget/`   | Glance 桌面小组件（今日课程、本周课程）                          |
| `work/`        | WorkManager 后台任务（下载 APK/补丁/启动图、通知推送）             |
| `config/mmkv/` | MMKV 存储的 Android 实现                              |

### 关键 expect/actual 模式

以下 API 在各平台分别实现：

- `Store.getValue/setValue/removeValue` —— Android 用 MMKV，iOS 用 NSUserDefaults/Settings
- `httpClientEngine()` —— Android 用 OkHttp，iOS 用 Darwin
- `killCurrentProcess()` / `platform()` / `sdkKey()` —— 平台相关应用信息

### 依赖注入

使用 Koin，在 `module/Module.kt` 的 `moduleList()` 统一注册所有模块。ViewModel 通过 Koin 注入，Repository
和 API 均为单例。

### 数据流

```
Screen (Composable) ←→ ViewModel (extends ComposeViewModel)
                            ↓
                     Repository (implements BaseDataRepo)
                       ↙          ↘
              API (Ktorfit)     Database (Room / DAO)
                                     ↕
                              ConfigStore / UserStore（MMKV/Settings）
```

### 状态管理与事件总线

- `ConfigStore`：应用配置（开学时间、主题、多账号模式等），通过 `getConfigStore { }` /
  `setConfigStore { }` 访问
- `UserStore`：已登录用户列表与主账号管理
- `CacheStore`：临时缓存数据
- `EventBus`：单次消费事件（`SingleEvent<EventType>`），用于跨组件通信（如隐私政策同意、登录状态变化）

### 导航

`ui/navigation/Nav.kt` 中定义所有路由，`Navs` 为 `NavGraphBuilder` 扩展函数，在 `App.kt` 中注册到
`NavHost`。路由跳转通过 `LocalNavController` 获取 `NavController`。

### 产品渠道（Flavor）

| 渠道         | `ENABLE_UPDATE_CHECK` | 用途     |
|------------|-----------------------|--------|
| `standard` | `true`                | 官网直接分发 |
| `store`    | `false`               | 应用商店分发 |

### 数据库

Room 数据库版本当前为 5，schema 文件存放在 `composeApp/schemas/`。修改数据库实体后必须编写 Migration
并更新 schema 文件。

### 网络层

`NetworkModule.kt` 中注册了四种 Ktor HttpClient 实例（命名 qualifier）：

- `HTTP_CLIENT`：主 API 客户端（含 `ServerApiPlugin` 鉴权拦截、20s 超时）
- `HTTP_CLIENT_POEMS`：今日诗词客户端（含 `PoemsPlugin`）
- `HTTP_CLIENT_WS`：WebSocket 客户端（用于意见反馈）
- `HTTP_CLIENT_FILE`：文件下载客户端

API Base URL：`https://xgkb.api.mystery0.vip/`
