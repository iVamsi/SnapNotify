package com.vamsi.snapnotify

import androidx.compose.foundation.layout.WindowInsets

/**
 * Where [SnapNotifyProvider] anchors its default snackbar host.
 *
 * [BottomSnackbar] keeps the provider's [SnapNotifyProvider] `hostAlignment` and `hostInsets`.
 * [TopPill] and [TopBanner] pin the snackbar to the top. A null [TopPill.topInsetPadding] or
 * [TopBanner.topInsetPadding] uses the status-bar inset.
 */
sealed interface NotificationPlacement {
    data object BottomSnackbar : NotificationPlacement

    data class TopPill(
        val topInsetPadding: WindowInsets? = null,
    ) : NotificationPlacement

    data class TopBanner(
        val topInsetPadding: WindowInsets? = null,
    ) : NotificationPlacement
}
