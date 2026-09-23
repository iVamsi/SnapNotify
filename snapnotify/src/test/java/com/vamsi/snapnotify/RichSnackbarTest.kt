package com.vamsi.snapnotify

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RichSnackbarTest {

    private lateinit var snackbarManager: SnackbarManager
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() {
        SnapNotify.initialize()
        snackbarManager = SnackbarManager.getInstance()
        snackbarManager.updateConfig(SnapNotifyConfig().withDispatcher(testDispatcher))
        runBlocking { snackbarManager.clearAll() }
    }

    @After
    fun cleanup() {
        runBlocking { snackbarManager.clearAll() }
    }

    @Test
    fun `show stores a title, icon, and close button`() = runTest {
        val icon = sampleIcon()

        SnapNotify.show(
            message = "Report_Q3.pdf saved to Cloud Drive",
            title = "File uploaded",
            leadingIcon = icon,
            leadingIconContentDescription = "Uploaded",
            showCloseButton = true,
        )

        val current = snackbarManager.awaitMessage()
        assertEquals("File uploaded", current.title)
        assertEquals("Report_Q3.pdf saved to Cloud Drive", current.text)
        assertSame(icon, current.leadingIcon)
        assertEquals("Uploaded", current.leadingIconContentDescription)
        assertTrue(current.showCloseButton)
        assertFalse(current.showProgressBar)
        assertNull(current.onTimeout)
    }

    @Test
    fun `show with an action keeps the rich fields and haptic choice`() = runTest {
        SnapNotify.show(
            message = "Report saved",
            actionLabel = "View",
            onAction = {},
            title = "File uploaded",
            showCloseButton = true,
            hapticFeedback = SnackbarHapticFeedback.Success,
        )

        val current = snackbarManager.awaitMessage()
        assertEquals("File uploaded", current.title)
        assertEquals("View", current.actionLabel)
        assertEquals(SnackbarHapticFeedback.Success, current.hapticFeedback)
        assertTrue(current.showCloseButton)
    }

    @Test
    fun `blank title is treated as no title`() = runTest {
        SnapNotify.show(message = "Body", title = "   ")

        val current = snackbarManager.awaitMessage()
        assertNull(current.title)
        assertEquals("Body", current.text)
    }

    @Test
    fun `showUndoable arms a five second countdown and timeout callback`() = runTest {
        var timedOut = false

        SnapNotify.showUndoable(
            message = "Item moved to trash",
            onAction = {},
            onTimeout = { timedOut = true },
        )

        val current = snackbarManager.awaitMessage()
        assertEquals("Item moved to trash", current.text)
        assertEquals("Undo", current.actionLabel)
        assertEquals(5_000L, current.effectiveDuration.getMilliseconds())
        assertTrue(current.showProgressBar)
        current.onTimeout?.invoke()
        assertTrue(timedOut)
    }

    @Test
    fun `duplicate text is dropped even when the title differs`() = runTest {
        SnapNotify.show(message = "Saved", title = "First")
        SnapNotify.show(message = "Saved", title = "Second")

        val current = snackbarManager.awaitMessage()
        assertEquals("First", current.title)
    }

    private fun sampleIcon(): ImageVector = ImageVector.Builder(
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f,
    ).build()
}
