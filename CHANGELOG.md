# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.0.6] - 2026-04-05

### Changed

- Android Gradle Plugin **9.1.0** and Gradle **9.3.1**; Kotlin compilation uses [AGP built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin) (removed `org.jetbrains.kotlin.android`); sample app uses `com.android.legacy-kapt` for Hilt.
- Kotlin **2.3.20**, Jetpack Compose BOM **2026.03.01**, AndroidX (core-ktx 1.18.0, lifecycle 2.10.0, activity-compose 1.13.0), Hilt **2.59.2**, AppCompat **1.7.1**, Gradle Maven Publish plugin **0.36.0**.
- README badges and maintainer-facing docs for developing and releasing.

[1.0.6]: https://github.com/iVamsi/SnapNotify/compare/v1.0.5...v1.0.6
