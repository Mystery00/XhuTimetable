# 西瓜课表-新装版

[西瓜课表官网](https://xgkb.mystery0.vip)

服务端接口 ![接口可用性](https://status.admin.mystery0.vip/api/badge/9/uptime/24?label=24%E5%B0%8F%E6%97%B6%E5%8F%AF%E7%94%A8%E6%80%A7&labelSuffix=d)

教务系统 ![教务系统可用性](https://status.admin.mystery0.vip/api/badge/12/uptime/24?label=24%E5%B0%8F%E6%97%B6%E5%8F%AF%E7%94%A8%E6%80%A7&labelSuffix=d)

[![Build Android APK](https://github.com/Mystery00/XhuTimetable/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Mystery00/XhuTimetable/actions/workflows/build.yml)

## 和以前有什么不一样？

我们尽量让新旧版本功能上没有太大的差异，旧版本的西瓜课表是使用Android View开发的，新装版是使用Jetpack
Compose进行开发的，所以新装版在写界面上更加的灵活，这也是新装版界面与旧版本不太一致的原因。
旧版本依赖了一些以前自行编写的View组件，比如主页底部那个有渐变的Bar，经常会出现更新依赖然后这些组件出问题的情况，所以渐渐的不想在旧版本的基础进行开发了。
因此，我们使用Jetpack
Compose这个全新的UI框架重写了一遍西瓜课表（对，我们似乎很擅长重写），经过一年多的开发，新装版已经把旧版本的所有功能全部迁移过来了，部分功能还做了增强。所以旧版本我们进入维护状态，除非出现重大不可使用的情况，不会再做更新，新功能的开发会放到新装版中。

## 如何自行编译？

项目使用Gradle进行构建，因为Gradle脚本中绑定了签名信息，所以需要先拥有一个自己的签名（这一步就不明说了，如果你是Android开发，自然知道是什么意思，如果你只是一个普通人，没有必要去自行编译），然后在local.properties中设置相应的变量，查阅Gradle脚本可以得到具体的变量名称。
准备工作（一个Android Studio、一个Android SDK、一个可靠的网络）做好之后，就可以通过以下命令进行自动编译和打包签名：
```shell
./gradlew assembleRelease
```
编译的签名APK文件在 `app/build/outputs/apk/release` 中。

## 想要贡献代码？
欢迎之极，正常发PR就行了
如果对功能上有什么建议，欢迎在 Issue 中提出，也可以通过应用内的意见反馈功能进行反馈。
