package com.vamsi.snapnotify

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vamsi.snapnotify.core.SnackbarManager
import com.vamsi.snapnotify.core.SnackbarMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel that bridges the SnackbarManager with the Compose UI.
 * 
 * This ViewModel observes the SnackbarManager's message flow and provides
 * lifecycle-aware access to snackbar messages for the SnapNotifyProvider composable.
 */
@HiltViewModel
class SnapNotifyViewModel @Inject constructor(
    private val snackbarManager: SnackbarManager
) : ViewModel() {
    
    /**
     * StateFlow of the current message to be displayed.
     */
    val currentMessage: StateFlow<SnackbarMessage?> = snackbarManager.messages
    
    /**
     * Dismisses the current message and triggers the next queued message if any.
     */
    fun dismissCurrent() {
        viewModelScope.launch {
            snackbarManager.dismissCurrent()
        }
    }
    
    /**
     * Clears all messages from the queue and dismisses any current message.
     */
    fun clearAll() {
        viewModelScope.launch {
            snackbarManager.clearAll()
        }
    }
}