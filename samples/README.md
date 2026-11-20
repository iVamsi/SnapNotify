# SnapNotify 2.0 – Sample Applications

This directory (together with `:SnapNotifyDemo`) provides standalone sample applications for every supported architecture and platform target:

| App Type | Module / Directory | Description | How to Build / Run |
| :--- | :--- | :--- | :--- |
| **1. Android Native App** | [`:SnapNotifyDemo`](../SnapNotifyDemo) | Android-only Jetpack Compose + Hilt consumer app (`com.android.application`) | `./gradlew :SnapNotifyDemo:installDebug` |
| **2a. iOS Native App (Xcode Project)** | [`SnapNotifyIosDemo`](../SnapNotifyIosDemo) | Standalone Xcode project (`SnapNotifyIosDemo.xcodeproj`) opening directly in Xcode and linking `SnapNotify.framework` (`:snapnotify`) | `open SnapNotifyIosDemo/SnapNotifyIosDemo.xcodeproj` (Cmd+R) |
| **2b. iOS Native App (Gradle CLI)** | [`:samples:ios-native`](ios-native) | Gradle-built native SwiftUI iOS app (`SnapNotifyIosNativeApp.swift`) calling `SnapNotifyIosBridge.framework` | `./gradlew :samples:ios-native:buildIosSimulatorApp` |
| **3. KMM App (Shared Logic + Native UI)** | [`:samples:kmm-app`](kmm-app) | Shared KMM business logic (`KmmOrderPresenter` in `commonMain`) calling `SnapNotify` from pure Kotlin, paired with native Android (`KmmAndroidScreen`) and iOS SwiftUI (`SnapNotifyKmmApp.swift`) UIs | `./gradlew :samples:kmm-app:testAndroidHostTest :samples:kmm-app:buildIosSimulatorApp` |
| **4. CMM / CMP App (Shared Compose UI)** | [`:samples:cmp-app`](cmp-app) | 100% shared Compose Multiplatform (`CmpApp` in `commonMain`) running across Android, iOS, Desktop, and Web | `./gradlew :samples:cmp-app:run` or `./gradlew :samples:cmp-app:packageIosSimulatorApp` |
| **5. Web App (`wasmJs`)** | [`:samples:web-app`](web-app) | Dedicated Compose for Web (WebAssembly) browser application | `./gradlew :samples:web-app:wasmJsBrowserDevelopmentRun` |
| **6. Mac Desktop App (`jvm` / `.dmg`)** | [`:samples:desktop-mac`](desktop-mac) | Dedicated macOS Desktop app with `Cmd+1..2` menu bar shortcuts and `TargetFormat.Dmg` distribution | `./gradlew :samples:desktop-mac:run` |
| **7. Windows Desktop App (`jvm` / `.msi` / `.exe`)** | [`:samples:desktop-windows`](desktop-windows) | Dedicated Windows Desktop app with Fluent dark styling, `Ctrl+1..2` menu shortcuts, and `TargetFormat.Msi` / `TargetFormat.Exe` distribution | `./gradlew :samples:desktop-windows:run` |
