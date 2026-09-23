package com.vamsi.snapnotify.core

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.vector.ImageVector
import com.vamsi.snapnotify.SnackbarDurationWrapper
import com.vamsi.snapnotify.SnackbarHapticFeedback
import com.vamsi.snapnotify.SnackbarPriority
import com.vamsi.snapnotify.SnackbarStyle
import java.util.UUID

/**
 * Represents a snackbar message with optional action, custom styling, priority, and haptic feedback.
 *
 * @param id Unique identifier for the message
 * @param text The message text to display
 * @param duration How long the snackbar should be displayed
 * @param actionLabel Optional action button label
 * @param onAction Optional action to execute when action button is pressed
 * @param style Optional custom styling for this specific message
 * @param customDuration Optional custom millisecond duration wrapper
 * @param priority Message priority ([SnackbarPriority.Low], [SnackbarPriority.Normal], [SnackbarPriority.High], [SnackbarPriority.Urgent])
 * @param hapticFeedback Haptic feedback preference for when the message displays
 * @param isAssertive Whether this message should be announced assertively by accessibility services
 * @param title Optional headline shown above [text]
 * @param leadingIcon Optional icon shown before the text
 * @param leadingIconContentDescription Accessibility label for [leadingIcon]. Null leaves the icon decorative.
 * @param showCloseButton Whether the snackbar draws a trailing dismiss control
 * @param showProgressBar Whether the snackbar draws a countdown bar and pauses that countdown while pressed
 * @param onTimeout Called when the library timer dismisses the snackbar. Not called for action clicks or manual dismiss.
 * @param sequenceNumber Monotonically increasing sequence number for deterministic FIFO ordering within same priority
 */
internal data class SnackbarMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val style: SnackbarStyle? = null,
    val customDuration: SnackbarDurationWrapper? = null,
    val priority: SnackbarPriority = SnackbarPriority.Normal,
    val hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
    val isAssertive: Boolean = false,
    val title: String? = null,
    val leadingIcon: ImageVector? = null,
    val leadingIconContentDescription: String? = null,
    val showCloseButton: Boolean = false,
    val showProgressBar: Boolean = false,
    val onTimeout: (() -> Unit)? = null,
    val sequenceNumber: Long = 0L,
) {
    /**
     * Returns the effective duration wrapper, preferring customDuration if available.
     */
    val effectiveDuration: SnackbarDurationWrapper
        get() = customDuration ?: SnackbarDurationWrapper.fromStandard(duration)

    /**
     * Resolves the concrete haptic feedback to fire.
     * See [SnackbarHapticFeedback.Auto] for how an automatic choice is made.
     */
    fun resolveHapticFeedback(): SnackbarHapticFeedback {
        if (hapticFeedback != SnackbarHapticFeedback.Auto) {
            return hapticFeedback
        }
        return when {
            priority == SnackbarPriority.Urgent || isAssertive -> SnackbarHapticFeedback.Error
            priority == SnackbarPriority.High -> SnackbarHapticFeedback.Warning
            else -> SnackbarHapticFeedback.None
        }
    }

    /**
     * Whether this message should be announced assertively by accessibility services.
     * Urgent messages and explicit error/assertive messages interrupt speech assertively;
     * informational messages (including High priority) announce politely.
     */
    val isAssertiveAccessibility: Boolean
        get() = isAssertive || priority == SnackbarPriority.Urgent
}
