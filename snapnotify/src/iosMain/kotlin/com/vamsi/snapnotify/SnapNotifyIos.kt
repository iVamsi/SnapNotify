package com.vamsi.snapnotify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import platform.UIKit.UIViewController

/**
 * Swift-friendly bridge exposed from `SnapNotify.framework` (`import SnapNotify`)
 * so native UIKit / SwiftUI apps in Xcode can host SnapNotify overlays and trigger
 * notifications directly from Swift without Compose imports.
 */
object SnapNotifyIos {
    private var currentPlacement by mutableStateOf<NotificationPlacement>(
        NotificationPlacement.BottomSnackbar
    )

    private val checkIcon: ImageVector by lazy {
        ImageVector.Builder(
            name = "Filled.Done",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 24f,
            viewportHeight = 24f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(9.0f, 16.2f)
                lineTo(4.8f, 12.0f)
                lineTo(3.4f, 13.4f)
                lineTo(9.0f, 19.0f)
                lineTo(21.0f, 7.0f)
                lineTo(19.6f, 5.6f)
                close()
            }
        }.build()
    }

    /** Creates the transparent Compose host `UIViewController` for global `SnapNotify` messages. */
    @OptIn(ExperimentalComposeUiApi::class)
    fun createHostViewController(): UIViewController = ComposeUIViewController(
        configure = { opaque = false }
    ) {
        MaterialTheme {
            SnapNotifyProvider(placement = currentPlacement) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    /** Creates an isolated transparent Compose host `UIViewController` bound to [channelId]. */
    @OptIn(ExperimentalComposeUiApi::class)
    fun createScopedHostViewController(channelId: String): UIViewController = ComposeUIViewController(
        configure = { opaque = false }
    ) {
        MaterialTheme {
            val channel = remember(channelId) { SnapNotify.channel(channelId) }
            SnapNotifyProvider(channel = channel) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    /** Configures SnapNotify's global queue behavior. */
    fun configure(
        maxQueueSize: Int = SnapNotifyConfig.DEFAULT_MAX_QUEUE_SIZE,
        deduplicationStrategy: DeduplicationStrategy = DeduplicationStrategy.DropDuplicate,
        isHapticFeedbackEnabled: Boolean = true,
        isAccessibilityScalingEnabled: Boolean = true,
        onMessageDropped: ((String) -> Unit)? = null,
    ) {
        SnapNotify.configure(
            SnapNotifyConfig(
                maxQueueSize = maxQueueSize,
                onMessageDropped = onMessageDropped,
                deduplicationStrategy = deduplicationStrategy,
                isHapticFeedbackEnabled = isHapticFeedbackEnabled,
                isAccessibilityScalingEnabled = isAccessibilityScalingEnabled,
            )
        )
    }

    fun show(message: String) {
        SnapNotify.show(message)
    }

    fun showLong(message: String) {
        SnapNotify.show(message = message, duration = SnackbarDuration.Long)
    }

    fun showWithAction(message: String, actionLabel: String, onAction: () -> Unit) {
        SnapNotify.show(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
        )
    }

    fun showSuccess(message: String) {
        SnapNotify.showSuccess(message)
    }

    fun showError(message: String) {
        SnapNotify.showError(message)
    }

    fun showWarning(message: String) {
        SnapNotify.showWarning(message)
    }

    fun showInfo(message: String) {
        SnapNotify.showInfo(message)
    }

    fun showStyled(
        message: String,
        containerColorArgb: Long,
        contentColorArgb: Long,
        actionColorArgb: Long,
        cornerRadiusDp: Float,
        elevationDp: Float,
        actionLabel: String?,
        onAction: (() -> Unit)?,
    ) {
        val safeCornerRadius = if (cornerRadiusDp.isFinite()) cornerRadiusDp.coerceAtLeast(0f) else 0f
        val safeElevation = if (elevationDp.isFinite()) elevationDp.coerceAtLeast(0f) else 0f
        val style = SnackbarStyle(
            containerColor = Color(containerColorArgb),
            contentColor = Color(contentColorArgb),
            actionColor = Color(actionColorArgb),
            shape = RoundedCornerShape(safeCornerRadius.dp),
            elevation = safeElevation.dp,
        )
        if (actionLabel != null && onAction != null) {
            SnapNotify.showStyled(
                message = message,
                style = style,
                actionLabel = actionLabel,
                onAction = onAction,
            )
        } else {
            SnapNotify.showStyled(message = message, style = style)
        }
    }

    fun showUrgent(message: String, actionLabel: String, onAction: () -> Unit) {
        SnapNotify.showUrgent(
            message = message,
            actionLabel = actionLabel,
            onAction = onAction,
        )
    }

    fun showRich(
        title: String,
        message: String,
        actionLabel: String?,
        showLeadingCheckIcon: Boolean,
        showCloseButton: Boolean,
        onAction: (() -> Unit)?,
    ) {
        val icon = if (showLeadingCheckIcon) checkIcon else null
        val iconDescription = if (showLeadingCheckIcon) title.takeIf { it.isNotBlank() } else null
        if (actionLabel != null && onAction != null) {
            SnapNotify.show(
                title = title,
                message = message,
                leadingIcon = icon,
                leadingIconContentDescription = iconDescription,
                actionLabel = actionLabel,
                onAction = onAction,
                showCloseButton = showCloseButton,
                hapticFeedback = SnackbarHapticFeedback.Success,
            )
        } else {
            SnapNotify.show(
                title = title,
                message = message,
                leadingIcon = icon,
                leadingIconContentDescription = iconDescription,
                showCloseButton = showCloseButton,
            )
        }
    }

    fun showUndoable(
        message: String,
        durationMillis: Long,
        onAction: () -> Unit,
        onTimeout: () -> Unit,
    ) {
        SnapNotify.showUndoable(
            message = message,
            durationMillis = durationMillis,
            onAction = onAction,
            onTimeout = onTimeout,
        )
    }

    fun clearAll() {
        SnapNotify.clearAll()
    }

    fun setPlacementBottom() {
        currentPlacement = NotificationPlacement.BottomSnackbar
    }

    fun setPlacementTopPill() {
        currentPlacement = NotificationPlacement.TopPill()
    }

    fun setPlacementTopBanner() {
        currentPlacement = NotificationPlacement.TopBanner()
    }

    fun isTopPlacement(): Boolean = currentPlacement !is NotificationPlacement.BottomSnackbar

    fun showScopedSuccess(channelId: String, message: String) {
        SnapNotify.channel(channelId).showSuccess(message)
    }

    fun showScopedError(channelId: String, message: String) {
        SnapNotify.channel(channelId).showError(message)
    }

    fun showScopedWarning(channelId: String, message: String) {
        SnapNotify.channel(channelId).showWarning(message)
    }

    fun showScopedInfo(channelId: String, message: String) {
        SnapNotify.channel(channelId).showInfo(message)
    }

    fun showScopedUndoable(
        channelId: String,
        message: String,
        durationMillis: Long,
        onAction: () -> Unit,
        onTimeout: () -> Unit,
    ) {
        SnapNotify.channel(channelId).showUndoable(
            message = message,
            durationMillis = durationMillis,
            onAction = onAction,
            onTimeout = onTimeout,
        )
    }

    fun clearScopedChannel(channelId: String) {
        SnapNotify.channel(channelId).clear()
    }
}
