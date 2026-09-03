package com.vamsi.snapnotify

/**
 * Priority levels for snackbar messages.
 *
 * Higher priority messages are displayed before lower priority messages in the queue.
 * [Urgent] messages immediately preempt any currently playing lower-priority message.
 */
enum class SnackbarPriority {
    /**
     * Low priority for background or non-critical informational messages.
     */
    Low,

    /**
     * Standard priority for typical notifications (default).
     */
    Normal,

    /**
     * High priority for important warnings or errors.
     */
    High,

    /**
     * Urgent priority that immediately preempts any currently displayed lower-priority message.
     */
    Urgent
}
