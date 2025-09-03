package com.vamsi.snapnotify.core

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.SnackbarStyle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Thread-safe singleton that manages a queue of snackbar messages.
 * 
 * This class handles message queuing, emission, and dismissal in a thread-safe manner.
 * Messages are queued when multiple snackbars are triggered rapidly and displayed
 * sequentially through StateFlow emissions.
 */
@Singleton
class SnackbarManager @Inject constructor() {
    
    private val messageQueue = ConcurrentLinkedQueue<SnackbarMessage>()
    private val _messages = MutableStateFlow<SnackbarMessage?>(null)
    private val mutex = Mutex()
    
    /**
     * StateFlow of messages that need to be displayed.
     * Observers should collect this flow to display snackbars.
     */
    val messages: StateFlow<SnackbarMessage?> = _messages.asStateFlow()
    
    /**
     * Shows a simple snackbar message.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     */
    suspend fun show(
        message: String, 
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        show(message, duration, null, null)
    }
    
    /**
     * Shows a snackbar message with an optional action button.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param actionLabel Optional action button label
     * @param onAction Optional action to execute when action button is pressed
     */
    suspend fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        show(message, duration, actionLabel, onAction, null)
    }
    
    /**
     * Shows a snackbar message with custom styling and optional action button.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param actionLabel Optional action button label
     * @param onAction Optional action to execute when action button is pressed
     * @param style Optional custom styling for this message
     */
    suspend fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        style: SnackbarStyle? = null
    ) {
        val snackbarMessage = SnackbarMessage(
            text = message,
            duration = duration,
            actionLabel = actionLabel,
            onAction = onAction,
            style = style
        )
        
        mutex.withLock {
            if (_messages.value == null) {
                // No message currently displayed, show immediately
                _messages.value = snackbarMessage
            } else {
                // Queue the message for later display
                messageQueue.offer(snackbarMessage)
            }
        }
    }
    
    /**
     * Non-suspending version of show for calling from anywhere without coroutine scope.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     */
    fun showMessage(
        message: String, 
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        showMessage(message, duration, null, null)
    }
    
    /**
     * Non-suspending version of show with action support.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param actionLabel Optional action button label
     * @param onAction Optional action to execute when action button is pressed
     */
    fun showMessage(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        showMessage(message, duration, actionLabel, onAction, null)
    }
    
    /**
     * Non-suspending version of show with custom styling and action support.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param actionLabel Optional action button label
     * @param onAction Optional action to execute when action button is pressed
     * @param style Optional custom styling for this message
     */
    fun showMessage(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        style: SnackbarStyle? = null
    ) {
        val snackbarMessage = SnackbarMessage(
            text = message,
            duration = duration,
            actionLabel = actionLabel,
            onAction = onAction,
            style = style
        )
        
        // Use a simple lock-free approach for non-suspending calls
        if (_messages.value == null) {
            _messages.value = snackbarMessage
        } else {
            messageQueue.offer(snackbarMessage)
        }
    }
    
    /**
     * Dismisses the current message and shows the next queued message if any.
     * This should be called when a snackbar is dismissed.
     */
    suspend fun dismissCurrent() {
        mutex.withLock {
            val nextMessage = messageQueue.poll()
            _messages.value = nextMessage
        }
    }
    
    /**
     * Clears all queued messages and the current message.
     */
    suspend fun clearAll() {
        mutex.withLock {
            messageQueue.clear()
            _messages.value = null
        }
    }
    
    /**
     * Non-suspending version of clearAll for calling from anywhere without coroutine scope.
     */
    fun clearAllMessages() {
        // Use a simple approach for non-suspending calls
        messageQueue.clear()
        _messages.value = null
    }
}