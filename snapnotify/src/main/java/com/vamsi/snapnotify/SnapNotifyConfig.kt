package com.vamsi.snapnotify

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlin.jvm.internal.DefaultConstructorMarker

/**
 * Configuration options for SnapNotify's internal snackbar queue.
 *
 * @param maxQueueSize Maximum number of pending snackbars allowed in the queue. Must be > 0.
 * Defaults to 50 messages. When the queue is full, the lowest-priority and oldest message is dropped.
 *
 * @param onMessageDropped Optional callback invoked with the text of a message that was dropped
 * when the queue exceeds [maxQueueSize]. This is useful for logging/monitoring queue saturation.
 *
 * @param deduplicationStrategy How repeated identical messages are handled.
 * Defaults to [DeduplicationStrategy.DropDuplicate].
 *
 * @param isHapticFeedbackEnabled Whether snackbars fire haptic feedback when they display.
 * Defaults to true.
 *
 * @param isAccessibilityScalingEnabled Whether display durations are scaled up while an
 * accessibility service such as TalkBack is running. Defaults to true.
 *
 * Example usage:
 * ```kotlin
 * SnapNotify.configure(
 *     SnapNotifyConfig(
 *         maxQueueSize = 100,
 *         deduplicationStrategy = DeduplicationStrategy.DropDuplicate,
 *         isHapticFeedbackEnabled = true,
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
    val deduplicationStrategy: DeduplicationStrategy = DeduplicationStrategy.DropDuplicate,
    val isHapticFeedbackEnabled: Boolean = true,
    val isAccessibilityScalingEnabled: Boolean = true,
) {
    /**
     * Preserves the default-argument constructor used by Kotlin callers compiled against 1.0.x.
     */
    @Suppress("UNUSED_PARAMETER")
    constructor(
        maxQueueSize: Int,
        onMessageDropped: ((String) -> Unit)?,
        mask: Int,
        marker: DefaultConstructorMarker?,
    ) : this(
        maxQueueSize = if (mask and 0x1 != 0) DEFAULT_MAX_QUEUE_SIZE else maxQueueSize,
        onMessageDropped = if (mask and 0x2 != 0) null else onMessageDropped,
    )

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
     * Preserves the two-property copy API used by Kotlin callers compiled against 1.0.x.
     */
    fun copy(
        maxQueueSize: Int = this.maxQueueSize,
        onMessageDropped: ((String) -> Unit)? = this.onMessageDropped,
    ): SnapNotifyConfig = SnapNotifyConfig(
        maxQueueSize = maxQueueSize,
        onMessageDropped = onMessageDropped,
        deduplicationStrategy = deduplicationStrategy,
        isHapticFeedbackEnabled = isHapticFeedbackEnabled,
        isAccessibilityScalingEnabled = isAccessibilityScalingEnabled,
    )

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
