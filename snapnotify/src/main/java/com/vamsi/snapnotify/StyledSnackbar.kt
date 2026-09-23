package com.vamsi.snapnotify

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * Snackbar layout for plain text and for rich content: a leading icon, a title, a dismiss
 * control, and a countdown bar.
 */
@Composable
internal fun StyledSnackbar(
    snackbarData: SnackbarData,
    style: SnackbarStyle,
    modifier: Modifier = Modifier,
    placement: NotificationPlacement = NotificationPlacement.BottomSnackbar,
    progress: Float? = null,
    onPressChanged: (Boolean) -> Unit = {},
) {
    val snapVisuals = snackbarData.visuals as? SnapNotifyVisuals
    val resolvedStyle = snapVisuals?.style ?: style
    val isAssertive = snapVisuals?.isAssertive == true
    val title = snapVisuals?.title
    val leadingIcon = snapVisuals?.leadingIcon
    val showCloseButton = snackbarData.visuals.withDismissAction

    val containerColor = if (resolvedStyle.containerColor != Color.Unspecified) {
        resolvedStyle.containerColor
    } else {
        MaterialTheme.colorScheme.inverseSurface
    }

    val contentColor = if (resolvedStyle.contentColor != Color.Unspecified) {
        resolvedStyle.contentColor
    } else {
        MaterialTheme.colorScheme.inverseOnSurface
    }

    val actionColor = if (resolvedStyle.actionColor != Color.Unspecified) {
        resolvedStyle.actionColor
    } else {
        MaterialTheme.colorScheme.inversePrimary
    }

    val messageTextStyle = resolvedStyle.messageTextStyle ?: MaterialTheme.typography.bodyMedium
    val actionTextStyle = resolvedStyle.actionTextStyle ?: MaterialTheme.typography.labelLarge
    val titleTextStyle = MaterialTheme.typography.titleSmall

    val shape = when (placement) {
        is NotificationPlacement.TopPill -> RoundedCornerShape(percent = 50)
        is NotificationPlacement.TopBanner -> RectangleShape
        NotificationPlacement.BottomSnackbar -> resolvedStyle.shape ?: MaterialTheme.shapes.small
    }
    val elevation = resolvedStyle.elevation ?: 6.dp
    val fillsWidth = placement !is NotificationPlacement.TopPill
    val horizontalPadding = if (placement is NotificationPlacement.TopBanner) 0.dp else 16.dp
    val currentOnPressChanged = rememberUpdatedState(onPressChanged)

    Surface(
        modifier = modifier
            .padding(horizontal = horizontalPadding)
            .then(if (fillsWidth) Modifier.fillMaxWidth() else Modifier.widthIn(max = 560.dp))
            .then(
                if (progress != null) {
                    Modifier.testTag("snapnotify_countdown").pointerInput(Unit) {
                        awaitEachGesture {
                            try {
                                awaitFirstDown(
                                    requireUnconsumed = false,
                                    pass = PointerEventPass.Initial,
                                )
                                currentOnPressChanged.value(true)
                                do {
                                    val event = awaitPointerEvent(PointerEventPass.Initial)
                                } while (event.changes.any { it.pressed })
                            } finally {
                                currentOnPressChanged.value(false)
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
            .semantics(mergeDescendants = true) {
                liveRegion = if (isAssertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
            },
        shape = shape,
        color = containerColor,
        shadowElevation = elevation,
    ) {
        Column {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = snapVisuals?.leadingIconContentDescription,
                        tint = contentColor,
                        modifier = Modifier.size(24.dp),
                    )
                }

                Column(
                    modifier = if (fillsWidth) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.weight(1f, fill = false)
                    },
                ) {
                    if (title != null) {
                        Text(
                            text = title,
                            style = titleTextStyle,
                            color = contentColor,
                        )
                    }
                    Text(
                        text = snackbarData.visuals.message,
                        style = messageTextStyle,
                        color = contentColor,
                    )
                }

                snackbarData.visuals.actionLabel?.let { actionLabel ->
                    TextButton(onClick = { snackbarData.performAction() }) {
                        Text(
                            text = actionLabel,
                            style = actionTextStyle,
                            color = actionColor,
                        )
                    }
                }

                if (showCloseButton) {
                    IconButton(onClick = { snackbarData.dismiss() }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Dismiss",
                            tint = contentColor,
                        )
                    }
                }
            }

            if (progress != null) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clearAndSetSemantics { },
                    color = actionColor,
                    trackColor = contentColor.copy(alpha = 0.24f),
                )
            }
        }
    }
}
