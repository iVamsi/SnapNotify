package com.vamsi.snapnotify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.vamsi.snapnotify.core.SnackbarManager
import com.vamsi.snapnotify.core.SnackbarMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Simple state holder for SnapNotify that doesn't require Hilt dependency injection.
 * 
 * This class provides the same functionality as SnapNotifyViewModel but works without
 * any DI framework, making it suitable for minimal dependency setups.
 */
internal class SimpleSnapNotifyState(
    private val snackbarManager: SnackbarManager,
    private val coroutineScope: CoroutineScope
) {
    
    /**
     * StateFlow of the current message to be displayed.
     */
    val currentMessage: StateFlow<SnackbarMessage?> = snackbarManager.messages
    
    /**
     * Dismisses the current message and triggers the next queued message if any.
     */
    fun dismissCurrent() {
        coroutineScope.launch {
            snackbarManager.dismissCurrent()
        }
    }
    
    /**
     * Clears all messages from the queue and dismisses any current message.
     */
    fun clearAll() {
        coroutineScope.launch {
            snackbarManager.clearAll()
        }
    }
}

/**
 * Composable function to remember a SimpleSnapNotifyState instance.
 * 
 * @param coroutineScope The coroutine scope to use for async operations
 * @return A remembered SimpleSnapNotifyState instance
 */
@Composable
internal fun rememberSimpleSnapNotifyState(
    coroutineScope: CoroutineScope
): SimpleSnapNotifyState {
    return remember {
        SimpleSnapNotifyState(
            snackbarManager = SnackbarManager.getInstance(),
            coroutineScope = coroutineScope
        )
    }
}