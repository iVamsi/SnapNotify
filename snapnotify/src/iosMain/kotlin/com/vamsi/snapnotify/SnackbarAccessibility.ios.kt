package com.vamsi.snapnotify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIAccessibilityIsSwitchControlRunning
import platform.UIKit.UIAccessibilityIsVoiceOverRunning
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

@Composable
internal actual fun rememberSnapNotifyPlatform(): SnapNotifyPlatform = remember {
    object : SnapNotifyPlatform {
        override val isAccessibilityEnabled: Boolean
            get() = UIAccessibilityIsVoiceOverRunning() || UIAccessibilityIsSwitchControlRunning()

        override fun recommendedTimeoutOrNull(
            rawDurationMillis: Long,
            hasAction: Boolean,
        ): Long? = null

        override fun triggerHapticFeedback(mode: SnackbarHapticFeedback) {
            when (mode) {
                SnackbarHapticFeedback.Success -> {
                    UINotificationFeedbackGenerator().notificationOccurred(
                        UINotificationFeedbackType.UINotificationFeedbackTypeSuccess
                    )
                }
                SnackbarHapticFeedback.Warning -> {
                    UINotificationFeedbackGenerator().notificationOccurred(
                        UINotificationFeedbackType.UINotificationFeedbackTypeWarning
                    )
                }
                SnackbarHapticFeedback.Error -> {
                    UINotificationFeedbackGenerator().notificationOccurred(
                        UINotificationFeedbackType.UINotificationFeedbackTypeError
                    )
                }
                SnackbarHapticFeedback.Gesture -> {
                    UIImpactFeedbackGenerator(
                        style = UIImpactFeedbackStyle.UIImpactFeedbackStyleLight
                    ).impactOccurred()
                }
                SnackbarHapticFeedback.None, SnackbarHapticFeedback.Auto -> Unit
            }
        }
    }
}
