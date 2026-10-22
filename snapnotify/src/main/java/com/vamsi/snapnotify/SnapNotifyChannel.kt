package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.vector.ImageVector
import com.vamsi.snapnotify.core.SnackbarManager

/**
 * An independent notification queue, obtained with [SnapNotify.channel].
 * Configuration, priority, deduplication, and clearing affect only this channel.
 * IDs and their queues live for the process lifetime. Use stable feature IDs rather than entity IDs.
 */
class SnapNotifyChannel internal constructor(
    val id: String,
    internal val manager: SnackbarManager,
) {
    /** Configures this channel without changing the global queue. */
    fun configure(config: SnapNotifyConfig) = manager.updateConfig(config)

    /** Shows a message in this channel, including the rich presentation options from 1.2.0. */
    fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        style: SnackbarStyle? = null,
        priority: SnackbarPriority = SnackbarPriority.Normal,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
        title: String? = null,
        leadingIcon: ImageVector? = null,
        leadingIconContentDescription: String? = null,
        showCloseButton: Boolean = false,
        showProgressBar: Boolean = false,
        onTimeout: (() -> Unit)? = null,
    ) {
        SnapNotify.dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            actionLabel = actionLabel,
            onAction = onAction,
            style = style,
            priority = priority,
            hapticFeedback = hapticFeedback,
            title = title,
            leadingIcon = leadingIcon,
            leadingIconContentDescription = leadingIconContentDescription,
            showCloseButton = showCloseButton,
            showProgressBar = showProgressBar,
            onTimeout = onTimeout,
            manager = manager,
        )
    }

    /** Shows a success message with success haptics. */
    fun showSuccess(message: String) = SnapNotify.dispatch(
        message, preset = SnapNotify.successPreset, manager = manager,
    )

    /** Shows an error message with an assertive accessibility announcement. */
    fun showError(message: String) = SnapNotify.dispatch(
        message, preset = SnapNotify.errorPreset, manager = manager,
    )

    /** Shows a warning message with warning haptics. */
    fun showWarning(message: String) = SnapNotify.dispatch(
        message, preset = SnapNotify.warningPreset, manager = manager,
    )

    /** Shows an informational message. */
    fun showInfo(message: String) = SnapNotify.dispatch(message, preset = SnapNotify.infoPreset, manager = manager)

    /** Preempts non-urgent messages in this channel only. */
    fun showUrgent(message: String) = SnapNotify.dispatch(
        message, priority = SnackbarPriority.Urgent,
        preset = SnapNotify.errorPreset, manager = manager,
    )

    /** Shows an Undo action with a countdown that pauses while pressed. */
    fun showUndoable(
        message: String,
        onAction: () -> Unit,
        actionLabel: String = "Undo",
        durationMillis: Long = 5_000L,
        onTimeout: (() -> Unit)? = null,
    ) = show(
        message, durationMillis = durationMillis, actionLabel = actionLabel, onAction = onAction,
        showProgressBar = true, onTimeout = onTimeout, hapticFeedback = SnackbarHapticFeedback.Gesture,
    )

    /** Asynchronously dismisses the active message and discards pending messages in this channel. */
    fun clear() {
        manager.clearAllMessages()
    }
}
