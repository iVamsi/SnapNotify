package com.vamsi.snapnotify.samples.iosnative

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.vamsi.snapnotify.NotificationPlacement
import com.vamsi.snapnotify.SnapNotify
import com.vamsi.snapnotify.SnapNotifyProvider
import com.vamsi.snapnotify.SnackbarHapticFeedback
import platform.UIKit.UIViewController

/**
 * Bridge exposed to Swift / SwiftUI (`SnapNotifyIosBridge.framework`) so a native iOS app
 * can trigger SnapNotify notifications from Swift code and embed the host view controller.
 */
object SnapNotifyIosBridge {
    private var currentPlacement by mutableStateOf<NotificationPlacement>(NotificationPlacement.BottomSnackbar)

    fun createHostViewController(): UIViewController = ComposeUIViewController {
        MaterialTheme {
            SnapNotifyProvider(placement = currentPlacement) {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
    }

    fun showSimple(message: String) {
        SnapNotify.show(message)
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

    fun showUrgent(message: String) {
        SnapNotify.showUrgent(
            message = message,
            actionLabel = "OK",
            onAction = {}
        )
    }

    fun showRich(title: String, message: String) {
        SnapNotify.show(
            message = message,
            title = title,
            actionLabel = "Open",
            onAction = { SnapNotify.showInfo("Opened from SwiftUI!") },
            showCloseButton = true,
            hapticFeedback = SnackbarHapticFeedback.Success
        )
    }

    fun showUndoable(message: String) {
        SnapNotify.showUndoable(
            message = message,
            durationMillis = 5000L,
            onAction = { SnapNotify.showSuccess("Restored via SwiftUI!") },
            onTimeout = { SnapNotify.showInfo("Permanently deleted") }
        )
    }

    fun useBottomPlacement() {
        currentPlacement = NotificationPlacement.BottomSnackbar
        SnapNotify.showInfo("Placement: Bottom Snackbar")
    }

    fun useTopPillPlacement() {
        currentPlacement = NotificationPlacement.TopPill()
        SnapNotify.showInfo("Placement: Top Pill")
    }

    fun useTopBannerPlacement() {
        currentPlacement = NotificationPlacement.TopBanner()
        SnapNotify.showInfo("Placement: Top Banner")
    }

    fun clearAll() {
        SnapNotify.clearAll()
    }
}
