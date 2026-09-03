package com.vamsi.snapnotify

/**
 * Haptic feedback modes for snackbar messages.
 */
enum class SnackbarHapticFeedback {
    /**
     * No haptic feedback is performed.
     */
    None,

    /**
     * Selects feedback from the message's priority and accessibility urgency:
     * - Urgent priority, or a message flagged assertive: [Error]
     * - High priority: [Warning]
     * - Normal or Low priority: [None]
     *
     * The themed helpers ([SnapNotify.showSuccess], [SnapNotify.showError],
     * [SnapNotify.showWarning]) pass their own constant instead of relying on this.
     */
    Auto,

    /**
     * Subtle confirmation tick for successful actions.
     */
    Success,

    /**
     * Subtle warning buzz.
     */
    Warning,

    /**
     * Rejection buzz for errors or failures.
     */
    Error,

    /**
     * Standard light haptic click.
     */
    Gesture
}
