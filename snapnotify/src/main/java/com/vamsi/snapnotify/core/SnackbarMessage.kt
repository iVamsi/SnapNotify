package com.vamsi.snapnotify.core

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.SnackbarStyle
import com.vamsi.snapnotify.SnackbarDurationWrapper
import java.util.UUID

/**
 * Represents a snackbar message with optional action and custom styling.
 *
 * @param id Unique identifier for the message
 * @param text The message text to display
 * @param duration How long the snackbar should be displayed
 * @param actionLabel Optional action button label
 * @param onAction Optional action to execute when action button is pressed
 * @param style Optional custom styling for this specific message
 */
internal data class SnackbarMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val actionLabel: String? = null,
    val onAction: (() -> Unit)? = null,
    val style: SnackbarStyle? = null,
    val customDuration: SnackbarDurationWrapper? = null,
) {
    /**
     * Returns the effective duration wrapper, preferring customDuration if available.
     */
    val effectiveDuration: SnackbarDurationWrapper
        get() = customDuration ?: SnackbarDurationWrapper.fromStandard(duration)
}
