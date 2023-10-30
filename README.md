# VapCache-Android

![$latestVersion](https://maven-badges.herokuapp.com/maven-central/io.github.kangyee/vapcache/badge.svg)

VAP（Video Animation Player）缓存库，用于更方便的从网络获取资源并播放。

支持从 RawRes、Asset、File 以及 Url 中读取资源。

<img src="https://github.com/kangyee/vapcache/blob/main/preview.jpg" alt="preview.jpg" width="30%" />

# 前言

最近使用 VAP 发现不支持直接通过 Url 播放，个人因为 Lottie 用的比较多，所以就直接扒了一下 Lottie 的缓存实现，并在
原有基础做了一点修改，为了方便使用，本库就诞生啦。

目前仅针对 Jetpack Compose 做了支持，View 方式的实现暂未去了解，计划过段时间更新。

# 集成

```
dependencies {
    implementation 'io.github.kangyee:vapcache:$latestVersion'
}
```

# 使用

```
val compositionResult = rememberVapComposition(...)
// 或
val composition by rememberVapComposition(...)
```

具体可参考 example 中的示例