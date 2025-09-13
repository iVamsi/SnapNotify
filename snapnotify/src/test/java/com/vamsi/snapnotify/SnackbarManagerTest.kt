package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SnackbarManager to verify message queueing and thread safety.
 */
@ExperimentalCoroutinesApi
class SnackbarManagerTest {

    private lateinit var snackbarManager: SnackbarManager

    @Before
    fun setup() {
        snackbarManager = SnackbarManager.getInstance()
        // Clear any existing messages from previous tests
        snackbarManager.clearAllMessages()
    }

    @Test
    fun `show message displays immediately when queue is empty`() = runTest {
        val message = "Test message"

        snackbarManager.show(message)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(SnackbarDuration.Short, currentMessage?.duration)
    }

    @Test
    fun `show message with action displays correctly`() = runTest {
        val message = "Test message"
        val actionLabel = "Action"
        var actionCalled = false
        val onAction = { actionCalled = true }

        snackbarManager.show(message, SnackbarDuration.Long, actionLabel, onAction)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(SnackbarDuration.Long, currentMessage?.duration)
        assertEquals(actionLabel, currentMessage?.actionLabel)

        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    @Test
    fun `non-suspending showMessage works correctly`() = runTest {
        val message = "Non-suspending test"

        snackbarManager.showMessage(message, SnackbarDuration.Indefinite)

        val currentMessage = snackbarManager.messages.first()
        assertNotNull(currentMessage)
        assertEquals(message, currentMessage?.text)
        assertEquals(SnackbarDuration.Indefinite, currentMessage?.duration)
    }

    @Test
    fun `dismissCurrent clears current message`() = runTest {
        snackbarManager.show("Test message")
        assertNotNull(snackbarManager.messages.first())

        snackbarManager.dismissCurrent()

        assertNull(snackbarManager.messages.first())
    }

    @Test
    fun `dismissCurrent shows next queued message`() = runTest {
        // Show first message
        snackbarManager.show("First message")

        // Queue second message (should be queued since first is still active)
        snackbarManager.show("Second message")

        // Verify first message is displayed
        val firstMessage = snackbarManager.messages.first()
        assertEquals("First message", firstMessage?.text)

        // Dismiss current and verify second message is now displayed
        snackbarManager.dismissCurrent()
        val secondMessage = snackbarManager.messages.first()
        assertEquals("Second message", secondMessage?.text)
    }

    @Test
    fun `clearAll removes all messages`() = runTest {
        snackbarManager.show("First message")
        snackbarManager.show("Second message")

        snackbarManager.clearAll()

        assertNull(snackbarManager.messages.first())
    }

    @Test
    fun `multiple messages are queued correctly`() = runTest {
        // Show multiple messages rapidly
        snackbarManager.show("Message 1")
        snackbarManager.show("Message 2")
        snackbarManager.show("Message 3")

        // First message should be displayed
        assertEquals("Message 1", snackbarManager.messages.first()?.text)

        // Dismiss and check next
        snackbarManager.dismissCurrent()
        assertEquals("Message 2", snackbarManager.messages.first()?.text)

        // Dismiss and check next
        snackbarManager.dismissCurrent()
        assertEquals("Message 3", snackbarManager.messages.first()?.text)

        // Dismiss last message
        snackbarManager.dismissCurrent()
        assertNull(snackbarManager.messages.first())
    }

    @Test
    fun `message has unique ID`() = runTest {
        snackbarManager.show("Message 1")
        val firstMessage = snackbarManager.messages.first()

        snackbarManager.dismissCurrent()
        snackbarManager.show("Message 2")
        val secondMessage = snackbarManager.messages.first()

        assertNotNull(firstMessage?.id)
        assertNotNull(secondMessage?.id)
        assertNotEquals(firstMessage?.id, secondMessage?.id)
    }

    @Test
    fun `clearAllMessages removes all messages non-suspending`() = runTest {
        snackbarManager.show("First message")
        snackbarManager.show("Second message")

        // Verify messages are present
        assertNotNull(snackbarManager.messages.first())

        // Clear all messages using non-suspending method
        snackbarManager.clearAllMessages()

        // Verify all messages are cleared
        assertNull(snackbarManager.messages.first())
    }
}
