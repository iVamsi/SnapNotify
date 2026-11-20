package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Internal [SnackbarVisuals] implementation providing styling, rich content, and accessibility
 * semantics to [StyledSnackbar].
 */
internal data class SnapNotifyVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val style: SnackbarStyle? = null,
    val isAssertive: Boolean = false,
    val title: String? = null,
    val leadingIcon: ImageVector? = null,
    val leadingIconContentDescription: String? = null,
) : SnackbarVisuals
