package com.vamsi.snapnotify

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.accessibility.AccessibilityManager
import kotlinx.coroutines.CancellationException

/**
 * Minimum multiplier applied to a snackbar's duration while an accessibility service is running,
 * per Google's accessibility guidance on giving users time to read and act.
 */
private const val ACCESSIBILITY_DURATION_MULTIPLIER = 2

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
 * Returns how long a snackbar should stay on screen.
 *
 * @param rawDurationMillis The duration the caller asked for.
 * @param isAccessibilityEnabled Whether an accessibility service is currently running.
 * @param isScalingConfigEnabled Whether [SnapNotifyConfig.isAccessibilityScalingEnabled] is on.
 * @param recommendedTimeoutMillis The platform's own recommendation, when it can supply one.
 */
internal fun computeAccessibleDuration(
    rawDurationMillis: Long,
    isAccessibilityEnabled: Boolean,
    isScalingConfigEnabled: Boolean,
    recommendedTimeoutMillis: Long? = null,
): Long {
    if (!isScalingConfigEnabled || !isAccessibilityEnabled) {
        return rawDurationMillis
    }
    val minScaledDuration = rawDurationMillis * ACCESSIBILITY_DURATION_MULTIPLIER
    return if (recommendedTimeoutMillis != null) {
        maxOf(minScaledDuration, recommendedTimeoutMillis)
    } else {
        minScaledDuration
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
        getRecommendedTimeoutMillis(rawDurationMillis.toInt(), flags).toLong()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (unavailable: RuntimeException) {
        null
    }
}
