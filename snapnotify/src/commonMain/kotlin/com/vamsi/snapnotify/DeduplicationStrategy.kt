package com.vamsi.snapnotify

/**
 * Strategy for handling duplicate snackbar messages.
 */
enum class DeduplicationStrategy {
    /**
     * Allow duplicate messages without suppression.
     */
    None,

    /**
     * Ignore incoming message if an identical message (by text) is already displayed or queued.
     */
    DropDuplicate,

    /**
     * If an identical message (by text) is already displayed or queued, replace it with the new message
     * (resetting its duration timer if currently displayed or updating it in the queue).
     */
    ReplaceExisting
}
