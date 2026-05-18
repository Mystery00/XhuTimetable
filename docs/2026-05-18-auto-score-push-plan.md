# 成绩查询订阅推送能力 App 端执行方案

## 背景与目标

App 需要新增云端推送能力，仅用于“成绩数据变更”这一类业务通知，不用于营销、活动或广告推送。

用户在 App 内通过“成绩查询订阅”入口主动开启订阅。开启时 App 将当前用户身份凭据和极光 `registrationId` 提交给服务端。服务端已经负责创建定时查询任务、绑定用户与设备注册 ID，并在后续成绩数据发生变化时通过极光向该设备发送通知。

App 端本次目标是：

- Android 和 iOS 都接入极光推送 SDK。
- 在用户同意隐私政策后初始化推送 SDK。
- 稳定获取并缓存极光 `registrationId`。
- 在成绩查询页面提供“成绩查询订阅”入口。
- 对接服务端通用 Job 接口，完成开启、取消、状态查询。
- 收到成绩更新通知后，点击进入成绩查询页面。

## 范围边界

### 本次需要做

- 接入 JPush/JCore SDK。
- Android 申请通知权限并配置通知渠道。
- iOS 配置 APNs、推送权限、device token 与 JPush 注册流程。
- App 内封装跨平台 `PushManager`，对上层只暴露 registrationId 与初始化状态。
- 开启订阅时向服务端传递设备类型，Android 传 `ANDROID`，iOS 传 `IOS`。
- 替换现有旧自动查分接口调用，迁移到服务端通用 Job 接口。
- 新增成绩查询订阅 UI、状态展示、取消订阅入口。

### 本次不做

- 不做营销推送开关。
- 不做推送到达率、点击率、回执统计。
- 不做服务端任务调度、成绩比对或推送发送逻辑。
- 不在 App 端自行定时查询成绩。
- 不在 App 端根据服务端状态做 Android/iOS 业务分支；推送目标仍以 `registrationId` 为准。

## 已确认的服务端对接逻辑

服务端当前提供通用 Job 接口，`auto-score` 是其中一种任务类型。

### 开启订阅

```http
POST /api/rest/job/start?job=auto-score
```

请求体：

```json
{
  "publicKey": "服务端返回的公钥",
  "password": "RSA 公钥加密后的教务系统密码",
  "registrationId": "极光 registrationId",
  "platform": "ANDROID 或 IOS"
}
```

响应：

```json
{
  "taskId": 123456789,
  "expireDate": "2026-06-07"
}
```

App 侧说明：

- 用户身份以当前登录 session 为准，App 不需要额外传学号。
- 服务端会根据当前日期自行确定成绩查询学期，App 不再传 `year` 和 `term`。
- 密码加密方式复用现有 `JobRepo.addAutoCheckScoreJob` 中的公钥获取与 RSA 加密流程。
- App 必须传递当前设备类型，取值使用现有 `platform().name`，Android 传 `ANDROID`，iOS 传 `IOS`。
- 服务端可记录设备类型用于任务数据归档或后续分析，但 App 端不需要根据服务端返回结果做平台分支。

### 取消订阅

```http
DELETE /api/rest/job/stop?job=auto-score
```

App 侧说明：

- 用户点击取消后调用该接口。
- 服务端若没有活跃任务会直接返回成功，App 可按无订阅状态展示。

### 查询订阅状态

```http
GET /api/rest/job/status?job=auto-score
```

响应字段：

```json
{
  "hasActiveTask": true,
  "status": "ACTIVE",
  "expireDate": "2026-06-07",
  "nextCheckTime": "2026-05-18T03:15:00Z",
  "lastCheckTime": "2026-05-18T00:20:00Z",
  "lastCheckResult": "NO_CHANGE"
}
```

App 侧展示建议：

- `hasActiveTask = true`：显示已开启、到期日期、下次检查时间、最近检查结果。
- `status = SUSPENDED`：显示已暂停，提示用户检查账号状态后重新开启。
- `status = EXPIRED`：显示已到期，可重新开启。
- `status = CANCELLED` 或空：显示未开启。

## App 端架构方案

### 1. Push 抽象层

在 `commonMain` 新增平台无关接口：

```kotlin
interface PushManager {
    suspend fun initialize()
    suspend fun requestPermissionIfNeeded(): PushPermissionState
    suspend fun registrationId(): String?
    suspend fun refreshRegistrationId(): String?
}
```

建议位置：

- `composeApp/src/commonMain/kotlin/vip/mystery0/xhu/timetable/push/PushManager.kt`
- `composeApp/src/androidMain/kotlin/vip/mystery0/xhu/timetable/push/PushManager.android.kt`
- `composeApp/src/iosMain/kotlin/vip/mystery0/xhu/timetable/push/PushManager.ios.kt`

设计原则：

- JPush SDK 调用只放在平台 source set。
- `commonMain` 只处理订阅业务、状态机和 UI。
- registrationId 在平台侧获取，必要时缓存到 `CacheStore`，避免每次进入页面都阻塞等待 SDK 回调。

### 2. 初始化时机

推送 SDK 必须晚于用户同意隐私政策初始化。

现有 App 已在以下位置判断隐私授权：

- Android Application 启动时根据 `GlobalCacheStore.allowPrivacy` 初始化部分能力。
- Compose `App` 中根据 `allowPrivacy` 启动 FeatureHub。

执行方案：

- 新增 `initPush()` 或通过 Koin 注入 `PushManager` 后调用 `initialize()`。
- Android 在 `Application.onCreate()` 中仅当 `GlobalCacheStore.allowPrivacy == true` 时初始化。
- 首次同意隐私政策后，在处理 `EventType.ALLOW_PRIVACY` 的流程中补一次初始化。
- iOS 在 `iOSApp.init()` 或 `App` 启动流程中只做轻量准备，真正注册推送权限放到用户进入订阅入口时触发。

### 3. Android 接入

执行项：

1. 在 `gradle/libs.versions.toml` 增加 JPush/JCore 依赖版本。
2. 在 `composeApp/build.gradle.kts` 的 `androidMain.dependencies` 添加极光 SDK 依赖。
3. 在 Android `defaultConfig` 或 product flavor 中配置 JPush AppKey、Channel、包名相关 manifestPlaceholders。
4. 在 `AndroidManifest.xml` 增加 JPush 所需组件、权限和 Android 13 通知权限适配。
5. 在 `Application.onCreate()` 的隐私授权后调用：
   - JPush 合规授权接口。
   - JPush 初始化。
   - 自定义通知渠道 `XhuTimetable-Push`。
6. 实现 Android `PushManager`：
   - 获取 `registrationId`。
   - 处理 SDK registrationId 为空时的重试。
   - 处理通知点击，跳转到 `RouteQueryScore`。

注意：

- Android 端不需要 App 自己对接多个厂商 SDK，统一使用 JPush。厂商通道配置由极光侧和对应 SDK 配置承接。
- Android 13+ 需要 `POST_NOTIFICATIONS` 权限。进入“成绩查询订阅”入口时再请求，避免启动时打扰用户。
- 通知渠道 ID 应与服务端当前推送体中的 `XhuTimetable-Push` 保持一致，否则 Android 8+ 可能无法按预期展示。

### 4. iOS 接入

当前 iOS 工程是 SwiftUI `@main` App，尚未看到 AppDelegate。JPush iOS 接入需要补齐应用生命周期回调。

执行项：

1. 为 `iosApp` 接入 JPush/JCore，优先使用 CocoaPods；如果项目暂不引入 Pod，则按极光官方手动集成方式接入 framework。
2. 在 Apple Developer 后台开启 Push Notifications 能力，配置 APNs Auth Key 或证书，并在极光后台绑定。
3. 在 Xcode 工程中启用 Push Notifications capability。
4. 增加 `AppDelegate`，并通过 SwiftUI adaptor 接入：

```swift
@UIApplicationDelegateAdaptor(AppDelegate.self) var appDelegate
```

5. 在 AppDelegate 中处理：
   - JPush 初始化。
   - APNs 注册。
   - device token 回传 JPush。
   - 前台通知展示策略。
   - 通知点击回调。
6. 将 iOS 获取到的 JPush registrationId 暴露给 Kotlin shared 层：
   - 方案 A：Swift 写入 shared 可读取的 Settings/Keychain。
   - 方案 B：通过 Kotlin/Native 暴露桥接方法，由 Swift 回调保存到 shared 内存与本地缓存。

建议优先采用方案 B，避免 registrationId 数据源分散。

### 5. 服务端 API 迁移

当前 App 中已有旧接口：

- `JobApi.autoCheckScore`
- `AutoCheckScoreRequest`
- `JobRepo.addAutoCheckScoreJob`
- `JobHistoryViewModel.addAutoCheckScoreJob`

这些逻辑仍按旧链路传 `username/year/term`，需要迁移。

新增或调整模型：

```kotlin
@Serializable
data class AutoScoreStartRequest(
    val publicKey: String,
    val password: String,
    val registrationId: String,
    /** 当前设备类型：ANDROID 或 IOS */
    val platform: String,
)

@Serializable
data class AutoScoreStartResponse(
    val taskId: Long,
    val expireDate: XhuLocalDate,
)

@Serializable
data class AutoScoreStatusResponse(
    val hasActiveTask: Boolean,
    val status: String?,
    val expireDate: XhuLocalDate?,
    val nextCheckTime: XhuInstant?,
    val lastCheckTime: XhuInstant?,
    val lastCheckResult: String?,
)
```

调整 `JobApi`：

```kotlin
@POST("api/rest/job/start")
suspend fun startAutoScoreJob(
    @Header("sessionToken") token: String,
    @Query("job") job: String = "auto-score",
    @Body request: AutoScoreStartRequest,
): AutoScoreStartResponse

@DELETE("api/rest/job/stop")
suspend fun stopAutoScoreJob(
    @Header("sessionToken") token: String,
    @Query("job") job: String = "auto-score",
)

@GET("api/rest/job/status")
suspend fun getAutoScoreJobStatus(
    @Header("sessionToken") token: String,
    @Query("job") job: String = "auto-score",
): AutoScoreStatusResponse
```

`JobRepo` 调整：

- 保留现有 RSA 公钥加密流程。
- 开启订阅前调用 `PushManager.registrationId()`。
- 如果 registrationId 为空，先触发 `PushManager.refreshRegistrationId()`。
- 仍为空则提示“推送服务初始化失败，请稍后重试”。
- 请求体中 `platform` 固定传 `platform().name`，确保服务端保存 Android/iOS 设备类型。
- 不再读取 `nowYear`、`nowTerm`，不再传 `username`。

### 6. UI 与交互

入口位置：成绩查询页面 `QueryScoreScreen`。

建议表现：

- 在成绩 GPA 信息卡或成绩列表标题区域附近增加“成绩查询订阅”入口。
- 未开启：显示“开启成绩变更提醒”按钮。
- 已开启：显示“已开启成绩变更提醒”、到期日期、下次检查时间和“取消订阅”按钮。
- 已暂停：显示暂停原因提示，引导用户重新登录或检查教务账号后重新开启。
- 已到期：显示到期信息和重新开启按钮。

开启流程：

1. 用户点击“开启成绩变更提醒”。
2. 展示确认弹窗，说明：
   - 仅用于成绩变更提醒。
   - 服务端会在有效期内定时查询成绩。
   - 通知内容不包含具体成绩。
   - 用户可随时取消。
3. 请求系统通知权限。
4. 获取 JPush registrationId。
5. 调用服务端开启接口。
6. 成功后刷新状态并提示“已开启”。

取消流程：

1. 用户点击“取消订阅”。
2. 展示确认弹窗。
3. 调用服务端取消接口。
4. 成功后刷新状态。

### 7. 通知点击路由

服务端当前推送内容只包含通知标题和正文，不携带具体成绩。App 点击通知后的目标应固定为成绩查询页面。

Android：

- JPush 通知点击 receiver 中启动主 Activity。
- intent extras 标记目标路由为 `RouteQueryScore`。
- 如果 App 已在前台，直接通过导航事件跳转。
- 如果 App 冷启动，在初始化完成后消费 pending route。

iOS：

- 在通知点击回调中保存 pending route。
- Compose 初始化后消费 pending route，跳转 `RouteQueryScore`。

建议新增：

- `PushRoute.Score`
- `PendingRouteStore`
- `EventType.OPEN_SCORE_FROM_PUSH`

### 8. 配置与合规

必须处理：

- 隐私政策补充极光 SDK 信息。
- SDK 初始化晚于隐私授权。
- iOS 首次订阅时申请通知权限。
- Android 13+ 首次订阅时申请通知权限。
- AppKey、Channel 等配置不要硬编码到业务代码中，优先放入 Gradle 配置、Xcode 配置或构建配置。

不需要处理：

- 营销通知退订。
- 个性化推送授权。
- 推送到达率展示。

### 9. 测试计划

本地验证：

- 隐私未同意时启动 App，确认 JPush 不初始化。
- 同意隐私后，确认 Android/iOS 能获取 registrationId。
- 未授予通知权限时点击开启订阅，确认权限申请流程正常。
- registrationId 为空时，确认 App 不调用服务端开启接口。
- 开启订阅成功后，状态页显示 ACTIVE、到期日期、下次检查时间。
- 重复开启时，服务端返回已有任务错误，App 以友好文案提示。
- 取消订阅后，状态刷新为未开启。
- 模拟通知点击，确认跳转到成绩查询页。

联调验证：

- Android 真机：debug 包、release/standard 包各验证一次。
- iOS 真机：开发环境 APNs、生产环境 APNs 分别验证。
- 服务端测试推送到 registrationId 后，Android 与 iOS 均可收到通知。
- 成绩变化推送点击后进入成绩查询页，页面能正常刷新成绩。

推荐验证命令：

```shell
./gradlew composeApp:compileKotlinAndroid
./gradlew composeApp:assembleDebug
./gradlew composeApp:exportLibraryDefinitions
```

## 分阶段执行清单

### 阶段一：基础接入

- [ ] 增加 Android JPush/JCore 依赖和 Manifest 配置。
- [ ] 增加 Android `PushManager` 实现。
- [ ] 增加 iOS JPush/JCore 接入方案和 AppDelegate。
- [ ] 增加 iOS `PushManager` 实现。
- [ ] 将推送初始化接入隐私授权流程。

### 阶段二：服务端接口迁移

- [ ] 新增 `AutoScoreStartRequest`、`AutoScoreStartResponse`、`AutoScoreStatusResponse`。
- [ ] 调整 `JobApi` 到 `/api/rest/job/start|stop|status?job=auto-score`。
- [ ] 重写 `JobRepo` 中成绩订阅开启、取消、状态查询方法。
- [ ] 废弃或删除旧的 `AutoCheckScoreRequest` 调用路径。

### 阶段三：成绩订阅 UI

- [ ] 在 `ScoreViewModel` 增加订阅状态、开启、取消、刷新方法。
- [ ] 在 `QueryScoreScreen` 增加订阅入口和状态卡片。
- [ ] 增加确认弹窗和错误提示。
- [ ] 处理通知权限未授权、registrationId 为空、已有任务、任务暂停等状态。

### 阶段四：通知点击路由

- [ ] Android 实现 JPush 通知点击到 `RouteQueryScore`。
- [ ] iOS 实现通知点击到 `RouteQueryScore`。
- [ ] 冷启动和前台状态都能消费 pending route。

### 阶段五：联调与收尾

- [ ] Android 真机获取 registrationId 并开启订阅。
- [ ] iOS 真机获取 registrationId 并开启订阅。
- [ ] 服务端按 registrationId 推送测试通知。
- [ ] 验证成绩变更通知点击进入成绩查询。
- [ ] 更新隐私政策中第三方 SDK 说明。
- [ ] 清理旧入口、旧模型和无用测试代码。

## 主要风险

- iOS APNs 环境、极光后台证书/Auth Key、bundle id 不一致会导致 registrationId 存在但收不到推送。
- Android 厂商通道配置不完整会影响后台到达率，但 App 端仍只接 JPush。
- 如果在隐私同意前初始化 SDK，会引入合规风险。
- 如果 notification channel id 与服务端不一致，Android 通知展示可能异常。
- 如果 registrationId 为空时仍开启服务端任务，后续任务会绑定无效设备，必须在 App 端拦截。

## 外部参考

- 极光 Android SDK 集成指南：https://docs.jiguang.cn/jpush/client/Android/android_guide
- 极光 Android 合规指南：https://docs.jiguang.cn/jpush/practice/compliance
- 极光 iOS SDK 集成指南：https://docs.jiguang.cn/jpush/client/iOS/ios_guide_new
- 极光 REST Push API：https://docs.jiguang.cn/jpush/server/push/rest_api_v3_push
