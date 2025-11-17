package com.vamsi.snapnotify

import androidx.compose.material3.SnackbarDuration
import com.vamsi.snapnotify.core.SnackbarManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for SnackbarManager to verify message queueing and thread safety.
 */
@ExperimentalCoroutinesApi
class SnackbarManagerTest {

    private lateinit var snackbarManager: SnackbarManager
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setup() = runBlocking {
        snackbarManager = SnackbarManager.getInstance()
        snackbarManager.updateConfig(SnapNotifyConfig().withDispatcher(testDispatcher))
        snackbarManager.clearAll()
    }

    @After
    fun tearDown() = runBlocking {
        snackbarManager.updateConfig(SnapNotifyConfig())
        snackbarManager.clearAll()
    }

    @Test
    fun `show message displays immediately when queue is empty`() = runTest {
        val message = "Test message"

        snackbarManager.show(message)

        val currentMessage = snackbarManager.awaitMessage()
        assertEquals(message, currentMessage.text)
        assertEquals(SnackbarDuration.Short, currentMessage.duration)
    }

    @Test
    fun `show message with action displays correctly`() = runTest {
        val message = "Test message"
        val actionLabel = "Action"
        var actionCalled = false
        val onAction = { actionCalled = true }

        snackbarManager.show(message, SnackbarDuration.Long, actionLabel, onAction)

        val currentMessage = snackbarManager.awaitMessage()
        assertEquals(message, currentMessage.text)
        assertEquals(SnackbarDuration.Long, currentMessage.duration)
        assertEquals(actionLabel, currentMessage.actionLabel)

        currentMessage?.onAction?.invoke()
        assertTrue(actionCalled)
    }

    @Test
    fun `non-suspending showMessage works correctly`() = runTest {
        val message = "Non-suspending test"

        snackbarManager.showMessage(message, SnackbarDuration.Indefinite)

        val currentMessage = snackbarManager.awaitMessage()
        assertEquals(message, currentMessage.text)
        assertEquals(SnackbarDuration.Indefinite, currentMessage.duration)
    }

    @Test
    fun `dismissCurrent clears current message`() = runTest {
        snackbarManager.show("Test message")
        snackbarManager.awaitMessage()

        snackbarManager.dismissCurrent()

        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `dismissCurrent shows next queued message`() = runTest {
        // Show first message
        snackbarManager.show("First message")

        // Queue second message (should be queued since first is still active)
        snackbarManager.show("Second message")

        // Verify first message is displayed
        val firstMessage = snackbarManager.awaitMessage()
        assertEquals("First message", firstMessage.text)

        // Dismiss current and verify second message is now displayed
        snackbarManager.dismissCurrent()
        val secondMessage = snackbarManager.awaitMessage()
        assertEquals("Second message", secondMessage.text)
    }

    @Test
    fun `clearAll removes all messages`() = runTest {
        snackbarManager.show("First message")
        snackbarManager.show("Second message")

        snackbarManager.clearAll()

        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `multiple messages are queued correctly`() = runTest {
        // Show multiple messages rapidly
        snackbarManager.show("Message 1")
        snackbarManager.show("Message 2")
        snackbarManager.show("Message 3")

        // First message should be displayed
        assertEquals("Message 1", snackbarManager.awaitMessage().text)

        // Dismiss and check next
        snackbarManager.dismissCurrent()
        assertEquals("Message 2", snackbarManager.awaitMessage().text)

        // Dismiss and check next
        snackbarManager.dismissCurrent()
        assertEquals("Message 3", snackbarManager.awaitMessage().text)

        // Dismiss last message
        snackbarManager.dismissCurrent()
        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `message has unique ID`() = runTest {
        snackbarManager.show("Message 1")
        val firstMessage = snackbarManager.awaitMessage()

        snackbarManager.dismissCurrent()
        snackbarManager.show("Message 2")
        val secondMessage = snackbarManager.awaitMessage()

        assertNotNull(firstMessage?.id)
        assertNotNull(secondMessage?.id)
        assertNotEquals(firstMessage?.id, secondMessage?.id)
    }

    @Test
    fun `clearAllMessages removes all messages non-suspending`() = runTest {
        snackbarManager.show("First message")
        snackbarManager.show("Second message")

        // Verify messages are present
        snackbarManager.awaitMessage()

        // Clear all messages using non-suspending method
        snackbarManager.clearAllMessages()?.join()

        // Verify all messages are cleared
        assertNull(snackbarManager.awaitNullMessage())
    }

    @Test
    fun `queue drops oldest message when exceeding limit`() = runTest {
        snackbarManager.updateConfig(SnapNotifyConfig(maxQueueSize = 1))

        snackbarManager.show("Active")
        snackbarManager.show("Queued")
        snackbarManager.show("Overflow") // Should drop "Queued"

        snackbarManager.dismissCurrent()
        val nextMessage = snackbarManager.awaitMessage()

        assertEquals("Overflow", nextMessage.text)
    }

    @Test
    fun `onMessageDropped callback is invoked`() = runTest {
        val dropped = mutableListOf<String>()
        snackbarManager.updateConfig(
            SnapNotifyConfig(
                maxQueueSize = 1,
                onMessageDropped = { dropped += it }
            )
        )

        snackbarManager.show("Active")
        snackbarManager.show("Queued")
        snackbarManager.show("Overflow")

        assertEquals(listOf("Queued"), dropped)
    }
}
