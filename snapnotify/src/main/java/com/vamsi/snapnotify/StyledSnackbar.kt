package com.vamsi.snapnotify

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

/**
 * A customizable snackbar composable that applies the provided SnackbarStyle.
 * 
 * @param snackbarData The snackbar data containing message and action information
 * @param style The styling configuration to apply
 * @param modifier Modifier for the snackbar
 */
@Composable
internal fun StyledSnackbar(
    snackbarData: SnackbarData,
    style: SnackbarStyle,
    modifier: Modifier = Modifier
) {
    val containerColor = if (style.containerColor != Color.Unspecified) {
        style.containerColor
    } else {
        MaterialTheme.colorScheme.inverseSurface
    }
    
    val contentColor = if (style.contentColor != Color.Unspecified) {
        style.contentColor
    } else {
        MaterialTheme.colorScheme.inverseOnSurface
    }
    
    val actionColor = if (style.actionColor != Color.Unspecified) {
        style.actionColor
    } else {
        MaterialTheme.colorScheme.inversePrimary
    }
    
    val messageTextStyle = style.messageTextStyle ?: MaterialTheme.typography.bodyMedium
    val actionTextStyle = style.actionTextStyle ?: MaterialTheme.typography.labelLarge
    
    val shape = style.shape ?: MaterialTheme.shapes.small
    val elevation = style.elevation ?: 6.dp
    
    // Create a custom snackbar with full styling control
    Surface(
        modifier = modifier.padding(horizontal = 16.dp),
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