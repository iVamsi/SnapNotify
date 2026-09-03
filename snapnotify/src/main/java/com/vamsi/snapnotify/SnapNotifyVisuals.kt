package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

/**
 * Internal [SnackbarVisuals] implementation providing styling and accessibility semantics
 * to [StyledSnackbar].
 */
internal class SnapNotifyVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val style: SnackbarStyle? = null,
    val isAssertive: Boolean = false,
) : SnackbarVisuals {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SnapNotifyVisuals) return false
        if (message != other.message) return false
        if (actionLabel != other.actionLabel) return false
        if (withDismissAction != other.withDismissAction) return false
        if (duration != other.duration) return false
        if (style != other.style) return false
        if (isAssertive != other.isAssertive) return false
        return true
    }

    override fun hashCode(): Int {
        var result = message.hashCode()
        result = 31 * result + (actionLabel?.hashCode() ?: 0)
        result = 31 * result + withDismissAction.hashCode()
        result = 31 * result + duration.hashCode()
        result = 31 * result + (style?.hashCode() ?: 0)
        result = 31 * result + isAssertive.hashCode()
        return result
    }
}
