# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).

## [1.3.0] - 2026-10-21

### Added

- Named channels with independent priority queues, deduplication, overflow limits, and configuration.
- `SnapNotify.channel(id)` and `SnapNotifyChannel.show`, themed helpers, `showUndoable`, and `clear`.
- Providers that accept a channel object or ID, and `LocalSnapNotifyChannel` for the nearest host.
  These providers fill the available width and allow enough height for their content or snackbar,
  including in compact sheets and dialogs.
- Dialog and bottom-sheet examples that host scoped notifications using the existing provider.

### Changed

- Built with Kotlin **2.4.21**, Jetpack Compose BOM **2026.09.00**, core-ktx **1.19.1**, and AGP
  **9.4.1**. Apps that upgrade receive the newer Compose BOM and Kotlin stdlib through the POM.

### Fixed

- When a provider leaves composition while a message is showing, such as a sheet closing during an
  Undo countdown, the message's `onTimeout` now runs. Before, neither `onAction` nor `onTimeout` ran.
  This also applies to the global provider, for example on a configuration change.

### Compatibility

- Global `SnapNotify` calls retain their existing queue and provider defaults. Providers for distinct
  channels may nest; repeated ancestors for the same channel suppress duplicate rendering.
- The 1.1 and 1.2 Compose provider entry points remain available for compiled callers.
- Named channels persist for the process lifetime. Use stable feature IDs and clear completed work.

## [1.2.0] - 2026-09-22

### Added

- **Rich snackbar**: `title`, `leadingIcon`, and `showCloseButton` on `show`, with or without an action. Pass `hapticFeedback` for typed feedback. A title is a headline above the message. The close control dismisses without running the action.
- **Undo countdown**: `SnapNotify.showUndoable(...)` shows an action (default label "Undo") and a bar that counts down from full to empty. Pressing the snackbar pauses the countdown. `onTimeout` runs when the timer finishes. An action click or a dismiss does not run `onTimeout`.
- **Placement**: `SnapNotifyProvider(placement = ...)` accepts `NotificationPlacement.BottomSnackbar` (unchanged default), `TopPill`, and `TopBanner`. Top placements slide in from the top with a spring. `TopPill` uses a pill shape. `TopBanner` spans the width. Pass `topInsetPadding` to override the status-bar inset.

### Compatibility

Source- and binary-compatible with 1.1.0. Existing calls keep their behavior, and code compiled against 1.1.0 links without a rebuild. A custom `hostContent` still owns layout, and `placement` applies to the default host only.

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

[1.3.0]: https://github.com/iVamsi/SnapNotify/compare/v1.2.0...v1.3.0
[1.2.0]: https://github.com/iVamsi/SnapNotify/compare/v1.1.0...v1.2.0
[1.1.0]: https://github.com/iVamsi/SnapNotify/compare/v1.0.6...v1.1.0
[1.0.6]: https://github.com/iVamsi/SnapNotify/compare/v1.0.5...v1.0.6
