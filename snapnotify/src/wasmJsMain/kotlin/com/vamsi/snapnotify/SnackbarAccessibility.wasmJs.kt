package com.vamsi.snapnotify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

@Composable
internal actual fun rememberSnapNotifyPlatform(): SnapNotifyPlatform {
    val hapticFeedback = LocalHapticFeedback.current
    return remember(hapticFeedback) {
        object : SnapNotifyPlatform {
            override val isAccessibilityEnabled: Boolean = false

            override fun recommendedTimeoutOrNull(
                rawDurationMillis: Long,
                hasAction: Boolean,
            ): Long? = null

            override fun triggerHapticFeedback(mode: SnackbarHapticFeedback) {
                when (mode) {
                    SnackbarHapticFeedback.Success,
                    SnackbarHapticFeedback.Gesture -> {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    }
                    SnackbarHapticFeedback.Warning,
                    SnackbarHapticFeedback.Error -> {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    }
                    SnackbarHapticFeedback.None,
                    SnackbarHapticFeedback.Auto -> Unit
                }
            }
        }
    }
}
