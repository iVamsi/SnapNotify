package com.vamsi.snapnotify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SnapNotifyComposeInstrumentationTest {

    @Test
    fun compact_channel_host_displays_rich_body_and_action() {
        val channel = SnapNotify.channel("instrumentation-compact-rich")
        val body = "First line\nSecond line\nThird line"
        var actionCalled = false
        composeTestRule.setContent {
            SnapNotifyProvider {
                SnapNotifyProvider(channel = channel, hostInsets = WindowInsets(0, 0, 0, 0)) {
                    Text("Compact content")
                }
            }
        }
        try {
            channel.show(
                body, title = "Important", actionLabel = "Finish", onAction = { actionCalled = true },
                duration = androidx.compose.material3.SnackbarDuration.Indefinite,
            )
            composeTestRule.waitUntil(5_000) {
                composeTestRule.onAllNodesWithText(body, useUnmergedTree = true).fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Important", useUnmergedTree = true).assertIsDisplayed()
            composeTestRule.onNodeWithText(body, useUnmergedTree = true).assertIsDisplayed()
            composeTestRule.onNodeWithText("Finish").assertIsDisplayed().performClick()
            composeTestRule.runOnIdle { assertTrue(actionCalled) }
        } finally {
            channel.clear()
        }
    }

    @Test
    fun dialog_channel_handles_actions_without_consuming_the_root_message() = overlayChannel(false)

    @Test
    fun bottom_sheet_channel_handles_actions_without_consuming_the_root_message() = overlayChannel(true)

    @OptIn(ExperimentalMaterial3Api::class)
    private fun overlayChannel(inSheet: Boolean) {
        val channel = SnapNotify.channel("instrumentation-overlay-$inSheet")
        var open by mutableStateOf(true)
        var localMatches = false
        composeTestRule.setContent {
            SnapNotifyProvider {
                Text("Root content")
                if (open) {
                    val body: @androidx.compose.runtime.Composable () -> Unit = {
                        SnapNotifyProvider(channel = channel) {
                            localMatches = LocalSnapNotifyChannel.current === channel
                            Button(onClick = { open = false }) { Text("Close overlay") }
                        }
                    }
                    if (inSheet) {
                        ModalBottomSheet(onDismissRequest = { open = false }) { body() }
                    } else {
                        Dialog(onDismissRequest = { open = false }, content = body)
                    }
                }
            }
        }
        try {
            SnapNotify.show("Root remains queued", duration = androidx.compose.material3.SnackbarDuration.Indefinite)
            channel.show("Scoped action", actionLabel = "Finish", onAction = { open = false })
            composeTestRule.waitUntil(5_000) {
                composeTestRule.onAllNodesWithText("Finish").fetchSemanticsNodes().isNotEmpty()
            }
            assertTrue(localMatches)
            composeTestRule.onNodeWithText("Scoped action").assertIsDisplayed()
            composeTestRule.onNodeWithText("Finish").performClick()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Root remains queued").assertIsDisplayed()
            assertTrue(SnackbarManager.getInstance().messages.value?.text == "Root remains queued")
        } finally {
            channel.clear()
        }
    }

    @Test
    fun nested_channels_display_independently_and_same_channel_hosts_are_suppressed() {
        val channel = SnapNotify.channel("instrumentation-nested")
        composeTestRule.setContent {
            SnapNotifyProvider(placement = NotificationPlacement.TopBanner()) {
                SnapNotifyProvider(channel = channel) {
                    SnapNotifyProvider(channel = "instrumentation-nested") { Text("Nested content") }
                }
            }
        }
        try {
            SnapNotify.show("Root notification", duration = androidx.compose.material3.SnackbarDuration.Indefinite)
            channel.show("Scoped notification", duration = androidx.compose.material3.SnackbarDuration.Indefinite)
            composeTestRule.waitUntil(5_000) {
                composeTestRule.onAllNodesWithText("Scoped notification").fetchSemanticsNodes().isNotEmpty()
            }
            composeTestRule.onNodeWithText("Root notification").assertIsDisplayed()
            composeTestRule.onAllNodesWithText("Scoped notification").assertCountEquals(1)
            channel.clear()
            composeTestRule.waitForIdle()
            composeTestRule.onNodeWithText("Root notification").assertIsDisplayed()
        } finally {
            channel.clear()
        }
    }

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setup() = runBlocking {
        SnackbarManager.getInstance().clearAll()
    }

    @After
    fun tearDown() = runBlocking {
        SnackbarManager.getInstance().clearAll()
    }

    @Test
    fun provider_displays_message_and_handles_action_click() {
        var actionClicked = false

        composeTestRule.setContent {
            SnapNotifyProvider {
                Box(modifier = Modifier.fillMaxSize()) {
                    Text("App Content")
                }
            }
        }

        SnapNotify.show(
            message = "Operation Finished",
            actionLabel = "Retry",
            onAction = { actionClicked = true }
        )

        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Operation Finished").assertIsDisplayed()
        composeTestRule.onNodeWithText("Retry").assertIsDisplayed()

        composeTestRule.onNodeWithText("Retry").performClick()
        composeTestRule.waitForIdle()

        assertTrue(actionClicked)
    }

    @Test
    fun custom_duration_automatically_dismisses_after_timeout() {
        composeTestRule.setContent {
            SnapNotifyProvider {
                Text("App Content")
            }
        }

        SnapNotify.show(
            message = "Temporary Notification",
            durationMillis = 800L
        )

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Temporary Notification").assertIsDisplayed()

        // Advance clock beyond custom duration to verify automatic dismissal
        composeTestRule.mainClock.autoAdvance = true
        composeTestRule.waitUntil(timeoutMillis = 5000) {
            composeTestRule.onAllNodes(
                androidx.compose.ui.test.hasText("Temporary Notification")
            ).fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.onNodeWithText("Temporary Notification").assertDoesNotExist()
    }

    @Test
    fun provider_unmount_cancels_active_display_cleanly() {
        var showProvider by mutableStateOf(true)

        composeTestRule.setContent {
            if (showProvider) {
                SnapNotifyProvider {
                    Text("Provider Content")
                }
            } else {
                Text("Unmounted Content")
            }
        }

        SnapNotify.show(
            message = "Persistent Notification",
            durationMillis = 30000
        )

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Persistent Notification").assertIsDisplayed()

        // Unmount the provider
        showProvider = false
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Unmounted Content").assertIsDisplayed()
        composeTestRule.onNodeWithText("Persistent Notification").assertDoesNotExist()
    }

    @Test
    fun closing_a_channel_host_mid_undo_runs_the_timeout_callback() {
        val channel = SnapNotify.channel("instrumentation-dispose-undo")
        var showProvider by mutableStateOf(true)
        var timedOut = false
        composeTestRule.setContent {
            if (showProvider) {
                // Mirrors the README advice to clear a feature's channel when it closes.
                DisposableEffect(channel) { onDispose { channel.clear() } }
                SnapNotifyProvider(channel = channel) { Text("Sheet content") }
            }
        }
        try {
            channel.showUndoable("Item removed", onAction = {}, durationMillis = 30_000, onTimeout = { timedOut = true })
            composeTestRule.waitUntil(5_000) {
                composeTestRule.onAllNodesWithText("Item removed").fetchSemanticsNodes().isNotEmpty()
            }

            showProvider = false
            composeTestRule.waitForIdle()

            composeTestRule.runOnIdle { assertTrue(timedOut) }
            composeTestRule.waitUntil(5_000) { channel.manager.messages.value == null }
        } finally {
            channel.clear()
        }
    }

    @Test
    fun clearing_a_channel_mid_undo_does_not_run_the_timeout_callback() {
        val channel = SnapNotify.channel("instrumentation-clear-undo")
        var timedOut = false
        composeTestRule.setContent {
            SnapNotifyProvider(channel = channel) { Text("Sheet content") }
        }
        channel.showUndoable("Item removed", onAction = {}, durationMillis = 30_000, onTimeout = { timedOut = true })
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Item removed").fetchSemanticsNodes().isNotEmpty()
        }

        channel.clear()
        composeTestRule.waitUntil(5_000) {
            composeTestRule.onAllNodesWithText("Item removed").fetchSemanticsNodes().isEmpty()
        }

        composeTestRule.runOnIdle { assertFalse(timedOut) }
    }

    @Test
    fun provider_adjusts_position_with_ime_insets() {
        var bottomInset by mutableStateOf(0.dp)

        composeTestRule.setContent {
            SnapNotifyProvider(
                hostAlignment = Alignment.BottomCenter,
                hostInsets = WindowInsets(bottom = bottomInset)
            ) {
                Text("Screen with IME Insets")
            }
        }

        SnapNotify.show("IME Bound Notification")
        composeTestRule.waitForIdle()

        val initialBounds = composeTestRule.onNodeWithText("IME Bound Notification").getBoundsInRoot()

        // Simulate IME opening by raising bottom inset
        bottomInset = 160.dp
        composeTestRule.waitForIdle()

        val raisedBounds = composeTestRule.onNodeWithText("IME Bound Notification").getBoundsInRoot()
        assertTrue(
            "Snackbar should move up when IME inset expands: raised ${raisedBounds.bottom} < initial ${initialBounds.bottom}",
            raisedBounds.bottom < initialBounds.bottom
        )
    }

    @Test
    fun rich_snackbar_shows_title_and_dismisses_from_close_without_timeout() {
        var timedOut = false

        composeTestRule.setContent {
            SnapNotifyProvider {
                Text("App Content")
            }
        }

        SnapNotify.show(
            message = "Body copy",
            title = "Headline",
            actionLabel = "View",
            onAction = {},
            showCloseButton = true,
            durationMillis = 30_000L,
            onTimeout = { timedOut = true },
        )

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Headline").assertIsDisplayed()
        composeTestRule.onNodeWithText("Body copy").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("Dismiss").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Headline").assertDoesNotExist()
        assertFalse(timedOut)
    }

    @Test
    fun top_pill_anchors_the_snackbar_above_the_bottom_host() {
        composeTestRule.setContent {
            Box(modifier = Modifier.fillMaxSize()) {
                SnapNotifyProvider(placement = NotificationPlacement.TopPill()) {
                    Text("App Content")
                }
            }
        }

        SnapNotify.show("Top pill message")
        composeTestRule.waitForIdle()

        val bounds = composeTestRule.onNodeWithText("Top pill message").getBoundsInRoot()
        val root = composeTestRule.onRoot().getBoundsInRoot()
        assertTrue(
            "Top pill should sit in the upper half: top ${bounds.top}, root ${root.bottom}",
            bounds.top < (root.bottom - root.top) / 2,
        )
    }
}
