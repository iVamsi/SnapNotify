package com.vamsi.snapnotify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select

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
    content: @Composable () -> Unit,
) {
    val alreadyInProvider = LocalSnapNotifyProvider.current

    // If we're already inside a provider, just render content without creating another provider
    if (alreadyInProvider) {
        content()
        return
    }

    val scope = rememberCoroutineScope()
    val snapNotifyState = rememberSimpleSnapNotifyState(scope)
    val currentMessage = snapNotifyState.currentMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val snackbarStyle = style ?: SnackbarStyle.default()

    // Register this provider and track its lifecycle
    DisposableEffect(Unit) {
        val isActive = ProviderRegistry.registerProvider()

        onDispose {
            ProviderRegistry.unregisterProvider()
        }
    }

    // Initialize SnapNotify 
    LaunchedEffect(Unit) {
        SnapNotify.initialize()
    }

    // Handle message display
    LaunchedEffect(currentMessage.value) {
        currentMessage.value?.let { message ->
            scope.launch {
                val effectiveDuration = message.effectiveDuration
                val result: SnackbarResult

                if (effectiveDuration is SnackbarDurationWrapper.Custom && !effectiveDuration.isIndefinite()) {
                    // Handle custom duration timing manually
                    val snackbarDeferred = async {
                        snackbarHostState.showSnackbar(
                            message = message.text,
                            actionLabel = message.actionLabel,
                            duration = SnackbarDuration.Indefinite
                        )
                    }

                    val timeoutDeferred = async {
                        delay(effectiveDuration.getMilliseconds())
                        SnackbarResult.Dismissed // Timeout
                    }

                    // Wait for either user action or timeout
                    result = select {
                        snackbarDeferred.onAwait { snackbarResult ->
                            timeoutDeferred.cancel() // Cancel timeout since user acted
                            snackbarResult
                        }
                        timeoutDeferred.onAwait { timeoutResult ->
                            snackbarHostState.currentSnackbarData?.dismiss() // Dismiss the snackbar
                            snackbarDeferred.cancel() // Cancel snackbar coroutine
                            timeoutResult
                        }
                    }
                } else {
                    // Use standard duration for Material Design durations or indefinite custom durations  
                    val standardDuration = effectiveDuration.getStandardDuration()
                        ?: if (effectiveDuration.isIndefinite()) SnackbarDuration.Indefinite
                        else SnackbarDuration.Short

                    result = snackbarHostState.showSnackbar(
                        message = message.text,
                        actionLabel = message.actionLabel,
                        duration = standardDuration
                    )
                }

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
                snapNotifyState.dismissCurrent()
            }
        }
    }

    CompositionLocalProvider(LocalSnapNotifyProvider provides true) {
        Box(modifier = modifier.fillMaxSize()) {
            // Content renders edge-to-edge without any padding
            content()

            // Snackbar with proper navigation bar padding only
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) { snackbarData ->
                val messageStyle = currentMessage.value?.style ?: snackbarStyle
                StyledSnackbar(
                    snackbarData = snackbarData,
                    style = messageStyle
                )
            }
        }
    }
}
