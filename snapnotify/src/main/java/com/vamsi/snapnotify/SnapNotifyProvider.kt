package com.vamsi.snapnotify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TextButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch

/**
 * Composable that wraps content and handles snackbar display logic.
 * 
 * This composable provides snackbar functionality for its content. Multiple providers
 * in the same composition hierarchy are automatically handled - nested providers will
 * be ignored to prevent duplicate snackbars, and only the outermost provider will
 * handle message display.
 * 
 * Usage patterns:
 * - **App-wide**: Place at the root level for global snackbar access
 * - **Screen-level**: Use per screen for screen-specific snackbar placement
 * - **Feature-level**: Wrap feature sections for contextual notifications
 * 
 * Note: SnapNotify uses a singleton message queue, so all providers share the same
 * message source. The provider's location determines where snackbars appear visually.
 *
 * @param modifier Modifier to be applied to the container
 * @param style Optional styling configuration for snackbars. If null, uses Material3 defaults
 * @param content The content to be wrapped with snackbar functionality
 */
@Composable
fun SnapNotifyProvider(
    modifier: Modifier = Modifier,
    style: SnackbarStyle? = null,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val alreadyInProvider = LocalSnapNotifyProvider.current
    
    // If we're already inside a provider, just render content without creating another provider
    if (alreadyInProvider) {
        content()
        return
    }
    
    val viewModel: SnapNotifyViewModel = hiltViewModel()
    val currentMessage = viewModel.currentMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snackbarStyle = style ?: SnackbarStyle.default()
    
    // Register this provider and track its lifecycle
    DisposableEffect(Unit) {
        val isActive = ProviderRegistry.registerProvider()
        
        onDispose {
            ProviderRegistry.unregisterProvider()
        }
    }
    
    // Initialize SnapNotify with the current context
    LaunchedEffect(Unit) {
        SnapNotify.initialize(context)
    }
    
    // Handle message display
    LaunchedEffect(currentMessage.value) {
        currentMessage.value?.let { message ->
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = message.text,
                    actionLabel = message.actionLabel,
                    duration = message.duration
                )
                
                // Handle action result
                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        message.onAction?.invoke()
                    }
                    SnackbarResult.Dismissed -> {
                        // Message was dismissed (timeout or swipe)
                    }
                }
                
                // Dismiss current message and show next one if queued
                viewModel.dismissCurrent()
            }
        }
    }
    
    CompositionLocalProvider(LocalSnapNotifyProvider provides true) {
        Scaffold(
            modifier = modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState
                ) { snackbarData ->
                    val messageStyle = currentMessage.value?.style ?: snackbarStyle
                    StyledSnackbar(
                        snackbarData = snackbarData,
                        style = messageStyle
                    )
                }
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                content()
            }
        }
    }
}