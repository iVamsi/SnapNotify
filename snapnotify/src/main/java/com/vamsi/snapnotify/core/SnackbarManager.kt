package com.vamsi.snapnotify.core

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.DeduplicationStrategy
import com.vamsi.snapnotify.SnackbarDurationWrapper
import com.vamsi.snapnotify.SnackbarHapticFeedback
import com.vamsi.snapnotify.SnackbarPriority
import com.vamsi.snapnotify.SnackbarStyle
import com.vamsi.snapnotify.SnapNotifyConfig
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.PriorityQueue
import java.util.concurrent.atomic.AtomicLong

/**
 * Thread-safe singleton that manages a prioritized queue of snackbar messages.
 *
 * This class handles message queuing, priority ordering, deduplication, emission,
 * and dismissal in a thread-safe manner.
 */
internal class SnackbarManager private constructor() {

    companion object {
        @Volatile
        private var INSTANCE: SnackbarManager? = null

        fun getInstance(): SnackbarManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SnackbarManager().also { INSTANCE = it }
            }
        }

        private const val INITIAL_QUEUE_CAPACITY = 11
    }

    private val messageComparator = Comparator<SnackbarMessage> { m1, m2 ->
        val priorityComp = m2.priority.compareTo(m1.priority)
        if (priorityComp != 0) {
            priorityComp
        } else {
            m1.sequenceNumber.compareTo(m2.sequenceNumber)
        }
    }

    private val messageQueue = PriorityQueue(INITIAL_QUEUE_CAPACITY, messageComparator)
    private val _messages = MutableStateFlow<SnackbarMessage?>(null)
    private val mutex = Mutex()
    private val sequenceGenerator = AtomicLong(0)

    @Volatile
    private var config: SnapNotifyConfig = SnapNotifyConfig()

    @Volatile
    private var queueScope: CoroutineScope? = CoroutineScope(
        SupervisorJob() + config.dispatcher + CoroutineName("SnapNotifySnackbarQueue")
    )

    fun updateConfig(newConfig: SnapNotifyConfig) {
        require(newConfig.maxQueueSize > 0) { "maxQueueSize must be greater than 0" }
        queueScope?.cancel()
        config = newConfig
        queueScope = CoroutineScope(
            SupervisorJob() + config.dispatcher + CoroutineName("SnapNotifySnackbarQueue")
        )
    }

    fun getConfig(): SnapNotifyConfig = config

    /**
     * StateFlow of messages that need to be displayed.
     */
    val messages: StateFlow<SnackbarMessage?> = _messages.asStateFlow()

    /**
     * Queues a message and suspends until it has been accepted by the queue.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     */
    suspend fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        show(message, duration, null, null)
    }

    /**
     * Queues a message with an action button.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     */
    suspend fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
    ) {
        show(message, duration, actionLabel, onAction, null)
    }

    private fun createMessage(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        style: SnackbarStyle? = null,
        customDuration: SnackbarDurationWrapper? = null,
        priority: SnackbarPriority = SnackbarPriority.Normal,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
        isAssertive: Boolean = false,
    ): SnackbarMessage = SnackbarMessage(
        text = message,
        duration = duration,
        actionLabel = actionLabel,
        onAction = onAction,
        style = style,
        customDuration = customDuration,
        priority = priority,
        hapticFeedback = hapticFeedback,
        isAssertive = isAssertive,
    )

    /**
     * Queues an already-built message.
     */
    suspend fun show(message: SnackbarMessage) {
        enqueueMessage(message)
    }

    /**
     * Queues an already-built message without suspending the caller.
     */
    fun showMessage(message: SnackbarMessage) {
        queueScope?.launch {
            enqueueMessage(message)
        }
    }

    /**
     * Queues a message with full control over styling, priority, and feedback.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param style Optional styling for this message
     * @param priority Queue priority; [SnackbarPriority.Urgent] preempts a displaying message
     * @param hapticFeedback Haptic feedback to fire when the message displays
     * @param isAssertive Whether accessibility services should interrupt to announce this
     */
    suspend fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        style: SnackbarStyle? = null,
        priority: SnackbarPriority = SnackbarPriority.Normal,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
        isAssertive: Boolean = false,
    ) {
        show(
            createMessage(
                message = message,
                duration = duration,
                actionLabel = actionLabel,
                onAction = onAction,
                style = style,
                priority = priority,
                hapticFeedback = hapticFeedback,
                isAssertive = isAssertive,
            )
        )
    }

    /**
     * Queues a message without suspending the caller.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     */
    fun showMessage(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
    ) {
        showMessage(message, duration, null, null)
    }

    /**
     * Queues a message with an action button without suspending the caller.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     */
    fun showMessage(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
    ) {
        showMessage(message, duration, actionLabel, onAction, null)
    }

    /**
     * Queues a message with full control over styling, priority, and feedback,
     * without suspending the caller.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param style Optional styling for this message
     * @param priority Queue priority; [SnackbarPriority.Urgent] preempts a displaying message
     * @param hapticFeedback Haptic feedback to fire when the message displays
     * @param isAssertive Whether accessibility services should interrupt to announce this
     */
    fun showMessage(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        style: SnackbarStyle? = null,
        priority: SnackbarPriority = SnackbarPriority.Normal,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
        isAssertive: Boolean = false,
    ) {
        showMessage(
            createMessage(
                message = message,
                duration = duration,
                actionLabel = actionLabel,
                onAction = onAction,
                style = style,
                priority = priority,
                hapticFeedback = hapticFeedback,
                isAssertive = isAssertive,
            )
        )
    }

    /**
     * Queues a message displayed for an exact number of milliseconds.
     *
     * @param message The text to display
     * @param durationMillis How long to display the snackbar, in milliseconds
     */
    suspend fun showWithCustomDuration(
        message: String,
        durationMillis: Long,
    ) {
        showWithCustomDuration(message, durationMillis, null, null, null)
    }

    /**
     * Queues a message with an action button, displayed for an exact number of milliseconds.
     *
     * @param message The text to display
     * @param durationMillis How long to display the snackbar, in milliseconds
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     */
    suspend fun showWithCustomDuration(
        message: String,
        durationMillis: Long,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
    ) {
        showWithCustomDuration(message, durationMillis, actionLabel, onAction, null)
    }

    /**
     * Queues a message displayed for an exact number of milliseconds, with full control
     * over styling, priority, and feedback.
     *
     * @param message The text to display
     * @param durationMillis How long to display the snackbar, in milliseconds
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param style Optional styling for this message
     * @param priority Queue priority; [SnackbarPriority.Urgent] preempts a displaying message
     * @param hapticFeedback Haptic feedback to fire when the message displays
     * @param isAssertive Whether accessibility services should interrupt to announce this
     */
    suspend fun showWithCustomDuration(
        message: String,
        durationMillis: Long,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        style: SnackbarStyle? = null,
        priority: SnackbarPriority = SnackbarPriority.Normal,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
        isAssertive: Boolean = false,
    ) {
        enqueueMessage(
            createMessage(
                message = message,
                duration = SnackbarDuration.Short,
                actionLabel = actionLabel,
                onAction = onAction,
                style = style,
                customDuration = SnackbarDurationWrapper.fromMillis(durationMillis),
                priority = priority,
                hapticFeedback = hapticFeedback,
                isAssertive = isAssertive,
            )
        )
    }

    suspend fun dismissCurrent() {
        mutex.withLock {
            _messages.value = messageQueue.poll()
        }
    }

    suspend fun dismissMessage(message: SnackbarMessage) {
        mutex.withLock {
            if (_messages.value?.id == message.id) {
                _messages.value = messageQueue.poll()
            }
        }
    }

    suspend fun clearAll() {
        mutex.withLock {
            messageQueue.clear()
            _messages.value = null
        }
    }

    fun clearAllMessages(): Job? {
        return queueScope?.launch {
            clearAll()
        }
    }

    private suspend fun enqueueMessage(snackbarMessage: SnackbarMessage) {
        var droppedMessageText: String? = null
        var dropCallback: ((String) -> Unit)? = null

        mutex.withLock {
            val configSnapshot = config
            dropCallback = configSnapshot.onMessageDropped

            val messageWithSeq = snackbarMessage.copy(
                sequenceNumber = sequenceGenerator.incrementAndGet()
            )

            when (configSnapshot.deduplicationStrategy) {
                DeduplicationStrategy.DropDuplicate -> {
                    val isDuplicateOfCurrent = _messages.value?.text == messageWithSeq.text
                    val isDuplicateInQueue = messageQueue.any { it.text == messageWithSeq.text }
                    if (isDuplicateOfCurrent || isDuplicateInQueue) {
                        return
                    }
                }
                DeduplicationStrategy.ReplaceExisting -> {
                    if (_messages.value?.text == messageWithSeq.text) {
                        _messages.value = messageWithSeq
                        return
                    }
                    val existing = messageQueue.firstOrNull { it.text == messageWithSeq.text }
                    if (existing != null) {
                        messageQueue.remove(existing)
                        // Bump to the top of its priority level by ensuring its sequenceNumber
                        // is smaller than all existing messages of the same priority
                        val minSeqInPriority = messageQueue.filter { it.priority == messageWithSeq.priority }
                            .minOfOrNull { it.sequenceNumber }
                        val bumpedMessage = if (minSeqInPriority != null) {
                            messageWithSeq.copy(sequenceNumber = minSeqInPriority - 1)
                        } else {
                            messageWithSeq
                        }
                        messageQueue.offer(bumpedMessage)
                        return
                    }
                }
                DeduplicationStrategy.None -> {
                    // Allow duplicates
                }
            }

            val active = _messages.value
            if (active == null) {
                _messages.value = messageWithSeq
                return
            }

            if (messageWithSeq.priority == SnackbarPriority.Urgent && active.priority != SnackbarPriority.Urgent) {
                droppedMessageText = offerWithinCapacityLocked(active, configSnapshot.maxQueueSize)
                _messages.value = messageWithSeq
            } else {
                droppedMessageText = offerWithinCapacityLocked(messageWithSeq, configSnapshot.maxQueueSize)
            }
        }

        droppedMessageText?.let {
            dropCallback?.invoke(it)
        }
    }

    /**
     * Queues [candidate], making room first if the queue is already at [maxQueueSize].
     *
     * The candidate itself loses when it ranks below everything already queued; otherwise the
     * lowest-priority, oldest queued message is evicted to make room. Returns the text of
     * whichever message was dropped, or null if nothing was.
     */
    private fun offerWithinCapacityLocked(candidate: SnackbarMessage, maxQueueSize: Int): String? {
        if (messageQueue.size < maxQueueSize) {
            messageQueue.offer(candidate)
            return null
        }
        if (candidate.priority < messageQueue.minOf { it.priority }) {
            return candidate.text
        }
        val evicted = evictLowestPriorityOldestLocked()
        messageQueue.offer(candidate)
        return evicted
    }

    private fun evictLowestPriorityOldestLocked(): String? {
        if (messageQueue.isEmpty()) return null
        val minPriority = messageQueue.minOf { it.priority }
        val oldestLowest = messageQueue.filter { it.priority == minPriority }
            .minByOrNull { it.sequenceNumber }
        return if (oldestLowest != null) {
            messageQueue.remove(oldestLowest)
            oldestLowest.text
        } else {
            messageQueue.poll()?.text
        }
    }
}
