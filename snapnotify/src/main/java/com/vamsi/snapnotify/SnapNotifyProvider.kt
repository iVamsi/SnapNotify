package com.vamsi.snapnotify

import android.content.Context
import android.view.accessibility.AccessibilityManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vamsi.snapnotify.core.SnackbarManager
import com.vamsi.snapnotify.core.SnackbarMessage
import java.util.concurrent.atomic.AtomicReference
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
internal val LocalSnapNotifyHosts = staticCompositionLocalOf { emptySet<SnackbarManager>() }

/** The nearest provider's channel, or the global channel outside a provider. */
val LocalSnapNotifyChannel = staticCompositionLocalOf { SnapNotify.defaultChannel }

/**
 * Composable that wraps content and handles snackbar display logic.
 *
 * If the provider leaves composition while a message is showing, for example when its sheet
 * closes, the message's `onTimeout` runs because its action can no longer be tapped.
 *
 * This composable provides snackbar functionality for its content. Multiple providers
 * in the same composition hierarchy may serve different channels. A nested provider for
 * an already hosted channel is ignored. Use one host per channel in separate composition roots.
 *
 * @param channel The queue to host.
 * @param modifier Modifier to be applied to the container. The container fills the available width
 * and takes the height needed by its content or snackbar, so it fits inside a bottom sheet or dialog.
 * Pass `Modifier.fillMaxSize()` for a full-screen host.
 * @param style Optional styling configuration for snackbars. If null, uses Material3 defaults
 * @param config Optional configuration for the snackbar queue (max size, drop callback, deduplication).
 * If provided, this updates only the selected channel's configuration.
 * @param hostAlignment Alignment for the snackbar host within the provider's Box.
 * Used when [placement] is [NotificationPlacement.BottomSnackbar].
 * @param hostInsets Insets applied to the snackbar host (defaults to navigation + IME).
 * Used when [placement] is [NotificationPlacement.BottomSnackbar].
 * @param hostContent Optional slot to completely override the snackbar host rendering
 * and styling (receives the resolved [SnackbarStyle]). Placement is ignored when this is set.
 * @param placement Where the default host anchors. [NotificationPlacement.TopPill] and
 * [NotificationPlacement.TopBanner] pin the snackbar to the top.
 * @param content The content to be wrapped with snackbar functionality
 */
@Composable
fun SnapNotifyProvider(
    channel: SnapNotifyChannel,
    modifier: Modifier = Modifier,
    style: SnackbarStyle? = null,
    config: SnapNotifyConfig? = null,
    hostAlignment: Alignment = Alignment.BottomCenter,
    hostInsets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
    hostContent: (@Composable BoxScope.(SnackbarHostState, SnackbarStyle) -> Unit)? = null,
    placement: NotificationPlacement = NotificationPlacement.BottomSnackbar,
    content: @Composable () -> Unit,
) {
    val selectedChannel = channel
    val manager = selectedChannel.manager
    val hostedChannels = LocalSnapNotifyHosts.current

    // An ancestor already hosts this channel; a second host would show each message twice.
    if (manager in hostedChannels) {
        CompositionLocalProvider(LocalSnapNotifyChannel provides selectedChannel) { content() }
        return
    }

    val scope = rememberCoroutineScope()
    val snapNotifyState = rememberSimpleSnapNotifyState(scope, manager)
    val currentMessage = snapNotifyState.currentMessage.collectAsStateWithLifecycle()
    val snackbarHostState = remember(manager) { SnackbarHostState() }
    val snackbarStyle = style ?: SnackbarStyle.default()
    val view = LocalView.current
    val context = LocalContext.current
    val isCountdownPaused = remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(1f) }
    val topInsets = when (placement) {
        NotificationPlacement.BottomSnackbar -> null
        is NotificationPlacement.TopPill -> placement.topInsetPadding ?: WindowInsets.statusBars
        is NotificationPlacement.TopBanner -> placement.topInsetPadding ?: WindowInsets.statusBars
    }

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
            val countdown = if (currentMessage.value?.showProgressBar == true) progress else null
            val snackbar: @Composable (SnackbarData) -> Unit = { snackbarData ->
                StyledSnackbar(
                    snackbarData = snackbarData,
                    style = currentStyle,
                    placement = placement,
                    progress = countdown,
                    onPressChanged = { isCountdownPaused.value = it },
                )
            }
            if (topInsets == null) {
                SnackbarHost(
                    hostState = hostState,
                    modifier = Modifier
                        .align(hostAlignment)
                        .windowInsetsPadding(hostInsets),
                    snackbar = snackbar,
                )
            } else {
                TopSnackbarHost(
                    hostState = hostState,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .windowInsetsPadding(topInsets),
                    snackbar = snackbar,
                )
            }
        }

    LaunchedEffect(manager, config) {
        if (config != null) {
            selectedChannel.configure(config)
        }
    }

    val unsettledMessage = remember(manager) { AtomicReference<SnackbarMessage?>(null) }
    DisposableEffect(manager) {
        onDispose {
            // The host is leaving, for example a sheet is closing, so the action can no longer be
            // tapped and an Undo window has ended. This runs synchronously, before a parent's
            // onDispose can clear the channel. A message replaced by clear or preemption is no
            // longer current and is skipped.
            val message = unsettledMessage.getAndSet(null)
            if (message != null && manager.messages.value?.id == message.id) {
                message.onTimeout?.invoke()
            }
        }
    }

    LaunchedEffect(manager, currentMessage.value?.id) {
        val message = currentMessage.value ?: return@LaunchedEffect
        val activeConfig = config ?: manager.getConfig()

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

        val tracksCountdown = message.showProgressBar && !isIndefinite
        // Material's host only knows Short/Long, so anything with its own deadline — a custom
        // duration, a countdown bar, or a standard one stretched for accessibility — is held open
        // and timed here.
        val hasOwnDeadline = !isIndefinite &&
            (effectiveDuration is SnackbarDurationWrapper.Custom ||
                durationMillis != rawDurationMillis ||
                tracksCountdown)

        val visualsDuration = if (hasOwnDeadline || isIndefinite) {
            SnackbarDuration.Indefinite
        } else {
            effectiveDuration.getStandardDuration() ?: SnackbarDuration.Short
        }

        val visuals = SnapNotifyVisuals(
            message = message.text,
            actionLabel = message.actionLabel,
            withDismissAction = message.showCloseButton,
            duration = visualsDuration,
            style = message.style ?: snackbarStyle,
            isAssertive = message.isAssertiveAccessibility,
            title = message.title,
            leadingIcon = message.leadingIcon,
            leadingIconContentDescription = message.leadingIconContentDescription,
        )

        progress = 1f
        isCountdownPaused.value = false
        var dismissedByTimeout = false
        unsettledMessage.set(message)

        try {
            val result = if (hasOwnDeadline) {
                coroutineScope {
                    val snackbarDeferred = async {
                        snackbarHostState.showSnackbar(visuals)
                    }
                    val timeoutJob = launch {
                        if (tracksCountdown) {
                            runCountdown(
                                durationMillis = durationMillis,
                                isPaused = { isCountdownPaused.value },
                                onProgress = { progress = it },
                            )
                        } else {
                            delay(durationMillis)
                        }
                        dismissedByTimeout = true
                        snackbarHostState.currentSnackbarData?.dismiss()
                    }
                    val res = snackbarDeferred.await()
                    timeoutJob.cancel()
                    res
                }
            } else {
                snackbarHostState.showSnackbar(visuals)
            }

            unsettledMessage.set(null)
            if (result == SnackbarResult.ActionPerformed) {
                message.onAction?.invoke()
            } else if (dismissedByTimeout) {
                message.onTimeout?.invoke()
            }
        } finally {
            withContext(NonCancellable) {
                snapNotifyState.dismissMessageSuspend(message)
            }
        }
    }

    CompositionLocalProvider(
        LocalSnapNotifyHosts provides (hostedChannels + manager),
        LocalSnapNotifyChannel provides selectedChannel,
    ) {
        Box(modifier = modifier.fillMaxWidth()) {
            content()
            val messageStyle = currentMessage.value?.style ?: snackbarStyle
            resolvedHostContent(snackbarHostState, messageStyle)
        }
    }
}

/**
 * Hosts the channel named [channel], creating it on first use. See the [SnapNotifyChannel]
 * overload for the other parameters.
 *
 * @param channel A stable feature ID passed to [SnapNotify.channel]. Must not be blank.
 */
@Composable
fun SnapNotifyProvider(
    channel: String,
    modifier: Modifier = Modifier,
    style: SnackbarStyle? = null,
    config: SnapNotifyConfig? = null,
    hostAlignment: Alignment = Alignment.BottomCenter,
    hostInsets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
    hostContent: (@Composable BoxScope.(SnackbarHostState, SnackbarStyle) -> Unit)? = null,
    placement: NotificationPlacement = NotificationPlacement.BottomSnackbar,
    content: @Composable () -> Unit,
) = SnapNotifyProvider(
    channel = SnapNotify.channel(channel),
    modifier = modifier,
    style = style,
    config = config,
    hostAlignment = hostAlignment,
    hostInsets = hostInsets,
    hostContent = hostContent,
    placement = placement,
    content = content,
)

// The parameter list matches 1.2.0 so the Compose JVM signature and default-argument masks stay the same.
/**
 * Hosts the global queue that [SnapNotify] calls target. Place it once near the root of the app.
 *
 * The container fills the screen. Other parameters match the [SnapNotifyChannel] overload.
 *
 * @param modifier Modifier applied to the full-screen container.
 * @param style Optional styling for snackbars. If null, uses Material3 defaults.
 * @param config Optional configuration for the global queue.
 * @param hostAlignment Alignment of the snackbar host when [placement] is [NotificationPlacement.BottomSnackbar].
 * @param hostInsets Insets applied to the snackbar host (defaults to navigation + IME).
 * @param hostContent Optional slot that replaces the default snackbar host. Placement is ignored when set.
 * @param placement Where the default host anchors.
 * @param content The content to wrap.
 */
@Composable
fun SnapNotifyProvider(
    modifier: Modifier = Modifier,
    style: SnackbarStyle? = null,
    config: SnapNotifyConfig? = null,
    hostAlignment: Alignment = Alignment.BottomCenter,
    hostInsets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
    hostContent: (@Composable BoxScope.(SnackbarHostState, SnackbarStyle) -> Unit)? = null,
    placement: NotificationPlacement = NotificationPlacement.BottomSnackbar,
    content: @Composable () -> Unit,
) = SnapNotifyProvider(
    channel = SnapNotify.defaultChannel,
    modifier = modifier.fillMaxSize(),
    style = style,
    config = config,
    hostAlignment = hostAlignment,
    hostInsets = hostInsets,
    hostContent = hostContent,
    placement = placement,
    content = content,
)

// Keeps the v1.1.0 JVM signature so callers compiled against it still link.
@Deprecated("Binary compatibility only", level = DeprecationLevel.HIDDEN)
@Composable
fun SnapNotifyProvider(
    modifier: Modifier = Modifier,
    style: SnackbarStyle? = null,
    config: SnapNotifyConfig? = null,
    hostAlignment: Alignment = Alignment.BottomCenter,
    hostInsets: WindowInsets = WindowInsets.navigationBars.union(WindowInsets.ime),
    hostContent: (@Composable BoxScope.(SnackbarHostState, SnackbarStyle) -> Unit)? = null,
    content: @Composable () -> Unit,
) = SnapNotifyProvider(
    modifier = modifier,
    style = style,
    config = config,
    hostAlignment = hostAlignment,
    hostInsets = hostInsets,
    hostContent = hostContent,
    placement = NotificationPlacement.BottomSnackbar,
    content = content,
)

@Composable
private fun TopSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    snackbar: @Composable (SnackbarData) -> Unit,
) {
    val snackbarData = hostState.currentSnackbarData
    var displayed by remember { mutableStateOf<SnackbarData?>(null) }
    if (snackbarData != null) {
        displayed = snackbarData
    }
    val motion = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMediumLow,
        visibilityThreshold = IntOffset(1, 1),
    )
    AnimatedVisibility(
        visible = snackbarData != null,
        enter = fadeIn() + slideInVertically(animationSpec = motion, initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = modifier,
    ) {
        displayed?.let { data -> snackbar(data) }
    }
}
