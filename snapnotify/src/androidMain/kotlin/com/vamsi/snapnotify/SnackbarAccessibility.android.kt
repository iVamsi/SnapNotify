package com.vamsi.snapnotify

import android.content.Context
import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.accessibility.AccessibilityManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.CancellationException

@Composable
internal actual fun rememberSnapNotifyPlatform(): SnapNotifyPlatform {
    val view = LocalView.current
    val context = LocalContext.current
    return remember(view, context) {
        val accessibilityManager = try {
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (unavailable: RuntimeException) {
            null
        }
        object : SnapNotifyPlatform {
            override val isAccessibilityEnabled: Boolean
                get() = accessibilityManager?.isEnabled == true

            override fun recommendedTimeoutOrNull(
                rawDurationMillis: Long,
                hasAction: Boolean,
            ): Long? = accessibilityManager?.recommendedTimeoutOrNull(
                rawDurationMillis = rawDurationMillis,
                hasAction = hasAction,
            )

            override fun triggerHapticFeedback(mode: SnackbarHapticFeedback) {
                triggerHapticFeedback(view, mode)
            }
        }
    }
}

/**
 * Fires tactile feedback for a displaying snackbar.
 *
 * @param view The view to perform feedback on. No-op when null.
 * @param mode The resolved feedback to fire. [SnackbarHapticFeedback.Auto] must already
 * have been resolved by [com.vamsi.snapnotify.core.SnackbarMessage.resolveHapticFeedback].
 */
internal fun triggerHapticFeedback(
    view: View?,
    mode: SnackbarHapticFeedback,
) {
    if (view == null) return

    val feedbackConstant = when (mode) {
        SnackbarHapticFeedback.Success -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.CONFIRM
            } else {
                HapticFeedbackConstants.VIRTUAL_KEY
            }
        }
        SnackbarHapticFeedback.Error -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                HapticFeedbackConstants.REJECT
            } else {
                HapticFeedbackConstants.LONG_PRESS
            }
        }
        SnackbarHapticFeedback.Warning -> HapticFeedbackConstants.LONG_PRESS
        SnackbarHapticFeedback.Gesture -> HapticFeedbackConstants.CLOCK_TICK
        SnackbarHapticFeedback.None, SnackbarHapticFeedback.Auto -> null
    }

    if (feedbackConstant == null) return

    try {
        view.performHapticFeedback(feedbackConstant)
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (unavailable: RuntimeException) {
        // Headless and unit-test views have no haptic hardware to drive.
    }
}

/**
 * Asks the platform how long content of this shape should stay on screen, or null when the
 * platform cannot answer.
 */
internal fun AccessibilityManager.recommendedTimeoutOrNull(
    rawDurationMillis: Long,
    hasAction: Boolean,
): Long? {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || !isEnabled) return null

    val flags = if (hasAction) {
        AccessibilityManager.FLAG_CONTENT_CONTROLS or AccessibilityManager.FLAG_CONTENT_TEXT
    } else {
        AccessibilityManager.FLAG_CONTENT_TEXT
    }

    return try {
        val clampedDuration = rawDurationMillis.coerceIn(0L, Int.MAX_VALUE.toLong()).toInt()
        getRecommendedTimeoutMillis(clampedDuration, flags).toLong()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (unavailable: RuntimeException) {
        null
    }
}
