package com.vamsi.snapnotify

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Defines the visual styling for SnapNotify snackbars.
 * 
 * @param containerColor Background color of the snackbar
 * @param contentColor Color of the message text
 * @param actionColor Color of the action button text
 * @param shape Shape of the snackbar container
 * @param elevation Elevation/shadow of the snackbar
 * @param messageTextStyle Text style for the message content
 * @param actionTextStyle Text style for the action button
 */
@Immutable
data class SnackbarStyle(
    val containerColor: Color = Color.Unspecified,
    val contentColor: Color = Color.Unspecified,
    val actionColor: Color = Color.Unspecified,
    val shape: Shape? = null,
    val elevation: Dp? = null,
    val messageTextStyle: TextStyle? = null,
    val actionTextStyle: TextStyle? = null
) {
    companion object {
        /**
         * Creates a default SnackbarStyle using Material3 theme colors.
         */
        @Composable
        fun default(): SnackbarStyle {
            return SnackbarStyle(
                containerColor = MaterialTheme.colorScheme.inverseSurface,
                contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                actionColor = MaterialTheme.colorScheme.inversePrimary,
                shape = RoundedCornerShape(4.dp),
                elevation = 6.dp,
                messageTextStyle = MaterialTheme.typography.bodyMedium,
                actionTextStyle = MaterialTheme.typography.labelLarge
            )
        }
        
        /**
         * Creates a success-themed SnackbarStyle with green colors.
         */
        @Composable
        fun success(): SnackbarStyle {
            return SnackbarStyle(
                containerColor = Color(0xFF2E7D32),
                contentColor = Color.White,
                actionColor = Color(0xFF81C784),
                shape = RoundedCornerShape(12.dp),
                elevation = 12.dp,
                messageTextStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White
                ),
                actionTextStyle = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xFF81C784)
                )
            )
        }
        
        /**
         * Creates an error-themed SnackbarStyle with red colors.
         */
        @Composable
        fun error(): SnackbarStyle {
            return SnackbarStyle(
                containerColor = Color(0xFFD32F2F),
                contentColor = Color.White,
                actionColor = Color(0xFFEF5350),
                shape = RoundedCornerShape(12.dp),
                elevation = 12.dp,
                messageTextStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White
                ),
                actionTextStyle = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xFFEF5350)
                )
            )
        }
        
        /**
         * Creates a warning-themed SnackbarStyle with orange colors.
         */
        @Composable
        fun warning(): SnackbarStyle {
            return SnackbarStyle(
                containerColor = Color(0xFFE65100),
                contentColor = Color.White,
                actionColor = Color(0xFFFFB74D),
                shape = RoundedCornerShape(12.dp),
                elevation = 12.dp,
                messageTextStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White
                ),
                actionTextStyle = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xFFFFB74D)
                )
            )
        }
        
        /**
         * Creates an info-themed SnackbarStyle with blue colors.
         */
        @Composable
        fun info(): SnackbarStyle {
            return SnackbarStyle(
                containerColor = Color(0xFF1976D2),
                contentColor = Color.White,
                actionColor = Color(0xFF42A5F5),
                shape = RoundedCornerShape(12.dp),
                elevation = 12.dp,
                messageTextStyle = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.White
                ),
                actionTextStyle = MaterialTheme.typography.labelLarge.copy(
                    color = Color(0xFF42A5F5)
                )
            )
        }
    }
}