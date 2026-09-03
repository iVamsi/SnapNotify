package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.graphics.Color
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
     */
    fun show(
        message: String,
        duration: SnackbarDuration = SnackbarDuration.Short,
        durationMillis: Long? = null,
    ) {
        dispatch(message = message, duration = duration, durationMillis = durationMillis)
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
        durationMillis: Long? = null,
    ) {
        dispatch(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
            duration = duration,
            durationMillis = durationMillis,
        )
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
     * Clears all queued snackbar messages and dismisses any currently displayed message.
     */
    fun clearAll() {
        snackbarManager.clearAllMessages()
    }
}
