package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.vamsi.snapnotify.core.SnackbarManager
import com.vamsi.snapnotify.core.SnackbarMessage

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
 * SnapNotify.showSuccess("Saved!")
 * SnapNotify.showUrgent("Connection lost! Immediate action needed.")
 * SnapNotify.showStyled("Custom!", SnackbarStyle.success())
 * SnapNotify.show(
 *     title = "File uploaded",
 *     message = "Report_Q3.pdf saved to Cloud Drive",
 *     leadingIcon = leadingIcon,
 *     actionLabel = "View",
 *     onAction = { openFile() },
 *     showCloseButton = true,
 * )
 * SnapNotify.showUndoable(message = "Item moved to trash", onAction = { restoreItem() })
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
     * Single funnel every public overload routes through, so queue behaviour is defined once.
     */
    private fun dispatch(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        style: SnackbarStyle? = null,
        priority: SnackbarPriority = SnackbarPriority.Normal,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
        isAssertive: Boolean = false,
        title: String? = null,
        leadingIcon: ImageVector? = null,
        leadingIconContentDescription: String? = null,
        showCloseButton: Boolean = false,
        showProgressBar: Boolean = false,
        onTimeout: (() -> Unit)? = null,
    ) {
        val customDuration = durationMillis?.let { SnackbarDurationWrapper.fromMillis(it) }
        val snackbarMessage = SnackbarMessage(
            text = message,
            duration = if (customDuration != null) SnackbarDuration.Short else duration,
            actionLabel = actionLabel,
            onAction = onAction,
            style = style,
            customDuration = customDuration,
            priority = priority,
            hapticFeedback = hapticFeedback,
            isAssertive = isAssertive,
            title = title?.takeIf { it.isNotBlank() },
            leadingIcon = leadingIcon,
            leadingIconContentDescription = leadingIconContentDescription?.takeIf { it.isNotBlank() },
            showCloseButton = showCloseButton,
            showProgressBar = showProgressBar,
            onTimeout = onTimeout,
        )
        snackbarManager.showMessage(snackbarMessage)
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
     * @param title Optional headline shown above [message]
     * @param leadingIcon Optional icon drawn before the text. Supply an [ImageVector] from your app.
     * @param leadingIconContentDescription Accessibility label for [leadingIcon]. Omit it for a decorative icon.
     * @param showCloseButton Draws a trailing dismiss control when true
     * @param showProgressBar Draws a countdown bar. Pressing the snackbar pauses that countdown.
     * @param onTimeout Called when the library timer dismisses the snackbar
     */
    fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        title: String? = null,
        leadingIcon: ImageVector? = null,
        leadingIconContentDescription: String? = null,
        showCloseButton: Boolean = false,
        showProgressBar: Boolean = false,
        onTimeout: (() -> Unit)? = null,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            title = title,
            leadingIcon = leadingIcon,
            leadingIconContentDescription = leadingIconContentDescription,
            showCloseButton = showCloseButton,
            showProgressBar = showProgressBar,
            onTimeout = onTimeout,
        )
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
     * @param title Optional headline shown above [message]
     * @param leadingIcon Optional icon drawn before the text
     * @param leadingIconContentDescription Accessibility label for [leadingIcon]
     * @param showCloseButton Draws a trailing dismiss control when true
     * @param hapticFeedback Haptic feedback to fire when the message displays
     * @param showProgressBar Draws a countdown bar. Pressing the snackbar pauses that countdown.
     * @param onTimeout Called when the library timer dismisses the snackbar
     */
    fun show(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        title: String? = null,
        leadingIcon: ImageVector? = null,
        leadingIconContentDescription: String? = null,
        showCloseButton: Boolean = false,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
        showProgressBar: Boolean = false,
        onTimeout: (() -> Unit)? = null,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            title = title,
            leadingIcon = leadingIcon,
            leadingIconContentDescription = leadingIconContentDescription,
            showCloseButton = showCloseButton,
            hapticFeedback = hapticFeedback,
            showProgressBar = showProgressBar,
            onTimeout = onTimeout,
        )
    }

    // Keep the v1.1.0 JVM signatures so callers compiled against it still link.
    @Deprecated("Binary compatibility only", level = DeprecationLevel.HIDDEN)
    fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) = show(message = message, duration = duration, durationMillis = durationMillis, title = null)

    @Deprecated("Binary compatibility only", level = DeprecationLevel.HIDDEN)
    fun show(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) = show(
        message = message,
        actionLabel = actionLabel,
        onAction = onAction,
        duration = duration,
        durationMillis = durationMillis,
        title = null,
    )
    
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
        durationMillis: Long? = null,
    ) {
        dispatch(message = message, style = style, duration = duration, durationMillis = durationMillis)
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
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            style = style,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
        )
    }
    
    /**
     * Shows a success-themed snackbar message.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showSuccess(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            style = successStyle,
            hapticFeedback = SnackbarHapticFeedback.Success,
        )
    }
    
    /**
     * Shows a success-themed snackbar message with an action button.
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
    fun showSuccess(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            style = successStyle,
            hapticFeedback = SnackbarHapticFeedback.Success,
        )
    }
    
    /**
     * Shows a error-themed snackbar message.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showError(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            style = errorStyle,
            hapticFeedback = SnackbarHapticFeedback.Error,
            isAssertive = true,
        )
    }
    
    /**
     * Shows a error-themed snackbar message with an action button.
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
    fun showError(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            style = errorStyle,
            hapticFeedback = SnackbarHapticFeedback.Error,
            isAssertive = true,
        )
    }
    
    /**
     * Shows a warning-themed snackbar message.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showWarning(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            style = warningStyle,
            hapticFeedback = SnackbarHapticFeedback.Warning,
        )
    }
    
    /**
     * Shows a warning-themed snackbar message with an action button.
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
    fun showWarning(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            style = warningStyle,
            hapticFeedback = SnackbarHapticFeedback.Warning,
        )
    }
    
    /**
     * Shows a info-themed snackbar message.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showInfo(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            style = infoStyle,
            hapticFeedback = SnackbarHapticFeedback.None,
        )
    }
    
    /**
     * Shows a info-themed snackbar message with an action button.
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
    fun showInfo(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            style = infoStyle,
            hapticFeedback = SnackbarHapticFeedback.None,
        )
    }

    /**
     * Shows a snackbar message at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun show(
        message: String,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows a snackbar message with an action button at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun show(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows a custom-styled snackbar message at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param style The custom style to apply to the snackbar
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showStyled(
        message: String,
        style: SnackbarStyle,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
    ) {
        dispatch(
            message = message,
            style = style,
            duration = duration,
            durationMillis = durationMillis,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows a custom-styled snackbar message with an action button at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param style The custom style to apply to the snackbar
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showStyled(
        message: String,
        style: SnackbarStyle,
        actionLabel: String,
        onAction: () -> Unit,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
    ) {
        dispatch(
            message = message,
            style = style,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows a success-themed snackbar message at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showSuccess(
        message: String,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.Success,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            style = successStyle,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows a success-themed snackbar message with an action button at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showSuccess(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.Success,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            style = successStyle,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows a error-themed snackbar message at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showError(
        message: String,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.Error,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            style = errorStyle,
            priority = priority,
            hapticFeedback = hapticFeedback,
            isAssertive = true,
        )
    }

    /**
     * Shows a error-themed snackbar message with an action button at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showError(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.Error,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            style = errorStyle,
            priority = priority,
            hapticFeedback = hapticFeedback,
            isAssertive = true,
        )
    }

    /**
     * Shows a warning-themed snackbar message at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showWarning(
        message: String,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.Warning,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            style = warningStyle,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows a warning-themed snackbar message with an action button at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showWarning(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.Warning,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            style = warningStyle,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows a info-themed snackbar message at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showInfo(
        message: String,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            style = infoStyle,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows a info-themed snackbar message with an action button at a given queue priority.
     *
     * This method is non-suspending and can be called from anywhere, including
     * ViewModels, repositories, and background threads.
     *
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param priority Queue priority. [SnackbarPriority.Urgent] preempts a displaying message.
     * @param hapticFeedback Haptic feedback to fire when the message displays.
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     */
    fun showInfo(
        message: String,
        actionLabel: String,
        onAction: () -> Unit,
        priority: SnackbarPriority,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.None,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
            style = infoStyle,
            priority = priority,
            hapticFeedback = hapticFeedback,
        )
    }

    /**
     * Shows an urgent snackbar that preempts a displaying lower-priority message immediately.
     *
     * The preempted message goes back on the queue and resumes afterwards, unless the queue is
     * already full and it ranks below everything in it.
     *
     * @param message The text to display
     * @param actionLabel The label for the action button
     * @param onAction The action to execute when the action button is pressed
     * @param duration How long the snackbar should be displayed
     * @param durationMillis Custom duration in milliseconds. If provided, overrides duration parameter.
     * @param style The style to apply. Defaults to the built-in error style.
     */
    fun showUrgent(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
        style: SnackbarStyle? = null,
    ) {
        dispatch(
            message = message,
            duration = duration,
            durationMillis = durationMillis,
            actionLabel = actionLabel,
            onAction = onAction,
            style = style ?: errorStyle,
            priority = SnackbarPriority.Urgent,
            hapticFeedback = SnackbarHapticFeedback.Error,
            isAssertive = true,
        )
    }

    /**
     * Shows a snackbar with an action and a countdown bar.
     *
     * The bar runs from full to empty over [durationMillis]. Pressing the snackbar pauses it.
     * [onAction] runs when the action is pressed. [onTimeout] runs only when the countdown
     * finishes. Dismissing the snackbar does not call [onTimeout].
     *
     * @param message The text to display
     * @param onAction The action to execute when the action button is pressed
     * @param actionLabel The label for the action button. Defaults to "Undo"
     * @param durationMillis How long the countdown lasts. Defaults to 5 seconds
     * @param showProgressBar Draws the countdown bar when true
     * @param onTimeout Called when the countdown finishes
     */
    fun showUndoable(
        message: String,
        onAction: () -> Unit,
        actionLabel: String = "Undo",
        durationMillis: Long = 5_000L,
        showProgressBar: Boolean = true,
        onTimeout: (() -> Unit)? = null,
        title: String? = null,
        leadingIcon: ImageVector? = null,
        leadingIconContentDescription: String? = null,
        showCloseButton: Boolean = false,
        style: SnackbarStyle? = null,
        priority: SnackbarPriority = SnackbarPriority.Normal,
        hapticFeedback: SnackbarHapticFeedback = SnackbarHapticFeedback.Gesture,
    ) {
        dispatch(
            message = message,
            durationMillis = durationMillis,
            actionLabel = actionLabel,
            onAction = onAction,
            style = style,
            priority = priority,
            hapticFeedback = hapticFeedback,
            title = title,
            leadingIcon = leadingIcon,
            leadingIconContentDescription = leadingIconContentDescription,
            showCloseButton = showCloseButton,
            showProgressBar = showProgressBar,
            onTimeout = onTimeout,
        )
    }

    /**
     * Clears all queued snackbar messages and dismisses any currently displayed message.
     */
    fun clearAll() {
        snackbarManager.clearAllMessages()
    }
}
