# SnapNotifyIosDemo (Native iOS Xcode App)

This standalone Xcode project is the native iOS counterpart to [`:SnapNotifyDemo`](../SnapNotifyDemo). It builds and runs a pure SwiftUI application against the `:snapnotify` library (`SnapNotify.framework`), demonstrating every SnapNotify feature from native Swift code.

## Open and Run in Xcode

1. Open the Xcode project:
   ```bash
   open SnapNotifyIosDemo/SnapNotifyIosDemo.xcodeproj
   ```
2. Select the `SnapNotifyIosDemo` scheme and an iOS Simulator (e.g., iPhone 16) or connected device.
3. Press **Run (`Cmd+R`)**.

During build, Xcode automatically runs `./gradlew :snapnotify:embedAndSignAppleFrameworkForXcode` via the **Compile SnapNotify Kotlin Framework** build phase so any changes in `:snapnotify` are compiled into `SnapNotify.framework`.

## Build from Command Line

To build the Kotlin framework and the Xcode project from CLI:

```bash
# 1. Link SnapNotify.framework for iOS Simulator
./gradlew :snapnotify:linkDebugFrameworkIosSimulatorArm64

# 2. Build the Xcode app
xcodebuild -project SnapNotifyIosDemo/SnapNotifyIosDemo.xcodeproj \
  -scheme SnapNotifyIosDemo \
  -configuration Debug \
  -sdk iphonesimulator \
  -derivedDataPath SnapNotifyIosDemo/build/DerivedData \
  SKIP_GRADLE_BUILD=YES \
  CODE_SIGNING_ALLOWED=NO \
  build
```

> Omit `SKIP_GRADLE_BUILD=YES` if you want `xcodebuild` itself to invoke `:snapnotify:embedAndSignAppleFrameworkForXcode`.

## Structure

- [SnapNotifyIosDemoApp.swift](SnapNotifyIosDemo/SnapNotifyIosDemoApp.swift) – App entry point hosting `DemoScreen` (counterpart to `MainActivity.kt`)
- [DemoScreen.swift](SnapNotifyIosDemo/DemoScreen.swift) – Full feature showcase matching [DemoScreen.kt](../SnapNotifyDemo/src/main/java/com/vamsi/snapnotify/demo/DemoScreen.kt)
- [ScopedChannelDemoView.swift](SnapNotifyIosDemo/ScopedChannelDemoView.swift) – Isolated bottom-sheet and dialog channels matching [ScopedChannelDemo.kt](../SnapNotifyDemo/src/main/java/com/vamsi/snapnotify/demo/ScopedChannelDemo.kt)
- [SnapNotifyHostView.swift](SnapNotifyIosDemo/SnapNotifyHostView.swift) – `UIViewControllerRepresentable` wrappers around `SnapNotifyIos.shared` from `SnapNotify.framework`
