package com.vamsi.snapnotify

import com.vamsi.snapnotify.core.SnackbarManager
import com.vamsi.snapnotify.core.SnackbarMessage
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull

internal suspend fun SnackbarManager.awaitMessage(): SnackbarMessage {
    return messages.filterNotNull().firstOrNull()
        ?: error("Expected snackbar message but none was emitted")
}

internal suspend fun SnackbarManager.awaitMessage(predicate: (SnackbarMessage) -> Boolean): SnackbarMessage {
    return messages.filterNotNull().firstOrNull { predicate(it) }
        ?: error("Expected snackbar message matching predicate but none was emitted")
}

internal suspend fun SnackbarManager.awaitNullMessage(): SnackbarMessage? {
    return messages.firstOrNull { it == null }
}

