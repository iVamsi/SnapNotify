package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import com.vamsi.snapnotify.core.SnackbarManager

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
    
    private val snackbarManager: SnackbarManager by lazy {
        SnackbarManager.getInstance()
    }
    private val successStyle = SnackbarStyle(
        containerColor = Color(0xFF2E7D32),
        contentColor = Color.White,
        actionColor = Color(0xFF81C784)
    )
    private val errorStyle = SnackbarStyle(
        containerColor = Color(0xFFD32F2F),
        contentColor = Color.White,
        actionColor = Color(0xFFEF5350)
    )
    private val warningStyle = SnackbarStyle(
        containerColor = Color(0xFFE65100),
        contentColor = Color.White,
        actionColor = Color(0xFFFFB74D)
    )
    private val infoStyle = SnackbarStyle(
        containerColor = Color(0xFF1976D2),
        contentColor = Color.White,
        actionColor = Color(0xFF42A5F5)
    )
    
    /**
     * Internal method to ensure SnapNotify is ready to use.
     * This is automatically called and doesn't require manual initialization.
     */
    internal fun initialize() {
        // The lazy initialization of snackbarManager handles setup automatically
        // This method is kept for API compatibility but is now essentially a no-op
    }

    /**
     * Configures SnapNotify's internal queue behavior.
     *
     * @param config The configuration to apply.
     */
    fun configure(config: SnapNotifyConfig) {
        snackbarManager.updateConfig(config)
    }
    
    /**
     * Shows a simple snackbar message.
     * 
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis)
        } else {
            snackbarManager.showMessage(message, duration)
        }
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
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun show(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, actionLabel, onAction)
        } else {
            snackbarManager.showMessage(message, duration, actionLabel, onAction)
        }
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
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showStyled(
        message: String,
        style: SnackbarStyle,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, null, null, style)
        } else {
            snackbarManager.showMessage(message, duration, null, null, style)
        }
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
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showStyled(
        message: String,
        style: SnackbarStyle,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, actionLabel, onAction, style)
        } else {
            snackbarManager.showMessage(message, duration, actionLabel, onAction, style)
        }
    }
    
    /**
     * Shows a success-themed snackbar message.
     * 
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showSuccess(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, null, null, successStyle)
        } else {
            snackbarManager.showMessage(message, duration, null, null, successStyle)
        }
    }
    
    /**
     * Shows a success-themed snackbar message with an action button.
     * 
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showSuccess(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, actionLabel, onAction, successStyle)
        } else {
            snackbarManager.showMessage(message, duration, actionLabel, onAction, successStyle)
        }
    }
    
    /**
     * Shows an error-themed snackbar message.
     * 
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showError(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, null, null, errorStyle)
        } else {
            snackbarManager.showMessage(message, duration, null, null, errorStyle)
        }
    }
    
    /**
     * Shows an error-themed snackbar message with an action button.
     * 
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showError(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, actionLabel, onAction, errorStyle)
        } else {
            snackbarManager.showMessage(message, duration, actionLabel, onAction, errorStyle)
        }
    }
    
    /**
     * Shows a warning-themed snackbar message.
     * 
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showWarning(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, null, null, warningStyle)
        } else {
            snackbarManager.showMessage(message, duration, null, null, warningStyle)
        }
    }
    
    /**
     * Shows a warning-themed snackbar message with an action button.
     * 
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showWarning(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, actionLabel, onAction, warningStyle)
        } else {
            snackbarManager.showMessage(message, duration, actionLabel, onAction, warningStyle)
        }
    }
    
    /**
     * Shows an info-themed snackbar message.
     * 
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showInfo(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, null, null, infoStyle)
        } else {
            snackbarManager.showMessage(message, duration, null, null, infoStyle)
        }
    }
    
    /**
     * Shows an info-themed snackbar message with an action button.
     * 
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showInfo(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null
    ) {
        if (durationMillis != null) {
            snackbarManager.showMessageWithCustomDuration(message, durationMillis, actionLabel, onAction, infoStyle)
        } else {
            snackbarManager.showMessage(message, duration, actionLabel, onAction, infoStyle)
        }
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
