package com.vamsi.snapnotify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

/**
 * A customizable snackbar composable that applies the provided [SnackbarStyle] and
 * accessibility semantics ([LiveRegionMode]).
 *
 * @param snackbarData The snackbar data containing message and action information
 * @param style The styling configuration to apply
 * @param modifier Modifier for the snackbar
 */
@Composable
internal fun StyledSnackbar(
    snackbarData: SnackbarData,
    style: SnackbarStyle,
    modifier: Modifier = Modifier,
) {
    val snapVisuals = snackbarData.visuals as? SnapNotifyVisuals
    val resolvedStyle = snapVisuals?.style ?: style

    val isAssertive = snapVisuals?.isAssertive == true

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

    val shape = resolvedStyle.shape ?: MaterialTheme.shapes.small
    val elevation = resolvedStyle.elevation ?: 6.dp

    Surface(
        modifier = modifier
            .padding(horizontal = 16.dp)
            // Announce on the container so the message and its action label are read as one unit.
            .semantics(mergeDescendants = true) {
                liveRegion = if (isAssertive) LiveRegionMode.Assertive else LiveRegionMode.Polite
            },
        shape = shape,
        color = containerColor,
        shadowElevation = elevation
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = snackbarData.visuals.message,
                style = messageTextStyle,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )

            snackbarData.visuals.actionLabel?.let { actionLabel ->
                TextButton(
                    onClick = { snackbarData.performAction() },
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = actionLabel,
                        style = actionTextStyle,
                        color = actionColor
                    )
                }
            }
        }
    }
}
