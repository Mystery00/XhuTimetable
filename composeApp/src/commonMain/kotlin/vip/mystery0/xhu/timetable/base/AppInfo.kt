package vip.mystery0.xhu.timetable.base

//设备id
expect fun publicDeviceId(): String

//应用名称
expect fun appName(): String

//应用包名
expect fun packageName(): String

//版本名称
expect fun appVersionName(): String

//版本号
expect fun appVersionCode(): String
expect fun appVersionCodeNumber(): Long

expect fun systemVersion(): String
expect fun deviceFactory(): String
expect fun deviceModel(): String
expect fun deviceRom(): String