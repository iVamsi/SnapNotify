package com.vamsi.snapnotify

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import androidx.compose.ui.unit.dp
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
