package com.vamsi.snapnotify

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Configuration options for SnapNotify's internal snackbar queue.
 *
 * @param maxQueueSize Maximum number of pending snackbars allowed in the queue. Must be > 0.
 * Defaults to 50 messages. When the queue is full, the oldest message will be dropped.
 *
 * @param onMessageDropped Optional callback invoked with the text of a message that was dropped
 * when the queue exceeds [maxQueueSize]. This is useful for logging/monitoring queue saturation.
 *
 * Example usage:
 * ```kotlin
 * SnapNotify.configure(
 *     SnapNotifyConfig(
 *         maxQueueSize = 100,
 *         onMessageDropped = { message ->
 *             Log.w("SnapNotify", "Dropped message: $message")
 *         }
 *     )
 * )
 * ```
 */
data class SnapNotifyConfig @JvmOverloads constructor(
    val maxQueueSize: Int = DEFAULT_MAX_QUEUE_SIZE,
    val onMessageDropped: ((String) -> Unit)? = null,
) {
    /**
     * Internal dispatcher for the queue scope.
     * This is not exposed in the public constructor to maintain API simplicity,
     * but can be overridden for testing purposes.
     */
    internal var dispatcher: CoroutineDispatcher = Dispatchers.Default
        private set

    init {
        require(maxQueueSize > 0) { "maxQueueSize must be greater than 0" }
    }

    /**
     * Internal method to create a config with a custom dispatcher (for testing).
     */
    internal fun withDispatcher(dispatcher: CoroutineDispatcher): SnapNotifyConfig {
        return copy().also { it.dispatcher = dispatcher }
    }

    companion object {
        const val DEFAULT_MAX_QUEUE_SIZE = 50
    }
}

