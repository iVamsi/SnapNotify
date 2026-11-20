package com.vamsi.snapnotify

import androidx.compose.runtime.Composable

/**
 * Minimum multiplier applied to a snackbar's duration while an accessibility service is running,
 * per Google's accessibility guidance on giving users time to read and act.
 */
private const val ACCESSIBILITY_DURATION_MULTIPLIER = 2

/**
 * Platform-specific accessibility and tactile feedback capabilities.
 */
internal interface SnapNotifyPlatform {
    val isAccessibilityEnabled: Boolean
    fun recommendedTimeoutOrNull(rawDurationMillis: Long, hasAction: Boolean): Long?
    fun triggerHapticFeedback(mode: SnackbarHapticFeedback)
}

@Composable
internal expect fun rememberSnapNotifyPlatform(): SnapNotifyPlatform

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
    val minScaledDuration = if (rawDurationMillis >= Long.MAX_VALUE / ACCESSIBILITY_DURATION_MULTIPLIER) {
        Long.MAX_VALUE
    } else {
        rawDurationMillis * ACCESSIBILITY_DURATION_MULTIPLIER
    }
    return if (recommendedTimeoutMillis != null) {
        maxOf(minScaledDuration, recommendedTimeoutMillis)
    } else {
        minScaledDuration
    }
}
