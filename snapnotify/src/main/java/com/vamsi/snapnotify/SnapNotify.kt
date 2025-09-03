package com.vamsi.snapnotify

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import com.vamsi.snapnotify.di.SnapNotifyEntryPoint
import dagger.hilt.android.EntryPointAccessors

/**
 * Public API object for SnapNotify library.
 * 
 * This object provides static methods for showing snackbars from anywhere in the application
 * without needing to pass CoroutineScopes or manage SnackbarHostState manually.
 * 
 * Usage:
 * ```
 * SnapNotify.show("Operation completed successfully!")
 * SnapNotify.show("Error occurred", "Retry") { retryOperation() }
 * SnapNotify.showStyled("Success!", SnackbarStyle.success())
 * ```
 */
object SnapNotify {
    
    private var _snackbarManager: SnackbarManager? = null
    private val snackbarManager: SnackbarManager
        get() = _snackbarManager ?: throw IllegalStateException(
            "SnapNotify not initialized. Make sure to use SnapNotifyProvider at the root of your app."
        )
    
    /**
     * Internal method to initialize SnapNotify with application context.
     * This should only be called by SnapNotifyProvider.
     */
    internal fun initialize(context: Context) {
        if (_snackbarManager == null) {
            val entryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                SnapNotifyEntryPoint::class.java
            )
            _snackbarManager = entryPoint.snackbarManager()
        }
    }
    
    /**
     * Shows a simple snackbar message.
     * 
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     */
    fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration)
    }
    
    /**
     * Shows a snackbar message with an action button.
     * 
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     */
    fun show(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, actionLabel, onAction)
    }
    
    /**
     * Shows a snackbar message with custom styling.
     * 
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param style The custom style to apply to the snackbar
     * @param duration How long the snackbar should be displayed
     */
    fun showStyled(
        message: String,
        style: SnackbarStyle,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, null, null, style)
    }
    
    /**
     * Shows a snackbar message with custom styling and an action button.
     * 
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param style The custom style to apply to the snackbar
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     */
    fun showStyled(
        message: String,
        style: SnackbarStyle,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, actionLabel, onAction, style)
    }
    
    /**
     * Shows a success-themed snackbar message.
     * 
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     */
    fun showSuccess(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        // Note: We can't use SnackbarStyle.success() here because it's a @Composable function
        // The style will be applied when the message is displayed in the UI
        snackbarManager.showMessage(message, duration, null, null, SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
            contentColor = androidx.compose.ui.graphics.Color.White,
            actionColor = androidx.compose.ui.graphics.Color(0xFF81C784)
        ))
    }
    
    /**
     * Shows a success-themed snackbar message with an action button.
     * 
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     */
    fun showSuccess(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, actionLabel, onAction, SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color(0xFF2E7D32),
            contentColor = androidx.compose.ui.graphics.Color.White,
            actionColor = androidx.compose.ui.graphics.Color(0xFF81C784)
        ))
    }
    
    /**
     * Shows an error-themed snackbar message.
     * 
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     */
    fun showError(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, null, null, SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color(0xFFD32F2F),
            contentColor = androidx.compose.ui.graphics.Color.White,
            actionColor = androidx.compose.ui.graphics.Color(0xFFEF5350)
        ))
    }
    
    /**
     * Shows an error-themed snackbar message with an action button.
     * 
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     */
    fun showError(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, actionLabel, onAction, SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color(0xFFD32F2F),
            contentColor = androidx.compose.ui.graphics.Color.White,
            actionColor = androidx.compose.ui.graphics.Color(0xFFEF5350)
        ))
    }
    
    /**
     * Shows a warning-themed snackbar message.
     * 
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     */
    fun showWarning(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, null, null, SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color(0xFFE65100),
            contentColor = androidx.compose.ui.graphics.Color.White,
            actionColor = androidx.compose.ui.graphics.Color(0xFFFFB74D)
        ))
    }
    
    /**
     * Shows a warning-themed snackbar message with an action button.
     * 
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     */
    fun showWarning(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, actionLabel, onAction, SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color(0xFFE65100),
            contentColor = androidx.compose.ui.graphics.Color.White,
            actionColor = androidx.compose.ui.graphics.Color(0xFFFFB74D)
        ))
    }
    
    /**
     * Shows an info-themed snackbar message.
     * 
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     */
    fun showInfo(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, null, null, SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color(0xFF1976D2),
            contentColor = androidx.compose.ui.graphics.Color.White,
            actionColor = androidx.compose.ui.graphics.Color(0xFF42A5F5)
        ))
    }
    
    /**
     * Shows an info-themed snackbar message with an action button.
     * 
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     */
    fun showInfo(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short
    ) {
        snackbarManager.showMessage(message, duration, actionLabel, onAction, SnackbarStyle(
            containerColor = androidx.compose.ui.graphics.Color(0xFF1976D2),
            contentColor = androidx.compose.ui.graphics.Color.White,
            actionColor = androidx.compose.ui.graphics.Color(0xFF42A5F5)
        ))
    }
    
    /**
     * Clears all queued snackbar messages and dismisses any currently displayed message.
     * 
     * This is useful when you want to clear all pending notifications, for example:
     * - When navigating away from a screen
     * - When user performs an action that makes pending messages irrelevant
     * - When you want to reset the snackbar state
     */
    fun clearAll() {
        snackbarManager.clearAllMessages()
    }
}