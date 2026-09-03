package com.vamsi.snapnotify

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * CompositionLocal to track if we're already inside a SnapNotifyProvider.
 */
internal val LocalSnapNotifyProvider = staticCompositionLocalOf { false }

/**
 * Composable that wraps content and handles snackbar display logic.
 *
 * This composable provides snackbar functionality for its content. Multiple providers
 * in the same composition hierarchy are automatically handled - nested providers will
 * be ignored to prevent duplicate snackbars, and only the outermost provider will
 * handle message display.
 *
 * @param modifier Modifier to be applied to the container
 * @param style Optional styling configuration for snackbars. If null, uses Material3 defaults
 * @param config Optional configuration for the snackbar queue (max size, drop callback, deduplication).
 * If provided, this will update the global SnapNotify configuration.
 * @param hostAlignment Alignment for the snackbar host within the provider's Box
 * @param hostInsets Insets applied to the snackbar host (defaults to navigation + IME)
 * @param hostContent Optional slot to completely override the snackbar host rendering
 * and styling (receives the resolved [SnackbarStyle])
 * @param content The content to be wrapped with snackbar functionality
 */
@Composable
fun SnapNotifyProvider(
    modifier: Modifier = Modifier,
    style: SnackbarStyle? = null,
    config: SnapNotifyConfig? = null,
    hostAlignment: Alignment = Alignment.BottomCenter,
    hostInsets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
    hostContent: (@Composable BoxScope.(SnackbarHostState, SnackbarStyle) -> Unit)? = null,
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
    val view = LocalView.current
    val context = LocalContext.current

    val accessibilityManager = remember(context) {
        try {
            context.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (unavailable: RuntimeException) {
            null
        }
    }

    val resolvedHostContent: @Composable BoxScope.(SnackbarHostState, SnackbarStyle) -> Unit =
        hostContent ?: { hostState, currentStyle ->
            SnackbarHost(
                hostState = hostState,
                modifier = Modifier
                    .align(hostAlignment)
                    .windowInsetsPadding(hostInsets)
            ) { snackbarData ->
                StyledSnackbar(
                    snackbarData = snackbarData,
                    style = currentStyle
                )
            }
        }

    LaunchedEffect(config) {
        SnapNotify.initialize()
        if (config != null) {
            SnapNotify.configure(config)
        }
    }

    LaunchedEffect(currentMessage.value?.id) {
        val message = currentMessage.value ?: return@LaunchedEffect
        val activeConfig = config ?: SnackbarManager.getInstance().getConfig()

        if (activeConfig.isHapticFeedbackEnabled) {
            triggerHapticFeedback(view, message.resolveHapticFeedback())
        }

        val effectiveDuration = message.effectiveDuration
        val isIndefinite = effectiveDuration.isIndefinite()
        val rawDurationMillis = effectiveDuration.getMilliseconds()

        val durationMillis = computeAccessibleDuration(
            rawDurationMillis = rawDurationMillis,
            isAccessibilityEnabled = accessibilityManager?.isEnabled == true,
            isScalingConfigEnabled = activeConfig.isAccessibilityScalingEnabled,
            recommendedTimeoutMillis = accessibilityManager?.recommendedTimeoutOrNull(
                rawDurationMillis = rawDurationMillis,
                hasAction = message.actionLabel != null,
            ),
        )

        // Material's host only knows Short/Long, so anything with its own deadline — a custom
        // duration, or a standard one stretched for accessibility — is held open and timed here.
        val hasOwnDeadline = !isIndefinite &&
            (effectiveDuration is SnackbarDurationWrapper.Custom || durationMillis != rawDurationMillis)

        val visualsDuration = if (hasOwnDeadline || isIndefinite) {
            SnackbarDuration.Indefinite
        } else {
            effectiveDuration.getStandardDuration() ?: SnackbarDuration.Short
        }

        val visuals = SnapNotifyVisuals(
            message = message.text,
            actionLabel = message.actionLabel,
            duration = visualsDuration,
            style = message.style ?: snackbarStyle,
            isAssertive = message.isAssertiveAccessibility,
        )

        try {
            val result = if (hasOwnDeadline) {
                coroutineScope {
                    val snackbarDeferred = async {
                        snackbarHostState.showSnackbar(visuals)
                    }
                    val timeoutJob = launch {
                        delay(durationMillis)
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                    val res = snackbarDeferred.await()
                    timeoutJob.cancel()
                    res
                }
            } else {
                snackbarHostState.showSnackbar(visuals)
            }

            if (result == SnackbarResult.ActionPerformed) {
                message.onAction?.invoke()
            }
        } finally {
            withContext(NonCancellable) {
                snapNotifyState.dismissMessageSuspend(message)
            }
        }
    }

    CompositionLocalProvider(LocalSnapNotifyProvider provides true) {
        Box(modifier = modifier.fillMaxSize()) {
            content()
            val messageStyle = currentMessage.value?.style ?: snackbarStyle
            resolvedHostContent(snackbarHostState, messageStyle)
        }
    }
}
