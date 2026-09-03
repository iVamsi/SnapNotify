# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.1.0] - 2026-09-02

### Added

- **Priority Queue & Preemption**: Introduced `SnackbarPriority` (`Low`, `Normal`, `High`, `Urgent`). Messages in the queue are ordered by priority with FIFO stability within the same priority level.
- **Urgent Preemption**: Messages with `SnackbarPriority.Urgent` immediately preempt currently displaying lower-priority notifications. The interrupted message goes back on the queue and resumes afterwards, unless the queue is already full and it ranks below everything in it, in which case it is dropped and reported to `onMessageDropped`. Added `SnapNotify.showUrgent(...)` convenience API.
- **Deduplication Engine**: Added `DeduplicationStrategy` (`None`, `DropDuplicate`, `ReplaceExisting`) in `SnapNotifyConfig` (defaulting to `DropDuplicate`) to prevent rapid repeated taps from spamming the interface.
- **Haptic Feedback**: Introduced `SnackbarHapticFeedback` (`Auto`, `Success`, `Warning`, `Error`, `Gesture`, `None`), firing subtle tactile vibrations when a snackbar displays. The themed helpers pick their own feedback; `Auto` picks from the message's priority. Turn it off with `SnapNotifyConfig(isHapticFeedbackEnabled = false)`.
- **Accessibility Hardening**: Added `LiveRegionMode.Assertive` semantics for error/urgent notifications and `LiveRegionMode.Polite` for standard notifications. While an accessibility service such as TalkBack is running, every snackbar stays up at least twice as long, standard `Short`/`Long` durations included. Turn it off with `SnapNotifyConfig(isAccessibilityScalingEnabled = false)`.
- **Accessibility Styling Carrier**: Introduced internal visuals carrier to transport styling and accessibility metadata to custom snackbars.
- **Queue Configuration**: `SnapNotifyConfig` gained `deduplicationStrategy`, `isHapticFeedbackEnabled`, and `isAccessibilityScalingEnabled`. Existing constructor calls and destructuring are unaffected.

### Changed

- **Queue Overflow**: A full queue now drops its lowest-priority, oldest message rather than simply its oldest, so an urgent message is no longer evicted by routine ones. `onMessageDropped` still reports whatever was dropped.

### Fixed

- **LaunchedEffect Cancellation Bug**: Removed detached coroutine launches (`scope.launch`) inside `LaunchedEffect` in `SnapNotifyProvider`, establishing structured concurrency and eliminating orphaned timers when composables leave composition.
- **Race Condition in Dismissal**: Implemented targeted message dismissal (`dismissMessage(message)`), ensuring that dismissing an expired message never accidentally dismisses a newly active message.

### Compatibility

Source- and binary-compatible with 1.0.6. `SnapNotifyConfig` retains its existing Kotlin constructor,
two-property `copy`, and destructuring signatures while adding the v1.1 configuration options.

## [1.0.6] - 2026-04-05

### Changed

- Android Gradle Plugin **9.1.0** and Gradle **9.3.1**; Kotlin compilation uses [AGP built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin) (removed `org.jetbrains.kotlin.android`); sample app uses `com.android.legacy-kapt` for Hilt.
- Kotlin **2.3.20**, Jetpack Compose BOM **2026.03.01**, AndroidX (core-ktx 1.18.0, lifecycle 2.10.0, activity-compose 1.13.0), Hilt **2.59.2**, AppCompat **1.7.1**, Gradle Maven Publish plugin **0.36.0**.
- README badges and maintainer-facing docs for developing and releasing.

[1.1.0]: https://github.com/iVamsi/SnapNotify/compare/v1.0.6...v1.1.0
[1.0.6]: https://github.com/iVamsi/SnapNotify/compare/v1.0.5...v1.0.6
